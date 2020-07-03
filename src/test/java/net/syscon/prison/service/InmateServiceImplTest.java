package net.syscon.prison.service;

import com.google.common.collect.ImmutableList;
import com.microsoft.applicationinsights.TelemetryClient;
import net.syscon.prison.api.model.CategorisationDetail;
import net.syscon.prison.api.model.InmateBasicDetails;
import net.syscon.prison.api.model.OffenderCategorise;
import net.syscon.prison.api.model.OffenderSummary;
import net.syscon.prison.api.model.PersonalCareNeed;
import net.syscon.prison.api.model.PersonalCareNeeds;
import net.syscon.prison.api.model.ReasonableAdjustment;
import net.syscon.prison.api.model.SecondaryLanguage;
import net.syscon.prison.api.model.UserDetail;
import net.syscon.prison.repository.InmateRepository;
import net.syscon.prison.repository.KeyWorkerAllocationRepository;
import net.syscon.prison.repository.UserRepository;
import net.syscon.prison.repository.jpa.model.LanguageReferenceCode;
import net.syscon.prison.repository.jpa.model.OffenderLanguage;
import net.syscon.prison.repository.jpa.repository.OffenderLanguageRepository;
import net.syscon.prison.security.AuthenticationFacade;
import net.syscon.prison.service.support.AssessmentDto;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InmateServiceImplTest {
    @Mock
    private InmateAlertService inmateAlertService;
    @Mock
    private InmateRepository repository;
    @Mock
    private CaseLoadService caseLoadService;
    @Mock
    private BookingService bookingService;
    @Mock
    private AgencyService agencyService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private ReferenceDomainService referenceDomainService;
    @Mock
    private AuthenticationFacade authenticationFacade;
    @Mock
    private KeyWorkerAllocationRepository keyWorkerAllocationRepository;
    @Mock
    private Environment env;
    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private OffenderLanguageRepository offenderLanguageRepository;


    @Captor
    private ArgumentCaptor<List<Long>> bookingIdsArgument;

    private InmateService serviceToTest;

    @BeforeEach
    public void init() {
        serviceToTest = new InmateService(repository, caseLoadService, inmateAlertService,
                referenceDomainService, bookingService, agencyService, userService, userRepository, authenticationFacade,
                keyWorkerAllocationRepository, env, telemetryClient, "WING", 100, offenderLanguageRepository);
    }

    @Test
    public void testGetAssessments() {

        final var data = Arrays.asList(
                // need to ensure we OrderBy:
                // Order.DESC, "cellSharingAlertFlag"
                // Order.DESC, "assessmentDate"
                //	Order.DESC, "assessmentSeq"
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE4").assessmentDate(LocalDate.of(2018, Month.MAY, 7)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE4").assessmentDate(LocalDate.of(2018, Month.MAY, 6)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.MAY, 2)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.MAY, 1)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 8)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 7)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).build()
        );
        when(repository.findAssessments(Collections.singletonList(10L), null, Collections.emptySet())).thenReturn(data);

        final var assessments = serviceToTest.getAssessments(10L);

        assertThat(assessments).hasSize(4); // 1 per code
        assertThat(assessments).extracting("bookingId", "assessmentCode", "assessmentDate").contains(
                Tuple.tuple(10L, "CODE1", LocalDate.of(2018, Month.APRIL, 5)),
                Tuple.tuple(10L, "CODE2", LocalDate.of(2018, Month.APRIL, 8)),
                Tuple.tuple(10L, "CODE3", LocalDate.of(2018, Month.MAY, 2)),
                Tuple.tuple(10L, "CODE4", LocalDate.of(2018, Month.MAY, 7))
        );
    }

    @Test
    public void testGetInmatesAssessmentsByCode() {

        final var data = Arrays.asList(
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(false).assessmentCreateLocation("MDI").approvalDate(LocalDate.of(2018, Month.MAY, 5)).assessmentSeq(3).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).assessmentCreateLocation("LEI").approvalDate(LocalDate.of(2018, Month.JUNE, 5)).assessmentSeq(2).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 1)).cellSharingAlertFlag(true).assessmentCreateLocation("LPI").approvalDate(LocalDate.of(2018, Month.OCTOBER, 5)).assessmentSeq(1).build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.MAY, 7)).cellSharingAlertFlag(true).assessmentCreateLocation("EXI").assessmentSeq(2).build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.MAY, 6)).cellSharingAlertFlag(true).assessmentSeq(1).build()
        );
        when(repository.findAssessmentsByOffenderNo(Arrays.asList("OFFENDER10", "OFFENDER11"), "THECODE", Collections.emptySet(), true, true)).thenReturn(data);

        final var assessments = serviceToTest.getInmatesAssessmentsByCode(Arrays.asList("OFFENDER10", "OFFENDER11"), "THECODE", true, true, false);

        assertThat(assessments).hasSize(2); // 1 per offender
        assertThat(assessments).extracting("bookingId", "assessmentCode", "assessmentDate", "assessmentAgencyId", "approvalDate", "assessmentSeq").containsExactly(
                Tuple.tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 5), "MDI", LocalDate.of(2018, Month.MAY, 5), 3),
                Tuple.tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY, 7), "EXI", null, 2)
        );
    }

    @Test
    public void testGetInmatesAssessmentsByCodeIncludingHistorical() {

        final var data = Arrays.asList(
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(false).assessmentCreateLocation("MDI").approvalDate(LocalDate.of(2018, Month.MAY, 5)).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).assessmentCreateLocation("LEI").approvalDate(LocalDate.of(2018, Month.JUNE, 5)).build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.APRIL, 1)).cellSharingAlertFlag(true).assessmentCreateLocation("LPI").approvalDate(LocalDate.of(2018, Month.OCTOBER, 5)).build(),
                AssessmentDto.builder().bookingId(9L).offenderNo("OFFENDER10").assessmentCode("THECODE").assessmentDate(LocalDate.of(2016, Month.APRIL, 1)).cellSharingAlertFlag(true).assessmentCreateLocation("LPI").approvalDate(LocalDate.of(2016, Month.OCTOBER, 5)).build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.MAY, 7)).cellSharingAlertFlag(true).build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("THECODE").assessmentDate(LocalDate.of(2018, Month.MAY, 6)).cellSharingAlertFlag(true).build()
        );
        when(repository.findAssessmentsByOffenderNo(Arrays.asList("OFFENDER10", "OFFENDER11"), "THECODE", Collections.emptySet(), false, true)).thenReturn(data);

        final var assessments = serviceToTest.getInmatesAssessmentsByCode(Arrays.asList("OFFENDER10", "OFFENDER11"), "THECODE", false, true, false);

        assertThat(assessments).hasSize(6);
        assertThat(assessments).extracting("bookingId", "assessmentCode", "assessmentDate").containsExactly(
                Tuple.tuple(9L, "THECODE", LocalDate.of(2016, Month.APRIL, 1)),
                Tuple.tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 5)),
                Tuple.tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 4)),
                Tuple.tuple(10L, "THECODE", LocalDate.of(2018, Month.APRIL, 1)),
                Tuple.tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY, 7)),
                Tuple.tuple(11L, "THECODE", LocalDate.of(2018, Month.MAY, 6))
        );
    }

    @Test
    public void testAllCodes() {
        // Ensure Ordering is same as from repository
        final var data = Arrays.asList(
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.MAY, 7)).cellSharingAlertFlag(true).reviewSupLevelType("STANDARD").reviewSupLevelTypeDesc("Standard").build(),
                AssessmentDto.builder().bookingId(11L).offenderNo("OFFENDER11").assessmentCode("CODE1").assessmentDate(LocalDate.of(2018, Month.MAY, 6)).cellSharingAlertFlag(true).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.APRIL, 5)).cellSharingAlertFlag(true).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 4)).cellSharingAlertFlag(true).reviewSupLevelType("LOW").reviewSupLevelTypeDesc("Low").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE3").assessmentDate(LocalDate.of(2018, Month.APRIL, 3)).cellSharingAlertFlag(true).reviewSupLevelType("MED").reviewSupLevelTypeDesc("Medium").build(),
                AssessmentDto.builder().bookingId(10L).offenderNo("OFFENDER10").assessmentCode("CODE2").assessmentDate(LocalDate.of(2018, Month.APRIL, 1)).cellSharingAlertFlag(false).reviewSupLevelType("HIGH").reviewSupLevelTypeDesc("High").build()
        );
        when(repository.findAssessmentsByOffenderNo(Arrays.asList("OFFENDER10", "OFFENDER11"), null, Collections.emptySet(), true, true)).thenReturn(data);

        final var assessments = serviceToTest.getInmatesAssessmentsByCode(Arrays.asList("OFFENDER10", "OFFENDER11"), null, true, true, false);

        assertThat(assessments).hasSize(2); // 1 per offender
        assertThat(assessments).extracting("bookingId", "assessmentCode", "assessmentDate", "classification").contains(
                Tuple.tuple(10L, "CODE3", LocalDate.of(2018, Month.APRIL, 5), "High"),
                Tuple.tuple(11L, "CODE1", LocalDate.of(2018, Month.MAY, 7), "Standard")
        );
    }

    @Test
    public void testCreateCategorisation() {

        final var catDetail = CategorisationDetail.builder().bookingId(-5L).category("D").committee("GOV").comment("comment").build();

        when(bookingService.getLatestBookingByBookingId(1234L)).thenReturn(OffenderSummary.builder().agencyLocationId("CDI").bookingId(-5L).build());
        when(userService.getUserByUsername("ME")).thenReturn(UserDetail.builder().staffId(444L).username("ME").build());
        when(repository.insertCategory(catDetail, "CDI", 444L, "ME")).thenReturn(Map.of("sequenceNumber", 2L, "bookingId", -5L));
        when(authenticationFacade.getCurrentUsername()).thenReturn("ME");

        final var responseMap = serviceToTest.createCategorisation(1234L, catDetail);

        assertThat(responseMap).contains(entry("bookingId", -5L), entry("sequenceNumber", 2L));
        assertThat(catDetail.getNextReviewDate()).isEqualTo(LocalDate.now().plusMonths(6));
        verify(repository, Mockito.times(1)).insertCategory(catDetail, "CDI", 444L, "ME");
    }

    @Test
    public void testCreateCategorisationWithReviewDateSpecified() {

        final var catDetail = CategorisationDetail.builder().bookingId(-5L).category("D").committee("GOV").comment("comment")
                .nextReviewDate(LocalDate.of(2019, 4, 1)).build();

        when(bookingService.getLatestBookingByBookingId(1234L)).thenReturn(OffenderSummary.builder().agencyLocationId("CDI").bookingId(-5L).build());
        when(userService.getUserByUsername("ME")).thenReturn(UserDetail.builder().staffId(444L).username("ME").build());
        when(authenticationFacade.getCurrentUsername()).thenReturn("ME");

        serviceToTest.createCategorisation(1234L, catDetail);

        verify(repository, Mockito.times(1)).insertCategory(catDetail, "CDI", 444L, "ME");
    }

    @Test
    public void testGetOffenderCategorisationsBatching() {

        final var setOf150Longs = Stream.iterate(1L, n -> n + 1)
                .limit(150)
                .collect(Collectors.toSet());

        final var agencyArgument = ArgumentCaptor.forClass(String.class);

        final var catDetail1 = OffenderCategorise.builder().bookingId(-5L).category("D").build();
        final var catDetail2 = OffenderCategorise.builder().bookingId(-4L).category("B").build();
        final var catDetail3 = OffenderCategorise.builder().bookingId(-3L).category("C").build();

        final var listOf100Longs = Stream.iterate(1L, n -> n + 1)
                .limit(100)
                .collect(Collectors.toList());

        final var listOf50Longs = Stream.iterate(101L, n -> n + 1)
                .limit(50)
                .collect(Collectors.toList());

        when(repository.getOffenderCategorisations(listOf100Longs, "LEI", true)).thenReturn(Collections.singletonList(catDetail1));
        when(repository.getOffenderCategorisations(listOf50Longs, "LEI", true)).thenReturn(ImmutableList.of(catDetail2, catDetail3));

        final var results = serviceToTest.getOffenderCategorisations("LEI", setOf150Longs, true);

        assertThat(results).hasSize(3);

        verify(repository, Mockito.times(2)).getOffenderCategorisations(bookingIdsArgument.capture(), agencyArgument.capture(), eq(true));
        final var capturedArguments = bookingIdsArgument.getAllValues();
        assertThat(capturedArguments.get(0)).containsAll(listOf100Longs);
        assertThat(capturedArguments.get(1)).containsAll(listOf50Longs);
    }

    @Test
    public void testGetBasicInmateDetailsByBookingIdsBatching() {

        final var setOf150Longs = Stream.iterate(1L, n -> n + 1)
                .limit(150)
                .collect(Collectors.toSet());

        final var agencyArgument = ArgumentCaptor.forClass(String.class);

        final var detail1 = InmateBasicDetails.builder().bookingId(-5L).lastName("D").build();
        final var detail2 = InmateBasicDetails.builder().bookingId(-4L).lastName("B").build();
        final var detail3 = InmateBasicDetails.builder().bookingId(-3L).lastName("C").build();

        final var listOf100Longs = Stream.iterate(1L, n -> n + 1)
                .limit(100)
                .collect(Collectors.toList());

        final var listOf50Longs = Stream.iterate(101L, n -> n + 1)
                .limit(50)
                .collect(Collectors.toList());

        when(repository.getBasicInmateDetailsByBookingIds("LEI", listOf100Longs)).thenReturn(Collections.singletonList(detail1));
        when(repository.getBasicInmateDetailsByBookingIds("LEI", listOf50Longs)).thenReturn(ImmutableList.of(detail2, detail3));

        final var results = serviceToTest.getBasicInmateDetailsByBookingIds("LEI", setOf150Longs);

        assertThat(results).hasSize(3);

        verify(repository, Mockito.times(2)).getBasicInmateDetailsByBookingIds(agencyArgument.capture(), bookingIdsArgument.capture());
        final var capturedArguments = bookingIdsArgument.getAllValues();
        assertThat(capturedArguments.get(0)).containsAll(listOf100Longs);
        assertThat(capturedArguments.get(1)).containsAll(listOf50Longs);
    }


    @Test
    public void testMappingForOffenderDetailsAreCorrect() {
        final var offenderNumbers = Set.of("A123");
        final var caseLoadsIds = Set.of("1");

        when(authenticationFacade.getCurrentUsername()).thenReturn("ME");
        when(caseLoadService.getCaseLoadIdsForUser("ME", false)).thenReturn(caseLoadsIds);
        when(repository.getBasicInmateDetailsForOffenders(offenderNumbers, false, caseLoadsIds, true))
                .thenReturn(List.of(InmateBasicDetails.builder()
                        .lastName("LAST NAME")
                        .firstName("FIRST NAME")
                        .middleName("MIDDLE NAME")
                        .build()));

        final var offenders = serviceToTest.getBasicInmateDetailsForOffenders(offenderNumbers, true);

        assertThat(offenders)
                .containsExactly(InmateBasicDetails.builder()
                        .lastName("Last Name")
                        .firstName("First Name")
                        .middleName("Middle Name")
                        .build());
    }

    @Test
    public void testThatAnExceptionIsThrown_whenAStandardUserWithNoActiveCaseloadsRequestsInmateDetails() {
        when(authenticationFacade.getCurrentUsername()).thenReturn("ME");
        when(authenticationFacade.isOverrideRole(any())).thenReturn(false);
        when(caseLoadService.getCaseLoadIdsForUser("ME", false)).thenReturn(Collections.emptySet());

        Assertions.assertThatThrownBy(() -> serviceToTest.getBasicInmateDetailsForOffenders(Set.of("A123"), true))
                .isInstanceOf(HttpClientErrorException.class)
                .hasMessageContaining("User has not active caseloads");
    }

    @Test
    public void testThatAnExceptionIsNotThrown_whenGlobalSearchUserWithNoActiveCaseloadsRequestsInmateDetails() {
        when(authenticationFacade.isOverrideRole(any())).thenReturn(true);
        serviceToTest.getBasicInmateDetailsForOffenders(Set.of("A123"), false);
        verify(repository).getBasicInmateDetailsForOffenders(Set.of("A123"), true, Collections.emptySet(), false);
    }

    @Test
    public void testThatGetBasicInmateDetailsForOffenders_isCalledWithCorrectParameters() {
        final var caseLoad = Set.of("LEI");

        when(authenticationFacade.getCurrentUsername()).thenReturn("ME");
        when(caseLoadService.getCaseLoadIdsForUser("ME", false)).thenReturn(caseLoad);

        serviceToTest.getBasicInmateDetailsForOffenders(Set.of("A123"), true);

        verify(repository).getBasicInmateDetailsForOffenders(Set.of("A123"), false, caseLoad, true);
        verify(caseLoadService).getCaseLoadIdsForUser("ME", false);
    }

    @Test
    public void testGetPersonalCareNeedsByProblemTypeAndSubtype() {
        final var problemTypes = List.of("DISAB+RM", "DISAB+RC", "MATSTAT");
        final var personalCareNeedsAll = List.of(
                PersonalCareNeed.builder().problemType("DISAB").problemCode("MI").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
                PersonalCareNeed.builder().problemType("DISAB").problemCode("RM").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
                PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build()
        );
        final var personalCareNeeds = new PersonalCareNeeds(
                List.of(
                        PersonalCareNeed.builder().problemType("DISAB").problemCode("RM").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build(),
                        PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9").problemStatus("ON").startDate(LocalDate.parse("2019-01-02")).build()
                )
        );

        when(repository.findPersonalCareNeeds(anyLong(), anySet())).thenReturn(personalCareNeedsAll);

        final var response = serviceToTest.getPersonalCareNeeds(1L, problemTypes);

        verify(repository).findPersonalCareNeeds(1L, Set.of("DISAB", "MATSTAT"));
        assertThat(response).isEqualTo(personalCareNeeds);
    }

    @Test
    public void testGetPersonalCareNeedsSplitByOffender() {
        final var problemTypes = List.of("DISAB+RM", "DISAB+RC", "MATSTAT");

        final var aaMat = PersonalCareNeed.builder().problemType("MATSTAT").problemCode("ACCU9")
                .startDate(LocalDate.parse("2010-06-21")).offenderNo("A1234AA").build();
        final var aaDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("RM")
                .startDate(LocalDate.parse("2010-06-21")).offenderNo("A1234AA").build();
        final var abDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("RC")
                .startDate(LocalDate.parse("2010-06-22")).offenderNo("A1234AB").build();
        final var acDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("RM")
                .startDate(LocalDate.parse("2010-06-22")).offenderNo("A1234AC").build();
        final var adDisab = PersonalCareNeed.builder().problemType("DISAB").problemCode("ND")
                .startDate(LocalDate.parse("2010-06-24")).offenderNo("A1234AD").build();

        when(repository.findPersonalCareNeeds(anyList(), anySet())).thenReturn(
                List.of(aaMat, aaDisab, abDisab, acDisab, adDisab));

        final var response = serviceToTest.getPersonalCareNeeds(List.of("A1234AA"), problemTypes);

        verify(repository).findPersonalCareNeeds(List.of("A1234AA"), Set.of("DISAB", "MATSTAT"));
        assertThat(response).containsExactly(
                new PersonalCareNeeds("A1234AA", List.of(aaMat, aaDisab)),
                new PersonalCareNeeds("A1234AB", List.of(abDisab)),
                new PersonalCareNeeds("A1234AC", List.of(acDisab)));
    }

    @Test
    public void testGetReasonableAdjustmentsByType() {
        final var types = List.of("PEEP", "WHEELCHR_ACC");
        final var reasonableAdjustments = List.of(
                ReasonableAdjustment.builder().treatmentCode("WHEELCHR_ACC").commentText("abcd").startDate(LocalDate.parse("2019-01-02")).build()
        );

        when(repository.findReasonableAdjustments(1L, types)).thenReturn(reasonableAdjustments);

        serviceToTest.getReasonableAdjustments(1L, types);

        verify(repository).findReasonableAdjustments(1L, types);
    }

    @Test
    public void testGetSecondaryLanguages() {
       when(offenderLanguageRepository.findByOffenderBookId(anyLong())).thenReturn(List.of(
               OffenderLanguage.builder()
                       .offenderBookId(-1l)
                       .speakSkill("Y")
                       .readSkill("n")
                       .writeSkill("Y")
                       .code("ENG")
                       .type("SEC")
                       .referenceCode(new LanguageReferenceCode("ENG", "English"))
                       .build(),
               OffenderLanguage.builder()
                       .offenderBookId(-1l)
                       .speakSkill("Y")
                       .readSkill("n")
                       .writeSkill("Y")
                       .code("ENG")
                       .referenceCode(new LanguageReferenceCode("ENG", "English"))
                       .build(),
               OffenderLanguage.builder()
                       .offenderBookId(-1l)
                       .code("LAT")
                       .type("SEC")
                       .referenceCode(new LanguageReferenceCode("LAT", "Latvian"))
                       .build(),
               OffenderLanguage.builder()
                       .offenderBookId(-1l)
                       .code("TUR")
                       .type("PREF_SPEAK")
                       .referenceCode(new LanguageReferenceCode("TUR", "Turkish"))
                       .build()
               )
       );

       final var secondaryLanguages = serviceToTest.getSecondaryLanguages(-1L);

       assertThat(secondaryLanguages).containsExactlyInAnyOrder(
               SecondaryLanguage.builder()
                       .bookingId(-1L)
                       .code("ENG")
                       .description("English")
                       .canSpeak(true)
                       .canRead(false)
                       .canWrite(true)
                       .build(),
               SecondaryLanguage.builder()
                       .bookingId(-1L)
                       .code("LAT")
                       .description("Latvian")
                       .canSpeak(false)
                       .canRead(false)
                       .canWrite(false)
                       .build()
       );
    }

}
