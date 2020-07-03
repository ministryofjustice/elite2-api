package net.syscon.prison.api.resource.impl;

import net.syscon.prison.executablespecification.steps.AuthTokenHelper;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

public class ReferenceDataResourceTest extends ResourceTest {
    @Test
    public void testCreateANewSubReferenceType() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token,
                "{" +
                        "    \"description\": \"TASK_TEST1\"," +
                        "    \"expiredDate\": \"2018-07-19\"," +
                        "    \"activeFlag\": \"N\"," +
                        "    \"listSeq\": 88," +
                        "    \"parentCode\": \"GEN\"," +
                        "    \"parentDomain\": \"TASK_TYPE\"" +
                        "}");

        final var response = testRestTemplate.exchange(
                "/api/reference-domains/domains/{domain}/codes/{code}",
                HttpMethod.POST,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "TASK_SUBTYPE", "TEST1");

        assertThatStatus(response, 200);

        assertThatJson(response.getBody()).isEqualTo("{domain:\"TASK_SUBTYPE\",code:\"TEST1\",description:\"TASK_TEST1\",parentDomain:\"TASK_TYPE\",parentCode:\"GEN\",activeFlag:\"N\",listSeq:88,systemDataFlag:\"Y\",expiredDate:\"2018-07-19\",\"subCodes\":[]}");
    }

    @Test
    public void testUpdateASubReferenceTypeToActive() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token,
                "{" +
                        "    \"description\": \"Amended Type\"," +
                        "    \"activeFlag\": \"Y\"," +
                        "    \"systemDataFlag\": \"N\"," +
                        "    \"listSeq\": 999," +
                        "    \"parentCode\": \"ATR\"," +
                        "    \"parentDomain\": \"TASK_TYPE\"" +
                        "}");

        final var response = testRestTemplate.exchange(
                "/api/reference-domains/domains/{domain}/codes/{code}",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "TASK_SUBTYPE", "ATRCC");

        assertThatStatus(response, 200);

        assertThatJson(response.getBody()).isEqualTo("{domain:\"TASK_SUBTYPE\",code:\"ATRCC\",description:\"Amended Type\",parentDomain:\"TASK_TYPE\",parentCode:\"ATR\",activeFlag:\"Y\",listSeq:999,systemDataFlag:\"N\",\"subCodes\":[]}");
    }

    @Test
    public void testUpdateASubReferenceTypeToInactive() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.SYSTEM_USER_READ_WRITE);

        final var httpEntity = createHttpEntity(token,
                "{" +
                        "    \"description\": \"Alcohol Rehab - community -changed\"," +
                        "    \"activeFlag\": \"N\"," +
                        "    \"systemDataFlag\": \"Y\"," +
                        "    \"expiredDate\": \"2019-07-19\"," +
                        "    \"listSeq\": 10," +
                        "    \"parentCode\": \"ATR\"," +
                        "    \"parentDomain\": \"TASK_TYPE\"" +
                        "}");

        final var response = testRestTemplate.exchange(
                "/api/reference-domains/domains/{domain}/codes/{code}",
                HttpMethod.PUT,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                },
                "TASK_SUBTYPE", "AREH-C");

        assertThatStatus(response, 200);

        assertThatJson(response.getBody()).isEqualTo("{domain:\"TASK_SUBTYPE\",code:\"AREH-C\",description:\"Alcohol Rehab - community -changed\",parentDomain:\"TASK_TYPE\",parentCode:\"ATR\",activeFlag:\"N\",listSeq:10,expiredDate: \"2019-07-19\",systemDataFlag:\"Y\",\"subCodes\":[]}");
    }

    @Test
    public void testReadDomainInformation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/reference-domains/domains/{domain}",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<String>() {
                },
                "ADDRESS_TYPE");

        assertThatStatus(response, 200);
    }

    @Test
    public void testReadDomainCodeInformation() {
        final var token = authTokenHelper.getToken(AuthTokenHelper.AuthToken.NORMAL_USER);

        final var response = testRestTemplate.exchange(
                "/api/reference-domains/domains/{domain}/codes/{code}",
                HttpMethod.GET,
                createHttpEntity(token, null),
                new ParameterizedTypeReference<String>() {
                },
                "ADDRESS_TYPE", "ROTL");

        assertThatStatus(response, 200);
        assertThatJson(response.getBody()).isEqualTo("{\"activeFlag\":\"Y\",\"code\":\"ROTL\",\"description\":\"Release on Temporary Licence\",\"domain\":\"ADDRESS_TYPE\",\"listSeq\":8,\"subCodes\":[],\"systemDataFlag\":\"N\"}");

    }
}