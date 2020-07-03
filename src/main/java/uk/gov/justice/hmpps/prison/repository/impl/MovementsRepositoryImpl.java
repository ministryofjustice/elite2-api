package uk.gov.justice.hmpps.prison.repository.impl;

import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.CourtEvent;
import uk.gov.justice.hmpps.prison.api.model.Movement;
import uk.gov.justice.hmpps.prison.api.model.MovementCount;
import uk.gov.justice.hmpps.prison.api.model.MovementSummary;
import uk.gov.justice.hmpps.prison.api.model.OffenderIn;
import uk.gov.justice.hmpps.prison.api.model.OffenderInReception;
import uk.gov.justice.hmpps.prison.api.model.OffenderMovement;
import uk.gov.justice.hmpps.prison.api.model.OffenderOut;
import uk.gov.justice.hmpps.prison.api.model.ReleaseEvent;
import uk.gov.justice.hmpps.prison.api.model.RollCount;
import uk.gov.justice.hmpps.prison.api.model.TransferEvent;
import uk.gov.justice.hmpps.prison.repository.MovementsRepository;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;

@Repository
public class MovementsRepositoryImpl extends RepositoryBase implements MovementsRepository {

    private static final Set<String> DEACTIVATE_REASON_CODES = Set.of("A", "C", "E", "I");
    private final StandardBeanPropertyRowMapper<Movement> MOVEMENT_MAPPER = new StandardBeanPropertyRowMapper<>(Movement.class);
    private final StandardBeanPropertyRowMapper<OffenderMovement> OFFENDER_MOVEMENT_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderMovement.class);
    private final StandardBeanPropertyRowMapper<RollCount> ROLLCOUNT_MAPPER = new StandardBeanPropertyRowMapper<>(RollCount.class);
    private final StandardBeanPropertyRowMapper<OffenderIn> OFFENDER_IN_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderIn.class);
    private final StandardBeanPropertyRowMapper<OffenderOut> OFFENDER_OUT_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderOut.class);
    private final StandardBeanPropertyRowMapper<OffenderInReception> OFFENDER_IN_RECEPTION_MAPPER = new StandardBeanPropertyRowMapper<>(OffenderInReception.class);
    private final StandardBeanPropertyRowMapper<MovementSummary> MOVEMENT_SUMMARY_MAPPER = new StandardBeanPropertyRowMapper<>(MovementSummary.class);
    private final StandardBeanPropertyRowMapper<CourtEvent> COURT_EVENT_MAPPER = new StandardBeanPropertyRowMapper<>(CourtEvent.class);
    private final StandardBeanPropertyRowMapper<TransferEvent> OFFENDER_TRANSFER_MAPPER = new StandardBeanPropertyRowMapper<>(TransferEvent.class);
    private final StandardBeanPropertyRowMapper<ReleaseEvent> OFFENDER_RELEASE_MAPPER = new StandardBeanPropertyRowMapper<>(ReleaseEvent.class);

    private static final String MOVEMENT_DATE_CLAUSE = " AND OEM.MOVEMENT_DATE = :movementDate";


    @Override
    public List<Movement> getRecentMovementsByDate(final LocalDateTime fromDateTime, final LocalDate movementDate, final List<String> movementTypes) {
        final var sql = getQuery("GET_RECENT_MOVEMENTS_BY_DATE_FOR_BATCH");
        final var types = (movementTypes == null || movementTypes.isEmpty()) ? Set.of("TRN", "REL", "ADM") : movementTypes;


        return jdbcTemplate.query(sql,
                createParams(
                        "movementTypes", types,
                        "fromDateTime", DateTimeConverter.fromLocalDateTime(fromDateTime),
                        "movementDate", DateTimeConverter.toDate(movementDate)), MOVEMENT_MAPPER);
    }

    @Override
    public Movement getMovementByBookingIdAndSequence(final long bookingId, final int sequenceNumber) {
        return jdbcTemplate.queryForObject(getQuery("GET_MOVEMENT_BY_BOOKING_AND_SEQUENCE"),
                createParams(
                "bookingId", bookingId,
                "sequenceNumber", sequenceNumber),
                MOVEMENT_MAPPER);
    }

    @Override
    public List<Movement> getMovementsByOffenders(final List<String> offenderNumbers, final List<String> movementTypes, final boolean latestOnly) {
        if (movementTypes == null || movementTypes.isEmpty()) {
            return jdbcTemplate.query(getQuery("GET_MOVEMENTS_BY_OFFENDERS"), createParams(
                    "offenderNumbers", offenderNumbers, "latestOnly", latestOnly),
                    MOVEMENT_MAPPER);
        }
        return jdbcTemplate.query(getQuery("GET_MOVEMENTS_BY_OFFENDERS_AND_MOVEMENT_TYPES"), createParams(
                "offenderNumbers", offenderNumbers,
                "movementTypes", movementTypes,
                "latestOnly", latestOnly),
                MOVEMENT_MAPPER);
    }

    @Override
    public List<OffenderMovement> getOffendersOut(final String agencyId, final LocalDate movementDate) {
        final var sql = getQuery("GET_OFFENDERS_OUT_TODAY");
        return jdbcTemplate.query(sql, createParams(
                "agencyId", agencyId,
                "movementDate", DateTimeConverter.toDate(movementDate)),
                OFFENDER_MOVEMENT_MAPPER);
    }

    @Override
    public List<RollCount> getRollCount(final String agencyId, final String certifiedFlag) {
        final var sql = getQuery("GET_ROLL_COUNT");
        return jdbcTemplate.query(sql, createParams(
                "agencyId", agencyId,
                "certifiedFlag", certifiedFlag,
                "livingUnitId", null,
                "deactivateReasonCodes", DEACTIVATE_REASON_CODES,
                "currentDateTime", new Date()),
                ROLLCOUNT_MAPPER);
    }

    @Override
    public MovementCount getMovementCount(final String agencyId, final LocalDate date) {

        final var movements = jdbcTemplate.query(
                getQuery("GET_ROLLCOUNT_MOVEMENTS"),
                createParams("agencyId", agencyId, "movementDate", DateTimeConverter.toDate(date)), MOVEMENT_MAPPER);

        final var movementsGroupedByDirection = movements.stream().filter(movement ->
                (movement.getDirectionCode().equals("IN") && movement.getToAgency().equals(agencyId)) ||
                        (movement.getDirectionCode().equals("OUT") && movement.getFromAgency().equals(agencyId)))
                .collect(groupingBy(Movement::getDirectionCode));

        final var outMovements = movementsGroupedByDirection.containsKey("OUT") ? movementsGroupedByDirection.get("OUT").size() : 0;
        final var inMovements = movementsGroupedByDirection.containsKey("IN") ? movementsGroupedByDirection.get("IN").size() : 0;

        return MovementCount.builder()
                .out(outMovements)
                .in(inMovements)
                .build();
    }

    @Override
    public List<OffenderMovement> getEnrouteMovementsOffenderMovementList(final String agencyId, final LocalDate date) {

        final var initialSql = getQuery("GET_ENROUTE_OFFENDER_MOVEMENTS");
        final var sql = date == null ? initialSql : initialSql + MOVEMENT_DATE_CLAUSE;

        return jdbcTemplate.query(sql,
                createParams(
                        "agencyId", agencyId,
                        "movementDate", DateTimeConverter.toDate(date)),
                OFFENDER_MOVEMENT_MAPPER);
    }

    @Override
    public int getEnrouteMovementsOffenderCount(final String agencyId, final LocalDate date) {

        return jdbcTemplate.queryForObject(
                getQuery("GET_ENROUTE_OFFENDER_COUNT"),
                createParams(
                        "agencyId", agencyId,
                        "movementDate", DateTimeConverter.toDate(date)),
                Integer.class);
    }

    @Override
    public List<OffenderIn> getOffendersIn(final String agencyId, final LocalDate movementDate) {
        return jdbcTemplate.query(getQuery("GET_OFFENDER_MOVEMENTS_IN"),
                createParams(
                        "agencyId", agencyId,
                        "movementDate", DateTimeConverter.toDate(movementDate)),
                OFFENDER_IN_MAPPER);
    }

    @Override
    public List<OffenderInReception> getOffendersInReception(final String agencyId) {
        return jdbcTemplate.query(getQuery("GET_OFFENDERS_IN_RECEPTION"),
                createParams("agencyId", agencyId),
                OFFENDER_IN_RECEPTION_MAPPER);
    }

    @Override
    public List<OffenderOut> getOffendersCurrentlyOut(final long livingUnitId) {
        return jdbcTemplate.query(
                getQuery("GET_OFFENDERS_CURRENTLY_OUT_OF_LIVING_UNIT"),
                createParams(
                        "livingUnitId", livingUnitId,
                        "bookingSeq", 1,
                        "inOutStatus", "OUT"),
                OFFENDER_OUT_MAPPER);
    }

    @Override
    public List<OffenderOut> getOffendersCurrentlyOut(final String agencyId) {
        return jdbcTemplate.query(
                getQuery("GET_OFFENDERS_CURRENTLY_OUT_OF_AGENCY"),
                createParams(
                        "agencyId", agencyId,
                        "bookingSeq", 1,
                        "inOutStatus", "OUT",
                        "certifiedFlag", "Y",
                        "activeFlag", "Y"
                ),
                OFFENDER_OUT_MAPPER);
    }

    public List<MovementSummary> getCompletedMovementsForAgencies(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        return jdbcTemplate.query(
                getQuery("GET_MOVEMENTS_BY_AGENCY_AND_TIME_PERIOD"),
                createParams("agencyListFrom", agencies,
                        "agencyListTo", agencies,
                        "fromDateTime", DateTimeConverter.fromLocalDateTime(from),
                        "toDateTime", DateTimeConverter.fromLocalDateTime(to)),
                MOVEMENT_SUMMARY_MAPPER);
    }

    public List<CourtEvent> getCourtEvents(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        return jdbcTemplate.query(
                getQuery("GET_COURT_EVENTS_BY_AGENCY_AND_TIME_PERIOD"),
                createParams("agencyListFrom", agencies,
                        "agencyListTo", agencies,
                        "fromDateTime", DateTimeConverter.fromLocalDateTime(from),
                        "toDateTime", DateTimeConverter.fromLocalDateTime(to)),
                COURT_EVENT_MAPPER);
    }

    public List<TransferEvent> getOffenderTransfers(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        return jdbcTemplate.query(
                getQuery("GET_OFFENDER_TRANSFERS_BY_AGENCY_AND_TIME_PERIOD"),
                createParams("agencyListFrom", agencies,
                        "agencyListTo", agencies,
                        "fromDateTime", DateTimeConverter.fromLocalDateTime(from),
                        "toDateTime", DateTimeConverter.fromLocalDateTime(to)),
                OFFENDER_TRANSFER_MAPPER);
    }

    public List<ReleaseEvent> getOffenderReleases(final List<String> agencies, final LocalDateTime from, final LocalDateTime to) {

        return jdbcTemplate.query(
                getQuery("GET_OFFENDER_RELEASES_BY_AGENCY_AND_DATE"),
                createParams("agencyListFrom", agencies,
                        "fromDate", DateTimeConverter.fromTimestamp(DateTimeConverter.fromLocalDateTime(from)),
                        "toDate", DateTimeConverter.fromTimestamp(DateTimeConverter.fromLocalDateTime(to))),
                OFFENDER_RELEASE_MAPPER);
    }

}
