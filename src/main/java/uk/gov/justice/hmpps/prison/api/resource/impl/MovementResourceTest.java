package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

public class MovementResourceTest extends ResourceTest {

    @Test
    public void testReadTodaysMovementsForbidden() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/movements?fromDateTime={fromDateTime}",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<String>() {
                },
                LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );

        assertThatStatus(response, 403);
    }

    @Test
    public void testReadTodaysMovements() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.GLOBAL_SEARCH);

        final var response = testRestTemplate.exchange(
                "/api/movements?fromDateTime={fromDateTime}",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<String>() {
                },
                LocalDateTime.now().truncatedTo(ChronoUnit.DAYS).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            );

        assertThatStatus(response, 200);
        assertThatJson(response.getBody()).isEqualTo("[]");
    }

    @Test
    public void testReadRollcountByAgency() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/movements/rollcount/{agencyId}/movements",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<String>() {
                }, "LEI"
        );

        assertThatStatus(response, 200);
        assertThatJson(response.getBody()).isEqualTo("{\"in\":0,\"out\":0}");
    }

    @Test
    public void testReadTodaysMovementsByAgencyEnRoute() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/movements/{agencyId}/enroute",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<String>() {
                }, "LEI"
        );

        assertThatStatus(response, 200);
        assertThatJson(response.getBody()).isEqualTo("[{\"offenderNo\":\"A1183SH\",\"bookingId\":-44,\"dateOfBirth\":\"1980-01-02\",\"firstName\":\"SAM\",\"lastName\":\"HEMP\",\"fromAgency\":\"BMI\",\"fromAgencyDescription\":\"Birmingham\",\"toAgency\":\"LEI\",\"toAgencyDescription\":\"Leeds\",\"movementType\":\"TRN\",\"movementTypeDescription\":\"Transfers\",\"movementReason\":\"NOTR\",\"movementReasonDescription\":\"Normal Transfer\",\"directionCode\":\"OUT\",\"movementTime\":\"13:00:00\",\"movementDate\":\"2017-10-12\"},{\"offenderNo\":\"A1183AD\",\"bookingId\":-45,\"dateOfBirth\":\"1980-01-02\",\"firstName\":\"AMY\",\"lastName\":\"DENTON\",\"fromAgency\":\"BMI\",\"fromAgencyDescription\":\"Birmingham\",\"toAgency\":\"LEI\",\"toAgencyDescription\":\"Leeds\",\"movementType\":\"TRN\",\"movementTypeDescription\":\"Transfers\",\"movementReason\":\"NOTR\",\"movementReasonDescription\":\"Normal Transfer\",\"directionCode\":\"OUT\",\"movementTime\":\"15:00:00\",\"movementDate\":\"2017-10-12\"}]");
    }

    @Test
    public void testGetRolllcountByAgencyEnroute() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/movements/rollcount/{agencyId}/enroute",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<String>() {
                }, "LEI"

        );

        assertThatStatus(response, 200);
        assertThatJson(response.getBody()).isEqualTo("2");
    }

}
