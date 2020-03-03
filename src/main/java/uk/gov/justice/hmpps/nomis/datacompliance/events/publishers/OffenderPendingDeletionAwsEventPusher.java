package uk.gov.justice.hmpps.nomis.datacompliance.events.publishers;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionEvent;
import uk.gov.justice.hmpps.nomis.datacompliance.events.dto.OffenderPendingDeletionReferralCompleteEvent;

import java.util.Map;

@Slf4j
@Component
@ConditionalOnExpression("{'aws', 'localstack'}.contains('${data.compliance.outbound.referral.sqs.provider}')")
public class OffenderPendingDeletionAwsEventPusher implements OffenderPendingDeletionEventPusher {

    private final ObjectMapper objectMapper;
    private final AmazonSQS sqsClient;
    private final String queueUrl;

    public OffenderPendingDeletionAwsEventPusher(
            @Autowired @Qualifier("outboundReferralSqsClient") final AmazonSQS sqsClient,
            @Value("${data.compliance.outbound.referral.sqs.queue.url}") final String queueUrl,
            final ObjectMapper objectMapper) {

        log.info("Configured to push offender pending deletion events to SQS queue: {}", queueUrl);

        this.objectMapper = objectMapper;
        this.sqsClient = sqsClient;
        this.queueUrl = queueUrl;
    }

    @Override
    public void sendPendingDeletionEvent(final OffenderPendingDeletionEvent event) {

        log.trace("Sending referral of offender pending deletion: {}", event.getOffenderIdDisplay());

        sqsClient.sendMessage(generatePendingDeletionRequest(event));
    }

    @Override
    public void sendReferralCompleteEvent(final OffenderPendingDeletionReferralCompleteEvent event) {

        log.trace("Sending process completed event for request: {}", event.getRequestId());

        sqsClient.sendMessage(generateReferralCompleteRequest(event));
    }

    private SendMessageRequest generatePendingDeletionRequest(final OffenderPendingDeletionEvent event) {
        return generateRequest("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION", event);
    }

    private SendMessageRequest generateReferralCompleteRequest(final OffenderPendingDeletionReferralCompleteEvent event) {
        return generateRequest("DATA_COMPLIANCE_OFFENDER-PENDING-DELETION-REFERRAL-COMPLETE", event);
    }

    private SendMessageRequest generateRequest(final String eventType, final Object messageBody) {
        return new SendMessageRequest()
                .withQueueUrl(queueUrl)
                .withMessageAttributes(Map.of(
                        "eventType", stringAttribute(eventType),
                        "contentType", stringAttribute("application/json;charset=UTF-8")))
                .withMessageBody(toJson(messageBody));
    }

    private MessageAttributeValue stringAttribute(final String value) {
        return new MessageAttributeValue()
                .withDataType("String")
                .withStringValue(value);
    }

    private String toJson(final Object event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
