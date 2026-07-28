package com.electricity.billing.entity;

import com.electricity.billing.entity.enums.ComplaintStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** A single note/remark added by Admin or SME while progressing a {@link Complaint}. */
@Entity
@Table(name = "complaint_remarks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintRemark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id", nullable = false)
    private Complaint complaint;

    @Column(nullable = false, length = 1000)
    private String remark;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_at_time", nullable = false, length = 20)
    private ComplaintStatus statusAtTime;

    @Column(name = "updated_by", nullable = false, length = 20)
    private String updatedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
