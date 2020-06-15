package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.CourtHearings;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

@ContextConfiguration(classes = OffenderMovementsResourceImplIntTest_cancelCourtHearing.TestClock.class)
public class OffenderMovementsResourceImplIntTest_cancelCourtHearing extends ResourceTest {

    @TestConfiguration
    static class TestClock {
        @Bean
        public Clock clock() {
            return Clock.fixed(Instant.now(), ZoneId.systemDefault());
        }
    }

    @Autowired
    private Clock clock;

    private String token;

    @Before
    public void setup() {
        token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);
    }

    @Test
    public void cancel_succeeds_of_future_scheduled_court_hearing() {
        final var futureScheduledHearing = givenScheduleHearing(token);

        final var courtHearingsBeforeCancellation = givenAllCourtHearings(token);

        assertThat(courtHearingsBeforeCancellation).containsOnlyOnce(futureScheduledHearing);

        cancel(futureScheduledHearing);

        final var courtHearingsAfterCancellation = givenAllCourtHearings(token);

        assertThat(courtHearingsAfterCancellation)
                .hasSize(courtHearingsBeforeCancellation.size() - 1)
                .doesNotContain(futureScheduledHearing);
    }

    @Test
    public void cancel_fails_when_booking_and_hearing_not_found() {
        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-hearings/-9999999/cancel",
                HttpMethod.DELETE,
                createHttpEntity(token, Map.of()),
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Court hearing '-9999999' with booking '-1' not found.")
                        .developerMessage("Court hearing '-9999999' with booking '-1' not found.")
                        .build());
    }

    @Test
    public void cancel_fails_when_hearing_not_in_future() {
        final var response = testRestTemplate.exchange(
                "/api/bookings/-8/court-hearings/-209/cancel",
                HttpMethod.DELETE,
                createHttpEntity(token, Map.of()),
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(400)
                        .userMessage("Court hearing '-209' cannot be deleted as its start date/time is in the past.")
                        .developerMessage("Court hearing '-209' cannot be deleted as its start date/time is in the past.")
                        .build());
    }

    @Test
    public void cancel_fails_when_not_authorised() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.RENEGADE_USER);

        final var response = testRestTemplate.exchange(
                "/api/bookings/-8/court-hearings/-209/cancel",
                HttpMethod.DELETE,
                createHttpEntity(token, null),
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(403)
                        .userMessage("Access is denied")
                        .build());
    }

    private void cancel(final CourtHearing hearing) {
        final var response = testRestTemplate.exchange(
                "/api/bookings/-1/court-hearings/" + hearing.getId() + "/cancel",
                HttpMethod.DELETE,
                createHttpEntity(token, Map.of()),
                new ParameterizedTypeReference<Void>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
    }

    private Collection<CourtHearing> givenAllCourtHearings(final String token) {
        final var courtHearings = testRestTemplate.exchange(
                "/api/bookings/-1/court-hearings",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<CourtHearings>() {
                }).getBody();

        assertThat(courtHearings).isNotNull();
        assertThat(courtHearings.getHearings().size()).isGreaterThanOrEqualTo(1);

        return courtHearings.getHearings();
    }

    private CourtHearing givenScheduleHearing(final String token) {
        return testRestTemplate.exchange(
                "/api/bookings/-1/prison-to-court-hearings",
                HttpMethod.POST,
                createHttpEntity(token, Map.of(
                        "fromPrisonLocation", "LEI",
                        "toCourtLocation", "COURT1",
                        "courtHearingDateTime", LocalDateTime.now(clock).plusDays(1).truncatedTo(ChronoUnit.MINUTES)
                )),
                new ParameterizedTypeReference<CourtHearing>() {
                }).getBody();
    }
}
