package com.electricity.billing.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** US015 - Result of a bulk bill upload (CSV) attempt. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUploadResultResponse {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private List<String> errors;
}
