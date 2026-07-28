package com.electricity.billing.service;

import com.electricity.billing.dto.request.AddBillRequest;
import com.electricity.billing.dto.response.BillResponse;
import com.electricity.billing.dto.response.BillSelectionSummaryResponse;
import com.electricity.billing.dto.response.BulkUploadResultResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.entity.enums.BillStatus;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface BillService {

    BillResponse addBill(AddBillRequest request);

    BulkUploadResultResponse bulkUpload(MultipartFile file);

    PageResponse<BillResponse> myBills(BillStatus status, int page, int size);

    PageResponse<BillResponse> myBillHistory(LocalDate fromDate, LocalDate toDate, BillStatus status,
                                              String sortBy, int page, int size);

    BillSelectionSummaryResponse getSelectionSummary(List<Long> billIds);

    PageResponse<BillResponse> adminSearch(String consumerNumber, String customerCode, BillStatus status,
                                            int page, int size);

    byte[] exportBillHistory(String consumerNumber, String format);

    BillResponse getBill(Long id);
}
