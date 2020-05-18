package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.DataDuplicateResult;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderDeletionComplete;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.Booking;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletion.OffenderWithBookings;
import uk.gov.justice.hmpps.nomis.datacompliance.events.publishers.dto.OffenderPendingDeletionReferralComplete;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataComplianceEventPusherTest {

    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private AmazonSQS client;

    private DataComplianceEventPusher eventPusher;

    @BeforeEach
    void setUp() {
        eventPusher = new DataComplianceAwsEventPusher(client, "queue.url", OBJECT_MAPPER);
    }

    @Test
    void sendPendingDeletionEvent() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.send(OffenderPendingDeletion.builder()
                .offenderIdDisplay("offender1")
                .firstName("Bob")
                .middleName("Middle")
                .lastName("Jones")
                .birthDate(LocalDate.of(1990, 1, 2))
                .offender(OffenderWithBookings.builder()
                        .offenderId(123L)
                        .booking(new Booking(321L))
                        .build())
                .build());

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody()).isEqualTo(
                "{" +
                        "\"offenderIdDisplay\":\"offender1\"," +
                        "\"firstName\":\"Bob\"," +
                        "\"middleName\":\"Middle\"," +
                        "\"lastName\":\"Jones\"," +
                        "\"birthDate\":\"1990-01-02\"," +
                        "\"offenders\":[{\"offenderId\":123,\"bookings\":[{\"offenderBookId\":321}]}]" +
                "}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION");
    }

    @Test
    void sendReferralCompleteEvent() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.send(new OffenderPendingDeletionReferralComplete(123L));

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody()).isEqualTo("{\"batchId\":123}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION-REFERRAL-COMPLETE");
    }

    @Test
    void sendDeletionCompleteEvent() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.send(new OffenderDeletionComplete("offender1", 123L));

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody()).isEqualTo("{\"offenderIdDisplay\":\"offender1\",\"referralId\":123}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_OFFENDER-DELETION-COMPLETE");
    }

    @Test
    void sendDataDuplicateResult() {

        final var request = ArgumentCaptor.forClass(SendMessageRequest.class);

        when(client.sendMessage(request.capture()))
                .thenReturn(new SendMessageResult().withMessageId("message1"));

        eventPusher.send(DataDuplicateResult.builder()
                .offenderIdDisplay("offender1")
                .retentionCheckId(123L)
                .duplicateOffender("offender2")
                .build());

        assertThat(request.getValue().getQueueUrl()).isEqualTo("queue.url");
        assertThat(request.getValue().getMessageBody())
                .isEqualTo("{\"offenderIdDisplay\":\"offender1\",\"retentionCheckId\":123,\"duplicateOffenders\":[\"offender2\"]}");
        assertThat(request.getValue().getMessageAttributes().get("eventType").getStringValue())
                .isEqualTo("DATA_COMPLIANCE_DATA-DUPLICATE-RESULT");
    }

    @Test
    void sendEventPropagatesException() {
        when(client.sendMessage(any())).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> eventPusher.send(
                OffenderPendingDeletion.builder().offenderIdDisplay("offender1").build()))
                .isInstanceOf(RuntimeException.class);
    }
}
