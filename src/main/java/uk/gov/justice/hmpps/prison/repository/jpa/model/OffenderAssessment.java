package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper=false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(OffenderAssessment.Pk.class)
@Table(name = "OFFENDER_ASSESSMENTS")
@ToString(of = {"bookingId", "assessmentSeq"})
public class OffenderAssessment extends ExtendedAuditableEntity {

    @Id
    @Column(name = "OFFENDER_BOOK_ID")
    private Long bookingId;

    @Id
    @Column(name = "ASSESSMENT_SEQ")
    private Long assessmentSeq;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    @MapsId("bookingId")
    private OffenderBooking offenderBooking;

    @Column(name = "CALC_SUP_LEVEL_TYPE")
    private String calculatedClassification;

    @Column(name = "OVERRIDED_SUP_LEVEL_TYPE")
    private String overridingClassification;

    @Column(name = "REVIEW_SUP_LEVEL_TYPE")
    private String reviewedClassification;

    @Column(name = "ASSESSMENT_DATE")
    private LocalDate assessmentDate;

    @Column(name = "ASSESS_COMMENT_TEXT")
    private String assessmentComment;

    @Column(name = "ASSESSMENT_CREATE_LOCATION")
    private String assessmentCreateLocation;

    @Column(name = "ASSESS_STATUS")
    private String assessStatus;

    @Column(name = "ASSESS_COMMITTE_CODE")
    private String assessCommitteeCode;

    @Column(name = "OVERRIDE_REASON")
    private String overrideReason;

    @Column(name = "OVERRIDE_USER_ID")
    private String overrideUserId;

    @Column(name = "REVIEW_COMMITTE_CODE")
    private String reviewAuthority;

    @Column(name = "NEXT_REVIEW_DATE")
    private LocalDate nextReviewDate;

    @Column(name = "EVALUATION_DATE")
    private LocalDate evaluationDate;

    @Column(name = "CREATION_USER")
    private String creationUser;

    // This allows access to protected variable
    public String getModifyUser() {
        return this.getModifyUserId();
    }

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ASSESSMENT_TYPE_ID")
    private AssessmentEntry assessmentType;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name="OFFENDER_BOOK_ID", referencedColumnName="OFFENDER_BOOK_ID"),
        @JoinColumn(name="ASSESSMENT_SEQ", referencedColumnName="ASSESSMENT_SEQ")
    })
    private List<OffenderAssessmentItem> assessmentItems;

    @EqualsAndHashCode
    @NoArgsConstructor
    @AllArgsConstructor
    @ToString
    public static class Pk implements Serializable {
        private Long bookingId;
        private Long assessmentSeq;
    }
}
