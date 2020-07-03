package net.syscon.prison.repository;

import lombok.val;
import net.syscon.prison.api.model.adjudications.Adjudication;
import net.syscon.prison.api.model.adjudications.AdjudicationCharge;
import net.syscon.prison.api.model.adjudications.AdjudicationDetail;
import net.syscon.prison.api.model.adjudications.Hearing;
import net.syscon.prison.api.model.adjudications.HearingResult;
import net.syscon.prison.api.model.adjudications.Sanction;
import net.syscon.prison.api.support.PageRequest;
import net.syscon.prison.service.AdjudicationSearchCriteria;
import net.syscon.prison.web.config.PersistenceConfigs;
import org.assertj.core.api.ListAssert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@JdbcTest
@AutoConfigureTestDatabase(replace = NONE)
@ContextConfiguration(classes = PersistenceConfigs.class)
public class AdjudicationsRepositoryTest {

    private static final Adjudication EARLIEST_ADJUDICATION = new Adjudication(-1, LocalDateTime.of(2017, 2, 23, 0, 1), -1, "LEI", 1L, List.of(
            new AdjudicationCharge(
                    "5139/1",
                    "51:1N",
                    "Commits any assault - assault on non prison officer member of staff",
                    null),
            new AdjudicationCharge(
                    "5139/2",
                    "51:2B",
                    "Detains any person against his will - detention against will -non offr/staff/inmate",
                    "DISMISSED")));
    private static final Adjudication MIDDLE_ADJUDICATION = new Adjudication(-5, LocalDateTime.of(2019, 1, 25, 0, 2), -2, "LEI", 1L, List.of(
            new AdjudicationCharge(
                    "5139/5",
                    "51:8D",
                    "Fails to comply with any condition upon which he is temporarily released under rule 9 - failure to comply with conditions of temp release",
                    "PROVED")));
    private static final Adjudication LATEST_ADJUDICATION = new Adjudication(-3, LocalDateTime.of(2019, 8, 25, 0, 3), -3, "MDI", 1L, List.of(
            new AdjudicationCharge(
                    "5139/4",
                    "51:2D",
                    "Detains any person against his will - detention against will of staff (not prison offr)",
                    "PROVED")));

    @Autowired

    private AdjudicationsRepository repository;

    @Test
    public void testGetDetailsMultiple() {

        final var awards = repository.findAwards(-3L);

        assertThat(awards).asList()
                .hasSize(2)
                .extracting("sanctionCode", "sanctionCodeDescription", "limit", "months", "days", "comment", "status", "statusDescription", "effectiveDate")
                .contains(tuple("FORFEIT", "Forfeiture of Privileges", null, null, 30, null, "IMMEDIATE", "Immediate", LocalDate.of(2016, 11, 8)),
                        tuple("STOP_PCT", "Stoppage of Earnings (%)", BigDecimal.valueOf(2020L, 2), 4, 5, "test comment", "IMMEDIATE", "Immediate", LocalDate.of(2016, 11, 9)));
    }

    @Test
    public void testGetDetailsInvalidBookingId() {
        final var awards = repository.findAwards(1001L);
        assertThat(awards.isEmpty()).isTrue();
    }

    @Test
    public void testGetDetailsMultiple2() {

        val awards = repository.findAwards(-1L);

        assertThat(awards).asList()
                .hasSize(2)
                .extracting("sanctionCode", "sanctionCodeDescription", "limit", "months", "days", "comment", "status", "statusDescription", "effectiveDate")
                .contains(tuple("ADA", "Additional Days Added", null, null, null, "Some Comment Text", "SUSPENDED", "Suspended", LocalDate.of(2016, 10, 17)),
                        tuple("CC", "Cellular Confinement", null, null, 15, null, "IMMEDIATE", "Immediate", LocalDate.of(2016, 11, 9)));
    }


    @Test
    public void findAdjudicationOffences() {

        var offences = repository.findAdjudicationOffences("A118GGG");
        assertThat(offences).extracting("id", "code", "description").containsExactly(
                tuple("81", "51:1N", "Commits any assault - assault on non prison officer member of staff"),
                tuple("83", "51:2B", "Detains any person against his will - detention against will -non offr/staff/inmate"),
                tuple("85", "51:2D", "Detains any person against his will - detention against will of staff (not prison offr)"),
                tuple("86", "51:8D", "Fails to comply with any condition upon which he is temporarily released under rule 9 - failure to comply with conditions of temp release")
        );

        offences = repository.findAdjudicationOffences("A118HHH");
        assertThat(offences).extracting("id", "code", "description").containsExactly(
                tuple("84", "51:2C", "Detains any person against his will - detention against will of prison officer grade"),
                tuple("85", "51:2D", "Detains any person against his will - detention against will of staff (not prison offr)")
        );
    }

    @Test
    public void findAdjudicationLocations() {

        var locations = repository.findAdjudicationAgencies("A118GGG");
        assertThat(locations).extracting("agencyId", "description", "agencyType").containsExactly(
                tuple("LEI", "LEEDS", "INST"),
                tuple("MDI", "MOORLAND", "INST")
        );

        locations = repository.findAdjudicationAgencies("A118HHH");
        assertThat(locations).extracting("agencyId", "description", "agencyType").containsExactly(
                tuple("LEI", "LEEDS", "INST"),
                tuple("MDI", "MOORLAND", "INST")
        );
    }

    @Test
    public void retrieveAdjudicationsForOffender() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
    }

    @Test
    public void filterByStartDate() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .startDate(MIDDLE_ADJUDICATION.getReportTime().plusDays(1).toLocalDate())
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(LATEST_ADJUDICATION);
    }

    @Test
    public void filterByEndDate() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .endDate(MIDDLE_ADJUDICATION.getReportTime().minusDays(1).toLocalDate())
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(EARLIEST_ADJUDICATION);
    }

    @Test
    public void filterByOffence() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .offenceId("86")
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(MIDDLE_ADJUDICATION);
    }

    @Test
    public void filterByLocation() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .agencyId(LATEST_ADJUDICATION.getAgencyId())
                .pageRequest(new PageRequest(0L, 10L))
                .build());

        assertThat(results.getItems()).containsExactly(LATEST_ADJUDICATION);
    }

    @Test
    public void pagination() {

        resultsFor(new PageRequest(0L, null)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(1L, null)).containsExactly(MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(2L, null)).containsExactly(EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(3L, null)).isEmpty();

        resultsFor(new PageRequest(null, 0L)).isEmpty();
        resultsFor(new PageRequest(null, 1L)).containsExactly(LATEST_ADJUDICATION);
        resultsFor(new PageRequest(null, 2L)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION);
        resultsFor(new PageRequest(null, 3L)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(null, 4L)).containsExactly(LATEST_ADJUDICATION, MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);

        resultsFor(new PageRequest(1L, 1L)).containsExactly(MIDDLE_ADJUDICATION);
        resultsFor(new PageRequest(1L, 2L)).containsExactly(MIDDLE_ADJUDICATION, EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(2L, 1L)).containsExactly(EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(2L, 2L)).containsExactly(EARLIEST_ADJUDICATION);
        resultsFor(new PageRequest(3L, 1L)).isEmpty();
    }

    private ListAssert<Adjudication> resultsFor(final PageRequest pageRequest) {
        return assertThat(repository.findAdjudications(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118GGG")
                .pageRequest(pageRequest)
                .build()).getItems());
    }

    @Test
    public void anotherInmateHasAnAdjudicationForSameIncident() {

        val results = repository.findAdjudications(AdjudicationSearchCriteria.builder()
                .offenderNumber("A118HHH")
                .pageRequest(new PageRequest(1L, 1L))
                .build());

        assertThat(results.getItems()).containsExactly(
                new Adjudication(-2, LocalDateTime.of(2017, 2, 23, 0, 1), -1, "LEI", 2L, List.of(
                        new AdjudicationCharge(
                                "5139/3",
                                "51:2C",
                                "Detains any person against his will - detention against will of prison officer grade",
                                "NOT_PROVED"))));
    }

    @Test
    public void findAdjudicationDetails() {

        val results = repository.findAdjudicationDetails("A118HHH", -7);


        assertThat(results.get()).isEqualTo(
                AdjudicationDetail.builder()
                        .adjudicationNumber(-7L)
                        .incidentTime(LocalDateTime.of(1999, 6, 25, 0, 0))
                        .agencyId("MDI")
                        .internalLocationId(-41)
                        .incidentDetails("mKSouDOCmKSouDO")
                        .reportNumber(-4L)
                        .reportType("Miscellaneous")
                        .reporterFirstName("Jo")
                        .reporterLastName("O'brien")
                        .reportTime(LocalDateTime.of(2019, 8, 25, 0, 3))
                        .hearing(
                                Hearing.builder()
                                        .oicHearingId(-1L)
                                        .hearingType("Governor's Hearing Adult")
                                        .hearingTime(LocalDateTime.of(2015, 1, 2, 14, 0))
                                        .internalLocationId(-1000L)
                                        .heardByFirstName("Test")
                                        .heardByLastName("User")
                                        .otherRepresentatives("Other folk")
                                        .comment("A Comment")
                                        .result(HearingResult.builder()
                                                .oicOffenceCode("51:2D")
                                                .offenceType("Prison Rule 51")
                                                .offenceDescription("Detains any person against his will - detention against will of staff (not prison offr)")
                                                .plea("Unfit to Plea or Attend")
                                                .finding("Charge Proved")
                                                .oicHearingId(-1)
                                                .resultSeq(1L)
                                                .sanction(Sanction.builder()
                                                        .sanctionType("Stoppage of Earnings (%)")
                                                        .sanctionDays(21L)
                                                        .sanctionMonths(null)
                                                        .compensationAmount(50L)
                                                        .effectiveDate(LocalDateTime.of(2017, 11, 7, 0, 0))
                                                        .status("Immediate")
                                                        .statusDate(LocalDateTime.of(2017, 11, 8, 0, 0))
                                                        .comment(null)
                                                        .sanctionSeq(1L)
                                                        .consecutiveSanctionSeq(1L)
                                                        .oicHearingId(-1)
                                                        .resultSeq(1L)
                                                        .build())
                                                .build())
                                        .build())
                        .hearing(Hearing.builder()
                                .oicHearingId(-2L)
                                .hearingType("Governor's Hearing Adult")
                                .hearingTime(LocalDateTime.of(2015, 1, 2, 14, 0))
                                .internalLocationId(-1001L)
                                .heardByFirstName("CA")
                                .heardByLastName("User")
                                .otherRepresentatives("Some Other folk")
                                .comment("B Comment")
                                .result(HearingResult.builder()
                                        .oicOffenceCode("51:2C")
                                        .offenceType("Prison Rule 51")
                                        .offenceDescription("Detains any person against his will - detention against will of prison officer grade")
                                        .plea("Not guilty")
                                        .finding("Charge Proved")
                                        .oicHearingId(-2L)
                                        .resultSeq(1L)
                                        .sanction(Sanction.builder()
                                                .sanctionType("Cellular Confinement")
                                                .sanctionDays(7L)
                                                .sanctionMonths(1L)
                                                .compensationAmount(null)
                                                .effectiveDate(LocalDateTime.of(2017, 11, 07, 0, 0))
                                                .status("Immediate")
                                                .statusDate(null)
                                                .comment(null)
                                                .sanctionSeq(2L)
                                                .consecutiveSanctionSeq(2L)
                                                .oicHearingId(-2L)
                                                .resultSeq(1L)
                                                .build())
                                        .build())
                                .result(HearingResult.builder()
                                        .oicOffenceCode("51:1J")
                                        .offenceType("Prison Rule 51")
                                        .offenceDescription("Commits any assault - assault on prison officer")
                                        .plea("Not guilty")
                                        .finding("Charge Proved")
                                        .oicHearingId(-2L)
                                        .resultSeq(2L)
                                        .sanction(
                                                Sanction.builder()
                                                        .sanctionType("Forfeiture of Privileges")
                                                        .sanctionDays(7L)
                                                        .sanctionMonths(null)
                                                        .compensationAmount(null)
                                                        .effectiveDate(LocalDateTime.of(2017, 11, 8, 0, 0))
                                                        .status("Immediate")
                                                        .statusDate(null)
                                                        .comment("LOTV")
                                                        .sanctionSeq(3L)
                                                        .consecutiveSanctionSeq(1L)
                                                        .oicHearingId(-2)
                                                        .resultSeq(2L)
                                                        .build())
                                        .build())
                                .build())
                        .build());
    }

    @Test
    public void findAdjudicationDetailsWithoutSanctions() {

        val results = repository.findAdjudicationDetails("A1179MT", -8);

        assertThat(results.get()).isEqualTo(AdjudicationDetail.builder()
                .adjudicationNumber(-8L)
                .incidentTime(LocalDateTime.of(1999, 6, 25, 0, 0))
                .agencyId("MDI")
                .internalLocationId(-41L)
                .incidentDetails("mKSouDOCmKSouDO")
                .reportNumber(-5L)
                .reportType("Miscellaneous")
                .reporterFirstName("Jo")
                .reporterLastName("O'brien")
                .reportTime(LocalDateTime.of(2019, 8, 25, 0, 3))
                .hearing(Hearing.builder()
                        .oicHearingId(-3L)
                        .hearingType("Governor's Hearing Adult")
                        .hearingTime(LocalDateTime.of(2015, 1, 2, 14, 0))
                        .internalLocationId(-1001L)
                        .heardByFirstName("CA")
                        .heardByLastName("User")
                        .otherRepresentatives("Some Other folk")
                        .comment("B Comment")
                        .result(HearingResult.builder()
                                .oicOffenceCode("51:2C")
                                .offenceType("Prison Rule 51")
                                .offenceDescription("Detains any person against his will - detention against will of prison officer grade")
                                .plea("Not guilty")
                                .finding("Charge Proved")
                                .oicHearingId(-3L)
                                .resultSeq(1L)
                                .build())

                        .build())
                .build());
    }
}
