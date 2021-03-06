package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;

@AllArgsConstructor
@Builder
@Data
@EqualsAndHashCode(of = {"code"}, callSuper = false)
@NoArgsConstructor
@Entity
@Table(name = "STATUTES")
@ToString
public class Statute extends ExtendedAuditableEntity{

    @Id
    @Column(name = "STATUTE_CODE", nullable = false)
    private String code;

    @Column(name = "DESCRIPTION", nullable = false)
    private String description;

    @Column(name = "LEGISLATING_BODY_CODE", nullable = false)
    private String legislatingBodyCode;

    @Column(name = "ACTIVE_FLAG", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActiveFlag activeFlag;
}
