package ra.edu.rikkeibank.notification.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import ra.edu.rikkeibank.notification.event.TransactionEvent;
import ra.edu.rikkeibank.notification.service.NotificationService;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(topics = "transaction-events", groupId = "notification-group",
            autoStartup = "${spring.kafka.consumer.auto-startup:true}")
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info("Received event from Kafka topic [transaction-events]: {}", event);
        try {
            notificationService.handleTransactionEvent(event);
        } catch (Exception e) {
            log.error("Error consuming transaction event: {}", e.getMessage(), e);
        }
    }
}
