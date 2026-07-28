package com.electricity.billing.dto.response;

import com.electricity.billing.entity.enums.ComplaintStatus;
import com.electricity.billing.entity.enums.ComplaintType;
import com.electricity.billing.entity.enums.ContactMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplaintResponse {
    private Long id;
    private String complaintNumber;
    private String consumerNumber;
    private String customerName;
    private ComplaintType complaintType;
    private String category;
    private String description;
    private ContactMethod preferredContactMethod;
    private String contactDetails;
    private ComplaintStatus status;
    private String assignedTo;
    private LocalDateTime resolutionDueAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<RemarkResponse> remarks;
}
