package net.syscon.elite.service.impl;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.security.VerifyAgencyAccess;
import net.syscon.elite.service.CaseLoadService;
import net.syscon.elite.service.EntityNotFoundException;
import net.syscon.elite.service.StaffService;
import net.syscon.elite.service.UserService;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Service
@Slf4j
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final String ROLE_FUNCTION_ADMIN = "ADMIN";
    private static final CaseLoad EMPTY_CASELOAD = CaseLoad.builder()
            .caseLoadId("___")
            .type("DUMMY")
            .caseloadFunction("GENERAL")
            .description("-------")
            .build();

    private final CaseLoadService caseLoadService;
    private final StaffService staffService;
    private final UserRepository userRepository;
    private final AuthenticationFacade securityUtils;
    private final String apiCaseloadId;
    private final int maxBatchSize;

    public UserServiceImpl(final CaseLoadService caseLoadService, final StaffService staffService,
                           final UserRepository userRepository, final AuthenticationFacade securityUtils, @Value("${application.caseload.id:NWEB}") final String apiCaseloadId, @Value("${batch.max.size:1000}") final int maxBatchSize) {
        this.caseLoadService = caseLoadService;
        this.staffService = staffService;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
        this.apiCaseloadId = apiCaseloadId;
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    public UserDetail getUserByUsername(final String username) {
        final var userDetail = userRepository.findByUsername(username).orElseThrow(EntityNotFoundException.withId(username));
        final var caseLoadsForUser = caseLoadService.getCaseLoadsForUser(username, false);
        if (userDetail.getActiveCaseLoadId() == null && (caseLoadsForUser.isEmpty() || caseLoadsForUser.get(0).equals(EMPTY_CASELOAD))) {
            userDetail.setActiveCaseLoadId(EMPTY_CASELOAD.getCaseLoadId());
        }
        return userDetail;
    }

    @Override
    @VerifyAgencyAccess
    public List<UserDetail> getUserListByUsernames(final Set<String> usernames) {
        final List<UserDetail> results = new ArrayList<>();
        if (!usernames.isEmpty()) {
            final var batch = Lists.partition(new ArrayList<>(usernames), maxBatchSize);
            batch.forEach(userBatch -> {
                final var userList = userRepository.getUserListByUsernames(userBatch);
                results.addAll(userList);
            });
        }
        return results;
    }

    @Override
    @Transactional
    public List<CaseLoad> getCaseLoads(final String username, final boolean allCaseloads) {
        final var caseLoadsForUser = caseLoadService.getCaseLoadsForUser(username, allCaseloads);
        if (caseLoadsForUser.isEmpty()) {
            caseLoadsForUser.add(EMPTY_CASELOAD);
        }
        return caseLoadsForUser;
    }

    @Override
    public Set<String> getCaseLoadIds(final String username) {
        return getCaseLoads(username, false).stream()
                .map(CaseLoad::getCaseLoadId)
                .collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void setActiveCaseLoad(final String username, final String caseLoadId) {
        final var userCaseLoads = caseLoadService.getCaseLoadsForUser(username, true);

        if (userCaseLoads.stream().anyMatch(cl -> cl.getCaseLoadId().equalsIgnoreCase(caseLoadId))) {
            userRepository.updateWorkingCaseLoad(username, caseLoadId);
        } else {
            throw new AccessDeniedException(format("The user does not have access to the caseLoadId = %s", caseLoadId));
        }
    }

    @Override
    public List<UserRole> getRolesByUsername(final String username, final boolean allRoles) {
        final var query = allRoles ? null : format("caseloadId:eq:'%s',or:caseloadId:is:null", apiCaseloadId);
        final var rolesByUsername = userRepository.findRolesByUsername(username, query);

        if (!allRoles) {
            rolesByUsername.forEach(role -> role.setRoleCode(RegExUtils.replaceFirst(role.getRoleCode(), apiCaseloadId + "_", "")));
        }
        return rolesByUsername;
    }

    @Override
    public UserDetail getUserByExternalIdentifier(final String idType, final String id, final boolean activeOnly) {
        final var staffDetail = staffService.getStaffDetailByPersonnelIdentifier(idType, id);

        final Optional<UserDetail> userDetail;

        if (activeOnly && !StaffService.isStaffActive(staffDetail)) {
            log.info("Staff member found for external identifier with idType [{}] and id [{}] but not active.", idType, id);

            userDetail = Optional.empty();
        } else {
            userDetail = userRepository.findByStaffIdAndStaffUserType(
                    staffDetail.getStaffId(), STAFF_USER_TYPE_FOR_EXTERNAL_USER_IDENTIFICATION);
        }

        return userDetail.orElseThrow(EntityNotFoundException
                .withMessage("User not found for external identifier with idType [{}] and id [{}].", idType, id));
    }

    @Override
    public Set<String> getAllUsernamesForCaseloadAndRole(final String caseload, final String roleCode) {
        return userRepository
                .getAllStaffRolesForCaseload(caseload, roleCode)
                .stream()
                .map(StaffUserRole::getUsername)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isUserAssessibleCaseloadAvailable(final String caseload, final String username) {
        return userRepository.isUserAssessibleCaseloadAvailable(caseload, username);
    }

    @Override
    @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
    @Transactional
    public void removeUsersAccessRoleForCaseload(final String username, final String caseload, final String roleCode) {
        final var role = userRepository.getRoleByCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));

        verifyMaintainRolesAdminAccess(role);

        if (!userRepository.isRoleAssigned(username, caseload, role.getRoleId())) {
            throw EntityNotFoundException.withMessage("Role [%s] not assigned to user [%s] at caseload [%s]", roleCode, username, caseload);
        }
        userRepository.removeRole(username, caseload, role.getRoleId()); // Don't care if it doesn't exist...
        log.info("Removed role '{}' from username '{}' at caseload '{}'", roleCode, username, caseload);
    }

    private void verifyMaintainRolesAdminAccess(final AccessRole role) {
        if (role.getRoleFunction().equals(ROLE_FUNCTION_ADMIN)) {
            if (!securityUtils.isOverrideRole("MAINTAIN_ACCESS_ROLES_ADMIN")) {
                throw new AccessDeniedException("Maintain roles Admin access required to perform this action");
            }
        }
    }

    /**
     * Add an 'access' role - using the API caseload
     *
     * @param username The user to whom the role is being assigned
     * @param roleCode The role to assign
     * @return true if the role was added, false if the role assignment already exists (no change).
     */
    @Override
    @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
    @Transactional
    public boolean addAccessRole(final String username, final String roleCode) {

        return addAccessRole(username, roleCode, apiCaseloadId);
    }

    /**
     * Add an 'access' role
     *
     * @param username   The user to whom the role is being assigned
     * @param roleCode   The role to assign
     * @param caseloadId The caseload to assign the role to
     * @return true if the role was added, false if the role assignment already exists (no change).
     */
    @Override
    @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
    @Transactional
    public boolean addAccessRole(final String username, final String roleCode, final String caseloadId) {

        final var role = userRepository.getRoleByCode(roleCode).orElseThrow(EntityNotFoundException.withId(roleCode));

        verifyMaintainRolesAdminAccess(role);

        if (userRepository.isRoleAssigned(username, caseloadId, role.getRoleId())) {
            return false;
        }

        if (!userRepository.isUserAssessibleCaseloadAvailable(caseloadId, username)) {
            if (caseloadId.equals(apiCaseloadId)) {
                // only for NWEB - ensure that user accessible caseload exists...
                userRepository.addUserAssessibleCaseload(apiCaseloadId, username);
            } else {
                throw EntityNotFoundException.withMessage("Caseload %s is not accessible for user %s", caseloadId, username);
            }
        }

        userRepository.addRole(username, caseloadId, role.getRoleId());
        log.info("Assigned role '{}' to username '{}' at caseload '{}'", roleCode, username, caseloadId);
        return true;
    }


    @Override
    @PreAuthorize("hasAnyRole('MAINTAIN_ACCESS_ROLES,MAINTAIN_ACCESS_ROLES_ADMIN')")
    @Transactional
    public CaseloadUpdate addDefaultCaseloadForPrison(final String caseloadId) {
        final var users = userRepository.findAllUsersWithCaseload(caseloadId, apiCaseloadId);

        log.debug("Found {} users with caseload {} that do not have {} caseload", users.size(), caseloadId);
        final List<UserDetail> caseloadsAdded = new ArrayList<>();
        users.forEach(user -> {
            final var username = user.getUsername();
            try {
                userRepository.addUserAssessibleCaseload(apiCaseloadId, username);
                caseloadsAdded.add(user);
            } catch (final Exception e) {
                log.error("Failed to add {} caseload to user {}", apiCaseloadId, username);
            }
        });

        log.debug("{} users API enabled for caseload {}", caseloadsAdded.size(), caseloadId);
        return CaseloadUpdate.builder()
                .caseload(caseloadId)
                .numUsersEnabled(caseloadsAdded.size())
                .build();
    }

    @Override
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES_ADMIN')")
    public Page<UserDetail> getUsersByCaseload(final String caseload, final String nameFilter, final String accessRole, final PageRequest pageRequest) {

        final var pageWithDefaults = getPageRequestDefaultLastNameOrder(pageRequest);

        return userRepository
                .findUsersByCaseload(caseload, accessRole, new NameFilter(nameFilter), pageWithDefaults);
    }

    @Override
    public Page<UserDetail> getUsersAsLocalAdministrator(final String laaUsername, final String nameFilter, final String accessRole, final PageRequest pageRequest) {

        final var pageWithDefaults = getPageRequestDefaultLastNameOrder(pageRequest);

        return userRepository
                .getUsersAsLocalAdministrator(laaUsername, accessRole, new NameFilter(nameFilter), pageWithDefaults);
    }

    private PageRequest getPageRequestDefaultLastNameOrder(final PageRequest pageRequest) {
        var pageWithDefaults = pageRequest;
        if (pageWithDefaults == null) {
            pageWithDefaults = new PageRequest("lastName,firstName");
        } else {
            if (pageWithDefaults.getOrderBy() == null) {
                pageWithDefaults = new PageRequest("lastName,firstName", pageWithDefaults.getOrder(), pageWithDefaults.getOffset(), pageWithDefaults.getLimit());
            }
        }
        return pageWithDefaults;
    }

    @Override
    public List<AccessRole> getAccessRolesByUserAndCaseload(final String username, final String caseload, final boolean includeAdmin) {
        Validate.notBlank(caseload, "A caseload id is required.");
        Validate.notBlank(username, "A username is required.");

        if (!caseLoadService.getCaseLoad(caseload).isPresent()) {
            throw EntityNotFoundException.withMessage("Caseload with id [%s] not found", caseload);
        }

        return userRepository
                .findAccessRolesByUsernameAndCaseload(username, caseload, includeAdmin);
    }

    @Override
    @PreAuthorize("hasRole('MAINTAIN_ACCESS_ROLES_ADMIN')")
    public Page<UserDetail> getUsers(final String nameFilter, final String accessRole, final PageRequest pageRequest) {

        final var pageWithDefaults = getPageRequestDefaultLastNameOrder(pageRequest);

        return userRepository
                .findUsers(accessRole, new NameFilter(nameFilter), pageWithDefaults);
    }
}
