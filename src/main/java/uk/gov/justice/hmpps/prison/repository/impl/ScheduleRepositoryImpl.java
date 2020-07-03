package uk.gov.justice.hmpps.prison.repository.impl;

import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.PrisonerSchedule;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.repository.ScheduleRepository;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.sql.Types;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Repository
public class ScheduleRepositoryImpl extends RepositoryBase implements ScheduleRepository {

    private static final String AND_OFFENDER_NUMBERS = " AND O.OFFENDER_ID_DISPLAY in (:offenderNos)";
    private static final StandardBeanPropertyRowMapper<PrisonerSchedule> EVENT_ROW_MAPPER = new StandardBeanPropertyRowMapper<>(PrisonerSchedule.class);

    @Override
    public List<PrisonerSchedule> getAllActivitiesAtAgency(final String agencyId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order, boolean includeSuspended) {
        final var initialSql = getQuery("GET_ALL_ACTIVITIES_AT_AGENCY");

        final var sql = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap())
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("agencyId", agencyId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
                        "includeSuspended", includeSuspended ? Set.of("Y", "N") : Set.of("N")),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<PrisonerSchedule> getActivitiesAtLocation(final Long locationId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order, boolean includeSuspended) {
        final var initialSql = getQuery("GET_ACTIVITIES_AT_ONE_LOCATION");

        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("locationId", locationId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate)),
                        "includeSuspended", includeSuspended ? Set.of("Y", "N") : Set.of("N")),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<PrisonerSchedule> getLocationAppointments(final Long locationId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(locationId, "locationId is a required parameter");

        final var initialSql = getQuery("GET_APPOINTMENTS_AT_LOCATION");

        return getScheduledEvents(initialSql, locationId, fromDate, toDate, orderByFields, order);
    }

    @Override
    public List<PrisonerSchedule> getLocationVisits(final Long locationId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        Objects.requireNonNull(locationId, "locationId is a required parameter");

        final var initialSql = getQuery("GET_VISITS_AT_LOCATION");

        return getScheduledEvents(initialSql, locationId, fromDate, toDate, orderByFields, order);
    }

    private List<PrisonerSchedule> getScheduledEvents(final String initialSql, final Long locationId, final LocalDate fromDate, final LocalDate toDate, final String orderByFields, final Order order) {
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, EVENT_ROW_MAPPER.getFieldMap());

        final var sql = builder
                .addOrderBy(order, orderByFields)
                .build();

        return jdbcTemplate.query(
                sql,
                createParams("locationId", locationId,
                        "fromDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(fromDate)),
                        "toDate", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(toDate))), EVENT_ROW_MAPPER);
    }

    @Override
    public List<PrisonerSchedule> getVisits(final String agencyId, final List<String> offenderNo, final LocalDate date) {
        return jdbcTemplate.query(
                getQuery("GET_VISITS") + AND_OFFENDER_NUMBERS,
                createParams(
                        "offenderNos", offenderNo,
                        "date", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<PrisonerSchedule> getAppointments(final String agencyId, final List<String> offenderNo, final LocalDate date) {
        return jdbcTemplate.query(
                getQuery("GET_APPOINTMENTS") + AND_OFFENDER_NUMBERS,
                createParams(
                        "offenderNos", offenderNo,
                        "date", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<PrisonerSchedule> getActivities(final String agencyId, final List<String> offenderNumbers, final LocalDate date) {
        return jdbcTemplate.query(
                getQuery("GET_ACTIVITIES") + AND_OFFENDER_NUMBERS,
                createParams(
                        "offenderNos", offenderNumbers,
                        "date", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<PrisonerSchedule> getCourtEvents(final List<String> offenderNumbers, final LocalDate date) {
        return jdbcTemplate.query(
                getQuery("GET_COURT_EVENTS"),
                createParams(
                        "offenderNos", offenderNumbers,
                        "date", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))),
                EVENT_ROW_MAPPER);
    }

    @Override
    public List<PrisonerSchedule> getExternalTransfers(final String agencyId, final List<String> offenderNumbers, final LocalDate date) {
        return jdbcTemplate.query(
                getQuery("GET_EXTERNAL_TRANSFERS") + AND_OFFENDER_NUMBERS,
                createParams(
                        "offenderNos", offenderNumbers,
                        "agencyId", agencyId,
                        "date", new SqlParameterValue(Types.DATE, DateTimeConverter.toDate(date))),
                EVENT_ROW_MAPPER);
    }
}
