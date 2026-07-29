package com.electricity.billing.repository;

import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUser_UserId(String userId);
    Optional<Customer> findByCustomerCode(String customerCode);
    boolean existsByEmail(String email);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByMobileNumber(String mobileNumber);

    @Query("select c from Customer c where " +
            "(:electricalSection is null or c.electricalSection = :electricalSection) and " +
            "(:customerType is null or c.customerType = :customerType) and " +
            "(:search is null or lower(c.fullName) like lower(concat('%', :search, '%')) " +
            "  or lower(c.customerCode) like lower(concat('%', :search, '%')) " +
            "  or lower(c.email) like lower(concat('%', :search, '%')))")
    Page<Customer> search(@Param("electricalSection") ElectricalSection electricalSection,
                           @Param("customerType") CustomerType customerType,
                           @Param("search") String search,
                           Pageable pageable);
}
