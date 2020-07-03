package net.syscon.prison.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.syscon.prison.api.model.StaffDetail;
import net.syscon.prison.api.model.StaffLocationRole;
import net.syscon.prison.api.model.StaffRole;
import net.syscon.prison.api.support.Page;
import net.syscon.prison.api.support.PageRequest;
import net.syscon.prison.repository.StaffRepository;
import net.syscon.prison.repository.mapping.PageAwareRowMapper;
import net.syscon.prison.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.prison.service.support.LocationProcessor;
import net.syscon.util.DateTimeConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
@Slf4j
public class StaffRepositoryImpl extends RepositoryBase implements StaffRepository {
    private static final String NAME_FILTER_QUERY_TEMPLATE = " AND (UPPER(FIRST_NAME) LIKE '%s%%' OR UPPER(LAST_NAME) LIKE '%s%%')";
    private static final String STAFF_ID_FILTER_QUERY_TEMPLATE = " AND STAFF_ID = %d";
    private static final String ACTIVE_FILTER_CLAUSE = " AND SM.STATUS = 'ACTIVE'";

    private static final StandardBeanPropertyRowMapper<StaffDetail> STAFF_DETAIL_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(StaffDetail.class);

    private static final StandardBeanPropertyRowMapper<StaffLocationRole> STAFF_LOCATION_ROLE_ROW_MAPPER =
            new StandardBeanPropertyRowMapper<>(StaffLocationRole.class);

    private static final StandardBeanPropertyRowMapper<StaffRole> STAFF_ROLES_MAPPER =
            new StandardBeanPropertyRowMapper<>(StaffRole.class);


    @Override
    @Cacheable("findByStaffId")
    public Optional<StaffDetail> findByStaffId(final Long staffId) {
        Validate.notNull(staffId, "A staff id is required in order to retrieve staff details.");

        final var sql = getQuery("FIND_STAFF_BY_STAFF_ID");

        StaffDetail staffDetail;

        try {
            staffDetail = jdbcTemplate.queryForObject(
                    sql,
                    createParams("staffId", staffId),
                    STAFF_DETAIL_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            staffDetail = null;
        }

        return Optional.ofNullable(staffDetail);
    }

    @Override
    public Optional<StaffDetail> findStaffByPersonnelIdentifier(final String idType, final String id) {
        Validate.notBlank(idType, "An id type is required.");
        Validate.notBlank(id, "An id is required.");

        final var sql = getQuery("FIND_STAFF_BY_PERSONNEL_IDENTIFIER");

        StaffDetail staffDetail;

        try {
            staffDetail = jdbcTemplate.queryForObject(
                    sql,
                    createParams("idType", idType, "id", id),
                    STAFF_DETAIL_ROW_MAPPER);
        } catch (final EmptyResultDataAccessException ex) {
            staffDetail = null;
        } catch (final IncorrectResultSizeDataAccessException ex) {
            log.error("Duplicate personnel identification records found for idType [{}] and id [{}].", idType, id);
            staffDetail = null;
        }

        return Optional.ofNullable(staffDetail);
    }

    public List<String> findEmailAddressesForStaffId(final Long staffId) {

        return jdbcTemplate.query(getQuery("GET_STAFF_EMAIL_ADDRESSES"), createParams("staffId", staffId, "ownerClass", "STF", "addressClass", "EMAIL"), (rs, rowNum) -> rs.getString(1));
    }

    @Override
    public Page<StaffLocationRole> findStaffByAgencyPositionRole(final String agencyId, final String position, final String role, final String nameFilter, final Long staffId, final Boolean activeOnly, final PageRequest pageRequest) {
        Validate.notBlank(agencyId, "An agency id is required.");
        Validate.notBlank(position, "A position code is required.");
        Validate.notBlank(role, "A role code is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        var baseSql = applyStaffIdFilterQuery(applyNameFilterQuery(getQuery("FIND_STAFF_BY_AGENCY_POSITION_ROLE"), nameFilter), staffId);
        baseSql = applyActiveClause(baseSql, activeOnly);

        final var builder = queryBuilderFactory.getQueryBuilder(baseSql, STAFF_LOCATION_ROLE_ROW_MAPPER.getFieldMap());
        final var sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<StaffLocationRole>(STAFF_LOCATION_ROLE_ROW_MAPPER);

        final var staffDetails = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId, "position", position, "role", role),
                paRowMapper);

        staffDetails.forEach(sd -> sd.setAgencyDescription(LocationProcessor.formatLocation(sd.getAgencyDescription())));
        return new Page<>(staffDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public Page<StaffLocationRole> findStaffByAgencyRole(final String agencyId, final String role, final String nameFilter, final Long staffId, final Boolean activeOnly, final PageRequest pageRequest) {
        Validate.notBlank(agencyId, "An agency id is required.");
        Validate.notBlank(role, "A role code is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        var baseSql = applyStaffIdFilterQuery(applyNameFilterQuery(getQuery("FIND_STAFF_BY_AGENCY_AND_ROLE"), nameFilter), staffId);
        baseSql = applyActiveClause(baseSql, activeOnly);

        final var builder = queryBuilderFactory.getQueryBuilder(baseSql, STAFF_LOCATION_ROLE_ROW_MAPPER.getFieldMap());
        final var sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        final var paRowMapper = new PageAwareRowMapper<>(STAFF_LOCATION_ROLE_ROW_MAPPER);

        final var staffDetails = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId, "role", role),
                paRowMapper);
        staffDetails.forEach(sd -> sd.setAgencyDescription(LocationProcessor.formatLocation(sd.getAgencyDescription())));

        return new Page<>(staffDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public List<StaffRole> getAllRolesForAgency(final Long staffId, final String agencyId) {
        Validate.notNaN(staffId, "A staffId code is required.");
        Validate.notBlank(agencyId, "An agency id is required.");

        final var sql = getQuery("GET_STAFF_ROLES_FOR_AGENCY");

        return jdbcTemplate.query(
                sql,
                createParams("staffId", staffId, "agencyId", agencyId, "currentDate", DateTimeConverter.toDate(LocalDate.now())),
                STAFF_ROLES_MAPPER);
    }

    private String applyNameFilterQuery(final String baseSql, final String nameFilter) {
        var nameFilterQuery = baseSql;

        if (StringUtils.isNotBlank(nameFilter)) {
            final var upperNameFilter = StringUtils.replace(nameFilter.toUpperCase(), "'", "''");

            nameFilterQuery += String.format(NAME_FILTER_QUERY_TEMPLATE, upperNameFilter, upperNameFilter);
        }
        return nameFilterQuery;
    }

    private String applyStaffIdFilterQuery(final String baseSql, final Long staffIdFilter) {
        var nameFilterQuery = baseSql;

        if (staffIdFilter != null) {
            nameFilterQuery += String.format(STAFF_ID_FILTER_QUERY_TEMPLATE, staffIdFilter);
        }
        return nameFilterQuery;
    }

    private String applyActiveClause(final String baseSql, final Boolean activeOnly) {
        var query = baseSql;

        if (activeOnly != null && activeOnly) {
            query += ACTIVE_FILTER_CLAUSE;
        }
        return query;
    }
}
