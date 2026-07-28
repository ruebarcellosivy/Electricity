package com.electricity.billing.repository;

import com.electricity.billing.entity.Consumer;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConsumerRepository extends JpaRepository<Consumer, Long> {

    Optional<Consumer> findByConsumerNumber(String consumerNumber);
    boolean existsByConsumerNumber(String consumerNumber);
    List<Consumer> findByCustomer_Id(Long customerId);

    @Query("select cn from Consumer cn where " +
            "(:electricalSection is null or cn.customer.electricalSection = :electricalSection) and " +
            "(:customerType is null or cn.customer.customerType = :customerType)")
    Page<Consumer> search(@Param("electricalSection") ElectricalSection electricalSection,
                           @Param("customerType") CustomerType customerType,
                           Pageable pageable);
}
