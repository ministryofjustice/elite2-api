package net.syscon.elite.service;

import net.syscon.elite.api.model.Agency;
import net.syscon.elite.api.model.BookingActivity;
import net.syscon.elite.api.model.CourtCase;
import net.syscon.elite.api.model.IepLevelAndComment;
import net.syscon.elite.api.model.Location;
import net.syscon.elite.api.model.MilitaryRecord;
import net.syscon.elite.api.model.MilitaryRecords;
import net.syscon.elite.api.model.OffenderSummary;
import net.syscon.elite.api.model.PrivilegeDetail;
import net.syscon.elite.api.model.ScheduledEvent;
import net.syscon.elite.api.model.SentenceAdjustmentDetail;
import net.syscon.elite.api.model.UpdateAttendance;
import net.syscon.elite.api.model.VisitBalances;
import net.syscon.elite.api.support.Order;
import net.syscon.elite.repository.BookingRepository;
import net.syscon.elite.repository.jpa.model.ActiveFlag;
import net.syscon.elite.repository.jpa.model.AgencyInternalLocation;
import net.syscon.elite.repository.jpa.model.AgencyLocation;
import net.syscon.elite.repository.jpa.model.CaseStatus;
import net.syscon.elite.repository.jpa.model.OffenderKeyDateAdjustment;
import net.syscon.elite.repository.jpa.model.OffenderSentenceAdjustment;
import net.syscon.elite.repository.jpa.model.PropertyContainer;
import net.syscon.elite.repository.jpa.model.DisciplinaryAction;
import net.syscon.elite.repository.jpa.model.LegalCaseType;
import net.syscon.elite.repository.jpa.model.MilitaryBranch;
import net.syscon.elite.repository.jpa.model.MilitaryDischarge;
import net.syscon.elite.repository.jpa.model.MilitaryRank;
import net.syscon.elite.repository.jpa.model.Offender;
import net.syscon.elite.repository.jpa.model.OffenderBooking;
import net.syscon.elite.repository.jpa.model.OffenderCourtCase;
import net.syscon.elite.repository.jpa.model.OffenderMilitaryRecord;
import net.syscon.elite.repository.jpa.model.OffenderPropertyContainer;
import net.syscon.elite.repository.jpa.model.WarZone;
import net.syscon.elite.repository.jpa.repository.AgencyInternalLocationRepository;
import net.syscon.elite.repository.jpa.repository.OffenderBookingRepository;
import net.syscon.elite.repository.jpa.repository.OffenderKeyDateAdjustmentRepository;
import net.syscon.elite.repository.jpa.repository.OffenderSentenceAdjustmentRepository;
import net.syscon.elite.security.AuthenticationFacade;
import net.syscon.elite.service.support.PayableAttendanceOutcomeDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test cases for {@link BookingService}.
 */
@ExtendWith(MockitoExtension.class)
public class BookingServiceTest {
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private OffenderBookingRepository offenderBookingRepository;
    @Mock
    private AgencyService agencyService;
    @Mock
    private ReferenceDomainService referenceDomainService;
    @Mock
    private AgencyInternalLocationRepository agencyInternalLocationRepository;
    @Mock
    private OffenderSentenceAdjustmentRepository offenderSentenceAdjustmentRepository;
    @Mock
    private OffenderKeyDateAdjustmentRepository offenderKeyDateAdjustmentRepository;
    @Mock
    private AuthenticationFacade securityUtils;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private CaseloadToAgencyMappingService caseloadToAgencyMappingService;

    private BookingService bookingService;

    @BeforeEach
    public void init() {
        bookingService = new BookingService(
                bookingRepository,
                offenderBookingRepository,
                null,
                agencyService,
                null,
                referenceDomainService,
                caseloadToAgencyMappingService,
                agencyInternalLocationRepository,
                offenderSentenceAdjustmentRepository,
                offenderKeyDateAdjustmentRepository,
                securityUtils, authenticationFacade,
                "1",
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
        when(securityUtils.isOverrideRole(any())).thenReturn(true);

        when(bookingRepository.getBookingIdByOffenderNo("off-1")).thenReturn(Optional.of(-1L));

        bookingService.verifyCanViewSensitiveBookingInfo("off-1");

        verify(securityUtils).isOverrideRole(
                "SYSTEM_USER",
                "GLOBAL_SEARCH",
                "CREATE_CATEGORISATION",
                "APPROVE_CATEGORISATION"
        );
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
        final var bookingId = 1L;

        when(referenceDomainService.isReferenceCodeActive("IEP_LEVEL", "STD")).thenReturn(true);
        when(bookingRepository.getIepLevelsForAgencySelectedByBooking(bookingId)).thenReturn(Set.of("ENT", "BAS", "STD", "ENH"));

        final var iepLevelAndComment = IepLevelAndComment.builder().iepLevel("STD").comment("Comment").build();

        bookingService.addIepLevel(bookingId, "FRED", iepLevelAndComment);

        verify(bookingRepository).addIepLevel(bookingId, "FRED", iepLevelAndComment);
    }

    @Test
    public void givenInvalidIepLevel_whenIepLevelAdded() {
        final var bookingId = 1L;

        when(referenceDomainService.isReferenceCodeActive("IEP_LEVEL", "STD")).thenReturn(false);

        final var iepLevelAndComment = IepLevelAndComment.builder().iepLevel("STD").comment("Comment").build();
        assertThatThrownBy(() -> bookingService.addIepLevel(bookingId, "FRED", iepLevelAndComment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("IEP Level 'STD' is not a valid NOMIS value.");
    }

    @Test
    public void givenValidIepLevel_whenIepLevelNotValidForAgencyAssociatedWithBooking() {
        final var bookingId = 1L;

        when(referenceDomainService.isReferenceCodeActive("IEP_LEVEL", "STD")).thenReturn(true);
        when(bookingRepository.getIepLevelsForAgencySelectedByBooking(bookingId)).thenReturn(Set.of("ENT", "BAS", "ENH"));

        final var iepLevelAndComment = IepLevelAndComment.builder().iepLevel("STD").comment("Comment").build();
        assertThatThrownBy(() -> bookingService.addIepLevel(bookingId, "FRED", iepLevelAndComment))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("IEP Level 'STD' is not active for this booking's agency: Booking Id 1.");
    }

    @Test
    public void testThatUpdateAttendanceIsCalledForEachBooking() {
        final var bookingIds = Set.of(1L, 2L, 3L);
        final var activityId = 2L;

        when(bookingRepository.getLatestBookingByBookingId(anyLong()))
                .thenReturn(Optional.of(OffenderSummary.builder().bookingId(1L).build()))
                .thenReturn(Optional.of(OffenderSummary.builder().bookingId(2L).build()))
                .thenReturn(Optional.of(OffenderSummary.builder().bookingId(3L).build()));

        when(bookingRepository.getAttendanceEventDate(anyLong())).thenReturn(LocalDate.now());
        when(bookingRepository.getPayableAttendanceOutcome(anyString(), anyString()))
                .thenReturn(PayableAttendanceOutcomeDto
                        .builder()
                        .paid(true)
                        .build());

        final var updateAttendance = UpdateAttendance
                .builder()
                .eventOutcome("ATT")
                .performance("STANDARD")
                .build();

        final var bookingActivities = bookingIds.stream()
                .map(bookingId -> BookingActivity.builder().bookingId(bookingId).activityId(activityId).build())
                .collect(Collectors.toSet());

        bookingService.updateAttendanceForMultipleBookingIds(bookingActivities, updateAttendance);

        final var expectedOutcome = UpdateAttendance.builder().performance("STANDARD").eventOutcome("ATT").build();

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

    @Test
    public void getMilitaryRecords_map() {
        when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.of(OffenderBooking.builder()
                .militaryRecords(List.of(
                        OffenderMilitaryRecord.builder()
                                .startDate(LocalDate.parse("2000-01-01"))
                                .endDate(LocalDate.parse("2020-10-17"))
                                .militaryDischarge(new MilitaryDischarge("DIS", "Dishonourable"))
                                .warZone(new WarZone("AFG", "Afghanistan"))
                                .militaryBranch(new MilitaryBranch("ARM", "Army"))
                                .description("left")
                                .unitNumber("auno")
                                .enlistmentLocation("Somewhere")
                                .militaryRank(new MilitaryRank("LCPL_RMA", "Lance Corporal  (Royal Marines)"))
                                .serviceNumber("asno")
                                .disciplinaryAction(new DisciplinaryAction("CM", "Court Martial"))
                                .dischargeLocation("Sheffield")
                                .build(),
                        OffenderMilitaryRecord.builder()
                                .startDate(LocalDate.parse("2001-01-01"))
                                .militaryBranch(new MilitaryBranch("NAV", "Navy"))
                                .description("second record")
                                .build()))
                .build()));
        final var militaryRecords = bookingService.getMilitaryRecords(-1L);
        assertThat(militaryRecords).usingRecursiveComparison().isEqualTo(new MilitaryRecords(List.of(
                MilitaryRecord.builder()
                        .startDate(LocalDate.parse("2000-01-01"))
                        .endDate(LocalDate.parse("2020-10-17"))
                        .militaryDischargeCode("DIS")
                        .militaryDischargeDescription("Dishonourable")
                        .warZoneCode("AFG")
                        .warZoneDescription("Afghanistan")
                        .militaryBranchCode("ARM")
                        .militaryBranchDescription("Army")
                        .description("left")
                        .unitNumber("auno")
                        .enlistmentLocation("Somewhere")
                        .militaryRankCode("LCPL_RMA")
                        .militaryRankDescription("Lance Corporal  (Royal Marines)")
                        .serviceNumber("asno")
                        .disciplinaryActionCode("CM")
                        .disciplinaryActionDescription("Court Martial")
                        .dischargeLocation("Sheffield")
                        .build(),
                MilitaryRecord.builder()
                        .startDate(LocalDate.parse("2001-01-01"))
                        .militaryBranchCode("NAV")
                        .militaryBranchDescription("Navy")
                        .description("second record")
                        .build())));
    }

    @Test
    public void getMilitaryRecords_notfound() {
        when(offenderBookingRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> bookingService.getMilitaryRecords(-1L)).isInstanceOf(EntityNotFoundException.class).hasMessage("Offender booking with id -1 not found.");
    }

    @Test
    void getOffenderCourtCases_active_only_mapped() {
        final var activeCourtCase = caseWithDefaults().id(-1L).caseSeq(-1L).caseStatus(new CaseStatus("A", "Active")).build();
        final var inactiveCourtCase = caseWithDefaults().id(-2L).caseSeq(-2L).caseStatus(new CaseStatus("I", "Inactive")).build();

        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
                .courtCases(List.of(activeCourtCase, inactiveCourtCase))
                .build()));

        final var activeOnlyCourtCases = bookingService.getOffenderCourtCases(-1L, true);

        assertThat(activeOnlyCourtCases).containsExactly(CourtCase.builder()
                .id(-1L)
                .caseSeq(-1L)
                .beginDate(LocalDate.EPOCH)
                .agency(Agency.builder()
                        .agencyId("agency_id")
                        .active(true)
                        .agencyType("CRT")
                        .description("The Agency Description")
                        .build())
                .caseType("Adult")
                .caseInfoPrefix("cip")
                .caseInfoNumber("cin")
                .caseStatus("Active")
                .courtHearings(Collections.emptyList())
                .build());
    }

    @Test
    void getOffenderCourtCases_all_mapped() {
        final var activeCourtCase = caseWithDefaults().id(-1L).caseSeq(-1L).caseStatus(new CaseStatus("A", "Active")).build();
        final var inactiveCourtCase = caseWithDefaults().id(-2L).caseSeq(-2L).caseStatus(new CaseStatus("I", "Inactive")).build();

        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
                .courtCases(List.of(activeCourtCase, inactiveCourtCase))
                .build()));

        final var allCourtCases = bookingService.getOffenderCourtCases(-1L, false);

        assertThat(allCourtCases).containsExactly(
                CourtCase.builder()
                        .id(-1L)
                        .caseSeq(-1L)
                        .beginDate(LocalDate.EPOCH)
                        .agency(Agency.builder()
                                .agencyId("agency_id")
                                .active(true)
                                .agencyType("CRT")
                                .description("The Agency Description")
                                .build())
                        .caseType("Adult")
                        .caseInfoPrefix("cip")
                        .caseInfoNumber("cin")
                        .caseStatus("Active")
                        .courtHearings(Collections.emptyList())
                        .build(),
                CourtCase.builder()
                        .id(-2L)
                        .caseSeq(-2L)
                        .beginDate(LocalDate.EPOCH)
                        .agency(Agency.builder()
                                .agencyId("agency_id")
                                .active(true)
                                .agencyType("CRT")
                                .description("The Agency Description")
                                .build())
                        .caseType("Adult")
                        .caseInfoPrefix("cip")
                        .caseInfoNumber("cin")
                        .caseStatus("Inactive")
                        .courtHearings(Collections.emptyList())
                        .build());
    }

    private OffenderCourtCase.OffenderCourtCaseBuilder caseWithDefaults() {
        return OffenderCourtCase.builder().beginDate(LocalDate.EPOCH)
                .agencyLocation(AgencyLocation.builder()
                        .id("agency_id")
                        .activeFlag(ActiveFlag.Y)
                        .type("CRT")
                        .description("The agency description")
                        .build())
                .legalCaseType(new LegalCaseType("A", "Adult"))
                .caseInfoPrefix("cip")
                .caseInfoNumber("cin");
    }

    @Test
    void getOffenderPropertyContainers() {
        final var activePropertyContainer = containerWithDefaults().containerId(-1L).activeFlag("Y").build();
        final var inactivePropertyContainer = containerWithDefaults().containerId(-2L).activeFlag("N").build();

        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.of(OffenderBooking.builder()
                .propertyContainers(List.of(activePropertyContainer, inactivePropertyContainer))
                .build()));

        final var propertyContainers = bookingService.getOffenderPropertyContainers(-1L);

        assertThat(propertyContainers).containsExactly(net.syscon.elite.api.model.PropertyContainer.builder()
                .sealMark("TEST1")
                .location(Location.builder()
                        .locationId(10L)
                        .description(null)
                        .build())
                .containerType("Bulk")
                .build());
    }

    private OffenderPropertyContainer.OffenderPropertyContainerBuilder containerWithDefaults() {
        return OffenderPropertyContainer.builder()
                .sealMark("TEST1")
                .internalLocation(AgencyInternalLocation.builder()
                        .activeFlag(ActiveFlag.Y)
                        .locationId(10L)
                        .build())
                .containerType(new PropertyContainer("BULK", "Bulk"));
    }

    @Test
    void getSentenceAdjustments() {
        final var offenderSentenceAdjustments = List.of(
                OffenderSentenceAdjustment.builder()
                        .id(-8L)
                        .offenderBookId(-6L)
                        .sentenceAdjustCode("RSR")
                        .activeFlag(ActiveFlag.Y)
                        .adjustDays(4)
                        .build(),
                OffenderSentenceAdjustment.builder()
                        .id(-9L)
                        .offenderBookId(-6L)
                        .sentenceAdjustCode("RST")
                        .activeFlag(ActiveFlag.N)
                        .adjustDays(4)
                        .build(),
                OffenderSentenceAdjustment.builder()
                        .id(-10L)
                        .offenderBookId(-6L)
                        .sentenceAdjustCode("RX")
                        .activeFlag(ActiveFlag.Y)
                        .adjustDays(4)
                        .build(),
                OffenderSentenceAdjustment.builder()
                        .id(-11L)
                        .offenderBookId(-6L)
                        .sentenceAdjustCode("S240A")
                        .activeFlag(ActiveFlag.N)
                        .adjustDays(4)
                        .build(),
                OffenderSentenceAdjustment.builder()
                        .id(-12L)
                        .offenderBookId(-6L)
                        .sentenceAdjustCode("UR")
                        .activeFlag(ActiveFlag.Y)
                        .adjustDays(4)
                        .build(),
                OffenderSentenceAdjustment.builder()
                        .id(-13L)
                        .offenderBookId(-6L)
                        .sentenceAdjustCode("RX")
                        .activeFlag(ActiveFlag.Y)
                        .adjustDays(4)
                        .build()
        );

        final var offenderKeyDateAdjustments = List.of(
                OffenderKeyDateAdjustment
                        .builder()
                        .id(-8L)
                        .sentenceAdjustCode("ADA")
                        .activeFlag(ActiveFlag.Y)
                        .offenderBookId(-6L)
                        .adjustDays(4)
                        .build(),
                OffenderKeyDateAdjustment
                        .builder()
                        .id(-9L)
                        .sentenceAdjustCode("ADA")
                        .activeFlag(ActiveFlag.N)
                        .offenderBookId(-6L)
                        .adjustDays(9)
                        .build(),
                OffenderKeyDateAdjustment
                        .builder()
                        .id(-10L)
                        .sentenceAdjustCode("ADA")
                        .activeFlag(ActiveFlag.Y)
                        .offenderBookId(-6L)
                        .adjustDays(13)
                        .build(),
                OffenderKeyDateAdjustment
                        .builder()
                        .id(-11L)
                        .sentenceAdjustCode("UAL")
                        .activeFlag(ActiveFlag.N)
                        .offenderBookId(-6L)
                        .adjustDays(1)
                        .build(),
                OffenderKeyDateAdjustment
                        .builder()
                        .id(-12L)
                        .sentenceAdjustCode("RADA")
                        .activeFlag(ActiveFlag.Y)
                        .offenderBookId(-6L)
                        .adjustDays(2)
                        .build(),
                OffenderKeyDateAdjustment
                        .builder()
                        .id(-13L)
                        .sentenceAdjustCode("UAL")
                        .activeFlag(ActiveFlag.Y)
                        .offenderBookId(-6L)
                        .adjustDays(7)
                        .build()
        );

        when(offenderKeyDateAdjustmentRepository.findAllByOffenderBookId(-6L)).thenReturn(offenderKeyDateAdjustments);
        when(offenderSentenceAdjustmentRepository.findAllByOffenderBookId(-6L)).thenReturn(offenderSentenceAdjustments);

        final SentenceAdjustmentDetail sentenceAdjustmentDetail = bookingService.getBookingSentenceAdjustments(-6L);

        assertThat(sentenceAdjustmentDetail).isEqualTo(
                SentenceAdjustmentDetail.builder()
                        .additionalDaysAwarded(17)
                        .unlawfullyAtLarge(7)
                        .restoredAdditionalDaysAwarded(2)
                        .recallSentenceRemand(4)
                        .remand(8)
                        .unusedRemand(4)
                        .build());
    }


    @Test
    void getOffenderCourtCases_errors_for_unknown_booking() {
        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getOffenderCourtCases(-1L, true))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Offender booking with id -1 not found.");
    }

    @Test
    void getOffenderProprtyContainers_errors_for_unknown_booking() {
        when(offenderBookingRepository.findById(-1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getOffenderPropertyContainers(-1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Offender booking with id -1 not found.");
    }

    private ScheduledEvent createEvent(final String type, final String time) {
        return ScheduledEvent.builder().bookingId(-1L)
                .startTime(Optional.ofNullable(time).map(t -> "2019-01-02T" + t).map(LocalDateTime::parse).orElse(null))
                .eventType(type + time).build();
    }

    @Test
    public void getOffenderSentenceSummaries_forOveriddenRole() {
        when(securityUtils.isOverrideRole(any())).thenReturn(true);
        when(caseloadToAgencyMappingService.agenciesForUsersWorkingCaseload(any())).thenReturn(List.of());
        assertThatThrownBy(() -> bookingService.getOffenderSentencesSummary(null, List.of()))
                .isInstanceOf(HttpClientErrorException.class).hasMessage("400 Request must be restricted to either a caseload, agency or list of offenders");
    }

    @Nested
    class UpdateLivingUnit {

        final Long SOME_BOOKING_ID = 1L;
        final Long BAD_BOOKING_ID = 2L;
        final Long OLD_LIVING_UNIT_ID = 11L;
        final String OLD_LIVING_UNIT_DESC = "A-1";
        final Long NEW_LIVING_UNIT_ID = 12L;
        final String NEW_LIVING_UNIT_DESC = "Z-1";
        final String SOME_AGENCY_ID = "MDI";
        final String DIFFERENT_AGENCY_ID = "NOT_MDI";

        @Test
        void bookingNotFound_throws() {
            when(offenderBookingRepository.findById(BAD_BOOKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.updateLivingUnit(BAD_BOOKING_ID, NEW_LIVING_UNIT_DESC))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(valueOf(BAD_BOOKING_ID));
        }

        @Test
        void livingUnitNotFound_throws() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID)).thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID, SOME_AGENCY_ID));
            when(agencyInternalLocationRepository.findOneByDescription(NEW_LIVING_UNIT_DESC)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining(valueOf(NEW_LIVING_UNIT_DESC));
        }

        @Test
        void livingUnitNotCell_throws() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                    .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID, SOME_AGENCY_ID));
            when(agencyInternalLocationRepository.findOneByDescription(NEW_LIVING_UNIT_DESC))
                    .thenReturn(Optional.of(aLocation(NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC, SOME_AGENCY_ID, "WING")));

            assertThatThrownBy(() -> bookingService.updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(valueOf(NEW_LIVING_UNIT_DESC))
                    .hasMessageContaining("WING");
        }

        @Test
        void differentAgency_throws() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                    .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID, SOME_AGENCY_ID));
            when(agencyInternalLocationRepository.findOneByDescription(NEW_LIVING_UNIT_DESC))
                    .thenReturn(Optional.of(aLocation(NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC, DIFFERENT_AGENCY_ID)));

            assertThatThrownBy(() -> bookingService.updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(SOME_AGENCY_ID)
                    .hasMessageContaining(DIFFERENT_AGENCY_ID);
        }

        @Test
        void ok_updatesRepo() {
            when(offenderBookingRepository.findById(SOME_BOOKING_ID))
                    .thenReturn(anOffenderBooking(SOME_BOOKING_ID, OLD_LIVING_UNIT_ID, SOME_AGENCY_ID));
            when(agencyInternalLocationRepository.findOneByDescription(NEW_LIVING_UNIT_DESC))
                    .thenReturn(Optional.of(aLocation(NEW_LIVING_UNIT_ID, NEW_LIVING_UNIT_DESC, SOME_AGENCY_ID)));

            bookingService.updateLivingUnit(SOME_BOOKING_ID, NEW_LIVING_UNIT_DESC);

            ArgumentCaptor<OffenderBooking> updatedOffenderBooking = ArgumentCaptor.forClass(OffenderBooking.class);
            verify(offenderBookingRepository).save(updatedOffenderBooking.capture());
            assertThat(updatedOffenderBooking.getValue().getAssignedLivingUnit().getLocationId()).isEqualTo(NEW_LIVING_UNIT_ID);
        }

        private Optional<OffenderBooking> anOffenderBooking(final Long bookingId, final Long livingUnitId, final String agencyId) {
            final var agencyLocation = AgencyLocation.builder().id(agencyId).build();
            final var livingUnit = AgencyInternalLocation.builder().locationId(livingUnitId).build();
            final var offender = Offender.builder().nomsId("any noms id").build();
            return Optional.of(
                    OffenderBooking.builder()
                            .bookingId(bookingId)
                            .assignedLivingUnit(livingUnit)
                            .location(agencyLocation)
                            .offender(offender)
                            .build());
        }

        private AgencyInternalLocation aLocation(final Long locationId, final String locationDescription, final String agencyId) {
            return aLocation(locationId, locationDescription, agencyId, "CELL");
        }

        private AgencyInternalLocation aLocation(final Long locationId, final String locationDescription, final String agencyId, final String locationType) {
            return AgencyInternalLocation.builder()
                    .locationId(locationId)
                    .description(locationDescription)
                    .agencyId(agencyId)
                    .locationType(locationType)
                    .build();
        }
    }
}
