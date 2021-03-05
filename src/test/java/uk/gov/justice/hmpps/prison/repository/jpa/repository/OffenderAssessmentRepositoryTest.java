package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.justice.hmpps.prison.repository.jpa.model.*;
import uk.gov.justice.hmpps.prison.security.AuthenticationFacade;
import uk.gov.justice.hmpps.prison.web.config.AuditorAwareImpl;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = NONE)
@Import({AuthenticationFacade.class, AuditorAwareImpl.class})
@WithMockUser
public class OffenderAssessmentRepositoryTest {

    @Autowired
    private OffenderAssessmentRepository repository;

    @Test
    void getAssessmentByBookingIdAndAssessmentSeq() {
        final var assessment = repository.findByBookingIdAndAssessmentSeq(-43L, 2).orElseThrow();

        assertThat(assessment.getBookingId()).isEqualTo(-43L);
        assertThat(assessment.getAssessmentSeq()).isEqualTo(2L);
        assertThat(assessment.getOffenderBooking().getBookingId()).isEqualTo(-43L);
        assertThat(assessment.getCalculatedClassification()).isEqualTo("STANDARD");
        assertThat(assessment.getOverridingClassification()).isEqualTo("HI");
        assertThat(assessment.getReviewedClassification()).isEqualTo("HI");
        assertThat(assessment.getAssessmentDate()).isEqualTo(LocalDate.parse("2019-01-02"));
        assertThat(assessment.getAssessmentCreateLocation()).isEqualTo("LEI");
        assertThat(assessment.getAssessmentComment()).isEqualTo("A Comment");
        assertThat(assessment.getAssessCommittee().getCode()).isEqualTo("RECP");
        assertThat(assessment.getAssessCommittee().getDescription()).isEqualTo("Reception");
        assertThat(assessment.getAssessStatus()).isEqualTo("A");
        assertThat(assessment.getOverrideReason()).isEqualTo("Incomplete");
        assertThat(assessment.getOverrideUserId()).isEqualTo("1234");
        assertThat(assessment.getReviewCommittee().getCode()).isEqualTo("GOV");
        assertThat(assessment.getReviewCommittee().getDescription()).isEqualTo("Governor");
        assertThat(assessment.getNextReviewDate()).isEqualTo(LocalDate.parse("2019-11-22"));
        assertThat(assessment.getEvaluationDate()).isEqualTo(LocalDate.parse("2016-07-07"));
        assertThat(assessment.getCreationUser().getUsername()).isEqualTo("JBRIEN");
        assertThat(assessment.getModifyUser()).isEqualTo("ITAG_USER");
        assertThat(assessment.getAssessmentType().getAssessmentId()).isEqualTo(-4L);

        final var expectedQuestion1 = "Reason for review";
        final var expectedAnswer1 = "Scheduled";
        final var expectedQuestion2 = "Risk of harming a cell mate:";
        final var expectedAnswer2 = "Standard";
        final var expectedQuestion3 = "Outcome of review:";
        final var expectedAnswer3 = "A new plan must be agreed";

        assertThat(assessment.getAssessmentItems()).usingRecursiveComparison()
            .ignoringFields("createDatetime", "createUserId", "assessmentAnswer.assessmentCode", "assessmentAnswer.cellSharingAlertFlag",
                "assessmentAnswer.createDatetime", "assessmentAnswer.createUserId", "assessmentAnswer.listSeq",
                // AssertJ cannot handle recursive properties - we will check the assessmentAnswer.parentAssessment separately
                "assessmentAnswer.parentAssessment")
            .isEqualTo(List.of(
                OffenderAssessmentItem.builder()
                    .bookingId(-43L)
                    .assessmentSeq(2L)
                    .itemSeq(1L)
                    .assessmentAnswer(AssessmentEntry.builder()
                        .assessmentId(-22L)
                        .description(expectedAnswer1)
                        .build())
                    .build(),
                OffenderAssessmentItem.builder()
                    .bookingId(-43L)
                    .assessmentSeq(2L)
                    .itemSeq(2L)
                    .assessmentAnswer(AssessmentEntry.builder()
                        .assessmentId(-28L)
                        .description(expectedAnswer2)
                        .build())
                    .build(),
                OffenderAssessmentItem.builder()
                    .bookingId(-43L)
                    .assessmentSeq(2L)
                    .itemSeq(3L)
                    .assessmentAnswer(AssessmentEntry.builder()
                        .assessmentId(-32L)
                        .description(expectedAnswer3)
                        .build())
                    .build()
                )
        );

        // Check each assessmentAnswer's parentAssessments contains the question
        final var parentAssessmentByAssessmentAnswer = new HashMap<String, AssessmentEntry>();
        assessment.getAssessmentItems().forEach(a -> parentAssessmentByAssessmentAnswer.put(a.getAssessmentAnswer().getDescription(), a.getAssessmentAnswer().getParentAssessment()));

        assertThat(parentAssessmentByAssessmentAnswer.get(expectedAnswer1).getDescription()).isEqualTo(expectedQuestion1);
        assertThat(parentAssessmentByAssessmentAnswer.get(expectedAnswer2).getDescription()).isEqualTo(expectedQuestion2);
        assertThat(parentAssessmentByAssessmentAnswer.get(expectedAnswer3).getDescription()).isEqualTo(expectedQuestion3);
    }

    @Test
    void getAssessmentByBookingIdAndAssessmentSeq_ReturnsNothing() {
        final var assessment = repository.findByBookingIdAndAssessmentSeq(-43L, 4);

        assertThat(assessment).isEmpty();
    }
}


