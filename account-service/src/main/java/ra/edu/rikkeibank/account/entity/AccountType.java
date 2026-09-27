package ra.edu.rikkeibank.account.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String typeCode; // CHECKING, SAVINGS, SALARY, CREDIT

    @Column(nullable = false)
    private String typeName;

    private String description;

    @Column(nullable = false)
    private Double interestRate; // e.g. 0.05 (5% / year)

    @Column(nullable = false)
    private BigDecimal minBalance; // e.g. 50000 VND

    @Column(nullable = false)
    private BigDecimal dailyTransferLimit; // e.g. 100000000 VND

    @Column(nullable = false, length = 10)
    @Builder.Default
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
