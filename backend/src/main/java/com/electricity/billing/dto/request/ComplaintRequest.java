package com.electricity.billing.dto.request;

import com.electricity.billing.entity.enums.ComplaintType;
import com.electricity.billing.entity.enums.ContactMethod;
import jakarta.validation.constraints.*;
import lombok.Data;

/** US008 - Customer registers a complaint against one of their consumer connections. */
@Data
public class ComplaintRequest {

    @NotBlank(message = "Please select a Consumer Number.")
    @Pattern(regexp = "\\d{13}", message = "Please select a valid Consumer Number.")
    private String consumerNumber;

    @NotNull(message = "Complaint type is required.")
    private ComplaintType complaintType;

    @NotBlank(message = "Category is required.")
    private String category;

    @NotBlank(message = "Description is required.")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters.")
    private String description;

    @NotNull(message = "Preferred contact method is required.")
    private ContactMethod preferredContactMethod;

    @NotBlank(message = "Contact details are required.")
    @Size(max = 100, message = "Contact details cannot exceed 100 characters.")
    private String contactDetails;
}
