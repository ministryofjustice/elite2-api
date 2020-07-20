package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

import static javax.persistence.EnumType.STRING;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.City.CITY;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementReason.REASON;
import static uk.gov.justice.hmpps.prison.repository.jpa.model.MovementType.TYPE;
import static org.hibernate.annotations.NotFoundAction.IGNORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "OFFENDER_EXTERNAL_MOVEMENTS")
@IdClass(ExternalMovement.PK.class)
@EqualsAndHashCode(callSuper = false)
public class ExternalMovement extends AuditableEntity {

    @NoArgsConstructor
    @AllArgsConstructor
    public static class PK implements Serializable {
        @Column(name = "OFFENDER_BOOK_ID", updatable = false, insertable = false)
        private Long bookingId;

        @Column(name = "MOVEMENT_SEQ", updatable = false, insertable = false)
        private Long movementSequence;
    }

    @Id
    private Long bookingId;

    @Id
    private Long movementSequence;

    @Column(name = "MOVEMENT_TIME")
    private LocalDateTime movementTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "FROM_AGY_LOC_ID", nullable = true)
    private AgencyLocation fromAgency;

    @ManyToOne(optional = false)
    @JoinColumn(name = "TO_AGY_LOC_ID", nullable = true)
    private AgencyLocation toAgency;

    @ManyToOne(optional = false)
    @JoinColumn(name = "OFFENDER_BOOK_ID", nullable = false)
    private OffenderBooking booking;

    @Enumerated(EnumType.STRING)
    private ActiveFlag activeFlag;

    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CITY + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "TO_CITY", referencedColumnName = "code"))
    })
    private City toCity;


    @ManyToOne
    @NotFound(action = IGNORE)
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + CITY + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "FROM_CITY", referencedColumnName = "code"))
    })
    private City fromCity;

    @ManyToOne
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + REASON + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "MOVEMENT_REASON_CODE", referencedColumnName = "code"))
    })
    private MovementReason movementReason;

    @Enumerated(STRING)
    @Column(name = "DIRECTION_CODE")
    private MovementDirection movementDirection;


    @ManyToOne
    @JoinColumnsOrFormulas(value = {
            @JoinColumnOrFormula(formula = @JoinFormula(value = "'" + TYPE + "'", referencedColumnName = "domain")),
            @JoinColumnOrFormula(column = @JoinColumn(name = "MOVEMENT_TYPE", referencedColumnName = "code"))
    })
    private MovementType movementType;


}
