package net.syscon.elite.repository.impl;

import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.encoder.org.apache.commons.lang.StringEscapeUtils;
import net.syscon.elite.api.model.StaffDetail;
import net.syscon.elite.api.model.StaffLocationRole;
import net.syscon.elite.api.model.StaffRole;
import net.syscon.elite.api.support.Page;
import net.syscon.elite.api.support.PageRequest;
import net.syscon.elite.repository.StaffRepository;
import net.syscon.elite.repository.mapping.PageAwareRowMapper;
import net.syscon.elite.repository.mapping.StandardBeanPropertyRowMapper;
import net.syscon.elite.service.support.LocationProcessor;
import net.syscon.util.DateTimeConverter;
import net.syscon.util.IQueryBuilder;
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
    public Optional<StaffDetail> findByStaffId(Long staffId) {
        Validate.notNull(staffId, "A staff id is required in order to retrieve staff details.");

        String sql = getQuery("FIND_STAFF_BY_STAFF_ID");

        StaffDetail staffDetail;

        try {
            staffDetail = jdbcTemplate.queryForObject(
                    sql,
                    createParams("staffId", staffId),
                    STAFF_DETAIL_ROW_MAPPER);
        } catch (EmptyResultDataAccessException ex) {
            staffDetail = null;
        }

        return Optional.ofNullable(staffDetail);
    }

    @Override
    public Optional<StaffDetail> findStaffByPersonnelIdentifier(String idType, String id) {
        Validate.notBlank(idType, "An id type is required.");
        Validate.notBlank(id, "An id is required.");

        String sql = getQuery("FIND_STAFF_BY_PERSONNEL_IDENTIFIER");

        StaffDetail staffDetail;

        try {
            staffDetail = jdbcTemplate.queryForObject(
                    sql,
                    createParams("idType", idType, "id", id),
                    STAFF_DETAIL_ROW_MAPPER);
        } catch (EmptyResultDataAccessException ex) {
            staffDetail = null;
        } catch (IncorrectResultSizeDataAccessException ex) {
            log.error("Duplicate personnel identification records found for idType [{}] and id [{}].", idType, id);

            staffDetail = null;
        }

        return Optional.ofNullable(staffDetail);
    }

    @Override
    public Page<StaffLocationRole> findStaffByAgencyPositionRole(String agencyId, String position, String role, String nameFilter, Long staffId, Boolean activeOnly, PageRequest pageRequest) {
        Validate.notBlank(agencyId, "An agency id is required.");
        Validate.notBlank(position, "A position code is required.");
        Validate.notBlank(role, "A role code is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        String baseSql = applyStaffIdFilterQuery(applyNameFilterQuery(getQuery("FIND_STAFF_BY_AGENCY_POSITION_ROLE"), nameFilter), staffId);
        baseSql = applyActiveClause(baseSql, activeOnly);

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(baseSql, STAFF_LOCATION_ROLE_ROW_MAPPER.getFieldMap());
        String sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        PageAwareRowMapper<StaffLocationRole> paRowMapper = new PageAwareRowMapper<>(STAFF_LOCATION_ROLE_ROW_MAPPER);

        List<StaffLocationRole> staffDetails = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId, "position", position, "role", role),
                paRowMapper);

        staffDetails.forEach(sd -> sd.setAgencyDescription(LocationProcessor.formatLocation(sd.getAgencyDescription())));
        return new Page<>(staffDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public Page<StaffLocationRole> findStaffByAgencyRole(String agencyId, String role, String nameFilter, Long staffId, Boolean activeOnly, PageRequest pageRequest) {
        Validate.notBlank(agencyId, "An agency id is required.");
        Validate.notBlank(role, "A role code is required.");
        Validate.notNull(pageRequest, "Page request details are required.");

        String baseSql = applyStaffIdFilterQuery(applyNameFilterQuery(getQuery("FIND_STAFF_BY_AGENCY_AND_ROLE"), nameFilter), staffId);
        baseSql = applyActiveClause(baseSql, activeOnly);

        IQueryBuilder builder = queryBuilderFactory.getQueryBuilder(baseSql, STAFF_LOCATION_ROLE_ROW_MAPPER.getFieldMap());
        String sql = builder
                .addRowCount()
                .addOrderBy(pageRequest)
                .addPagination()
                .build();

        PageAwareRowMapper<StaffLocationRole> paRowMapper = new PageAwareRowMapper<>(STAFF_LOCATION_ROLE_ROW_MAPPER);

        List<StaffLocationRole> staffDetails = jdbcTemplate.query(
                sql,
                createParamSource(pageRequest, "agencyId", agencyId, "role", role),
                paRowMapper);
        staffDetails.forEach(sd -> sd.setAgencyDescription(LocationProcessor.formatLocation(sd.getAgencyDescription())));

        return new Page<>(staffDetails, paRowMapper.getTotalRecords(), pageRequest.getOffset(), pageRequest.getLimit());
    }

    @Override
    public List<StaffRole> getAllRolesForAgency(Long staffId, String agencyId) {
        Validate.notNaN(staffId, "A staffId code is required.");
        Validate.notBlank(agencyId, "An agency id is required.");

        String sql = getQuery("GET_STAFF_ROLES_FOR_AGENCY");

        return jdbcTemplate.query(
                sql,
                createParams("staffId", staffId, "agencyId", agencyId, "currentDate", DateTimeConverter.toDate(LocalDate.now())),
                STAFF_ROLES_MAPPER);
    }

    private String applyNameFilterQuery(String baseSql, String nameFilter) {
        String nameFilterQuery = baseSql;

        if (StringUtils.isNotBlank(nameFilter)) {
            String upperNameFilter = StringEscapeUtils.escapeSql(nameFilter.toUpperCase());

            nameFilterQuery += String.format(NAME_FILTER_QUERY_TEMPLATE, upperNameFilter, upperNameFilter);
        }
        return nameFilterQuery;
    }

    private String applyStaffIdFilterQuery(String baseSql, Long staffIdFilter) {
        String nameFilterQuery = baseSql;

        if (staffIdFilter != null) {
            nameFilterQuery += String.format(STAFF_ID_FILTER_QUERY_TEMPLATE, staffIdFilter);
        }
        return nameFilterQuery;
    }

    private String applyActiveClause(String baseSql, Boolean activeOnly) {
        String query = baseSql;

        if (activeOnly != null && activeOnly) {
            query += ACTIVE_FILTER_CLAUSE;
        }
        return query;
    }
}
