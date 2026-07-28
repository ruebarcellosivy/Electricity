package com.electricity.billing.service;

import com.electricity.billing.dto.request.AdminCreateCustomerRequest;
import com.electricity.billing.dto.request.UpdateCustomerRequest;
import com.electricity.billing.dto.response.CustomerResponse;
import com.electricity.billing.dto.response.HomeSummaryResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.enums.CustomerType;
import com.electricity.billing.entity.enums.ElectricalSection;

public interface CustomerService {

    CustomerResponse createCustomer(AdminCreateCustomerRequest request);

    PageResponse<CustomerResponse> listCustomers(String search, ElectricalSection section, CustomerType type,
                                                  int page, int size);

    CustomerResponse getCustomer(Long id);

    CustomerResponse updateCustomer(Long id, UpdateCustomerRequest request);

    void deleteCustomer(Long id);

    CustomerResponse getMyProfile();

    HomeSummaryResponse getHomeSummary();
}
