package net.syscon.elite.api.model;

import com.fasterxml.jackson.annotation.*;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Assessment
 **/
@SuppressWarnings("unused")
@ApiModel(description = "Assessment")
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Assessment {
    @JsonIgnore
    private Map<String, Object> additionalProperties;
    
    @NotNull
    private Long bookingId;

    @NotBlank
    private String offenderNo;

    @NotBlank
    private String classificationCode;

    @NotBlank
    private String classification;

    @NotBlank
    private String assessmentCode;

    @NotBlank
    private String assessmentDescription;

    @NotNull
    private boolean cellSharingAlertFlag;

    @NotNull
    private LocalDate assessmentDate;

    @NotNull
    private LocalDate nextReviewDate;

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties == null ? new HashMap<>() : additionalProperties;
    }

    @ApiModelProperty(hidden = true)
    @JsonAnySetter
    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    /**
      * Offender Booking Id
      */
    @ApiModelProperty(required = true, value = "Offender Booking Id")
    @JsonProperty("bookingId")
    public Long getBookingId() {
        return bookingId;
    }

    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    /**
      * Offender Number
      */
    @ApiModelProperty(required = true, value = "Offender Number")
    @JsonProperty("offenderNo")
    public String getOffenderNo() {
        return offenderNo;
    }

    public void setOffenderNo(String offenderNo) {
        this.offenderNo = offenderNo;
    }

    /**
      * Classification code
      */
    @ApiModelProperty(required = true, value = "Classification code")
    @JsonProperty("classificationCode")
    public String getClassificationCode() {
        return classificationCode;
    }

    public void setClassificationCode(String classificationCode) {
        this.classificationCode = classificationCode;
    }

    /**
      * Classification description
      */
    @ApiModelProperty(required = true, value = "Classification description")
    @JsonProperty("classification")
    public String getClassification() {
        return classification;
    }

    public void setClassification(String classification) {
        this.classification = classification;
    }

    /**
      * Assessment Code
      */
    @ApiModelProperty(required = true, value = "Assessment Code")
    @JsonProperty("assessmentCode")
    public String getAssessmentCode() {
        return assessmentCode;
    }

    public void setAssessmentCode(String assessmentCode) {
        this.assessmentCode = assessmentCode;
    }

    /**
      * Assessment Desc
      */
    @ApiModelProperty(required = true, value = "Assessment Desc")
    @JsonProperty("assessmentDescription")
    public String getAssessmentDescription() {
        return assessmentDescription;
    }

    public void setAssessmentDescription(String assessmentDescription) {
        this.assessmentDescription = assessmentDescription;
    }

    /**
      * Cell Sharing Alert Flag
      */
    @ApiModelProperty(required = true, value = "Cell Sharing Alert Flag")
    @JsonProperty("cellSharingAlertFlag")
    public boolean getCellSharingAlertFlag() {
        return cellSharingAlertFlag;
    }

    public void setCellSharingAlertFlag(boolean cellSharingAlertFlag) {
        this.cellSharingAlertFlag = cellSharingAlertFlag;
    }

    /**
      * Assessment Date
      */
    @ApiModelProperty(required = true, value = "Assessment Date")
    @JsonProperty("assessmentDate")
    public LocalDate getAssessmentDate() {
        return assessmentDate;
    }

    public void setAssessmentDate(LocalDate assessmentDate) {
        this.assessmentDate = assessmentDate;
    }

    /**
      * Next Review Date
      */
    @ApiModelProperty(required = true, value = "Next Review Date")
    @JsonProperty("nextReviewDate")
    public LocalDate getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(LocalDate nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    @Override
    public String toString()  {
        StringBuilder sb = new StringBuilder();

        sb.append("class Assessment {\n");
        
        sb.append("  bookingId: ").append(bookingId).append("\n");
        sb.append("  offenderNo: ").append(offenderNo).append("\n");
        sb.append("  classificationCode: ").append(classificationCode).append("\n");
        sb.append("  classification: ").append(classification).append("\n");
        sb.append("  assessmentCode: ").append(assessmentCode).append("\n");
        sb.append("  assessmentDescription: ").append(assessmentDescription).append("\n");
        sb.append("  cellSharingAlertFlag: ").append(cellSharingAlertFlag).append("\n");
        sb.append("  assessmentDate: ").append(assessmentDate).append("\n");
        sb.append("  nextReviewDate: ").append(nextReviewDate).append("\n");
        sb.append("}\n");

        return sb.toString();
    }
}
