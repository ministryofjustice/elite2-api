package uk.gov.justice.hmpps.prison.repository.jpa.model;

import lombok.NoArgsConstructor;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(EducationSchedule.DOMAIN)
@NoArgsConstructor
public class EducationSchedule extends ReferenceCode {
    public static final String DOMAIN = "EDU_SCHED";

    public EducationSchedule(final String code, final String description) {
        super(DOMAIN, code, description);
    }
}
