package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import uk.gov.justice.hmpps.prison.api.model.LegalStatus;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "IMPRISONMENT_STATUSES")
@EqualsAndHashCode(of = { "id" }, callSuper = false)
@ToString(of = { "id", "status", "description"})
public class ImprisonmentStatus extends AuditableEntity {

    @Id
    @Column(name = "IMPRISONMENT_STATUS_ID")
    private Long id;

    @Column(name = "IMPRISONMENT_STATUS", nullable = false)
    private String status;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "BAND_CODE", nullable = false)
    private String bandCode;

    @Column(name = "RANK_VALUE", nullable = false)
    private String rankValue;

    @Column(name = "IMPRISONMENT_STATUS_SEQ")
    private String sequence;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    private String activeFlag;

    @Column(name = "EXPIRY_DATE")
    private LocalDate expiryDate;

    public boolean isActive() {
        return "Y".equals(activeFlag);
    }

    private final static List<String> RECALL_STATUS_CODES = List.of("14FTR_ORA","14FTRHDC_ORA","CUR_ORA","FTR/08","FTR_HDC","FTR_HDC_ORA","FTR_ORA","FTR_SCH15","FTRSCH15_ORA","HDR_ORA","LR","LR_ALP","LR_ALP_LASPO","LR_DLP","LR_DPP","LR_EPP","LR_ES","LR_HDC","LR_IPP","LR_LASPO_AR","LR_LASPO_DR","LR_LIFE","LR_MLP","LR_ORA","LR_SEC236A","LR_SEC91_ORA","LR_YOI","LR_YOI_ORA");

    public LegalStatus getLegalStatus() {
        return calcLegalStatus(getBandCode(), getStatus());
    }

    public String getConvictedStatus() {
        return calcConvictedStatus(getBandCode());
    }

    public static LegalStatus calcLegalStatus(final String bandCode, final String mainLegalStatusCode) {

        if (StringUtils.isBlank(bandCode) || !StringUtils.isNumeric(bandCode)) {
            return null;
        }

        final var legalStatusBand = Integer.parseInt(bandCode);

        if (RECALL_STATUS_CODES.contains(mainLegalStatusCode)) {
            return LegalStatus.RECALL;
        }

        if (legalStatusBand == 0) {
            return LegalStatus.DEAD;
        }

        if (legalStatusBand == 1) {
            return LegalStatus.INDETERMINATE_SENTENCE;
        }

        if (legalStatusBand == 2 || legalStatusBand == 3) {
            return LegalStatus.SENTENCED;
        }

        if (legalStatusBand >= 4 && legalStatusBand <= 7) {
            return LegalStatus.CONVICTED_UNSENTENCED;
        }

        if (legalStatusBand == 9 || legalStatusBand == 10 || mainLegalStatusCode.equals("CIV_RMD")) {
            return LegalStatus.CIVIL_PRISONER;
        }

        if (legalStatusBand == 8 || legalStatusBand == 11) {
            return LegalStatus.IMMIGRATION_DETAINEE;
        }

        if (legalStatusBand >= 12 && legalStatusBand <= 14) {
            return LegalStatus.REMAND;
        }

        if (mainLegalStatusCode.equals("UNKNOWN")) {
            return LegalStatus.UNKNOWN;
        }

        return LegalStatus.OTHER;
    }


    public static String calcConvictedStatus(final String bandCode) {
        if (StringUtils.isNotBlank(bandCode) && StringUtils.isNumeric(bandCode)) {
            final var legalStatusBand = Integer.parseInt(bandCode);
            return (legalStatusBand <= 8 || legalStatusBand == 11) ? "Convicted" : "Remand";
        }
        return null;
    }
}