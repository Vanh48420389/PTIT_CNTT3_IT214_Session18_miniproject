package ra.edu.rikkeibank.customer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Staff implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String staffCode; // e.g. STF001

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private String role; // TELLER, MANAGER, SUPERVISOR

    @Column(nullable = false)
    private String branchCode; // BRANCH_HN, BRANCH_HCM

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
