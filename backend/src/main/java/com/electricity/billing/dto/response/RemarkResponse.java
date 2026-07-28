package com.electricity.billing.dto.response;

import com.electricity.billing.entity.enums.ComplaintStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemarkResponse {
    private String remark;
    private ComplaintStatus statusAtTime;
    private String updatedBy;
    private LocalDateTime createdAt;
}
