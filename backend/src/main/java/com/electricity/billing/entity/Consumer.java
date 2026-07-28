package com.electricity.billing.entity;

import com.electricity.billing.entity.enums.ConnectionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * A single electricity connection (consumer number) belonging to a {@link Customer}.
 * Bills and complaints are always tied to a consumer number, not directly to the customer,
 * since one customer may hold several connections.
 */
@Entity
@Table(name = "consumers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consumer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consumer_number", nullable = false, unique = true, length = 13)
    private String consumerNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_status", nullable = false, length = 20)
    @Builder.Default
    private ConnectionStatus connectionStatus = ConnectionStatus.CONNECTED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
