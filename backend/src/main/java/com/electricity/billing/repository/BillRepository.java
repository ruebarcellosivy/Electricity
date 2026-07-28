package com.electricity.billing.repository;

import com.electricity.billing.entity.Bill;
import com.electricity.billing.entity.enums.BillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillNumber(String billNumber);
    boolean existsByConsumer_ConsumerNumberAndBillingPeriod(String consumerNumber, String billingPeriod);
    List<Bill> findByConsumer_ConsumerNumberOrderByBillDateDesc(String consumerNumber);
    List<Bill> findTop1ByConsumer_Customer_IdOrderByBillDateDesc(Long customerId);
    long countByConsumer_Customer_IdAndStatus(Long customerId, BillStatus status);
    List<Bill> findByIdInAndConsumer_Customer_Id(List<Long> ids, Long customerId);

    @Query("select b from Bill b where b.consumer.customer.id = :customerId " +
            "and (:status is null or b.status = :status)")
    Page<Bill> findByCustomer(@Param("customerId") Long customerId,
                               @Param("status") BillStatus status,
                               Pageable pageable);

    @Query("select b from Bill b where b.consumer.customer.id = :customerId " +
            "and (:status is null or b.status = :status) " +
            "and (:fromDate is null or b.billDate >= :fromDate) " +
            "and (:toDate is null or b.billDate <= :toDate)")
    Page<Bill> findByCustomerWithFilters(@Param("customerId") Long customerId,
                                          @Param("status") BillStatus status,
                                          @Param("fromDate") LocalDate fromDate,
                                          @Param("toDate") LocalDate toDate,
                                          Pageable pageable);

    @Query("select b from Bill b where " +
            "(:consumerNumber is null or b.consumer.consumerNumber = :consumerNumber) " +
            "and (:customerCode is null or b.consumer.customer.customerCode = :customerCode) " +
            "and (:status is null or b.status = :status)")
    Page<Bill> searchForAdmin(@Param("consumerNumber") String consumerNumber,
                               @Param("customerCode") String customerCode,
                               @Param("status") BillStatus status,
                               Pageable pageable);
}
