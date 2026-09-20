package dev.sorokin.domain.order;

import dev.sorokin.infrastructure.persistence.order.PaymentStatusConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "address")
    private String address;

    @Column(name =  "client_id")
    private Long clientId;

    @Column(name = "client_estimate")
    private BigDecimal clientEstimate;

    @Column(name = "final_amount")
    private BigDecimal finalAmount;

    @Column(name = "authorized_amount")
    private BigDecimal authorizedAmount;

    @Column(name = "captured_amount")
    private BigDecimal capturedAmount;

    @Column(name =  "payment_status")
    @Convert(converter = PaymentStatusConverter.class)
    private PaymentStatus paymentStatus;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}
