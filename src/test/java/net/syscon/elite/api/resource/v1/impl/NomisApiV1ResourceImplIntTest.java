package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.CreateTransaction;
import net.syscon.elite.api.model.v1.Image;
import net.syscon.elite.api.model.v1.Offender;
import net.syscon.elite.api.model.v1.OffenderPssDetailEvent;
import net.syscon.elite.api.resource.impl.ResourceTest;
import net.syscon.elite.repository.v1.model.AliasSP;
import net.syscon.elite.repository.v1.model.OffenderSP;
import net.syscon.elite.repository.v1.storedprocs.FinanceProcs.PostTransaction;
import net.syscon.elite.repository.v1.storedprocs.FinanceProcs.PostTransfer;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderDetails;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderImage;
import net.syscon.elite.repository.v1.storedprocs.OffenderProcs.GetOffenderPssDetail;
import net.syscon.elite.api.model.v1.Hold;
import net.syscon.elite.repository.v1.model.HoldSP;
import net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import oracle.sql.BlobDBAccess;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.Blob;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static net.syscon.elite.repository.v1.storedprocs.StoreProcMetadata.*;
import static org.assertj.core.api.Assertions.fail;
import static net.syscon.elite.repository.v1.storedprocs.FinanceProcs.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.core.ResolvableType.forType;



public class NomisApiV1ResourceImplIntTest extends ResourceTest {
    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        public PostTransaction postTransaction() {
            return Mockito.mock(PostTransaction.class);
        }

        @Bean
        @Primary
        public PostTransfer postTransfer() {
            return Mockito.mock(PostTransfer.class);
        }

        @Bean
        @Primary
        public GetOffenderPssDetail getOffenderPssDetail() {
            return Mockito.mock(GetOffenderPssDetail.class);
        }

        @Bean
        @Primary
        public GetOffenderDetails getOffenderDetails() {
            return Mockito.mock(GetOffenderDetails.class);
        }

        @Bean
        @Primary
        public GetOffenderImage getOffenderImage() {
            return Mockito.mock(GetOffenderImage.class);
        }

        @Bean
        @Primary
        public GetHolds getHolds() {
            return Mockito.mock(GetHolds.class);
        }
    }

    @Autowired
    private PostTransaction postTransaction;

    @Autowired
    private PostTransfer postTransfer;

    @Autowired
    private GetOffenderPssDetail offenderPssDetail;

    @Autowired
    private GetOffenderDetails offenderDetails;

    @Autowired
    private GetOffenderImage offenderImage;

    @Autowired
    private GetHolds getHolds;

    @Test
    public void transferTransaction() {
        final var transaction = new CreateTransaction();
        transaction.setAmount(1234L);
        transaction.setClientUniqueRef("clientRef");
        transaction.setDescription("desc");
        transaction.setType("type");
        transaction.setClientTransactionId("transId");

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);

        when(postTransfer.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_TXN_ID, "someId", P_TXN_ENTRY_SEQ, "someSeq", P_CURRENT_AGY_DESC, "someDesc", P_CURRENT_AGY_LOC_ID, "someLoc"));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transfer_transactions", HttpMethod.POST, requestEntity, String.class);

        assertThatJson(responseEntity.getBody()).isEqualTo("{current_location: {code: \"someLoc\", desc: \"someDesc\"}, transaction: {id:\"someId-someSeq\"}}");
    }

    @Test
    public void createTransaction() {
        final var transaction = new CreateTransaction();
        transaction.setAmount(1234L);
        transaction.setClientUniqueRef("clientRef");
        transaction.setDescription("desc");
        transaction.setType("type");
        transaction.setClientTransactionId("transId");

        final var requestEntity = createHttpEntityWithBearerAuthorisationAndBody("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), transaction);

        when(postTransaction.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_TXN_ID, "someId", P_TXN_ENTRY_SEQ, "someSeq"));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/transactions", HttpMethod.POST, requestEntity, String.class);

        assertThatJson(responseEntity.getBody()).isEqualTo("{id:\"someId-someSeq\"}");
    }

    @Test
    public void getOffenderPssDetail() throws SQLException {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var exampleJson = "{\n" +
                "        \"offender_details\": {\n" +
                "            \"personal_details\": {\n" +
                "                \"offender_surname\": \"ABDORIA\",\n" +
                "                \"offender_given_name_1\": \"ONGMETAIN\",\n" +
                "                \"offender_dob\": \"1990-12-06 00:00:00\",\n" +
                "                \"gender\": {\n" +
                "                    \"code\": \"M\",\n" +
                "                    \"desc\": \"Male\"\n" +
                "                },\n" +
                "                \"religion\": {\n" +
                "                    \"code\": \"NIL\",\n" +
                "                    \"desc\": \"EfJSmIEfJSm\"\n" +
                "                },\n" +
                "                \"security_category\": {\n" +
                "                    \"code\": \"C\",\n" +
                "                    \"desc\": \"Cat C\"\n" +
                "                },\n" +
                "                \"nationality\": {\n" +
                "                    \"code\": \"BRIT\",\n" +
                "                    \"desc\": \"sxiVsxi\"\n" +
                "                },\n" +
                "                \"ethnicity\": {\n" +
                "                    \"code\": \"W1\",\n" +
                "                    \"desc\": \"White: Eng./Welsh/Scot./N.Irish/British\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"sentence_information\": {\n" +
                "                \"reception_arrival_date_and_time\": \"2017-05-03 15:50:00\",\n" +
                "                \"status\": \"Convicted\",\n" +
                "                \"imprisonment_status\": {\n" +
                "                    \"code\": \"LR\",\n" +
                "                    \"desc\": \"Recalled to Prison from Parole (Non HDC)\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"location\": {\n" +
                "                \"agency_location\": \"LEI\",\n" +
                "                \"internal_location\": \"LEI-E-5-004\",\n" +
                "                \"location_type\": \"CELL\"\n" +
                "            },\n" +
                "            \"warnings\": [\n" +
                "                {\n" +
                "                    \"warning_type\": {\n" +
                "                        \"code\": \"P\",\n" +
                "                        \"desc\": \"MAPPP Case\"\n" +
                "                    },\n" +
                "                    \"warning_sub_type\": {\n" +
                "                        \"code\": \"P2\",\n" +
                "                        \"desc\": \"MAPPA Level 2 Case\"\n" +
                "                    },\n" +
                "                    \"warning_date\": \"2015-06-03 00:00:00\",\n" +
                "                    \"status\": \"ACTIVE\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"warning_type\": {\n" +
                "                        \"code\": \"R\",\n" +
                "                        \"desc\": \"Risk\"\n" +
                "                    },\n" +
                "                    \"warning_sub_type\": {\n" +
                "                        \"code\": \"RCS\",\n" +
                "                        \"desc\": \"Risk to Children - Custody\"\n" +
                "                    },\n" +
                "                    \"warning_date\": \"2013-06-04 00:00:00\",\n" +
                "                    \"status\": \"ACTIVE\"\n" +
                "                }\n" +
                "            ],\n" +
                "            \"entitlement\": {\n" +
                "                \"canteen_adjudication\": false,\n" +
                "                \"iep_level\": {\n" +
                "                    \"code\": \"STD\",\n" +
                "                    \"desc\": \"Standard\"\n" +
                "                }\n" +
                "            },\n" +
                "            \"case_details\": {\n" +
                "                \"personal_officer\": \"Griffine, Ymmnatpher\"\n" +
                "            }\n" +
                "        }\n" +
                "     }";

        final var testClob = new javax.sql.rowset.serial.SerialClob(exampleJson.toCharArray());
        final var timestamp = Timestamp.valueOf(LocalDateTime.now());
        final var localDateTime = LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
        final var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        final var expectedTime = formatter.format(localDateTime);

        final var procedureResponse = Map.of(
                P_NOMS_ID, (Object) "G7806VO",
                P_ROOT_OFFENDER_ID, (Object) 0L,
                P_SINGLE_OFFENDER_ID, (Object) "",
                P_AGY_LOC_ID, (Object)"LEI",
                P_DETAILS_CLOB, (Object) testClob,
                P_TIMESTAMP, (Object) timestamp);

        when(offenderPssDetail.execute(any(SqlParameterSource.class))).thenReturn(procedureResponse);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/G7806VO/pss_detail", HttpMethod.GET, requestEntity, OffenderPssDetailEvent.class);

        if (responseEntity.getStatusCodeValue()!= 200) {
            fail("PSS detail call failed. Response body : " + responseEntity.getBody());
            return;
        }

        final var actual = (OffenderPssDetailEvent) responseEntity.getBody();

        assertThat(actual.getNomsId()).isEqualTo("G7806VO");
        assertThat(actual.getPrisonId()).isEqualTo("LEI");
        assertThat(actual.getPssDetail()).isNotNull();
        assertThat(actual.getPssDetail().getOffenderDetails()).isNotNull();
        assertThat(actual.getPssDetail().getOffenderDetails().getWarningData()).hasSize(2);
    }

    @Test
    public void offenderDetail() {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);
        final var expectedSurname = "HALIBUT";
        final var procedureResponse = Map.of(P_OFFENDER_CSR, (Object) List.of(OffenderSP.builder().lastName(expectedSurname)
                .offenderAliases(List.of(AliasSP.builder().lastName("PLAICE").build()))
                .build()));

        when(offenderDetails.execute(any(SqlParameterSource.class))).thenReturn(procedureResponse);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE", HttpMethod.GET, requestEntity, Offender.class);

        if (responseEntity.getStatusCodeValue()!= 200) {
            fail("Offender detail failed. Response body : " + responseEntity.getBody());
            return;
        }

        final var offenderActual = (Offender) responseEntity.getBody();

        assertThat(offenderActual.getSurname()).isEqualToIgnoringCase(expectedSurname);
        assertThat(offenderActual.getAliases()).hasSize(1);
    }

    @Test
    public void offenderImage() throws SQLException {

        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        byte[] imageBytes = "XXX".getBytes();
        Blob blob = new javax.sql.rowset.serial.SerialBlob(imageBytes);
        final var procedureResponse = Map.of( P_IMAGE, (Object) blob);

        when(offenderImage.execute(any(SqlParameterSource.class))).thenReturn(procedureResponse);

        final var responseEntity = testRestTemplate.exchange("/api/v1/offenders/A1404AE/image", HttpMethod.GET, requestEntity, String.class);
        if (responseEntity.getStatusCodeValue()!= 200) {
            fail("offenderImage failed. Response body : " + responseEntity.getBody());
            return;
        }

        // Encoded image returns this value for the test XXX value used
        final var actualJson = responseEntity.getBody();
        assertThatJson(actualJson).isEqualTo("{\"image\":\"WFhY\"}");
    }

    @Test
    public void getHolds() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER", List.of("ROLE_NOMIS_API_V1"), null);

        final var holds = List.of(
                new HoldSP(3L, "ref", "12345", "entry", null, new BigDecimal("123.45"), null),
                new HoldSP(4L, "ref2", "12346", "entry2", LocalDate.of(2019, 1, 2), new BigDecimal("123.46"), LocalDate.of(2018, 12, 30))
        );

        when(getHolds.execute(any(SqlParameterSource.class))).thenReturn(Map.of(P_HOLDS_CSR, holds));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/holds", HttpMethod.GET, requestEntity, String.class);

        //noinspection ConstantConditions
        assertThat(new JsonContent<Hold>(getClass(), forType(Hold.class), responseEntity.getBody())).isEqualToJson("holds.json");
    }

    @Test
    public void getHoldsWithClientReference() {
        final var requestEntity = createHttpEntityWithBearerAuthorisation("ITAG_USER_ADM", List.of("ROLE_NOMIS_API_V1"), Map.of("X-Client-Name", "some-client"));

        final var holds = List.of(
                new HoldSP(3L, "ref", "12345", "entry", null, new BigDecimal("123.45"), null),
                new HoldSP(4L, "some-client-ref2", "12346", "entry2", LocalDate.of(2019, 1, 2), new BigDecimal("123.46"), LocalDate.of(2018, 12, 30))
        );

        final var captor = ArgumentCaptor.forClass(SqlParameterSource.class);
        when(getHolds.execute(captor.capture())).thenReturn(Map.of(P_HOLDS_CSR, holds));

        final var responseEntity = testRestTemplate.exchange("/api/v1/prison/CKI/offenders/G1408GC/holds?client_unique_ref=some-reference", HttpMethod.GET, requestEntity, String.class);

        //noinspection ConstantConditions
        assertThat(new JsonContent<Hold>(getClass(), forType(Hold.class), responseEntity.getBody())).isEqualToJson("holds.json");

        assertThat(captor.getValue().getValue(P_CLIENT_UNIQUE_REF)).isEqualTo("some-client-some-reference");
    }
}
