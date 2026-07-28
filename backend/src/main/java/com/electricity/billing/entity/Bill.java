package com.electricity.billing.entity;

import com.electricity.billing.entity.enums.BillStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A monthly electricity bill raised against a {@link Consumer}. Uniqueness of
 * (consumer, billingPeriod) is enforced to prevent duplicate bills for the same period.
 */
@Entity
@Table(name = "bills", uniqueConstraints = @UniqueConstraint(columnNames = {"consumer_id", "billing_period"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_number", nullable = false, unique = true, length = 30)
    private String billNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consumer_id", nullable = false)
    private Consumer consumer;

    /** Billing month/year, e.g. "JUL-2026". */
    @Column(name = "billing_period", nullable = false, length = 20)
    private String billingPeriod;

    @Column(name = "bill_date", nullable = false)
    private LocalDate billDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "disconnection_date")
    private LocalDate disconnectionDate;

    @Column(name = "bill_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal billAmount;

    @Column(name = "late_fee", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BillStatus status = BillStatus.UNPAID;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Transient
    public BigDecimal getPayableAmount() {
        BigDecimal amount = billAmount == null ? BigDecimal.ZERO : billAmount;
        BigDecimal fee = lateFee == null ? BigDecimal.ZERO : lateFee;
        return amount.add(fee);
    }
}
