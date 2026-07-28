package com.electricity.billing.service;

import com.electricity.billing.dto.request.ComplaintRequest;
import com.electricity.billing.dto.request.ComplaintStatusUpdateRequest;
import com.electricity.billing.dto.response.ComplaintResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.enums.ComplaintStatus;
import com.electricity.billing.entity.enums.ComplaintType;

import java.time.LocalDateTime;

public interface ComplaintService {

    ComplaintResponse register(ComplaintRequest request);

    ComplaintResponse getByComplaintNumber(String complaintNumber);

    PageResponse<ComplaintResponse> myHistory(int page, int size);

    PageResponse<ComplaintResponse> search(String customerCode, String consumerNumber, String complaintNumber,
                                            ComplaintType complaintType, ComplaintStatus status,
                                            LocalDateTime fromDate, LocalDateTime toDate, int page, int size);

    ComplaintResponse updateStatus(Long id, ComplaintStatusUpdateRequest request);

    byte[] exportComplaints(String customerCode, String consumerNumber, ComplaintType complaintType,
                             ComplaintStatus status, String format);
}
