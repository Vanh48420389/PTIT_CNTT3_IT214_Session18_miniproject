package ra.edu.rikkeibank.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ra.edu.rikkeibank.notification.entity.NotificationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private String accountNumber;
    private String title;
    private String message;
    private BigDecimal amount;
    private String transactionRef;
    private NotificationType type;
    private boolean isRead;
    private LocalDateTime createdAt;
}
