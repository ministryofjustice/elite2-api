package net.syscon.elite.service.impl;

import com.google.common.collect.ImmutableList;
import net.syscon.elite.api.model.*;
import net.syscon.elite.repository.MovementsRepository;
import net.syscon.elite.service.MovementsService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MovementsServiceImplTest {
    private static final String TEST_OFFENDER_NO = "AA1234A";
    @Mock
    private MovementsRepository movementsRepository;

    private MovementsService movementsService;

    @Before
    public void init() {
        movementsService = new MovementsServiceImpl(movementsRepository, 1);
    }

    @Test
    public void testGetRecentMovements_ByOffenders() {
        final List<Movement> movements = ImmutableList.of(Movement.builder().offenderNo(TEST_OFFENDER_NO).fromAgencyDescription("LEEDS").toAgencyDescription("BLACKBURN").build());
        final var offenderNoList = ImmutableList.of(TEST_OFFENDER_NO);

        when(movementsRepository.getMovementsByOffenders(offenderNoList, null, true)).thenReturn(movements);

        final var processedMovements = movementsService.getMovementsByOffenders(offenderNoList, null, true);
        assertThat(processedMovements).extracting("toAgencyDescription").containsExactly("Blackburn");
        assertThat(processedMovements).extracting("fromAgencyDescription").containsExactly("Leeds");

        verify(movementsRepository).getMovementsByOffenders(offenderNoList, null, true);
    }

    @Test
    public void testGetMovements_ByOffenders() {
        final List<Movement> movements = ImmutableList.of(Movement.builder().offenderNo(TEST_OFFENDER_NO).fromAgencyDescription("LEEDS").toAgencyDescription("BLACKBURN").build());
        final var offenderNoList = ImmutableList.of(TEST_OFFENDER_NO);

        when(movementsRepository.getMovementsByOffenders(offenderNoList, null, false)).thenReturn(movements);

        final var processedMovements = movementsService.getMovementsByOffenders(offenderNoList, null, false);
        assertThat(processedMovements).extracting("toAgencyDescription").containsExactly("Blackburn");
        assertThat(processedMovements).extracting("fromAgencyDescription").containsExactly("Leeds");

        verify(movementsRepository).getMovementsByOffenders(offenderNoList, null, false);
    }

    @Test
    public void testGetMovements_ByOffendersNullDescriptions() {
        final List<Movement> movements = ImmutableList.of(Movement.builder().offenderNo(TEST_OFFENDER_NO).build());
        final var offenderNoList = ImmutableList.of(TEST_OFFENDER_NO);

        when(movementsRepository.getMovementsByOffenders(offenderNoList, null, true)).thenReturn(movements);

        final var processedMovements = movementsService.getMovementsByOffenders(offenderNoList, null, true);

        assertThat(processedMovements).hasSize(1);
        assertThat(processedMovements.get(0).getFromAgencyDescription()).isEmpty();

        verify(movementsRepository).getMovementsByOffenders(offenderNoList, null, true);
    }

    @Test
    public void testGetEnrouteOffenderMovements() {
        final List<OffenderMovement> oms = ImmutableList.of(OffenderMovement.builder()
                .offenderNo(TEST_OFFENDER_NO)
                .bookingId(123L).firstName("JAMES")
                .lastName("SMITH")
                .fromAgencyDescription("LEEDS")
                .toAgencyDescription("MOORLANDS")
                .build());

        when(movementsRepository.getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12))).thenReturn(oms);

        final var enrouteOffenderMovements = movementsService.getEnrouteOffenderMovements("LEI", LocalDate.of(2015, 9, 12));
        assertThat(enrouteOffenderMovements).extracting("fromAgencyDescription").contains("Leeds");
        assertThat(enrouteOffenderMovements).extracting("toAgencyDescription").contains("Moorlands");
        assertThat(enrouteOffenderMovements).extracting("lastName").contains("SMITH");
        assertThat(enrouteOffenderMovements).extracting("bookingId").contains(123L);

        verify(movementsRepository).getEnrouteMovementsOffenderMovementList("LEI", LocalDate.of(2015, 9, 12));
    }


    @Test
    public void testGetEnrouteOffender_NoDateFilter() {
        /* call service with no specified date */
        movementsService.getEnrouteOffenderMovements("LEI", null);

        verify(movementsRepository).getEnrouteMovementsOffenderMovementList("LEI", null);
    }

    @Test
    public void testGetEnrouteOffenderMovements_Count() {
        when(movementsRepository.getEnrouteMovementsOffenderCount("LEI", LocalDate.of(2015, 9, 12))).thenReturn(5);

        final var count = movementsService.getEnrouteOffenderCount("LEI", LocalDate.of(2015, 9, 12));
        assertThat(count).isEqualTo(5);

        verify(movementsRepository).getEnrouteMovementsOffenderCount("LEI", LocalDate.of(2015, 9, 12));
    }

    @Test
    public void testGetOffenders_OutToday() {
        final var timeOut = LocalTime.now();
        final List<OffenderMovement> offenders = ImmutableList.of(
                OffenderMovement.builder()
                        .offenderNo("1234")
                        .directionCode("OUT")
                        .dateOfBirth(LocalDate.now())
                        .movementDate(LocalDate.now())
                        .fromAgency("LEI")
                        .firstName("JOHN")
                        .lastName("DOE")
                        .movementReasonDescription("NORMAL TRANSFER")
                        .movementTime(timeOut)
                        .build());


        when(movementsRepository.getOffendersOut("LEI", LocalDate.now())).thenReturn(offenders);

        final var offendersOutToday = movementsService.getOffendersOut("LEI", LocalDate.now());

        assertThat(offendersOutToday).hasSize(1);

        assertThat(offendersOutToday).extracting("offenderNo").contains("1234");
        assertThat(offendersOutToday).extracting("firstName").contains("John");
        assertThat(offendersOutToday).extracting("lastName").contains("Doe");
        assertThat(offendersOutToday).extracting("dateOfBirth").contains(LocalDate.now());
        assertThat(offendersOutToday).extracting("timeOut").contains(timeOut);
        assertThat(offendersOutToday).extracting("reasonDescription").contains("Normal Transfer");
    }

    @Test
    public void testMapping_ToProperCase() {
        final var agency = "LEI";

        when(movementsRepository.getOffendersInReception(agency))
                .thenReturn(
                        Collections.singletonList(OffenderInReception.builder()
                                .firstName("FIRST")
                                .lastName("LASTNAME")
                                .dateOfBirth(LocalDate.of(1950, 10, 10))
                                .offenderNo("1234A")
                                .build()
                        )
                );

        final var offenders = movementsService.getOffendersInReception(agency);

        assertThat(offenders)
                .containsExactly(OffenderInReception.builder()
                        .firstName("First")
                        .lastName("Lastname")
                        .dateOfBirth(LocalDate.of(1950, 10, 10))
                        .offenderNo("1234A")
                        .build());
    }

    @Test
    public void testMappingToProperCase_CurrentlyOut() {

        when(movementsRepository.getOffendersCurrentlyOut(1L))
                .thenReturn(
                        Collections.singletonList(OffenderOut.builder()
                                .firstName("FIRST")
                                .lastName("LASTNAME")
                                .dateOfBirth(LocalDate.of(1950, 10, 10))
                                .offenderNo("1234A")
                                .location("x-1-1")
                                .build()
                        )
                );

        final var offenders = movementsService.getOffendersCurrentlyOut(1L);

        assertThat(offenders)
                .containsExactly(OffenderOut.builder()
                        .firstName("First")
                        .lastName("Lastname")
                        .dateOfBirth(LocalDate.of(1950, 10, 10))
                        .offenderNo("1234A")
                        .location("x-1-1")
                        .build());
    }

    @Test
    public void testThatCallsToRecentMovements_AreBatched() {
        final var offenders = List.of("offender1", "offender2");
        final var movement1 = Movement.builder()
                .offenderNo("offender1")
                .fromAgencyDescription("Lei")
                .toAgencyDescription("York")
                .toCity("York")
                .fromCity("Leeds")
                .movementType("TRN")
                .movementReason("COURT")
                .build();
        final var movement2 = Movement.builder()
                .offenderNo("offender2")
                .fromAgencyDescription("Hli")
                .toAgencyDescription("York")
                .toCity("York")
                .fromCity("Hull")
                .movementType("TRN")
                .movementReason("COURT")
                .build();

        when(movementsRepository.getMovementsByOffenders(List.of("offender1"), Collections.emptyList(), true))
                .thenReturn(List.of(movement1));

        when(movementsRepository.getMovementsByOffenders(List.of("offender2"), Collections.emptyList(), true))
                .thenReturn(List.of(movement2));

        final var movements = movementsService.getMovementsByOffenders(offenders, Collections.emptyList(), true);

        assertThat(movements).containsSequence(List.of(movement1, movement2));

        verify(movementsRepository).getMovementsByOffenders(List.of("offender1"), Collections.emptyList(), true);
        verify(movementsRepository).getMovementsByOffenders(List.of("offender2"), Collections.emptyList(), true);
    }


    @Test
    public void testMovementsForAgenciesBetweenTwoTimes() {

        final var listOfMovements = List.of(
                MovementSummary.builder().offenderNo("1111").movementType("TRN").movementTime(LocalDateTime.now()).fromAgency("LEI").fromAgencyDescription("Leicester").toAgency("MDI").toAgencyDescription("Midlands").movementReason("Court").build(),
                MovementSummary.builder().offenderNo("2222").movementType("TRN").movementTime(LocalDateTime.now()).fromAgency("MDI").fromAgencyDescription("Midlands").toAgency("LEI").toAgencyDescription("Leicester").movementReason("Transfer").build(),
                MovementSummary.builder().offenderNo("4333").movementType("TRN").movementTime(LocalDateTime.now()).fromAgency("MDI").fromAgencyDescription("Midlands").toAgency("HOW").toAgencyDescription("Howden").movementReason("Transfer").build()
        );

        final var listOfCourtEvents = List.of(
                CourtEvent.builder().offenderNo("5555").eventType("CRT").startTime(LocalDateTime.now()).build()
        );

        final var listOfReleaseEvents = List.of(
                ReleaseEvent.builder().offenderNo("6666").movementTypeCode("REL").createDateTime(LocalDateTime.now()).build()
        );

        final var listOfTransferEvents = List.of(
                TransferEvent.builder().offenderNo("7777").eventClass("TRN").createDateTime(LocalDateTime.now()).build()
        );

        final var from = LocalDateTime.parse("2019-05-01T11:00:00");
        final var to = LocalDateTime.parse("2019-05-01T17:00:00");
        final var agencyList = List.of("LEI", "MDI");

        when(movementsRepository.getCompletedMovementsForAgencies(agencyList, from, to)).thenReturn(listOfMovements);
        when(movementsRepository.getCourtEvents(agencyList, from, to)).thenReturn(listOfCourtEvents);
        when(movementsRepository.getOffenderReleases(agencyList, from, to)).thenReturn(listOfReleaseEvents);
        when(movementsRepository.getOffenderTransfers(agencyList, from, to)).thenReturn(listOfTransferEvents);

        final var courtEvents = true;
        final var releaseEvents = true;
        final var transferEvents = true;
        final var movements = true;

        final var transferSummary = movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements);

        assertThat(transferSummary).isNotNull();

        assertThat(transferSummary.getCourtEvents()).containsAll(listOfCourtEvents);
        assertThat(transferSummary.getReleaseEvents()).containsAll(listOfReleaseEvents);
        assertThat(transferSummary.getTransferEvents()).containsAll(listOfTransferEvents);
        assertThat(transferSummary.getMovements()).containsAll(listOfMovements);

        verify(movementsRepository).getCompletedMovementsForAgencies(agencyList, from, to);
        verify(movementsRepository).getCourtEvents(agencyList, from, to);
        verify(movementsRepository).getOffenderReleases(agencyList, from, to);
        verify(movementsRepository).getOffenderTransfers(agencyList, from, to);

        verifyNoMoreInteractions(movementsRepository);
    }

    @Test
    public void testMovementsForAgenciesNoAgencyCodes() {

        // No agency identifiers provided
        final var from = LocalDateTime.parse("2019-05-01T11:00:00");
        final var to = LocalDateTime.parse("2019-05-01T17:00:00");
        final var agencyList = Collections.<String>emptyList();

        final var courtEvents = true;
        final var releaseEvents = true;
        final var transferEvents = true;
        final var movements = true;

        assertThatThrownBy(() -> {
            final var transferSummary = movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements);
        }).isInstanceOf(HttpClientErrorException.class).hasMessageContaining("No agency location identifiers were supplied");

        verifyNoMoreInteractions(movementsRepository);
    }

    @Test
    public void testAgencyEventsInvalidDateRange() {

        // From time is AFTER the to time
        final var from = LocalDateTime.parse("2019-05-01T17:00:00");
        final var to = LocalDateTime.parse("2019-05-01T11:00:00");
        final var agencyList = List.of("LEI", "MDI");

        final var courtEvents = true;
        final var releaseEvents = true;
        final var transferEvents = true;
        final var movements = true;

        assertThatThrownBy(() -> {
            final var transferSummary = movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements);
        }).isInstanceOf(HttpClientErrorException.class).hasMessageContaining("The supplied fromDateTime parameter is after the toDateTime value");

        verifyNoMoreInteractions(movementsRepository);
    }

    @Test
    public void testAgencyEventsNoQueryParameters() {

        // Valid date range
        final var from = LocalDateTime.parse("2019-05-01T11:00:00");
        final var to = LocalDateTime.parse("2019-05-01T17:00:00");
        final var agencyList = List.of("LEI", "MDI");

        // All false - no data is being requested
        final var courtEvents = false;
        final var releaseEvents = false;
        final var transferEvents = false;
        final var movements = false;

        assertThatThrownBy(() -> {
            final var transferSummary = movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements);
        }).isInstanceOf(HttpClientErrorException.class).hasMessageContaining("At least one query parameter must be true [courtEvents|releaseEvents|transferEvents|movements]");

        verifyNoMoreInteractions(movementsRepository);
    }

    @Test
    public void testAgencyEventsCombinationQuery() {

        final var listOfCourtEvents = List.of(
                CourtEvent.builder().offenderNo("5555").eventType("CRT").startTime(LocalDateTime.now()).build()
        );

        final var listOfTransferEvents = List.of(
                TransferEvent.builder().offenderNo("7777").eventClass("TRN").createDateTime(LocalDateTime.now()).build()
        );

        final var from = LocalDateTime.parse("2019-05-01T11:00:00");
        final var to = LocalDateTime.parse("2019-05-01T17:00:00");
        final var agencyList = List.of("LEI", "MDI");

        final var courtEvents = true;
        final var releaseEvents = false;
        final var transferEvents = true;
        final var movements = false;

        when(movementsRepository.getCourtEvents(agencyList, from, to)).thenReturn(listOfCourtEvents);
        when(movementsRepository.getOffenderTransfers(agencyList, from, to)).thenReturn(listOfTransferEvents);

        final var transferSummary = movementsService.getTransferMovementsForAgencies(agencyList, from, to, courtEvents, releaseEvents, transferEvents, movements);

        assertThat(transferSummary).isNotNull();

        assertThat(transferSummary.getCourtEvents()).containsAll(listOfCourtEvents);
        assertThat(transferSummary.getReleaseEvents()).isNullOrEmpty();
        assertThat(transferSummary.getTransferEvents()).containsAll(listOfTransferEvents);
        assertThat(transferSummary.getMovements()).isNullOrEmpty();

        verify(movementsRepository).getCourtEvents(agencyList, from, to);
        verify(movementsRepository).getOffenderTransfers(agencyList, from, to);

        verifyNoMoreInteractions(movementsRepository);
    }

}
