package net.syscon.elite.service.impl;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.val;
import net.syscon.elite.api.model.*;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.*;
import net.syscon.elite.service.support.PayableAttendanceOutcomeDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link BookingServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AgencyService agencyService;

    @Mock
    private ReferenceDomainService referenceDomainService;

    @Mock
    private LocationService locationService;

    @Mock
    private TelemetryClient telemetryClient;

    @Mock
    private AuthenticationFacade securityUtils;

    @Mock
    private AuthenticationFacade authenticationFacade;

    private BookingService bookingService;
    
    @Before
    public void init() {
        bookingService = new BookingServiceImpl(
                bookingRepository,
                null,
                agencyService,
                null,
                locationService,
                referenceDomainService,
                null,
                telemetryClient,
                securityUtils, authenticationFacade, "1",
                10);
    }


    @Test
    public void testVerifyCanAccessLatestBooking() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getBookingIdByOffenderNo("off-1")).thenReturn(Optional.of(bookingId));
        when(agencyService.getAgencyIds()).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(true);

        bookingService.verifyCanViewSensitiveBookingInfo("off-1");
    }

    @Test
    public void testVerifyCannotAccessLatestBooking() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getBookingIdByOffenderNo("off-1")).thenReturn(Optional.of(bookingId));
        when(agencyService.getAgencyIds()).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(false);

        assertThatThrownBy(() ->
                bookingService.verifyCanViewSensitiveBookingInfo("off-1"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void verifyCanViewSensitiveBookingInfo() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getBookingIdByOffenderNo("off-1")).thenReturn(Optional.of(bookingId));
        when(agencyService.getAgencyIds()).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(true);


        bookingService.verifyCanViewSensitiveBookingInfo("off-1");
    }

    @Test
    public void verifyCanViewSensitiveBookingInfo_systemUser() {
        when(securityUtils.isOverrideRole()).thenReturn(true);

        when(bookingRepository.getBookingIdByOffenderNo("off-1")).thenReturn(Optional.of(-1L));

        bookingService.verifyCanViewSensitiveBookingInfo("off-1");
    }

    @Test
    public void verifyCanViewSensitiveBookingInfo_not() {

        final var agencyIds = Set.of("agency-1");
        final var bookingId = 1L;

        when(bookingRepository.getBookingIdByOffenderNo("off-1")).thenReturn(Optional.of(bookingId));
        when(agencyService.getAgencyIds()).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(false);

        assertThatThrownBy(() ->
                bookingService.verifyCanViewSensitiveBookingInfo("off-1"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void givenValidBookingIdIepLevelAndComment_whenIepLevelAdded() {
        val bookingId = 1L;

        when(referenceDomainService.isReferenceCodeActive("IEP_LEVEL", "STD")).thenReturn(true);
        when(bookingRepository.getIepLevelsForAgencySelectedByBooking(bookingId)).thenReturn(Set.of("ENT", "BAS", "STD", "ENH"));

        val iepLevelAndComment = IepLevelAndComment.builder().iepLevel("STD").comment("Comment").build();

        bookingService.addIepLevel(bookingId, "FRED", iepLevelAndComment);

        verify(bookingRepository).addIepLevel(bookingId, "FRED", iepLevelAndComment);
    }

    @Test
    public void givenInvalidIepLevel_whenIepLevelAdded() {
        val bookingId = 1L;

        when(referenceDomainService.isReferenceCodeActive("IEP_LEVEL", "STD")).thenReturn(false);

        val iepLevelAndComment = IepLevelAndComment.builder().iepLevel("STD").comment("Comment").build();
        assertThatThrownBy(() -> bookingService.addIepLevel(bookingId, "FRED", iepLevelAndComment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("IEP Level 'STD' is not a valid NOMIS value.");
    }

    @Test
    public void givenValidIepLevel_whenIepLevelNotValidForAgencyAssociatedWithBooking() {
        val bookingId = 1L;

        when(referenceDomainService.isReferenceCodeActive("IEP_LEVEL", "STD")).thenReturn(true);
        when(bookingRepository.getIepLevelsForAgencySelectedByBooking(bookingId)).thenReturn(Set.of("ENT", "BAS", "ENH"));

        val iepLevelAndComment = IepLevelAndComment.builder().iepLevel("STD").comment("Comment").build();
        assertThatThrownBy(() -> bookingService.addIepLevel(bookingId, "FRED", iepLevelAndComment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("IEP Level 'STD' is not active for this booking's agency: Booking Id 1.");
    }

    @Test
    public void testThatUpdateAttendanceIsCalledForEachBooking() {
        val bookingIds = Set.of(1L, 2L, 3L);
        val activityId = 2L;

        when(bookingRepository.getLatestBookingByBookingId(anyLong()))
                .thenReturn(Optional.of(OffenderSummary.builder().bookingId(1L).build()))
                .thenReturn(Optional.of(OffenderSummary.builder().bookingId(2L).build()))
                .thenReturn(Optional.of(OffenderSummary.builder().bookingId(3L).build()));

        when(agencyService.getAgencyIds()).thenReturn(Set.of("MDI"));
        when(bookingRepository.verifyBookingAccess(anyLong(), anySet())).thenReturn(true);

        when(bookingRepository.getAttendanceEventDate(anyLong())).thenReturn(LocalDate.now());
        when(bookingRepository.getPayableAttendanceOutcome(anyString(), anyString()))
                .thenReturn(PayableAttendanceOutcomeDto
                        .builder()
                        .paid(true)
                        .build());

        val updateAttendance = UpdateAttendance
                .builder()
                .eventOutcome("ATT")
                .performance("STANDARD")
                .build();

        val bookingActivities = bookingIds.stream()
                .map(bookingId -> BookingActivity.builder().bookingId(bookingId).activityId(activityId).build())
                .collect(Collectors.toSet());

        bookingService.updateAttendanceForMultipleBookingIds(bookingActivities, updateAttendance);

        val expectedOutcome = UpdateAttendance.builder().performance("STANDARD").eventOutcome("ATT").build();

        bookingIds.forEach(bookingId -> verify(bookingRepository).updateAttendance(bookingId, activityId, expectedOutcome, true, false));
    }

    @Test
    public void getBookingIEPSummary_singleBooking_withDetail_noPrivileges() {
        assertThatThrownBy(() -> bookingService.getBookingIEPSummary(-1L, true))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_withDetail_noPrivileges() {
        assertThatThrownBy(() -> bookingService.getBookingIEPSummary(List.of(-1L, -2L), true))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getBookingIEPSummary_singleBooking_noPrivileges() {
        assertThatThrownBy(() -> bookingService.getBookingIEPSummary(-1L, false))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_noPrivileges() {
        assertThatThrownBy(() -> bookingService.getBookingIEPSummary(List.of(-1L, -2L), false))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_globalSearchUser() {
        when(securityUtils.isOverrideRole(anyString(), anyString(), anyString(), anyString())).thenReturn(true);
        when(bookingRepository.getBookingIEPDetailsByBookingIds(anyList())).thenReturn(Map.of(-5L, List.of(PrivilegeDetail.builder().iepDate(LocalDate.now()).build())));
        assertThat(bookingService.getBookingIEPSummary(List.of(-1L, -2L), false)).containsKeys(-5L);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_withDetail_systemUser() {
        when(securityUtils.isOverrideRole()).thenReturn(true);
        when(bookingRepository.getBookingIEPDetailsByBookingIds(anyList())).thenReturn(Map.of(-5L, List.of(PrivilegeDetail.builder().iepDate(LocalDate.now()).build())));
        assertThat(bookingService.getBookingIEPSummary(List.of(-1L, -2L), true)).containsKeys(-5L);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_withDetail_onlyAccessToOneBooking() {
        final Long bookingId = -1L;

        final var agencyIds = Set.of("LEI");
        when(agencyService.getAgencyIds()).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(bookingId, agencyIds)).thenReturn(true);

        assertThatThrownBy(() -> bookingService.getBookingIEPSummary(List.of(-1L, -2L), true)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    public void getBookingIEPSummary_multipleBooking_withDetail_accessToBothBookings() {
        final var agencyIds = Set.of("LEI");
        when(agencyService.getAgencyIds()).thenReturn(agencyIds).thenReturn(agencyIds);
        when(bookingRepository.verifyBookingAccess(anyLong(), any())).thenReturn(true).thenReturn(true);
        when(bookingRepository.getBookingIEPDetailsByBookingIds(anyList())).thenReturn(Map.of(-5L, List.of(PrivilegeDetail.builder().iepDate(LocalDate.now()).build())));

        assertThat(bookingService.getBookingIEPSummary(List.of(-1L, -2L), true)).containsKeys(-5L);
    }

    @Test
    public void getBookingVisitBalances() {
        final var bookingId = -1L;
        when(bookingRepository.getBookingVisitBalances(bookingId)).thenReturn(Optional.of(new VisitBalances(25, 2)));

        bookingService.getBookingVisitBalances(bookingId);

        verify(bookingRepository).getBookingVisitBalances(bookingId);
    }

    @Test
    public void getEvents_CallsBookingRepository() {
        final var bookingId = -1L;
        final var from = LocalDate.parse("2019-02-03");
        final var to = LocalDate.parse("2019-03-03");
        bookingService.getEvents(bookingId, from, to);
        verify(bookingRepository).getBookingActivities(bookingId, from, to, "startTime", Order.ASC);
        verify(bookingRepository).getBookingVisits(bookingId, from, to, "startTime", Order.ASC);
        verify(bookingRepository).getBookingAppointments(bookingId, from, to, "startTime", Order.ASC);
    }

    @Test
    public void getEvents_SortsAndCombinesNullsLast() {
        final var bookingId = -1L;
        final var from = LocalDate.parse("2019-02-03");
        final var to = LocalDate.parse("2019-03-03");
        when(bookingRepository.getBookingActivities(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("act", "10:11:12"),
                        createEvent("act", "08:59:50"))
        );
        when(bookingRepository.getBookingVisits(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("vis", null))
        );
        when(bookingRepository.getBookingAppointments(anyLong(), any(), any(), anyString(), any())).thenReturn(
                List.of(createEvent("app", "09:02:03"))
        );
        final var events = bookingService.getEvents(bookingId, from, to);
        assertThat(events).extracting(ScheduledEvent::getEventType).containsExactly("act08:59:50", "app09:02:03", "act10:11:12", "visnull");
    }

    private ScheduledEvent createEvent(final String type, final String time) {
        return ScheduledEvent.builder().bookingId(-1L)
                .startTime(Optional.ofNullable(time).map(t -> "2019-01-02T" + t).map(LocalDateTime::parse).orElse(null))
                .eventType(type + time).build();
    }
}
