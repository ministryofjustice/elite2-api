package uk.gov.justice.hmpps.prison.repository.jpa.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ScheduledAppointment;

import java.time.LocalDate;
import java.util.List;

public interface ScheduledAppointmentRepository extends CrudRepository<ScheduledAppointment, Long> {
   List<ScheduledAppointment> findByAgencyIdAndEventDate(String agencyId, LocalDate date);
   List<ScheduledAppointment> findByAgencyIdAndEventDateAndLocationId(String agencyId, LocalDate date, Long locationId);
}
