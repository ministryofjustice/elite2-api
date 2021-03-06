package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;
import uk.gov.justice.hmpps.prison.repository.jpa.model.OffenderImprisonmentStatus;
import uk.gov.justice.hmpps.prison.repository.jpa.repository.OffenderImprisonmentStatusRepository;
import uk.gov.justice.hmpps.prison.service.PrisonerReleaseAndTransferService;
import uk.gov.justice.hmpps.prison.service.SmokeTestHelperService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;


public class SmokeTestHelperResourceIntTest extends ResourceTest {
    @Autowired
    private OffenderImprisonmentStatusRepository repository;
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    private PrisonerReleaseAndTransferService prisonerReleaseAndTransferService;
    @Autowired
    private SmokeTestHelperService smokeTestHelperService;

    @Test
    @DisplayName("requires ROLE_SMOKE_TEST")
    public void requiresCorrectRole() {
        final var request = createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE), null);

        final var response = testRestTemplate.exchange(
            "/api/smoketest/offenders/A1234AA/imprisonment-status",
            HttpMethod.POST,
            request,
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
    }

    @Test
    @DisplayName("not found")
    public void notFound() {

        final var response = testRestTemplate.exchange(
            "/api/smoketest/offenders/NOT_AN_OFFENDER/imprisonment-status",
            HttpMethod.POST,
            createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.SMOKE_TEST), null),
            Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
    }

    @Test
    @DisplayName("will add new imprisonment status to active booking")
    public void willAddImprisonmentStatus() {
        // GIVEN the offender booking as a single imprisonment status
        final var bookingId = -1L;
        assertThat(repository.findByOffenderBookingId(bookingId)).hasSize(1);

        // WHEN I setup the smoke test data
        final var response = testRestTemplate.exchange(
            "/api/smoketest/offenders/A1234AA/imprisonment-status",
            HttpMethod.POST,
            createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.SMOKE_TEST), null),
            Void.class
        );
        assertThat(response.getStatusCode()).isEqualTo(OK);


        // THEN I have two imprisonment statuses
        final List<OffenderImprisonmentStatus> statuses = repository.findByOffenderBookingId(bookingId);
        assertThat(statuses).hasSize(2);
        assertThat(statuses.get(0).isActiveLatestStatus()).isFalse();
        assertThat(statuses.get(0).getExpiryDate()).isCloseTo(LocalDateTime.now(), within(60, ChronoUnit.SECONDS));
        assertThat(statuses.get(1).getExpiryDate()).isNull();
        assertThat(statuses.get(1).isActiveLatestStatus()).isTrue();
    }

    @Nested
    public class ReleasePrisoner {

        @Test
        @DisplayName("requires ROLE_SMOKE_TEST")
        public void requiresCorrectRole() {
            final var request = createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE), null);

            final var response = testRestTemplate.exchange(
                "/api/smoketest/offenders/A1234AA/release",
                HttpMethod.PUT,
                request,
                Void.class
            );

            assertThat(response.getStatusCode()).isEqualTo(FORBIDDEN);
        }


        @Test
        @DisplayName("not found")
        public void notFound() {

            final var response = testRestTemplate.exchange(
                "/api/smoketest/offenders/NOT_AN_OFFENDER/release",
                HttpMethod.PUT,
                createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.SMOKE_TEST), null),
                Void.class
            );

            assertThat(response.getStatusCode()).isEqualTo(NOT_FOUND);
        }

        @Test
        @DisplayName("will release the prisoner")
        public void willReleasePrisoner() {

            final var response = testRestTemplate.exchange(
                "/api/smoketest/offenders/A1234AA/release",
                HttpMethod.PUT,
                createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.SMOKE_TEST), null),
                Void.class
            );
            assertThat(response.getStatusCode()).isEqualTo(OK);

            // TODO Remove this line - this is just to reset the imprisonment status back to its original
            // This can be removed once the smoke test is complete
            testRestTemplate.exchange(
                "/api/smoketest/offenders/A1234AA/recall",
                HttpMethod.PUT,
                createHttpEntity(authTokenHelper.getToken(AuthTokenHelper.AuthToken.SMOKE_TEST), null),
                Void.class
            );
        }
    }
}
