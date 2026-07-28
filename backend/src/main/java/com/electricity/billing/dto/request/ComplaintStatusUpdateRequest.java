package com.electricity.billing.dto.request;

import com.electricity.billing.entity.enums.ComplaintStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** US017/US018/US019 - Admin/SME update a complaint's status and add a remark. */
@Data
public class ComplaintStatusUpdateRequest {

    @NotNull(message = "Status is required.")
    private ComplaintStatus status;

    @NotBlank(message = "Please add a note describing this status change.")
    @Size(max = 1000, message = "Remark cannot exceed 1000 characters.")
    private String remark;

    /** Optional: Admin assigns the complaint to a specific SME user id. */
    private String assignedTo;
}
