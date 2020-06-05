package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.CourtHearing;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;

public class OffenderMovementsResourceImplIntTest_scheduleCourtCaseHearing extends ResourceTest {

    @Test
    public void schedules_court_case_hearing() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z",
                "comments", "some comments"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<CourtHearing>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(CREATED);
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getDateTime()).isEqualTo("2030-03-11T14:00:00");
        assertThat(response.getBody().getLocation().isActive()).isEqualTo(true);
        assertThat(response.getBody().getLocation().getAgencyId()).isEqualTo("COURT1");
        assertThat(response.getBody().getLocation().getAgencyType()).isEqualTo("CRT");
        assertThat(response.getBody().getLocation().getDescription()).isEqualTo("Court 1");
    }

    @Test
    public void schedules_court_case_hearing_fails_when_no_matching_booking() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/9999999/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Offender booking with id 9999999 not found.")
                        .developerMessage("Offender booking with id 9999999 not found.")
                        .build());
    }

    @Test
    public void schedules_court_case_hearing_fails_when_no_matching_prison() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "PRISON",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Prison with id PRISON not found.")
                        .developerMessage("Prison with id PRISON not found.")
                        .build());
    }

    @Test
    public void schedules_court_case_hearing_fails_when_no_matching_court() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Court with id COURT not found.")
                        .developerMessage("Court with id COURT not found.")
                        .build());
    }

    @Test
    public void schedules_court_case_hearing_fails_when_no_matching_case_id() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/8888888/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(404)
                        .userMessage("Court case with id 8888888 not found.")
                        .developerMessage("Court case with id 8888888 not found.")
                        .build());
    }

    @Test
    public void schedules_court_case_hearing_fails_when_unauthorised() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.RENEGADE_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        assertThat(response.getBody()).isEqualTo(
                ErrorResponse.builder()
                        .status(403)
                        .userMessage("Access is denied")
                        .build());
    }

    @Test
    public void schedules_court_case_hearing_fails_when_prison_not_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("The from prison location must be provided");
    }

    @Test
    public void schedules_court_case_hearing_fails_when_court_not_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("The court location to be moved to must be provided");
    }

    @Test
    public void schedules_court_case_hearing_fails_when_date_not_supplied() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("The future court hearing date time must be provided");
    }

    @Test
    public void schedules_court_case_hearing_fails_when_prison_longer_than_6_chars() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "PRISONx",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("From location must be a maximum of 6 characters");
    }

    @Test
    public void schedules_court_case_hearing_fails_when_court_longer_than_6_chars() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "PRISON",
                "toCourtLocation", "COURT1x",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z"
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("To location must be a maximum of 6 characters");
    }

    @Test
    public void schedules_court_case_hearing_fails_when_comments_longer_than_240_chars() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var request = createHttpEntity(token, Map.of(
                "fromPrisonLocation", "LEI",
                "toCourtLocation", "COURT1",
                "courtHearingDateTime", "2030-03-11T14:00:00.000Z",
                "comments", StringUtils.repeat("a", 241)
        ));

        final var response = testRestTemplate.exchange(
                "/api/bookings/-2/court-cases/-2/prison-to-court-hearings",
                HttpMethod.POST,
                request,
                ErrorResponse.class);

        final var error = response.getBody();

        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getUserMessage()).contains("Comment text must be a maximum of 240 characters");
    }
}
