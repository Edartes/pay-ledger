package uk.gov.pay.ledger.rule;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

public class SqsTestDocker {
    private static final Logger logger = LoggerFactory.getLogger(SqsTestDocker.class);

    private static GenericContainer sqsContainer;

    public static AmazonSQS initialise(String... queues) {
        try {
            createContainer();
            return createQueues(queues);
        } catch (Exception e) {
            logger.error("Exception initialising SQS Container - {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private static void createContainer() {
        if (sqsContainer == null) {
            sqsContainer = new GenericContainer("roribio16/alpine-sqs")
                    .withExposedPorts(9324)
                    .waitingFor(Wait.forHttp("/?Action=GetQueueUrl&QueueName=default"));

            sqsContainer.start();
        }
    }

    public static void stopContainer() {
        sqsContainer.stop();
        sqsContainer = null;
    }

    private static AmazonSQS createQueues(String... queues) {
        AmazonSQS amazonSQS = getSqsClient();
        if (queues != null) {
            for (String queue : queues) {
                amazonSQS.createQueue(queue);
            }
        }

        return amazonSQS;
    }

    public static String getQueueUrl(String queueName) {
        return getEndpoint() + "/queue/" + queueName;
    }

    public static String getEndpoint() {
        return "http://localhost:" + sqsContainer.getMappedPort(9324);
    }

    private static AmazonSQS getSqsClient() {
        // random credentials required by AWS SDK to build SQS client
        BasicAWSCredentials awsCreds = new BasicAWSCredentials("x", "x");

        return AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                getEndpoint(),
                                "region-1"
                        ))
                .withRequestHandlers()
                .build();
    }
}
