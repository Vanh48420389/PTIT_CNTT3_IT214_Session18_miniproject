package ra.edu.rikkeibank.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ra.edu.rikkeibank.notification.dto.NotificationResponse;
import ra.edu.rikkeibank.notification.entity.Notification;
import ra.edu.rikkeibank.notification.entity.NotificationType;
import ra.edu.rikkeibank.notification.event.TransactionEvent;
import ra.edu.rikkeibank.notification.repository.NotificationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void handleTransactionEvent(TransactionEvent event) {
        log.info("Processing Kafka transaction event: {} for Ref: {}", event.getEventType(), event.getTransactionRef());

        if ("TRANSACTION_COMPLETED".equalsIgnoreCase(event.getEventType())) {
            // 1. Notification for Source Account (Debit)
            Notification sourceNotif = Notification.builder()
                    .accountNumber(event.getSourceAccountNumber())
                    .title("Biến động số dư: - " + event.getAmount() + " VND")
                    .message("Quý khách đã chuyển thành công " + event.getAmount() + " VND đến tài khoản "
                            + event.getDestinationAccountNumber() + ". Nội dung: " + event.getMessage())
                    .amount(event.getAmount())
                    .transactionRef(event.getTransactionRef())
                    .type(NotificationType.BALANCE_DEBIT)
                    .build();
            notificationRepository.save(sourceNotif);

            // 2. Notification for Destination Account (Credit)
            Notification destNotif = Notification.builder()
                    .accountNumber(event.getDestinationAccountNumber())
                    .title("Biến động số dư: + " + event.getAmount() + " VND")
                    .message("Tài khoản của quý khách vừa được cộng " + event.getAmount() + " VND từ tài khoản "
                            + event.getSourceAccountNumber() + ". Nội dung: " + event.getMessage())
                    .amount(event.getAmount())
                    .transactionRef(event.getTransactionRef())
                    .type(NotificationType.BALANCE_CREDIT)
                    .build();
            notificationRepository.save(destNotif);

            log.info("Saved debit & credit notifications for txn: {}", event.getTransactionRef());

        } else if ("TRANSACTION_COMPENSATED".equalsIgnoreCase(event.getEventType())) {
            // Notification for Source Account (Saga Rollback Refund)
            Notification compNotif = Notification.builder()
                    .accountNumber(event.getSourceAccountNumber())
                    .title("Cảnh báo & Hoàn tiền Saga: + " + event.getAmount() + " VND")
                    .message("Giao dịch [" + event.getTransactionRef() + "] đến tài khoản "
                            + event.getDestinationAccountNumber() + " thất bại. Cơ chế Saga Rollback đã hoàn lại "
                            + event.getAmount() + " VND về tài khoản của quý khách an toàn.")
                    .amount(event.getAmount())
                    .transactionRef(event.getTransactionRef())
                    .type(NotificationType.SAGA_COMPENSATION_ALERT)
                    .build();
            notificationRepository.save(compNotif);

            log.info("Saved Saga compensation alert notification for txn: {}", event.getTransactionRef());
        }
    }

    public Flux<NotificationResponse> getNotificationsByAccount(String accountNumber) {
        return Mono.fromCallable(() -> notificationRepository.findByAccountNumberOrderByCreatedAtDesc(accountNumber))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(this::mapToResponse);
    }

    public Flux<NotificationResponse> getAllNotifications() {
        return Mono.fromCallable(notificationRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(this::mapToResponse);
    }

    public Mono<NotificationResponse> markAsRead(Long id) {
        return Mono.fromCallable(() -> {
            Notification notif = notificationRepository.findById(id).orElse(null);
            if (notif != null) {
                notif.setRead(true);
                return notificationRepository.save(notif);
            }
            return null;
        }).subscribeOn(Schedulers.boundedElastic())
        .map(this::mapToResponse);
    }

    private NotificationResponse mapToResponse(Notification n) {
        if (n == null) return null;
        return NotificationResponse.builder()
                .id(n.getId())
                .accountNumber(n.getAccountNumber())
                .title(n.getTitle())
                .message(n.getMessage())
                .amount(n.getAmount())
                .transactionRef(n.getTransactionRef())
                .type(n.getType())
                .isRead(n.isRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
