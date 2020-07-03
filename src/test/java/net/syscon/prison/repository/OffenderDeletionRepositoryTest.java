package net.syscon.prison.repository;

import net.syscon.prison.service.EntityNotFoundException;
import net.syscon.prison.web.config.PersistenceConfigs;
import org.assertj.core.api.Condition;
import org.assertj.core.api.ListAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@JdbcTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@AutoConfigureTestDatabase(replace = NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class OffenderDeletionRepositoryTest {

    @Autowired
    private OffenderDeletionRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    public void deleteOffender() {

        assertOffenderDataExists();

        assertThat(repository.deleteOffender("A1234AA"))
                .containsExactly("-1001");

        assertOffenderDataDeleted();

        // GL_TRANSACTIONS should still have the anonymised data:
        assertThat(jdbcTemplate.queryForList(
                "SELECT txn_id FROM gl_transactions WHERE txn_id = 301826802 and gl_entry_seq = 1",
                String.class))
                .isNotEmpty();
    }

    @Test
    @Transactional
    public void deleteUnknownOffenderThrows() {
        assertThatThrownBy(() -> repository.deleteOffender("unknown"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Resource with id [unknown] not found.");
    }

    private void assertOffenderDataExists() {
        checkTables(new Condition<>(list -> !list.isEmpty(), "Entry Found"));
    }

    private void assertOffenderDataDeleted() {
        checkTables(new Condition<>(List::isEmpty, "Entry Not Found"));
    }

    private void checkTables(final Condition<? super List<? extends String>> condition) {

        queryForCourtEventCharges().is(condition);

        queryByHealthProblemId("OFFENDER_MEDICAL_TREATMENTS").is(condition);
        queryByHealthProblemId("OFFENDER_HEALTH_PROBLEMS").is(condition);

        queryByProgramId("OFFENDER_COURSE_ATTENDANCES").is(condition);
        queryByProgramId("OFFENDER_PRG_PRF_PAY_BANDS").is(condition);
        queryByProgramId("OFFENDER_PROGRAM_PROFILES").is(condition);

        queryByAgencyIncidentId("AGENCY_INCIDENT_REPAIRS").is(condition);
        queryByAgencyIncidentId("AGENCY_INCIDENT_CHARGES").is(condition);
        queryByAgencyIncidentId("AGENCY_INCIDENT_PARTIES").is(condition);
        queryByAgencyIncidentId("AGENCY_INCIDENTS").is(condition);

        queryByIncidentCaseId("INCIDENT_CASES").is(condition);
        queryByIncidentCaseId("INCIDENT_CASE_QUESTIONS").is(condition);
        queryByIncidentCaseId("INCIDENT_CASE_RESPONSES").is(condition);
        queryByIncidentCaseId("INCIDENT_CASE_REQUIREMENTS").is(condition);

        queryByOffenderBookId("INCIDENT_CASE_PARTIES").is(condition);
        queryByOffenderBookId("BED_ASSIGNMENT_HISTORIES").is(condition);
        queryByOffenderBookId("COURT_EVENTS").is(condition);
        queryByOffenderBookId("OFFENDER_ALERTS").is(condition);
        queryByOffenderBookId("OFFENDER_ASSESSMENTS").is(condition);
        queryByOffenderBookId("OFFENDER_BOOKING_DETAILS").is(condition);
        queryByOffenderBookId("OFFENDER_CASE_NOTES").is(condition);
        queryByOffenderBookId("OFFENDER_CASES").is(condition);
        queryByOffenderBookId("OFFENDER_CONTACT_PERSONS").is(condition);
        queryByOffenderBookId("OFFENDER_CURFEWS").is(condition);
        queryByOffenderBookId("OFFENDER_EXTERNAL_MOVEMENTS").is(condition);
        queryByOffenderBookId("OFFENDER_IEP_LEVELS").is(condition);
        queryByOffenderBookId("OFFENDER_IMPRISON_STATUSES").is(condition);
        queryByOffenderBookId("OFFENDER_IND_SCHEDULES").is(condition);
        queryByOffenderBookId("OFFENDER_KEY_DATE_ADJUSTS").is(condition);
        queryByOffenderBookId("OFFENDER_KEY_WORKERS").is(condition);
        queryByOffenderBookId("OFFENDER_LANGUAGES").is(condition);
        queryByOffenderBookId("OFFENDER_OIC_SANCTIONS").is(condition);
        queryByOffenderBookId("OFFENDER_PHYSICAL_ATTRIBUTES").is(condition);
        queryByOffenderBookId("OFFENDER_PRG_OBLIGATIONS").is(condition);
        queryByOffenderBookId("OFFENDER_PROFILE_DETAILS").is(condition);
        queryByOffenderBookId("OFFENDER_RELEASE_DETAILS").is(condition);
        queryByOffenderBookId("OFFENDER_SENT_CALCULATIONS").is(condition);
        queryByOffenderBookId("OFFENDER_VISIT_VISITORS").is(condition);
        queryByOffenderBookId("OFFENDER_VISITS").is(condition);
        queryByOffenderBookId("OFFENDER_VISIT_BALANCES").is(condition);
        queryByOffenderBookId("OFFENDER_CHARGES").is(condition);
        queryByOffenderBookId("OFFENDER_SENTENCE_TERMS").is(condition);
        queryByOffenderBookId("OFFENDER_SENTENCES").is(condition);
        queryByOffenderBookId("ORDERS").is(condition);
        queryByOffenderBookId("OFFENDER_BELIEFS").is(condition);

        queryByOffenderId("GL_TRANSACTIONS").is(condition);
        queryByOffenderId("OFFENDER_BOOKINGS").is(condition);
        queryByOffenderId("OFFENDER_IDENTIFIERS").is(condition);
        queryByOffenderId("OFFENDER_SUB_ACCOUNTS").is(condition);
        queryByOffenderId("OFFENDER_TRANSACTIONS").is(condition);
        queryByOffenderId("OFFENDER_TRUST_ACCOUNTS").is(condition);
        queryByOffenderId("OFFENDERS").is(condition);
    }

    private ListAssert<String> queryByAgencyIncidentId(final String tableName) {
        return assertThat(jdbcTemplate.queryForList(
                "SELECT agency_incident_id FROM " + tableName + " WHERE agency_incident_id IN (-6)",
                String.class));
    }

    private ListAssert<String> queryByHealthProblemId(final String tableName) {
        return assertThat(jdbcTemplate.queryForList(
                "SELECT offender_health_problem_id FROM " + tableName + " WHERE offender_health_problem_id IN (-201, -205, -206)",
                String.class));
    }

    private ListAssert<String> queryByProgramId(final String tableName) {
        return assertThat(jdbcTemplate.queryForList(
                "SELECT off_prgref_id FROM " + tableName + " WHERE off_prgref_id IN (-1, -2, -3, -4)",
                String.class));
    }

    private ListAssert<String> queryForCourtEventCharges() {
        return assertThat(jdbcTemplate.queryForList(
                "SELECT event_id FROM court_event_charges WHERE event_id = -201 AND offender_charge_id = -1",
                String.class));
    }

    private ListAssert<String> queryByIncidentCaseId(final String tableName) {
        return assertThat(jdbcTemplate.queryForList(
                "SELECT incident_case_id FROM " + tableName + " WHERE incident_case_id IN (-1, -2, -3)",
                String.class));
    }

    private ListAssert<String> queryByOffenderBookId(final String tableName) {
        return assertThat(jdbcTemplate.queryForList(
                "SELECT offender_book_id FROM " + tableName + " WHERE offender_book_id = -1",
                String.class));
    }

    private ListAssert<String> queryByOffenderId(final String tableName) {
        return assertThat(jdbcTemplate.queryForList(
                "SELECT offender_id FROM " + tableName + " WHERE offender_id = -1001",
                String.class));
    }

}