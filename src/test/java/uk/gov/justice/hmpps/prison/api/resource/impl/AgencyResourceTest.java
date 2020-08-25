package uk.gov.justice.hmpps.prison.api.resource.impl;

import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentType;
import uk.gov.justice.hmpps.prison.api.model.AgencyEstablishmentTypes;
import uk.gov.justice.hmpps.prison.executablespecification.steps.AuthTokenHelper.AuthToken;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

public class AgencyResourceTest extends ResourceTest {

    @Test
    public void testCanFindAgenciesByType() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/type/INST",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });

        assertThatJsonFileAndStatus(response, 200, "agencies_by_type.json");
    }

    @Test
    public void testCanFindAgenciesByTypePlusInactive() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/type/INST?activeOnly={activeOnly}",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                }, "false");

        assertThatJsonFileAndStatus(response, 200, "inactive_agencies_by_type.json");
    }

    @Test
    public void testCanFindCellsWithCapacity() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/LEI/cellsWithCapacity",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });
        System.out.println(response);
        assertThatJsonFileAndStatus(response, 200, "cells_with_capacity.json");
    }

    @Test
    public void testCanFindCellsWithCapacity_filtered() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/LEI/cellsWithCapacity?attribute=DO",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<String>() {
                });
        System.out.println(response);
        assertThatJsonFileAndStatus(response, 200, "cells_with_capacity_filtered.json");
    }

    @Test
    public void testEstablishmentTypesForMoorlandPrison() {
        final var token = authTokenHelper.getToken(AuthToken.NORMAL_USER);

        final var httpEntity = createHttpEntity(token, null);

        final var response = testRestTemplate.exchange(
                "/api/agencies/MDI/establishment-types",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<AgencyEstablishmentTypes>() {
                });

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody().getAgencyId()).isEqualTo("MDI");
        assertThat(response.getBody().getEstablishmentTypes()).containsExactlyInAnyOrder(
                AgencyEstablishmentType.builder().code("CM").description("Closed (Male)").build(),
                AgencyEstablishmentType.builder().code("CNOMIS").description("C-NOMIS Establishment").build(),
                AgencyEstablishmentType.builder().code("IM").description("Closed Young Offender Institute (Male)").build());
    }
}
