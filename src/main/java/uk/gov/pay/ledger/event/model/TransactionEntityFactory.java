package uk.gov.pay.ledger.event.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.pay.ledger.transaction.entity.TransactionEntity;
import uk.gov.pay.ledger.transaction.search.model.ConvertedTransactionDetails;
import uk.gov.pay.ledger.transaction.state.TransactionState;

import java.util.Map;

public class TransactionEntityFactory {

    private ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionEntityFactory.class);

    @Inject
    public TransactionEntityFactory(ObjectMapper objectMapper){
        this.objectMapper = objectMapper;
    }

    public TransactionEntity create(EventDigest eventDigest) {
        String transactionDetail = convertToTransactionDetails(eventDigest.getEventPayload());
        TransactionEntity entity = objectMapper.convertValue(eventDigest.getEventPayload(), TransactionEntity.class);
        entity.setTransactionDetails(transactionDetail);
        entity.setEventCount(eventDigest.getEventCount());
        entity.setState(TransactionState.fromEventType(eventDigest.getMostRecentSalientEventType()).getState());
        entity.setCreatedDate(eventDigest.getEventCreatedDate());
        entity.setExternalId(eventDigest.getResourceExternalId());
        entity.setParentExternalId(eventDigest.getParentResourceExternalId());
        entity.setTransactionType(eventDigest.getResourceType().toString());

        return entity;
    }

    private String convertToTransactionDetails(Map<String, Object> transactionPayload) {
        ConvertedTransactionDetails details = objectMapper.convertValue(transactionPayload, ConvertedTransactionDetails.class);
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to parse incoming event payload: {}", e.getMessage());
        }
        return "{}";
    }
}
