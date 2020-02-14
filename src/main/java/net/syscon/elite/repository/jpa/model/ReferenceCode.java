package net.syscon.elite.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "REFERENCE_CODES")
@DiscriminatorColumn(name = "domain")
@Inheritance
public abstract class ReferenceCode implements Serializable {
    @Id
    private String domain;
    @Id
    private String code;

    private String description;

    static final String CASE_STATUS = "CASE_STATUS";
    static final String LEG_CASE_TYP = "LEG_CASE_TYP";
    static final String MLTY_BRANCH = "MLTY_BRANCH";
    static final String MLTY_WZONE = "MLTY_WZONE";
    static final String MLTY_DSCHRG = "MLTY_DSCHRG";
    static final String MLTY_DISCP = "MLTY_DISCP";
    static final String MLTY_RANK = "MLTY_RANK";
    static final String RELATIONSHIP = "RELATIONSHIP";
    static final String EVENT_STS = "EVENT_STS";
    static final String OUTCOMES = "OUTCOMES";
    static final String VISIT_TYPE = "VISIT_TYPE";
    static final String MOVE_CANC_RS = "MOVE_CANC_RS";

    public static String getDescriptionOrNull(final ReferenceCode referenceCode) {
        return referenceCode != null ? referenceCode.getDescription() : null;
    }

    public static String getCodeOrNull(final ReferenceCode referenceCode) {
        return referenceCode != null ? referenceCode.getCode() : null;
    }
}
