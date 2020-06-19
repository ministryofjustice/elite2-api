package net.syscon.elite.api.resource.impl;

import net.syscon.elite.api.model.Alert;
import net.syscon.elite.api.model.AlertChanges;
import net.syscon.elite.api.model.AlertCreated;
import net.syscon.elite.api.model.BookingActivity;
import net.syscon.elite.api.model.CreateAlert;
import net.syscon.elite.api.model.ErrorResponse;
import net.syscon.elite.api.model.InmateBasicDetails;
import net.syscon.elite.api.model.Movement;
import net.syscon.elite.api.model.UpdateAttendanceBatch;
import net.syscon.elite.executablespecification.steps.AuthTokenHelper.AuthToken;
import org.junit.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

@ContextConfiguration(classes = BookingResourceTest.TestClock.class)
public class BookingResourceTest extends ResourceTest {

    @TestConfiguration
    static class TestClock {
        @Bean
        public Clock clock() {
            return Clock.fixed(
                    LocalDateTime.of(2020, 1, 2, 3, 4, 5).atZone(ZoneId.systemDefault()).toInstant(),
                    ZoneId.systemDefault());
        }
    }

    @Test
    public void testGetBooking() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);
        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                -2);
        assertThatJsonFileAndStatus(response, 200, "booking_offender_-1.json");
    }

    @Test
    public void testThatUpdateAttendanceIsLockedDown_WhenPayRoleIsMissing() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var body = Map.of("eventOutcome", "ATT", "performance", "STANDARD");
        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/activities/{activityId}/attendance",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                -2, -11);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testUpdateAttendance_WithTheValidRole() {
        final var token = authTokenHelper.getToken(AuthToken.PAY);

        final var body = Map.of("eventOutcome", "ATT", "performance", "STANDARD");
        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/activities/{activityId}/attendance",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                -2, -11);

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void testUpdateAttendance_WithMultipleBookingIds() {
        final var token = authTokenHelper.getToken(AuthToken.PAY);

        final var body = UpdateAttendanceBatch
                .builder()
                .eventOutcome("ATT")
                .performance("STANDARD")
                .bookingActivities(Set.of(BookingActivity.builder().activityId(-11L).bookingId(-2L).build()))
                .build();

        final var httpEntity = createHttpEntity(token, body);

        final var response = testRestTemplate.exchange(
                "/api/bookings/activities/attendance",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void testCreateNewAlert_UnAuthorised() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var body = CreateAlert.builder().alertCode("X").alertType("XX").comment("XXX")
                .alertDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert",
                HttpMethod.POST,
                createHttpEntity(token, body),
                new ParameterizedTypeReference<ErrorResponse>() {
                }, -10L);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testUpdateAlert_UnAuthorised() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var body = AlertChanges.builder().expiryDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert/{alertSeq}",
                HttpMethod.PUT,
                createHttpEntity(token, body),
                new ParameterizedTypeReference<ErrorResponse>() {
                }, -1L, 4);

        assertThat(response.getStatusCodeValue()).isEqualTo(403);
    }

    @Test
    public void testUpdateAlert() {
        final var token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT);

        final var createdAlert = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert",
                HttpMethod.POST,
                createHttpEntity(token,
                        CreateAlert.builder()
                                .alertType("L")
                                .alertCode("LPQAA")
                                .comment("XXX")
                                .alertDate(LocalDate.now())
                                .build()),
                new ParameterizedTypeReference<Alert>() {
                }, -14L).getBody();

        final var body = AlertChanges.builder().expiryDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert/{alertSeq}",
                HttpMethod.PUT,
                createHttpEntity(token, body),
                new ParameterizedTypeReference<AlertCreated>() {
                }, -14L, createdAlert.getAlertId());

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void testUpdateAlert_CommentTextOnly() {
        final var token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT);

        final var createdAlert = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert",
                HttpMethod.POST,
                createHttpEntity(token,
                        CreateAlert.builder()
                                .alertType("L")
                                .alertCode("LPQAA")
                                .comment("XXX")
                                .alertDate(LocalDate.now())
                                .build()),
                new ParameterizedTypeReference<Alert>() {
                }, -14L).getBody();

        final var body = AlertChanges.builder().comment("New comment").build();

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert/{alertSeq}",
                HttpMethod.PUT,
                createHttpEntity(token, body),
                new ParameterizedTypeReference<AlertCreated>() {
                }, -14L, createdAlert.getAlertId());

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void testCreateNewAlert_BadRequest() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var body = CreateAlert.builder().build();

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert",
                HttpMethod.POST,
                createHttpEntity(token, body),
                new ParameterizedTypeReference<ErrorResponse>() {
                }, -10L);

        final var validationMessages = response.getBody().getUserMessage();

        assertThat(validationMessages).contains("alertType");
        assertThat(validationMessages).contains("alertCode");
        assertThat(validationMessages).contains("comment");
        assertThat(validationMessages).contains("alertDate");
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    public void testCreateNewAlert_MaximumLengths() {
        final var token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT);
        final var largeText = IntStream.range(1, 1002).mapToObj(i -> "A").collect(Collectors.joining(""));

        final var body = CreateAlert.builder()
                .alertCode(largeText.substring(0, 13))
                .alertType(largeText.substring(0, 13))
                .comment(largeText)
                .alertDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert",
                HttpMethod.POST,
                createHttpEntity(token, body),
                new ParameterizedTypeReference<ErrorResponse>() {
                }, -10L);

        final var validationMessages = response.getBody().getUserMessage();

        assertThat(validationMessages).contains("alertType");
        assertThat(validationMessages).contains("alertCode");
        assertThat(validationMessages).contains("comment");
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }


    @Test
    public void testCreateNewAlert() {
        final var token = authTokenHelper.getToken(AuthToken.UPDATE_ALERT);

        final var body = CreateAlert.builder().alertType("L").alertCode("LPQAA").comment("comments")
                .alertDate(LocalDate.now()).build();

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/alert",
                HttpMethod.POST,
                createHttpEntity(token, body),
                new ParameterizedTypeReference<AlertCreated>() {
                }, -10L);

        assertThat(response.getBody().getAlertId()).isGreaterThan(1);
        assertThat(response.getStatusCodeValue()).isEqualTo(201);
    }

    @Test
    public void testGetBasicInmateDetailsForOffendersActiveOnlyFalse() {
        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);

        final var body = List.of("Z0020ZZ");

        final var response = testRestTemplate.exchange(
                "/api/bookings/offenders?activeOnly=false",
                HttpMethod.POST,
                createHttpEntity(token, body),
                new ParameterizedTypeReference<List<InmateBasicDetails>>() {
                });

        assertThat(response.getBody().get(0).getBookingId()).isEqualTo(-20);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    public void testGetMovementForBooking() {
        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/movement/{sequenceNumber}",
                GET,
                createHttpEntity(token, null),
                Movement.class, "-29", "2");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();

        assertThat(response.getBody().getFromAgency()).isEqualTo("LEI");
        assertThat(response.getBody().getToAgency()).isEqualTo("BMI");
    }

    @Test
    public void testGetMovementForBookingNoResults() {
        final var token = authTokenHelper.getToken(AuthToken.SYSTEM_USER_READ_WRITE);

        final var response = testRestTemplate.exchange(
                "/api/bookings/{bookingId}/movement/{sequenceNumber}",
                GET,
                createHttpEntity(token, null),
                Movement.class, "-29", "999");

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    public void getMainOffence_testRetrieveSingleOffence() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/mainOffence", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, "-1");

        assertThatJsonFileAndStatus(response, 200, "offender_main_offence.json");
    }

    @Test
    public void getMainOffence_testRetrieveMultipleOffences() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/mainOffence", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, "-7");

        assertThatJsonFileAndStatus(response, 200, "offender_main_offences.json");
    }

    @Test
    public void getFullOffenderInformation() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}?extraInfo=true", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, "-7");

        assertThatJsonFileAndStatus(response, 200, "offender_extra_info.json");
    }

    @Test
    public void getFullOffenderInformation_byOffenderNo() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, "A1234AG");
        assertThatJsonFileAndStatus(response, 200, "offender_extra_info.json");
    }

    @Test
    public void getFullOffenderInformationNoCSRA_byOffenderNo() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, "A1184MA");
        assertThatJsonFileAndStatus(response, 200, "offender_extra_info_no_csra.json");
    }

    @Test
    public void getFullOffenderInformationPersonalCare_byOffenderNo() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}?extraInfo=true", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, "A1234AA");
        assertThatJsonFileAndStatus(response, 200, "offender_personal_care.json");
    }

    @Test
    public void getMainOffence_notFound() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/mainOffence", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, "-99");

        assertThatStatus(response, 404);
    }

    @Test
    public void getMainOffence_notInCaseload() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/mainOffence", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, "-16");

        assertThatStatus(response, 404);
    }

    @Test
    public void getMainOffence_noOffences() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/mainOffence", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, "-9");

        assertThatStatus(response, 200);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    public void getOffenceHistory_post() {
        final var response = testRestTemplate.exchange("/api/bookings/mainOffence", POST,
                createHttpEntity(AuthToken.SYSTEM_USER_READ_WRITE, "[-1, -7]"),
                String.class);

        assertThatJsonFileAndStatus(response, 200, "offender_main_offences_post.json");
    }

    @Test
    public void getOffenceHistory_post_no_offences() {
        final var response = testRestTemplate.exchange("/api/bookings/mainOffence", POST,
                createHttpEntity(AuthToken.SYSTEM_USER_READ_WRITE, "[ -98, -99 ]"),
                String.class);

        assertThatStatus(response, 200);
        assertThat(response.getBody()).isEqualTo("[]");
    }

    @Test
    public void getOffenceHistory() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}/offenceHistory", GET,
                createHttpEntity(AuthToken.CATEGORISATION_CREATE, null),
                String.class, "A1234AG");

        assertThatJsonFileAndStatus(response, 200, "offender_main_offences.json");
    }

    @Test
    public void getOffenceHistoryIncludeOffenderWithoutConviction() {
        final var response = testRestTemplate.exchange("/api/bookings/offenderNo/{offenderNo}/offenceHistory?convictionsOnly=false", GET,
                createHttpEntity(AuthToken.CATEGORISATION_CREATE, null),
                String.class, "A1234AB");

        assertThatJsonFileAndStatus(response, 200, "offender_offence_history_A12234AB_include_non_convictions.json");
    }

    @Test
    public void getSecondaryLanguages() {
        final var response = testRestTemplate.exchange("/api/bookings/{bookingId}/secondary-languages", GET,
                createHttpEntity(AuthToken.NORMAL_USER, null),
                String.class, -3L);

        assertThatJsonFileAndStatus(response, 200, "secondary_languages.json");
    }
}
