package uk.gov.justice.hmpps.prison.repository.impl;

import com.google.common.collect.ImmutableMap;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import uk.gov.justice.hmpps.prison.api.model.Alert;
import uk.gov.justice.hmpps.prison.api.model.AlertChanges;
import uk.gov.justice.hmpps.prison.api.model.CreateAlert;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.api.support.Order;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.repository.InmateAlertRepository;
import uk.gov.justice.hmpps.prison.repository.mapping.FieldMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.PageAwareRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.Row2BeanRowMapper;
import uk.gov.justice.hmpps.prison.repository.mapping.StandardBeanPropertyRowMapper;
import uk.gov.justice.hmpps.prison.util.DateTimeConverter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class InmateAlertRepositoryImpl extends RepositoryBase implements InmateAlertRepository {

    private final Map<String, FieldMapper> alertMapping = new ImmutableMap.Builder<String, FieldMapper>()
            .put("ALERT_SEQ", new FieldMapper("alertId"))
            .put("OFFENDER_BOOK_ID", new FieldMapper("bookingId"))
            .put("OFFENDER_ID_DISPLAY", new FieldMapper("offenderNo"))
            .put("ALERT_TYPE", new FieldMapper("alertType"))
            .put("ALERT_TYPE_DESC", new FieldMapper("alertTypeDescription"))
            .put("ALERT_CODE", new FieldMapper("alertCode"))
            .put("ALERT_CODE_DESC", new FieldMapper("alertCodeDescription"))
            .put("COMMENT_TEXT", new FieldMapper("comment", value -> value == null ? "" : value))
            .put("ALERT_STATUS", new FieldMapper("active", "ACTIVE"::equals))
            .put("ALERT_DATE", new FieldMapper("dateCreated", DateTimeConverter::toISO8601LocalDate))
            .put("EXPIRY_DATE", new FieldMapper("dateExpires", DateTimeConverter::toISO8601LocalDate))
            .put("ADD_FIRST_NAME", new FieldMapper("addedByFirstName"))
            .put("ADD_LAST_NAME", new FieldMapper("addedByLastName"))
            .put("UPDATE_FIRST_NAME", new FieldMapper("expiredByFirstName"))
            .put("UPDATE_LAST_NAME", new FieldMapper("expiredByLastName"))
            .build();

    private final StandardBeanPropertyRowMapper<OffenderSummary> CANDIDATE_MAPPER =
            new StandardBeanPropertyRowMapper<>(OffenderSummary.class);

    @Override
    public List<Alert> getActiveAlerts(final long bookingId) {
        final var sql = getQuery("FIND_INMATE_ALERTS");

        final var alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);

        return jdbcTemplate.query(
                sql,
                createParams(
                        "bookingId", bookingId,
                        "alertStatus", "ACTIVE"
                ),
                alertMapper);
    }

    @Override
    public Page<Alert> getAlerts(final long bookingId, final String query, final String orderByField, final Order order, final long offset, final long limit) {
        final var initialSql = getQuery("FIND_INMATE_ALERTS");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, alertMapping);

        final var sql = builder
                .addRowCount()
                .addQuery(query)
                .addOrderBy(order, orderByField)
                .addPagination()
                .build();

        final var alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);
        final var paRowMapper = new PageAwareRowMapper<>(alertMapper);

        final var results = jdbcTemplate.query(
                sql,
                createParams("bookingId", bookingId, "offset", offset, "limit", limit, "alertStatus", null),
                paRowMapper);

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    public Optional<Alert> getAlert(final long bookingId, final long alertSeqId) {
        final var initialSql = getQuery("FIND_INMATE_ALERT");
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, alertMapping);
        final var sql = builder.build();
        final var alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);

        Alert alert;

        try {
            alert = jdbcTemplate.queryForObject(
                    sql,
                    createParams("bookingId", bookingId, "alertSeqId", alertSeqId),
                    alertMapper);
        } catch (final EmptyResultDataAccessException e) {
            alert = null;
        }

        return Optional.ofNullable(alert);
    }

    @Override
    public List<Alert> getAlertsByOffenderNos(final String agencyId, final List<String> offenderNos, final boolean latestOnly, final String query, final String orderByField, final Order order) {
        final var basicSql = getQuery("FIND_INMATE_OFFENDERS_ALERTS");
        final var initialSql = latestOnly ? basicSql + " AND B.BOOKING_SEQ=1" : basicSql;
        final var builder = queryBuilderFactory.getQueryBuilder(initialSql, alertMapping);
        final var sql = builder
                .addQuery(query)
                .addOrderBy(order, orderByField)
                .build();
        final var alertMapper = Row2BeanRowMapper.makeMapping(sql, Alert.class, alertMapping);

        return jdbcTemplate.query(
                sql,
                createParams(
                        "offenderNos", offenderNos,
                        "agencyId", agencyId),
                alertMapper);
    }

    @Override
    public Page<String> getAlertCandidates(final LocalDateTime cutoffTimestamp, final long offset, final long limit) {
        final var builder = queryBuilderFactory.getQueryBuilder(getQuery("GET_ALERT_CANDIDATES"), CANDIDATE_MAPPER);

        final var sql = builder
                .addRowCount()
                .addPagination()
                .build();

        final var rowMapper = Row2BeanRowMapper.makeMapping(sql, OffenderSummary.class, CANDIDATE_MAPPER.getFieldMap());
        final var paRowMapper = new PageAwareRowMapper<>(rowMapper);

        final List<OffenderSummary> offenderSummaries = jdbcTemplate.query(
                sql,
                createParams("cutoffTimestamp", cutoffTimestamp, "offset", offset, "limit", limit),
                paRowMapper);
        final var results = offenderSummaries.stream().map(OffenderSummary::getOffenderNo).collect(Collectors.toList());

        return new Page<>(results, paRowMapper.getTotalRecords(), offset, limit);
    }

    @Override
    public Optional<Alert> updateAlert(final long bookingId, final long alertSeq, final AlertChanges alert) {
        final var expireAlertSql = getQuery("EXPIRE_ALERT");
        final var updateAlertCommentSql = getQuery("UPDATE_ALERT_COMMENT");
        final var insertNextWorkFlowLogEntry = getQuery("INSERT_NEXT_WORK_FLOW_LOG");

        if (alert.getExpiryDate() != null) {
            jdbcTemplate.update(
                    expireAlertSql,
                    createParams(
                            "alertSeq", alertSeq,
                            "bookingId", bookingId,
                            "alertStatus", alert.getAlertStatus(),
                            "comment", alert.getComment(),
                            "expiryDate", DateTimeConverter.toDate(alert.getExpiryDate())
                    ));
        } else {
            jdbcTemplate.update(
                    updateAlertCommentSql,
                    createParams(
                            "alertSeq", alertSeq,
                            "bookingId", bookingId,
                            "comment", alert.getComment()));
        }

        jdbcTemplate.update(
                insertNextWorkFlowLogEntry,
                createParams(
                        "bookingId", bookingId,
                        "alertSeq", alertSeq,
                        "actionCode", "MOD",
                        "workFlowStatus", "DONE",
                        "alertCode", "ALERT"
                )
        );

        return getAlert(bookingId, alertSeq);
    }

    @Override
    public long createNewAlert(final long bookingId, final CreateAlert alert) {
        final var createAlert = getQuery("CREATE_ALERT");
        final var insertWorkFlow = getQuery("INSERT_WORK_FLOW");
        final var insertWorkFlowLog = getQuery("INSERT_WORK_FLOW_LOG");
        final var newAlertsSeqHolder = new GeneratedKeyHolder();
        final var generatedKeyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(
                createAlert,
                createParams(
                        "bookingId", bookingId,
                        "status", "ACTIVE",
                        "caseLoadType", "INST",
                        "alertType", alert.getAlertType(),
                        "alertSubType", alert.getAlertCode(),
                        "alertDate", DateTimeConverter.toDate(alert.getAlertDate()),
                        "commentText", alert.getComment()
                ),
                newAlertsSeqHolder,
                new String[]{"ALERT_SEQ"});

        final long alertSeq = Objects.requireNonNull(newAlertsSeqHolder.getKey()).longValue();

        jdbcTemplate.update(
                insertWorkFlow,
                createParams(
                        "bookingId", bookingId,
                        "alertSeq", alertSeq,
                        "objectCode", "ALERT"
                ),
                generatedKeyHolder,
                new String[]{"WORK_FLOW_ID"});

        final long workFlowId = Objects.requireNonNull(generatedKeyHolder.getKey()).longValue();

        jdbcTemplate.update(
                insertWorkFlowLog,
                createParams(
                        "workFlowId", workFlowId,
                        "workFlowSeq", 1,
                        "actionCode", "ENT",
                        "workFlowStatus", "DONE"
                )
        );

        return alertSeq;
    }
}
