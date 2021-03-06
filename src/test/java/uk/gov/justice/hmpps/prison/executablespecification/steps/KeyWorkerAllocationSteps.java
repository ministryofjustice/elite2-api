package uk.gov.justice.hmpps.prison.executablespecification.steps;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import uk.gov.justice.hmpps.prison.api.model.OffenderSummary;
import uk.gov.justice.hmpps.prison.test.PrisonApiClientException;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

/**
 * BDD step implementations for Key worker allocation feature.
 */
public class KeyWorkerAllocationSteps extends CommonSteps {
    private static final String KEY_WORKER_API_URL = API_PREFIX + "key-worker/{agencyId}/offenders/unallocated";

    private List<OffenderSummary> offenderSummaryList;

    public void getUnallocatedOffendersList(final String agencyId) {
        doListApiCall(agencyId);
    }

    public void verifyAListOfUnallocatedOffendersIsReturned(final int count) {
        assertThat(offenderSummaryList).hasSize(count);
    }

    private void doListApiCall(final String agencyId) {
        init();

        try {
            final var response =
                    restTemplate.exchange(
                            KEY_WORKER_API_URL,
                            HttpMethod.GET,
                            createEntity(null, addPaginationHeaders()),
                            new ParameterizedTypeReference<List<OffenderSummary>>() {
                            },
                            agencyId);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            offenderSummaryList = response.getBody();

        } catch (final PrisonApiClientException ex) {
            setErrorResponse(ex.getErrorResponse());
        }
    }

    @Override
    protected void init() {
        super.init();

        offenderSummaryList = null;
    }

    public void verifyListIsSortedByLastNameAsc() {
        assertThat(offenderSummaryList).extracting("lastName").isSorted();
    }
}
