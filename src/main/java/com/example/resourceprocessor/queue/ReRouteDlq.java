package com.example.resourceprocessor.queue;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@RequiredArgsConstructor
@Configuration
public class ReRouteDlq {

    private static final String ORIGINAL_QUEUE = "resource-ids.resource-processor";
    private static final String DLQ = ORIGINAL_QUEUE + ".dlq";
    private static final String PARKING_LOT = ORIGINAL_QUEUE + ".parkingLot";
    private static final String X_RETRIES_HEADER = "x-retries";
    private static final String X_ORIGINAL_EXCHANGE_HEADER = RepublishMessageRecoverer.X_ORIGINAL_EXCHANGE;
    private static final String X_ORIGINAL_ROUTING_KEY_HEADER = RepublishMessageRecoverer.X_ORIGINAL_ROUTING_KEY;
    private static final int MAX_RETRIES = 2;

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = DLQ)
    public void rePublish(Message failedMessage) {
        Map<String, Object> headers = failedMessage.getMessageProperties().getHeaders();
        Integer retriesHeader = (Integer) headers.get(X_RETRIES_HEADER);
        if (retriesHeader == null) {
            retriesHeader = 0;
        }

        if (retriesHeader < MAX_RETRIES) {
            headers.put(X_RETRIES_HEADER, ++retriesHeader);
            String exchange = (String) headers.get(X_ORIGINAL_EXCHANGE_HEADER);
            String originalRoutingKey = (String) headers.get(X_ORIGINAL_ROUTING_KEY_HEADER);
            this.rabbitTemplate.send(exchange, originalRoutingKey, failedMessage);
        }
        else {
            this.rabbitTemplate.send(PARKING_LOT, failedMessage);
        }
    }

    @Bean
    public Queue parkingLot() {
        return new Queue(PARKING_LOT);
    }

}
