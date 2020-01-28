package net.syscon.elite.repository.impl;

import net.syscon.elite.api.model.AccessRole;
import net.syscon.elite.api.model.StaffUserRole;
import net.syscon.elite.api.model.UserDetail;
import net.syscon.elite.api.model.UserRole;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.UserRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.filters.NameFilter;
import net.syscon.util.DateTimeConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl extends RepositoryBase implements UserRepository {

    @Value("${application.caseload.id:NWEB}")
    private String apiCaseloadId;

    @Value("${application.type:APP}")
    private String applicationType;

    private static final String ADMIN_ROLE_FUNCTION = "ADMIN";

    private static final String NAME_FILTER_QUERY_TEMPLATE = " AND (UPPER(FIRST_NAME) LIKE :searchTerm OR UPPER(LAST_NAME) LIKE :searchTerm OR UPPER(SUA.USERNAME) LIKE :searchTerm)";

    private static final String FULL_NAME_FILTER_QUERY_TEMPLATE = " AND (UPPER(FIRST_NAME) LIKE :firstName AND UPPER(LAST_NAME) LIKE :surname)";

    private static final String EXCLUDE_BY_ROLE_FUNCTION_CLAUSE = " AND RL.ROLE_FUNCTION <> :roleFunction ";

    private static final String APPLICATION_ROLE_CODE_FILTER_QUERY_TEMPLATE = " AND SUA.username in  (select SUA_INNER.USERNAME FROM STAFF_USER_ACCOUNTS SUA_INNER\n" +
            "                INNER JOIN USER_ACCESSIBLE_CASELOADS UAC ON SUA_INNER.USERNAME = UAC.USERNAME\n" +
            "                INNER JOIN User_caseload_roles UCR ON UCR.USERNAME = SUA_INNER.username\n" +
            "                INNER JOIN OMS_ROLES RL ON RL.ROLE_ID = UCR.ROLE_ID\n" +
            "  WHERE UAC.CASELOAD_ID = :apiCaseloadId\n" +
            "  AND RL.ROLE_TYPE =  :applicationType\n" +
            "  AND RL.ROLE_CODE = :roleCode )";

    private final StandardBeanPropertyRowMapper<UserRole> USER_ROLE_MAPPER =
            new StandardBeanPropertyRowMapper<>(UserRole.class);

    private final StandardBeanPropertyRowMapper<AccessRole> ACCESS_ROLE_MAPPER =
            new StandardBeanPropertyRowMapper<>(AccessRole.class);

    private final StandardBeanPropertyRowMapper<StaffUserRole> STAFF_USER_ROLE_MAPPER =
            new StandardBeanPropertyRowMapper<>(StaffUserRole.class);

    private final StandardBeanPropertyRowMapper<UserDetail> USER_DETAIL_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(UserDetail.class);

    @Override
    public Optional<UserDetail> findByUsername(final String username) {
        final var sql = getQuery("FIND_USER_BY_USERNAME");
        UserDetail userDetails;
        try {
            userDetails = jdbcTemplate.queryForObject(
                    sql,
                    createParams("username", username),
                    USER_DETAIL_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            userDetails = null;
        }
        return Optional.ofNullable(userDetails);
    }

    @Override
    @Cacheable("findRolesByUsername")
    public List<UserRole> findRolesByUsername(final String username, final String query) {
        var builder = queryBuilderFactory.getQueryBuilder(getQuery("FIND_ROLES_BY_USERNAME"), USER_ROLE_MAPPER);

        if (StringUtils.isNotBlank(query)) {
            builder = builder.addQuery(query);
        }
        final var sql = builder
                .addOrderBy(Order.ASC, "roleCode")
                .build();

        return jdbcTemplate.query(sql, createParams("username", username), USER_ROLE_MAPPER);
    }

    @Override
    public List<AccessRole> findAccessRolesByUsernameAndCaseload(final String username, final String caseload, final boolean includeAdmin) {
        var query = getQuery("FIND_ACCESS_ROLES_BY_USERNAME_AND_CASELOAD");

        if (!includeAdmin) query += EXCLUDE_BY_ROLE_FUNCTION_CLAUSE;

        final var builder = queryBuilderFactory.getQueryBuilder(query, ACCESS_ROLE_MAPPER);

        final var sql = builder
                .addOrderBy(Order.ASC, "roleName")
                .build();

        return jdbcTemplate.query(sql, createParams("username", username, "caseloadId", caseload, "roleFunction", ADMIN_ROLE_FUNCTION), ACCESS_ROLE_MAPPER);
    }

    @Override
    public void updateWorkingCaseLoad(final String username, final String caseLoadId) {
        final var sql = getQuery("UPDATE_STAFF_ACTIVE_CASE_LOAD");
        jdbcTemplate.update(sql, createParams("caseLoadId", caseLoadId, "username", username));
    }

    @Override
    @Cacheable("findByStaffIdAndStaffUserType")
    public Optional<UserDetail> findByStaffIdAndStaffUserType(final Long staffId, final String staffUserType) {
        Validate.notNull(staffId, "Staff id is required.");
        Validate.notBlank(staffUserType, "Staff user type is required.");

        final var sql = getQuery("FIND_USER_BY_STAFF_ID_STAFF_USER_TYPE");

        UserDetail userDetail;

        try {
            userDetail = jdbcTemplate.queryForObject(
                    sql,
                    createParams("staffId", staffId, "staffUserType", staffUserType),
                    USER_DETAIL_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            userDetail = null;
        }

        return Optional.ofNullable(userDetail);
    }

    @Override
    public boolean isRoleAssigned(final String username, final String caseload, final long roleId) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(username, "username is required.");

        final var count = jdbcTemplate.queryForObject(
                getQuery("ROLE_ASSIGNED_COUNT"),
                createParams(
                        "caseloadId", caseload,
                        "username", username,
                        "roleId", roleId),
                Long.class);

        return count != null && count > 0;
    }

    @Override
    public boolean isUserAssessibleCaseloadAvailable(final String caseload, final String username) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(username, "username is required.");

        final var count = jdbcTemplate.queryForObject(
                getQuery("USER_ACCESSIBLE_CASELOAD_COUNT"),
                createParams("caseloadId", caseload, "username", username),
                Long.class);

        return count != null && count > 0;
    }

    @Override
    public Optional<Long> getRoleIdForCode(final String roleCode) {
        Validate.notBlank(roleCode, "roleCode is required.");

        Long roleId;
        try {
            roleId = jdbcTemplate.queryForObject(
                    getQuery("GET_ROLE_ID_FOR_ROLE_CODE"),
                    createParams("roleCode", roleCode),
                    Long.class);

        } catch (final EmptyResultDataAccessException ex) {
            roleId = null;
        }
        return Optional.ofNullable(roleId);
    }

    @Override
    public Optional<AccessRole> getRoleByCode(final String roleCode) {
        Validate.notBlank(roleCode, "roleCode is required.");

        AccessRole role;
        try {
            role = jdbcTemplate.queryForObject(
                    getQuery("GET_ROLE_BY_ROLE_CODE"),
                    createParams("roleCode", roleCode),
                    ACCESS_ROLE_MAPPER);

        } catch (final EmptyResultDataAccessException ex) {
            role = null;
        }
        return Optional.ofNullable(role);
    }


    @Override
    public void addUserAssessibleCaseload(final String caseload, final String username) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(username, "username is required.");

        jdbcTemplate.update(
                getQuery("USER_ACCESSIBLE_CASELOAD_INSERT"),
                createParams("caseloadId", caseload, "username", username, "startDate", DateTimeConverter.toDate(LocalDate.now())));
    }

    @Override
    public List<StaffUserRole> getAllStaffRolesForCaseload(final String caseload, final String roleCode) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(roleCode, "roleCode is required.");

        return jdbcTemplate.query(getQuery("FIND_ROLES_BY_CASELOAD_AND_ROLE"),
                createParams("caseloadId", caseload, "roleCode", roleCode),
                STAFF_USER_ROLE_MAPPER);

    }

    @Override
    @CacheEvict(value = "findRolesByUsername", allEntries = true)
    public void addRole(final String username, final String caseload, final Long roleId) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(username, "username is required.");
        Validate.notNull(roleId, "roleId is required.");

        jdbcTemplate.update(
                getQuery("INSERT_USER_ROLE"),
                createParams("caseloadId", caseload, "username", username, "roleId", roleId));
    }

    @Override
    @CacheEvict(value = "findRolesByUsername", allEntries = true)
    public void removeRole(final String username, final String caseload, final Long roleId) {
        Validate.notBlank(caseload, "caseload is required.");
        Validate.notBlank(username, "username is required.");
        Validate.notNull(roleId, "roleId is required.");

        jdbcTemplate.update(
                getQuery("DELETE_USER_ROLE"),
                createParams("caseloadId", caseload, "username", username, "roleId", roleId));
    }

    @Override
    public List<UserDetail> findAllUsersWithCaseload(final String caseloadId, final String missingCaseloadId) {
        Validate.notBlank(caseloadId, "An caseload id is required.");

        final var sql = getQuery("FIND_ACTIVE_STAFF_USERS_WITH_ACCESSIBLE_CASELOAD");

        return jdbcTemplate.query(
                sql,
                createParams("caseloadId", caseloadId, "missingCaseloadId", missingCaseloadId),
                USER_DETAIL_ROW_MAPPER);
    }

    @Override
    public List<UserDetail> getUserListByUsernames(final List<String> usernames) {

        final var sql = getQuery("FIND_USERS_BY_USERNAMES");

        return jdbcTemplate.query(
                sql,
                createParams("usernames", usernames),
                USER_DETAIL_ROW_MAPPER);
    }


    @Override
    public Page<UserDetail> findUsersByCaseload(final String caseload, final String accessRole, final NameFilter nameFilter, final PageRequest pageRequest) {
        Validate.notBlank(caseload, "An caseload id is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        return getUsersByCaseload("FIND_USERS_BY_CASELOAD", nameFilter, accessRole, pageRequest, caseload, null);
    }

    @Override
    public Page<UserDetail> findUsers(final String accessRole, final NameFilter nameFilter, final PageRequest pageRequest) {
        Validate.notNull(pageRequest, "Page request details are required.");

        return getUsersByCaseload("FIND_USERS", nameFilter, accessRole, pageRequest, null, null);
    }

    @Override
    public Page<UserDetail> getUsersAsLocalAdministrator(final String laaUsername, final String accessRole, final NameFilter nameFilter, final PageRequest pageRequest) {
        Validate.notBlank(laaUsername, "A username is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        return getUsersByCaseload("FIND_USERS_AVAILABLE_TO_LAA_USER", nameFilter, accessRole, pageRequest, null, laaUsername);
    }

    private Page<UserDetail> getUsersByCaseload(final String namedSql, final NameFilter nameFilter, final String accessRole, final PageRequest pageRequest, final String caseload, final String laaUsername) {
        final var baseSql = applyAccessRoleQuery(applyNameFilterQuery(getQuery(namedSql), nameFilter), accessRole);


        final var builder = queryBuilderFactory.getQueryBuilder(baseSql, USER_DETAIL_ROW_MAPPER.getFieldMap());
        final var sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(USER_DETAIL_ROW_MAPPER);

        final var users = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest,
                        "caseloadId", caseload,
                        "laaUsername", laaUsername,
                        "activeFlag", "Y",
                        "searchTerm", StringUtils.isNotBlank(nameFilter.getSearchTerm()) ? StringUtils.trimToEmpty(nameFilter.getSearchTerm()).toUpperCase() + "%" : null,
                        "surname", StringUtils.isNotBlank(nameFilter.getSurname()) ? StringUtils.trimToEmpty(nameFilter.getSurname()).toUpperCase() + "%" : null,
                        "firstName", StringUtils.isNotBlank(nameFilter.getFirstName()) ? StringUtils.trimToEmpty(nameFilter.getFirstName()).toUpperCase() + "%" : null,
                        "apiCaseloadId", apiCaseloadId,
                        "applicationType", applicationType,
                        "roleCode", accessRole),
                paRowMapper);

        return new Page<>(users, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    private String applyNameFilterQuery(final String baseSql, final NameFilter nameFilter) {
        var nameFilterQuery = baseSql;

        if (nameFilter.isProvided()) {
            if (nameFilter.isFullNameSearch()) {
                nameFilterQuery += FULL_NAME_FILTER_QUERY_TEMPLATE;
            } else {
                nameFilterQuery += NAME_FILTER_QUERY_TEMPLATE;
            }
        }
        return nameFilterQuery;
    }

    private String applyAccessRoleQuery(final String baseSql, final String accessRole) {
        var resultSql = baseSql;

        if (StringUtils.isNotBlank(accessRole)) {

            resultSql += APPLICATION_ROLE_CODE_FILTER_QUERY_TEMPLATE;
        }

        return resultSql;
    }
}
