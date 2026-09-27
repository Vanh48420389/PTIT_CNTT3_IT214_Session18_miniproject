package ra.edu.rikkeibank.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.edu.rikkeibank.notification.dto.ApiResponse;
import ra.edu.rikkeibank.notification.dto.NotificationResponse;
import ra.edu.rikkeibank.notification.event.TransactionEvent;
import ra.edu.rikkeibank.notification.service.NotificationService;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/{accountNumber}")
    public Mono<ResponseEntity<ApiResponse<List<NotificationResponse>>>> getNotificationsByAccount(
            @PathVariable String accountNumber) {
        return notificationService.getNotificationsByAccount(accountNumber)
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Retrieved notifications for account " + accountNumber)));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<NotificationResponse>>>> getAllNotifications() {
        return notificationService.getAllNotifications()
                .collectList()
                .map(list -> ResponseEntity.ok(ApiResponse.success(list, "Retrieved all system notifications")));
    }

    @PutMapping("/{id}/read")
    public Mono<ResponseEntity<ApiResponse<NotificationResponse>>> markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id)
                .map(res -> ResponseEntity.ok(ApiResponse.success(res, "Notification marked as read")))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/mock-event")
    public Mono<ResponseEntity<ApiResponse<Void>>> simulateIncomingKafkaEvent(@RequestBody TransactionEvent event) {
        return Mono.fromRunnable(() -> notificationService.handleTransactionEvent(event))
                .thenReturn(ResponseEntity.ok(ApiResponse.success(null, "Mock Kafka event processed successfully")));
    }
}
