package com.electricity.billing.repository;

import com.electricity.billing.entity.Complaint;
import com.electricity.billing.entity.enums.ComplaintStatus;
import com.electricity.billing.entity.enums.ComplaintType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByComplaintNumber(String complaintNumber);
    List<Complaint> findByConsumer_ConsumerNumberOrderByCreatedAtDesc(String consumerNumber);
    List<Complaint> findByConsumer_Customer_CustomerCodeOrderByCreatedAtDesc(String customerCode);
    long countByConsumer_Customer_IdAndStatusIn(Long customerId, List<ComplaintStatus> statuses);

    @Query("select c from Complaint c where c.consumer.customer.id = :customerId order by c.createdAt desc")
    Page<Complaint> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

    @Query("select c from Complaint c where " +
            "(:customerCode is null or c.consumer.customer.customerCode = :customerCode) " +
            "and (:consumerNumber is null or c.consumer.consumerNumber = :consumerNumber) " +
            "and (:complaintNumber is null or c.complaintNumber = :complaintNumber) " +
            "and (:complaintType is null or c.complaintType = :complaintType) " +
            "and (:status is null or c.status = :status) " +
            "and (:fromDate is null or c.createdAt >= :fromDate) " +
            "and (:toDate is null or c.createdAt <= :toDate)")
    Page<Complaint> search(@Param("customerCode") String customerCode,
                            @Param("consumerNumber") String consumerNumber,
                            @Param("complaintNumber") String complaintNumber,
                            @Param("complaintType") ComplaintType complaintType,
                            @Param("status") ComplaintStatus status,
                            @Param("fromDate") LocalDateTime fromDate,
                            @Param("toDate") LocalDateTime toDate,
                            Pageable pageable);
}
