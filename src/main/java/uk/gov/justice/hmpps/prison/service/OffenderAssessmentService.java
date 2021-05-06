package uk.gov.justice.hmpps.prison.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import uk.gov.justice.hmpps.prison.api.model.AssessmentClassification;
import uk.gov.justice.hmpps.prison.api.model.AssessmentDetail;
import uk.gov.justice.hmpps.prison.api.model.AssessmentQuestion;
import uk.gov.justice.hmpps.prison.api.model.AssessmentSummary;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AssessmentEntry;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderAssessmentItem;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AssessmentRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderAssessmentRepository;
import uk.gov.justice.hmpps.prison.security.VerifyBookingAccess;
import uk.gov.justice.hmpps.prison.security.VerifyOffenderAccess;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
@Validated
@Slf4j
public class OffenderAssessmentService {
    private final OffenderAssessmentRepository repository;
    private final AssessmentRepository assessmentRepository;

    public OffenderAssessmentService(final OffenderAssessmentRepository repository,
                                     final AssessmentRepository assessmentRepository) {
        this.repository = repository;
        this.assessmentRepository = assessmentRepository;
    }

    @Transactional(readOnly = true)
    @VerifyBookingAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public AssessmentDetail getOffenderAssessment(final Long bookingId, final Integer assessmentSeq) {
        final var assessment = repository.findByBookingIdAndAssessmentSeq(bookingId, assessmentSeq);

        if (assessment.isEmpty()) {
            throw new EntityNotFoundException(String.format("Csra assessment for booking %s and sequence %s not found.", bookingId, assessmentSeq));
        }

        final var assessmentDetails = assessment.get();
        final var classificationSummary = assessmentDetails.getClassificationSummary();

        return AssessmentDetail.detailBuilder()
            .summary(getAssessmentSummary(assessmentDetails))
            .assessmentCommitteeCode((assessmentDetails.getAssessCommittee() != null)?assessmentDetails.getAssessCommittee().getCode():null)
            .assessmentCommitteeName((assessmentDetails.getAssessCommittee() != null)?assessmentDetails.getAssessCommittee().getDescription():null)
            .approvalDate(assessmentDetails.getEvaluationDate())
            .approvalCommitteeCode((assessmentDetails.getReviewCommittee() != null)?assessmentDetails.getReviewCommittee().getCode():null)
            .approvalCommitteeName((assessmentDetails.getReviewCommittee() != null)?assessmentDetails.getReviewCommittee().getDescription():null)
            .originalClassificationCode((classificationSummary.getOriginalClassification() != null)?classificationSummary.getOriginalClassification().getCode(): null)
            .classificationReviewReason(classificationSummary.getClassificationApprovalReason())
            .questions(getCsraAssessmentQuestionsAndAnswers(assessmentDetails, bookingId, assessmentSeq))
            .build();
    }

    @Transactional(readOnly = true)
    @VerifyOffenderAccess(overrideRoles = {"SYSTEM_USER", "GLOBAL_SEARCH", "VIEW_PRISONER_DATA"})
    public List<AssessmentSummary> getOffenderAssessments(final String offenderNo) {
        final var assessments = repository.findByCsraAssessmentAndByOffenderNo_OrderByLatestFirst(offenderNo);

        return assessments.stream().map(this::getAssessmentSummary).collect(toList());
    }

    public CurrentCsraAssessment getCurrentCsraClassification(final String offenderNo) {
        final var assessments = repository.findByCsraAssessmentAndByOffenderNo_OrderByLatestFirst(offenderNo);

        return assessments.stream().filter(a -> a.getClassificationSummary().isSet()).findFirst()
            .map(CurrentCsraAssessment::fromAssessment).orElse(null);
    }

    private AssessmentSummary getAssessmentSummary(final OffenderAssessment assessmentDetails) {
        final var classificationSummary = assessmentDetails.getClassificationSummary();

        return AssessmentSummary.builder()
            .bookingId(assessmentDetails.getBookingId())
            .assessmentSeq(assessmentDetails.getAssessmentSeq())
            .offenderNo(assessmentDetails.getOffenderBooking().getOffender().getNomsId())
            .classificationCode((classificationSummary.getFinalClassification() != null)?classificationSummary.getFinalClassification().getCode(): null)
            .assessmentCode(assessmentDetails.getAssessmentType().getAssessmentCode())
            .cellSharingAlertFlag(true)
            .assessmentDate(assessmentDetails.getAssessmentDate())
            .assessmentAgencyId((assessmentDetails.getAssessmentCreateLocation() != null)?assessmentDetails.getAssessmentCreateLocation().getId(): null)
            .assessmentComment(assessmentDetails.getAssessmentComment())
            .assessorUser((assessmentDetails.getCreationUser() != null)?assessmentDetails.getCreationUser().getUsername():null)
            .nextReviewDate(assessmentDetails.getNextReviewDate())
            .build();
    }

    private List<AssessmentQuestion> getCsraAssessmentQuestionsAndAnswers(final OffenderAssessment assessmentDetails, final Long bookingId, final Integer assessmentSeq) {
        final var assessmentQuestions = assessmentRepository.findCsraQuestionsByAssessmentTypeIdOrderedByListSeq(assessmentDetails.getAssessmentType().getAssessmentId());

        if (assessmentQuestions.isEmpty()) {
            throw new EntityNotFoundException(String.format("Csra assessment questions for booking %s and sequence %s not found.", bookingId, assessmentSeq));
        }

        final var assessmentItems = assessmentDetails.getAssessmentItems();
        final var assessmentAnswersByQuestionId = assessmentItems.stream()
            .map(OffenderAssessmentItem::getAssessmentAnswer)
            .collect(groupingBy((aa) -> aa.getParentAssessment().getAssessmentId(), mapping(AssessmentEntry::getDescription, toList())));

        return assessmentQuestions.stream().map(aq -> getAssessmentQuestionAndAnswers(aq, assessmentAnswersByQuestionId.get(aq.getAssessmentId()))).collect(toList());
    }

    private AssessmentQuestion getAssessmentQuestionAndAnswers(final AssessmentEntry assessment, final List<String> answers) {
        if (answers == null) {
            return new AssessmentQuestion(assessment.getDescription(), null, null);
        }
        return new AssessmentQuestion(assessment.getDescription(),
            answers.stream().findFirst().orElse(null),
            answers.stream().skip(1).collect(toList()));
    }

    public List<AssessmentClassification> getOffendersAssessmentRatings(List<String> offenderList) {
        return offenderList.stream().map(this::getOffenderCurrentAssessmentRating).collect(Collectors.toList());
    }

    private AssessmentClassification getOffenderCurrentAssessmentRating(String offenderNo) {
        var currentClassification = getCurrentCsraClassification(offenderNo);
        if (currentClassification == null) {
            return AssessmentClassification.builder().offenderNo(offenderNo).build();
        }
        return AssessmentClassification.builder()
            .offenderNo(offenderNo)
            .classificationCode(currentClassification.classificationCode)
            .classificationDate(currentClassification.classificationDate)
            .build();
    }

    @AllArgsConstructor
    @Data
    public static class CurrentCsraAssessment {
        private String classificationCode;
        private LocalDate classificationDate;

        public static CurrentCsraAssessment fromAssessment(OffenderAssessment asseessmentWithClassificationSet) {
            return new CurrentCsraAssessment(asseessmentWithClassificationSet.getClassificationSummary().getFinalClassification().getCode(),
                asseessmentWithClassificationSet.getAssessmentDate());
        }
    }
}
