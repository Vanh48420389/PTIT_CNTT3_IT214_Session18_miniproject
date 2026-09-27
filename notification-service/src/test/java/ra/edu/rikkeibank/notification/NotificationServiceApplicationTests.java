package ra.edu.rikkeibank.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ra.edu.rikkeibank.notification.event.TransactionEvent;
import ra.edu.rikkeibank.notification.service.NotificationService;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class NotificationServiceApplicationTests {

    @Autowired
    private NotificationService notificationService;

    @Test
    void contextLoads() {
        assertNotNull(notificationService);
    }

    @Test
    void testProcessCompletedEvent() {
        TransactionEvent event = TransactionEvent.builder()
                .eventType("TRANSACTION_COMPLETED")
                .transactionRef("TXN-TEST-001")
                .sourceAccountNumber("1000000001")
                .destinationAccountNumber("1000000002")
                .amount(new BigDecimal("500000"))
                .message("Test WebFlux Notif")
                .build();

        notificationService.handleTransactionEvent(event);

        StepVerifier.create(notificationService.getNotificationsByAccount("1000000001"))
                .expectNextMatches(n -> n.getAmount().compareTo(new BigDecimal("500000")) == 0)
                .thenCancel()
                .verify();
    }
}
