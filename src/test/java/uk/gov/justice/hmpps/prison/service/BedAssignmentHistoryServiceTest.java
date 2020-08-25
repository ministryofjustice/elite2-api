package uk.gov.justice.hmpps.prison.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import uk.gov.justice.hmpps.prison.api.model.BedAssignment;
import uk.gov.justice.hmpps.prison.repository.jpa.model.ActiveFlag;
import uk.gov.justice.hmpps.prison.repository.jpa.model.AgencyInternalLocation;
import uk.gov.justice.hmpps.prison.repository.jpa.model.BedAssignmentHistory;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderBooking;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.AgencyInternalLocationRepository;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.BedAssignmentHistoriesRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;

class BedAssignmentHistoryServiceTest {

    private final BedAssignmentHistoriesRepository repository = mock(BedAssignmentHistoriesRepository.class);
    private final AgencyInternalLocationRepository locationRepository = mock(AgencyInternalLocationRepository.class);
    private final BedAssignmentHistoryService service = new BedAssignmentHistoryService(repository, locationRepository);

    @Test
    void add() {
        final var now = LocalDateTime.now();
        service.add(1L, 2L, "RSN", now);

        verify(repository).save(argThat(bedAssignment ->
                bedAssignment.getBedAssignmentHistoryPK().getOffenderBookingId() == 1L
                        && bedAssignment.getLivingUnitId() == 2L
                        && bedAssignment.getAssignmentReason().equals("RSN")
                        && bedAssignment.getAssignmentDate().isEqual(now.toLocalDate())
                        && bedAssignment.getAssignmentDateTime().isEqual(now)));
    }

    @Test
    void getBedAssignmentsHistory() {
        var assignments = List.of(
                BedAssignmentHistory.builder()
                        .assignmentDate(LocalDate.of(2015, 5, 1))
                        .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
                        .assignmentEndDate(LocalDate.of(2016, 5, 1))
                        .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                        .assignmentReason("Needs moving")
                        .livingUnitId(1L)
                        .offenderBooking(OffenderBooking.builder().bookingId(1L).build())
                        .build(),
                BedAssignmentHistory.builder()
                        .assignmentDate(LocalDate.of(2016, 5, 1))
                        .assignmentDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                        .assignmentEndDate(LocalDate.of(2017, 5, 1))
                        .assignmentEndDateTime(LocalDateTime.of(2017, 5, 1, 10, 10, 10))
                        .assignmentReason("Needs moving again")
                        .livingUnitId(2L)
                        .offenderBooking(OffenderBooking.builder().bookingId(1L).build())
                        .build()
        );
        var page = new PageImpl<>(assignments);
        when(repository.findAllByBedAssignmentHistoryPKOffenderBookingId(1L, PageRequest.of(0, 20))).thenReturn(page);
        when(locationRepository.findOneByLocationId(anyLong())).thenReturn(Optional.of(AgencyInternalLocation.builder().description("MDI-1-2").agencyId("MDI").build()));
        final var response = service.getBedAssignmentsHistory(1L, PageRequest.of(0, 20));
        assertThat(response).containsOnly(
            BedAssignment.builder()
                .bookingId(1L)
                .livingUnitId(1L)
                .assignmentDate(LocalDate.of(2015, 5, 1))
                .assignmentDateTime(LocalDateTime.of(2015, 5, 1, 10, 10, 10))
                .assignmentEndDate(LocalDate.of(2016, 5, 1))
                .assignmentEndDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                .assignmentReason("Needs moving")
                .description("MDI-1-2")
                .agencyId("MDI")
                .build(),
            BedAssignment.builder()
                .bookingId(1L)
                .livingUnitId(2L)
                .assignmentDate(LocalDate.of(2016, 5, 1))
                .assignmentDateTime(LocalDateTime.of(2016, 5, 1, 10, 10, 10))
                .assignmentEndDate(LocalDate.of(2017, 5, 1))
                .assignmentEndDateTime(LocalDateTime.of(2017, 5, 1, 10, 10, 10))
                .assignmentReason("Needs moving again")
                .description("MDI-1-2")
                .agencyId("MDI")
                .build()
        );


    }

}
