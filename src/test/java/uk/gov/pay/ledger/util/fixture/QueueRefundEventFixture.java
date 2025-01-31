package uk.gov.pay.ledger.util.fixture;

import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import com.amazonaws.services.sqs.AmazonSQS;
import com.google.common.collect.ImmutableMap;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.pay.ledger.event.model.Event;
import uk.gov.pay.ledger.event.model.ResourceType;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class QueueRefundEventFixture implements QueueFixture<QueueRefundEventFixture, Event> {
    private String sqsMessageId;
    private ResourceType resourceType = ResourceType.REFUND;
    private Long amount = 50L;
    private String resourceExternalId = RandomStringUtils.randomAlphanumeric(20);
    private String parentResourceExternalId = RandomStringUtils.randomAlphanumeric(20);
    private ZonedDateTime eventDate = ZonedDateTime.now(ZoneOffset.UTC);
    private String eventType = "REFUND_CREATED_BY_USER";
    private String eventData = "{\"event_data\": \"event data\"}";
    private String refundedBy = RandomStringUtils.randomAlphanumeric(20);
    private String reference = null;

    private QueueRefundEventFixture() {
    }

    public static QueueRefundEventFixture aQueueRefundEventFixture() {
        return new QueueRefundEventFixture();
    }

    public QueueRefundEventFixture withAmount(Long amount) {
        this.amount = amount;
        return this;
    }

    public QueueRefundEventFixture withResourceExternalId(String resourceExternalId) {
        this.resourceExternalId = resourceExternalId;
        return this;
    }

    public QueueRefundEventFixture withParentResourceExternalId(String parentResourceExternalId) {
        this.parentResourceExternalId = parentResourceExternalId;
        return this;
    }

    public QueueRefundEventFixture withEventDate(ZonedDateTime eventDate) {
        this.eventDate = eventDate;
        return this;
    }

    public QueueRefundEventFixture withEventType(String eventType) {
        this.eventType = eventType;
        return this;
    }

    public QueueRefundEventFixture withRefundedBy(String refundedBy) {
        this.refundedBy = refundedBy;
        return this;
    }

    public QueueRefundEventFixture withReference(String reference) {
        this.reference = reference;
        return this;
    }

    public QueueRefundEventFixture withDefaultEventDataForEventType(String eventType) {
        switch (eventType) {
            case "REFUND_CREATED_BY_SERVICE":
                eventData = new GsonBuilder().create()
                        .toJson(ImmutableMap.builder()
                                .put("amount", amount)
                                .build());
                break;
            case "REFUND_CREATED_BY_USER":
                eventData = new GsonBuilder().create()
                        .toJson(ImmutableMap.builder()
                                .put("amount", amount)
                                .put("refunded_by", refundedBy)
                                .build());
                break;
            case "REFUND_SUBMITTED":
                eventData = "{}";
                break;
            case "REFUND_SUCCEEDED" :
                eventData = new GsonBuilder().create()
                        .toJson(ImmutableMap.builder()
                                .put("reference", reference)
                                .build());
                break;
            default:
                eventData = new GsonBuilder().create()
                        .toJson(ImmutableMap.of("event_data", "event_data"));
        }
        return this;
    }

    @Override
    public Event toEntity() {
        return new Event(0L, sqsMessageId, resourceType, resourceExternalId, parentResourceExternalId, eventDate, eventType, eventData);
    }

    public String getSqsMessageId() {
        return sqsMessageId;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceExternalId() {
        return resourceExternalId;
    }

    public ZonedDateTime getEventDate() {
        return eventDate;
    }

    public String getEventType() {
        return eventType;
    }

    public String getEventData() {
        return eventData;
    }

    public String getRefundedBy() {
        return refundedBy;
    }

    public String getReference() {
        return reference;
    }

    @Override
    public QueueRefundEventFixture insert(AmazonSQS sqsClient) {
        this.sqsMessageId = QueueEventFixtureUtil.insert(sqsClient, eventType, eventDate, resourceExternalId,
                parentResourceExternalId, resourceType, eventData);
        return this;
    }

    public PactDslJsonBody getAsPact() {
        return QueueEventFixtureUtil.getAsPact(eventType, eventDate, resourceExternalId,
                parentResourceExternalId, resourceType, eventData);
    }

    public QueueRefundEventFixture withResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public String getParentResourceExternalId() {
        return parentResourceExternalId;
    }

    public Long getAmount() {
        return amount;
    }
}
