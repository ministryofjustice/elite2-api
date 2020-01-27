package net.syscon.elite.api.resource.impl;

import net.syscon.elite.executablespecification.steps.AuthTokenHelper.AuthToken;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class PrisonersResourceTest extends ResourceTest {
    @Test
    public void testCanFindMultiplePrisonersUsingPost() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, "{ \"offenderNos\": [ \"A1181MV\", \"A1234AC\", \"A1234AA\" ] }");

        final var response = testRestTemplate.exchange(
                "/api/prisoners",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "prisoners_multiple.json");
    }

    @Test
    public void testCanFindMulitplePrisonersAndFilterByMoreThanOneCriteria() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, "{ \"offenderNos\": [ \"A1181MV\", \"A1234AC\", \"A1234AA\" ], \"lastName\": \"BATES\" }");

        final var response = testRestTemplate.exchange(
                "/api/prisoners",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "prisoners_single.json");
    }

    @Test
    public void testCanReturnPrisonerInformationAtLocation() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, null, Map.of("Page-Limit", "30"));

        final var response = testRestTemplate.exchange(
                "/api/prisoners/at-location/LEI",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "prisoners_information.json");
    }

    @Test
    public void testCanReturnPrisonerInformationByEstablishmentWithSort() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, null, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/prisoners/by-establishment/LEI?size=5&page=2&sort=lastName,asc&sort=givenName1,asc",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "prisoners_information_paged.json");
    }

    @Test
    public void testCanReturnPrisonerInformationByNomsId() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, null, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/prisoners/A1234AA/full-status",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "prisoners_information_A1234AA.json");
    }

    @Test
    public void testReturn404WhenOffenderNotFound() {
        final var token = authTokenHelper.getToken(AuthToken.GLOBAL_SEARCH);

        final var httpEntity = createHttpEntity(token, null, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/prisoners/X1111XX/full-status",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void testReturn404WhenDoesNotHavePrivs() {
        final var token = authTokenHelper.getToken(AuthToken.NO_CASELOAD_USER);

        final var httpEntity = createHttpEntity(token, null, Map.of());

        final var response = testRestTemplate.exchange(
                "/api/prisoners/A1234AA/full-status",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatStatus(response, HttpStatus.NOT_FOUND.value());
    }

}
