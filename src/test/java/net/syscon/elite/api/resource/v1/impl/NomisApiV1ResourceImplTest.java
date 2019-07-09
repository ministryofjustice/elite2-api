package net.syscon.elite.api.resource.v1.impl;

import net.syscon.elite.api.model.v1.*;
import net.syscon.elite.repository.v1.model.TransferSP;
import net.syscon.elite.repository.v1.model.TransferSP.TransactionSP;
import net.syscon.elite.service.v1.NomisApiV1Service;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NomisApiV1ResourceImplTest {
    @Mock
    private NomisApiV1Service service;

    private NomisApiV1ResourceImpl nomisApiV1Resource;

    @Before
    public void setUp() {
        nomisApiV1Resource = new NomisApiV1ResourceImpl(service);
    }

    @Test
    public void transferTransaction() {
        when(service.transferTransaction(anyString(), anyString(), anyString(), anyString(), any(), any(), anyString(), anyString())).
                thenReturn(new TransferSP(CodeDescription.safeNullBuild("someCode", "desc"), new TransactionSP("someId")));
        final var createTransaction = new CreateTransaction();
        createTransaction.setAmount(1234L);
        createTransaction.setClientUniqueRef("clientRef");
        createTransaction.setDescription("desc");
        createTransaction.setType("type");
        createTransaction.setClientTransactionId("transId");

        final var transfer = nomisApiV1Resource.transferTransaction("client", "previous", "nomis", createTransaction);
        assertThat(transfer).isEqualTo(new Transfer(CodeDescription.safeNullBuild("someCode", "desc"), new Transaction("someId")));
    }

    @Test
    public void createTransaction() {
        when(service.createTransaction(anyString(), anyString(), anyString(), anyString(), any(), any(), anyString(), anyString())).
                thenReturn("someId");
        final var createTransaction = new CreateTransaction();
        createTransaction.setAmount(1234L);
        createTransaction.setClientUniqueRef("clientRef");
        createTransaction.setDescription("desc");
        createTransaction.setType("type");
        createTransaction.setClientTransactionId("transId");

        final var transfer = nomisApiV1Resource.createTransaction("client", "previous", "nomis", createTransaction);
        assertThat(transfer).isEqualTo(new Transaction("someId"));
    }

    @Test
    public void offenderPssDetail() {


        final var pssDetailData = PssDetailData.builder().personalData(PssPersonalData.builder().surname("SMITH").build()).build();
        final var pssOffenderDetail = PssOffenderDetail.builder().offenderDetails(pssDetailData).build();

        final var pssEventData = OffenderPssDetailEvent.builder()
                .eventType("offender_details_request")
                .id(0L)
                .eventTimeStamp(LocalDateTime.now())
                .prisonId("MDI")
                .nomsId("A1404AE")
                .pssDetail(pssOffenderDetail)
                .build();

        when(service.getOffenderPssDetail(anyString())).thenReturn(pssEventData);

        final var event = nomisApiV1Resource.getOffenderPssDetail("A1404AE");

        assertThat(event).extracting("id","nomsId","prisonId","eventType").contains(0L,"A1404AE","MDI","offender_details_request");

        verify(service).getOffenderPssDetail(anyString());
        verifyNoMoreInteractions(service);
    }

    @Test
    public void offenderDetail() {

        when(service.getOffender(anyString())).thenReturn(Offender.builder().nomsId("A1404AE").build());

        final var offender = nomisApiV1Resource.getOffender("A1404AE");

        assertThat(offender).extracting("nomsId").contains("A1404AE");

        verify(service).getOffender(anyString());
        verifyNoMoreInteractions(service);
    }

    @Test
    public void offenderImage() {

        when(service.getOffenderImage(anyString())).thenReturn(Image.builder().image("ABCDEFGHI").build());

        final var event = nomisApiV1Resource.getOffenderImage("A1404AE");

        assertThat(event).extracting("image").contains("ABCDEFGHI");

        verify(service).getOffenderImage(anyString());
        verifyNoMoreInteractions(service);
    }

    @Test    
    public void getHolds() {
        final var holds = List.of(new Hold(3L, "ref", "12345", "entry", null, 12345L, null));
        when(service.getHolds(anyString(), anyString(), anyString(), anyString())).thenReturn(holds);
        final var transfer = nomisApiV1Resource.getHolds("client", "prison", "nomis", "ref");
        assertThat(transfer).isEqualTo(holds);
    }

    @Test
    public void getEvents() {
        final var events = List.of(new Event("EVENT", 3L, "noms", "prison", LocalDateTime.now(), "entry"));
        when(service.getEvents(anyString(), any(), anyString(), any(), anyLong())).thenReturn(events);
        final var transfer = nomisApiV1Resource.getOffenderEvents("client", null, "nomis", null, 50L);
        assertThat(transfer).isEqualTo(new Events(events));
    }

    @Test
    public void getLiveRoll() {
        final var liveRoll = List.of("bob", "joe");
        when(service.getLiveRoll(anyString())).thenReturn(liveRoll);
        final var roll = nomisApiV1Resource.getLiveRoll("any");
        assertThat(roll).isEqualTo(new LiveRoll(liveRoll));
    }
}
