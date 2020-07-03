package net.syscon.prison.api.resource.impl;

import net.syscon.prison.api.model.AccessRole;
import net.syscon.prison.api.model.CaseLoad;
import net.syscon.prison.api.model.CaseloadUpdate;
import net.syscon.prison.api.model.ErrorResponse;
import net.syscon.prison.api.model.Location;
import net.syscon.prison.api.model.ReferenceCode;
import net.syscon.prison.api.model.StaffDetail;
import net.syscon.prison.api.model.UserDetail;
import net.syscon.prison.api.model.UserRole;
import net.syscon.prison.api.resource.UserResource;
import net.syscon.prison.api.support.Order;
import net.syscon.prison.api.support.PageRequest;
import net.syscon.prison.core.HasWriteScope;
import net.syscon.prison.core.ProxyUser;
import net.syscon.prison.security.AuthenticationFacade;
import net.syscon.prison.service.CaseLoadService;
import net.syscon.prison.service.CaseNoteService;
import net.syscon.prison.service.InmateService;
import net.syscon.prison.service.LocationService;
import net.syscon.prison.service.StaffService;
import net.syscon.prison.service.UserService;
import net.syscon.prison.service.keyworker.KeyWorkerAllocationService;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("${api.base.path}/users")
public class UserResourceImpl implements UserResource {
    private final AuthenticationFacade authenticationFacade;
    private final UserService userService;
    private final InmateService inmateService;
    private final LocationService locationService;
    private final StaffService staffService;
    private final CaseLoadService caseLoadService;
    private final CaseNoteService caseNoteService;
    private final KeyWorkerAllocationService keyWorkerAllocationService;
    private final Environment env;

    public UserResourceImpl(final AuthenticationFacade authenticationFacade,
                            final LocationService locationService,
                            final UserService userService,
                            final StaffService staffService,
                            final CaseLoadService caseLoadService,
                            final CaseNoteService caseNoteService,
                            final InmateService inmateService,
                            final KeyWorkerAllocationService keyWorkerAllocationService,
                            final Environment env) {
        this.authenticationFacade = authenticationFacade;
        this.locationService = locationService;
        this.userService = userService;
        this.staffService = staffService;
        this.caseLoadService = caseLoadService;
        this.caseNoteService = caseNoteService;
        this.inmateService = inmateService;
        this.keyWorkerAllocationService = keyWorkerAllocationService;
        this.env = env;
    }

    @Override
    public Set<String> getAllUsersHavingRoleAtCaseload(final String caseload, final String roleCode) {
        return userService.getAllUsernamesForCaseloadAndRole(caseload, roleCode);
    }

    @Override
    public ResponseEntity<List<UserDetail>> getUsersByCaseLoad(final String caseload, final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {

        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);
        final var userDetails = userService.getUsersByCaseload(caseload, nameFilter, accessRole, pageRequest);

        return ResponseEntity.ok()
                .headers(userDetails.getPaginationHeaders())
                .body(userDetails.getItems());
    }

    @Override
    public ResponseEntity<List<UserDetail>> getStaffUsersForLocalAdministrator(final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {

        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsersAsLocalAdministrator(authenticationFacade.getCurrentUsername(), nameFilter, accessRole, pageRequest);

        return ResponseEntity.ok()
                .headers(userDetails.getPaginationHeaders())
                .body(userDetails.getItems());
    }

    @Override
    public ResponseEntity<List<UserDetail>> deprecatedPleaseRemove(final String caseload, final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        return getStaffUsersForLocalAdministrator(nameFilter, accessRole, pageOffset, pageLimit, sortFields, sortOrder);
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> removeUsersAccessRoleForCaseload(final String username, final String caseload, final String roleCode) {
        userService.removeUsersAccessRoleForCaseload(username, caseload, roleCode);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<List<UserDetail>> getUsers(final String nameFilter, final String accessRole, final Long pageOffset, final Long pageLimit, final String sortFields, final Order sortOrder) {
        final var pageRequest = new PageRequest(sortFields, sortOrder, pageOffset, pageLimit);

        final var userDetails = userService.getUsers(nameFilter, accessRole, pageRequest);

        return ResponseEntity.ok()
                .headers(userDetails.getPaginationHeaders())
                .body(userDetails.getItems());
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> addAccessRole(final String username, final String roleCode) {
        final var added = userService.addAccessRole(username, roleCode);
        return added ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.ok().build();
    }

    @Override
    @ProxyUser
    public ResponseEntity<Void> addAccessRoleByCaseload(final String username, final String caseload, final String roleCode) {
        final var added = userService.addAccessRole(username, roleCode, caseload);
        return added ? ResponseEntity.status(HttpStatus.CREATED).build() : ResponseEntity.ok().build();
    }

    @Override
    public UserDetail getMyUserInformation() {
        return userService.getUserByUsername(authenticationFacade.getCurrentUsername());
    }

    @Override
    public List<CaseLoad> getMyCaseLoads(final boolean allCaseloads) {
        return userService.getCaseLoads(authenticationFacade.getCurrentUsername(), allCaseloads);
    }

    @Override
    public List<ReferenceCode>  getMyCaseNoteTypes(final String sortFields, final Order sortOrder) {
        final var currentCaseLoad =
                caseLoadService.getWorkingCaseLoadForUser(authenticationFacade.getCurrentUsername());

        final var caseLoadType = currentCaseLoad.isPresent() ? currentCaseLoad.get().getType() : "BOTH";
        return caseNoteService.getCaseNoteTypesWithSubTypesByCaseLoadType(caseLoadType);
    }

    @Override
    public List<Location> getMyLocations() {
        return locationService.getUserLocations(authenticationFacade.getCurrentUsername());
    }

    @Override
    public List<UserRole> getMyRoles(final boolean allRoles) {
        return userService.getRolesByUsername(authenticationFacade.getCurrentUsername(), allRoles);
    }

    @Override
    @HasWriteScope
    @ProxyUser
    public ResponseEntity<?> updateMyActiveCaseLoad(final CaseLoad caseLoad) {
        try {
            userService.setActiveCaseLoad(authenticationFacade.getCurrentUsername(), caseLoad.getCaseLoadId());
        } catch (final AccessDeniedException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.builder()
                    .userMessage("Not Authorized")
                    .developerMessage("The current user does not have acess to this CaseLoad")
                    .build());
        }
        return ResponseEntity.ok().build();
    }

    @Override
    public StaffDetail getStaffDetail(final Long staffId) {
        return staffService.getStaffDetail(staffId);
    }

    @Override
    public UserDetail getUserDetails(final String username) {
        return userService.getUserByUsername(username.toUpperCase());
    }

    @Override
    public List<UserDetail> getUserDetailsList(final Set<String> usernames) {
        return userService.getUserListByUsernames(usernames);
   }

    @Override
    public List<AccessRole> getRolesForUserAndCaseload(final String username, final String caseload, final boolean includeAdmin) {
        return userService.getAccessRolesByUserAndCaseload(username, caseload, includeAdmin);
    }

    @Override
    @ProxyUser
    public ResponseEntity<CaseloadUpdate> addApiAccessForCaseload(final String caseload) {
        final var caseloadUpdate = userService.addDefaultCaseloadForPrison(caseload);
        if (caseloadUpdate.getNumUsersEnabled() > 0) {
            return ResponseEntity.status(HttpStatus.CREATED).body(caseloadUpdate);
        }
        return ResponseEntity.ok().body(caseloadUpdate);    }

}