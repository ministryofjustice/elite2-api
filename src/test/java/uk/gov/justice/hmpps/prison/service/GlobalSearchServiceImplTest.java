package uk.gov.justice.hmpps.prison.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.justice.hmpps.prison.api.model.OffenderNumber;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetail;
import uk.gov.justice.hmpps.prison.api.model.PrisonerDetailSearchCriteria;
import uk.gov.justice.hmpps.prison.api.support.Page;
import uk.gov.justice.hmpps.prison.api.support.PageRequest;
import uk.gov.justice.hmpps.prison.repository.InmateRepository;
import uk.gov.justice.hmpps.prison.repository.OffenderRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(InmateRepository.class)
public class GlobalSearchServiceImplTest {
    private static final String LOCATION_FILTER_OUT = "OUT";
    private InmateRepository inmateRepository;
    private OffenderRepository offenderRepository;

    private PrisonerDetailSearchCriteria criteria;

    private GlobalSearchService service;

    private PageRequest pageRequest = new PageRequest();
    private static final List<String> TEST_OFFENDER_NO = List.of("AA1234B");
    private static final String TEST_PNC_NUMBER = "2002/713491N";
    private static final String TEST_CRO_NUMBER = "CRO987654";
    private static final String TEST_OFFENDER_NO_QUERY = "offenderNo:eq:'AA1234B'";

    @Before
    public void setUp() {
        inmateRepository = Mockito.mock(InmateRepository.class);
        offenderRepository = Mockito.mock(OffenderRepository.class);

        service = new GlobalSearchService(inmateRepository, offenderRepository);

        PowerMockito.mockStatic(InmateRepository.class);
    }

    @Test
    public void testFindOffendersBlankCriteria() {
        when(InmateRepository.generateFindOffendersQuery(criteria)).thenReturn("");

        criteria = PrisonerDetailSearchCriteria.builder().build();

        service.findOffenders(criteria, pageRequest);

        verify(inmateRepository, Mockito.never()).findOffenders(anyString(), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersByOffenderNo() {
        criteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build();

        when(InmateRepository.generateFindOffendersQuery(criteria)).thenReturn(TEST_OFFENDER_NO_QUERY);
        Mockito.when(inmateRepository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));

        service.findOffenders(criteria, pageRequest);

        verify(inmateRepository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersPrioritisedMatchWithOffenderNoMatch() {
        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNos(TEST_OFFENDER_NO)
                .pncNumber(TEST_PNC_NUMBER)
                .build();

        final var offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);

        Mockito.when(inmateRepository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(1));

        final var response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        verify(inmateRepository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
        verify(offenderRepository, Mockito.never()).findOffenders(any(PrisonerDetailSearchCriteria.class), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersPrioritisedMatchWithPncNumberMatch() {
        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNos(TEST_OFFENDER_NO)
                .pncNumber(TEST_PNC_NUMBER)
                .build();

        final var offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build();
        final var pncNumberCriteria = PrisonerDetailSearchCriteria.builder().pncNumber(TEST_PNC_NUMBER).build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);

        Mockito.when(inmateRepository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(offenderRepository.findOffenders(eq(pncNumberCriteria), any(PageRequest.class))).thenReturn(pageResponse(1));

        final var response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        verify(inmateRepository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
        verify(offenderRepository, Mockito.times(1)).findOffenders(eq(pncNumberCriteria), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersWithPncNumberMatch() {
        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(false)
                .pncNumber(TEST_PNC_NUMBER)
                .build();

        final var pncNumberCriteria = PrisonerDetailSearchCriteria.builder().pncNumber(TEST_PNC_NUMBER).build();

        Mockito.when(offenderRepository.findOffenders(eq(pncNumberCriteria), any(PageRequest.class))).thenReturn(pageResponse(1));

        final var response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        Mockito.verifyZeroInteractions(inmateRepository);
        verify(offenderRepository, Mockito.times(1)).findOffenders(eq(pncNumberCriteria), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersPrioritisedMatchWithCroNumberMatch() {
        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNos(TEST_OFFENDER_NO)
                .pncNumber(TEST_PNC_NUMBER)
                .croNumber(TEST_CRO_NUMBER)
                .build();

        final var offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build();
        final var pncNumberCriteria = PrisonerDetailSearchCriteria.builder().pncNumber(TEST_PNC_NUMBER).build();
        final var croNumberCriteria = PrisonerDetailSearchCriteria.builder().croNumber(TEST_CRO_NUMBER).build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);

        Mockito.when(inmateRepository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(offenderRepository.findOffenders(eq(pncNumberCriteria), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(offenderRepository.findOffenders(eq(croNumberCriteria), any(PageRequest.class))).thenReturn(pageResponse(1));

        final var response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        verify(inmateRepository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
        verify(offenderRepository, Mockito.times(1)).findOffenders(eq(pncNumberCriteria), any(PageRequest.class));
        verify(offenderRepository, Mockito.times(1)).findOffenders(eq(croNumberCriteria), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersWithCroNumberMatch() {

        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(false)
                .croNumber(TEST_CRO_NUMBER)
                .build();

        final var croNumberCriteria = PrisonerDetailSearchCriteria.builder().croNumber(TEST_CRO_NUMBER).build();

        Mockito.when(offenderRepository.findOffenders(eq(croNumberCriteria), any(PageRequest.class))).thenReturn(pageResponse(1));

        final var response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        Mockito.verifyZeroInteractions(inmateRepository);
        verify(offenderRepository, Mockito.times(1)).findOffenders(eq(croNumberCriteria), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersPrioritisedMatchWithPersonalAttrsMatch() {
        final var TEST_LAST_NAME = "STEPHENS";
        final var TEST_PERSONAL_ATTRS_QUERY = "lastName:eq:'STEPHENS'";

        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNos(TEST_OFFENDER_NO)
                .lastName(TEST_LAST_NAME)
                .build();

        final var offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build();
        final var personalAttrsCriteria = PrisonerDetailSearchCriteria.builder().lastName(TEST_LAST_NAME).build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);
        when(InmateRepository.generateFindOffendersQuery(personalAttrsCriteria)).thenReturn(TEST_PERSONAL_ATTRS_QUERY);

        Mockito.when(inmateRepository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(inmateRepository.findOffenders(eq(TEST_PERSONAL_ATTRS_QUERY), any(PageRequest.class))).thenReturn(pageResponse(3));

        final var response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        verify(inmateRepository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
        verify(inmateRepository, Mockito.times(1)).findOffenders(eq(TEST_PERSONAL_ATTRS_QUERY), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersAliasSearchLocationFilter() {
        criteria = PrisonerDetailSearchCriteria.builder()
                .location(LOCATION_FILTER_OUT)
                .includeAliases(true)
                .build();

        when(InmateRepository.generateFindOffendersQuery(criteria)).thenReturn(TEST_OFFENDER_NO_QUERY);
        when(inmateRepository.findOffendersWithAliases(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(1));

        final var response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        verify(inmateRepository, Mockito.times(1)).findOffendersWithAliases(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
    }

    @Test(expected = HttpClientErrorException.class)
    public void testFindOffendersAliasSearchInvalidLocationFilter() {
        criteria = PrisonerDetailSearchCriteria.builder()
                .location("ABC")
                .includeAliases(true)
                .build();

        service.findOffenders(criteria, pageRequest);
    }

    @Test
    public void testFindOffendersPrioritisedMatchWithDobRangeMatch() {
        final var TEST_LAST_NAME = "STEPHENS";
        final var TEST_DOB_FROM = LocalDate.of(1960, 1, 1);
        final var TEST_DOB_TO = LocalDate.of(1964, 12, 31);
        final var TEST_PERSONAL_ATTRS_QUERY = "lastName:eq:'STEPHENS'";
        final var TEST_DOB_RANGE_QUERY = "(and:dateOfBirth:gteq:'1960-01-01':'YYYY-MM-DD',and:dateOfBirth:lteq:'1964-12-31':'YYYY-MM-DD')";

        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNos(TEST_OFFENDER_NO)
                .lastName(TEST_LAST_NAME)
                .dobFrom(TEST_DOB_FROM)
                .dobTo(TEST_DOB_TO)
                .build();

        final var offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build();
        final var personalAttrsCriteria = PrisonerDetailSearchCriteria.builder().lastName(TEST_LAST_NAME).build();

        final var dobRangeCriteria = PrisonerDetailSearchCriteria.builder()
                .dobFrom(TEST_DOB_FROM)
                .dobTo(TEST_DOB_TO)
                .build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);
        when(InmateRepository.generateFindOffendersQuery(personalAttrsCriteria)).thenReturn(TEST_PERSONAL_ATTRS_QUERY);
        when(InmateRepository.generateFindOffendersQuery(dobRangeCriteria)).thenReturn(TEST_DOB_RANGE_QUERY);

        Mockito.when(inmateRepository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(inmateRepository.findOffenders(eq(TEST_PERSONAL_ATTRS_QUERY), any(PageRequest.class))).thenReturn(pageResponse(0));
        Mockito.when(inmateRepository.findOffenders(eq(TEST_DOB_RANGE_QUERY), any(PageRequest.class))).thenReturn(pageResponse(5));

        final var response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        verify(inmateRepository, Mockito.times(1)).findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class));
        verify(inmateRepository, Mockito.times(1)).findOffenders(eq(TEST_PERSONAL_ATTRS_QUERY), any(PageRequest.class));
        verify(inmateRepository, Mockito.times(1)).findOffenders(eq(TEST_DOB_RANGE_QUERY), any(PageRequest.class));
    }

    @Test
    public void testFindOffendersLocationFormatting() {
        criteria = PrisonerDetailSearchCriteria.builder()
                .prioritisedMatch(true)
                .offenderNos(TEST_OFFENDER_NO)
                .build();

        final var offNoCriteria = PrisonerDetailSearchCriteria.builder().offenderNos(TEST_OFFENDER_NO).build();

        when(InmateRepository.generateFindOffendersQuery(offNoCriteria)).thenReturn(TEST_OFFENDER_NO_QUERY);

        Mockito.when(inmateRepository.findOffenders(eq(TEST_OFFENDER_NO_QUERY), any(PageRequest.class))).thenReturn(pageResponse(1));

        final var response = service.findOffenders(criteria, pageRequest);

        assertThat(response.getItems()).isNotEmpty();

        assertThat(response.getItems().get(0).getLatestLocation()).isEqualTo("Wakefield (HMP)");
    }

    @Test
    public void getOffenderNumbers() {

        var pageRequest = new PageRequest(0L, 1L);

        when(offenderRepository.listAllOffenders(pageRequest))
                .thenReturn(new Page<>(List.of(new OffenderNumber("offender1")), 1L, pageRequest));

        assertThat(service.getOffenderNumbers(0L, 1L).getItems())
                .containsExactly(new OffenderNumber("offender1"));
    }

    private Page<PrisonerDetail> pageResponse(final int prisonerCount) {
        final List<PrisonerDetail> prisoners = new ArrayList<>();

        for (var i = 1; i <= prisonerCount; i++) {
            prisoners.add(PrisonerDetail.builder().offenderNo(String.format("A%4dAA", i)).latestLocation("WAKEFIELD (HMP)").build());
        }

        return new Page<>(prisoners, prisonerCount, 0, 10);
    }
}
