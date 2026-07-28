package com.electricity.billing.serviceimpl;

import com.electricity.billing.dto.request.ComplaintRequest;
import com.electricity.billing.dto.request.ComplaintStatusUpdateRequest;
import com.electricity.billing.dto.response.ComplaintResponse;
import com.electricity.billing.dto.response.PageResponse;
import com.electricity.billing.dto.response.RemarkResponse;
import com.electricity.billing.entity.Complaint;
import com.electricity.billing.entity.ComplaintRemark;
import com.electricity.billing.entity.Consumer;
import com.electricity.billing.entity.Customer;
import com.electricity.billing.entity.enums.ComplaintStatus;
import com.electricity.billing.entity.enums.ComplaintType;
import com.electricity.billing.exception.InvalidRequestException;
import com.electricity.billing.exception.ResourceNotFoundException;
import com.electricity.billing.repository.ComplaintRepository;
import com.electricity.billing.repository.ConsumerRepository;
import com.electricity.billing.repository.CustomerRepository;
import com.electricity.billing.service.ComplaintService;
import com.electricity.billing.util.ComplaintCategoryUtil;
import com.electricity.billing.util.CsvExportUtil;
import com.electricity.billing.util.IdGeneratorUtil;
import com.electricity.billing.util.PdfGeneratorUtil;
import com.electricity.billing.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ConsumerRepository consumerRepository;
    private final CustomerRepository customerRepository;

    @Override
    @PreAuthorize("hasRole('CUSTOMER')")
    public ComplaintResponse register(ComplaintRequest request) {
        Customer customer = currentCustomer();
        Consumer consumer = consumerRepository.findByConsumerNumber(request.getConsumerNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Please select a valid Consumer Number."));

        if (!consumer.getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("This Consumer Number is not linked to your account.");
        }
        if (!ComplaintCategoryUtil.isValidCategory(request.getComplaintType(), request.getCategory())) {
            throw new InvalidRequestException("Invalid category selected for the given complaint type.");
        }

        Complaint complaint = Complaint.builder()
                .complaintNumber(IdGeneratorUtil.generateComplaintNumber())
                .consumer(consumer)
                .complaintType(request.getComplaintType())
                .category(request.getCategory())
                .description(request.getDescription())
                .preferredContactMethod(request.getPreferredContactMethod())
                .contactDetails(request.getContactDetails())
                .status(ComplaintStatus.OPEN)
                .resolutionDueAt(LocalDateTime.now().plusHours(ComplaintCategoryUtil.resolutionHoursFor(request.getComplaintType())))
                .build();

        return toResponse(complaintRepository.save(complaint));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ComplaintResponse getByComplaintNumber(String complaintNumber) {
        Complaint complaint = complaintRepository.findByComplaintNumber(complaintNumber)
                .orElseThrow(() -> new ResourceNotFoundException("No complaint found with ID: " + complaintNumber));
        Customer customer = currentCustomer();
        if (!complaint.getConsumer().getCustomer().getId().equals(customer.getId())) {
            throw new AccessDeniedException("You do not have permission to view this complaint.");
        }
        return toResponse(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CUSTOMER')")
    public PageResponse<ComplaintResponse> myHistory(int page, int size) {
        Customer customer = currentCustomer();
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = complaintRepository.findByCustomerId(customer.getId(), pageable).map(this::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SME')")
    public PageResponse<ComplaintResponse> search(String customerCode, String consumerNumber, String complaintNumber,
                                                   ComplaintType complaintType, ComplaintStatus status,
                                                   LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        var result = complaintRepository.search(customerCode, consumerNumber, complaintNumber, complaintType, status,
                fromDate, toDate, pageable).map(this::toResponse);
        return PageResponse.from(result);
    }

    @Override
    @PreAuthorize("hasAnyRole('ADMIN', 'SME')")
    public ComplaintResponse updateStatus(Long id, ComplaintStatusUpdateRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint not found with id: " + id));

        complaint.setStatus(request.getStatus());
        if (request.getAssignedTo() != null && !request.getAssignedTo().isBlank()) {
            complaint.setAssignedTo(request.getAssignedTo());
        }

        ComplaintRemark remark = ComplaintRemark.builder()
                .complaint(complaint)
                .remark(request.getRemark())
                .statusAtTime(request.getStatus())
                .updatedBy(SecurityUtil.currentUserId())
                .build();
        complaint.getRemarks().add(remark);

        return toResponse(complaintRepository.save(complaint));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasAnyRole('ADMIN', 'SME')")
    public byte[] exportComplaints(String customerCode, String consumerNumber, ComplaintType complaintType,
                                    ComplaintStatus status, String format) {
        var pageable = PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "createdAt"));
        List<Complaint> complaints = complaintRepository.search(customerCode, consumerNumber, null, complaintType,
                status, null, null, pageable).getContent();

        if ("pdf".equalsIgnoreCase(format)) {
            List<String[]> rows = new ArrayList<>();
            for (Complaint c : complaints) {
                rows.add(new String[]{c.getComplaintNumber(), c.getComplaintType() + " | " + c.getStatus()
                        + " | " + c.getConsumer().getConsumerNumber()});
            }
            return PdfGeneratorUtil.generateDocument("Complaint Report", rows);
        }
        List<String> headers = List.of("Complaint ID", "Consumer Number", "Complaint Type", "Category",
                "Status", "Date Submitted", "Last Updated");
        String csv = CsvExportUtil.toCsv(headers, complaints, c -> List.of(
                c.getComplaintNumber(), c.getConsumer().getConsumerNumber(), c.getComplaintType().toString(),
                c.getCategory(), c.getStatus().toString(), c.getCreatedAt().toString(), c.getUpdatedAt().toString()));
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    private Customer currentCustomer() {
        String userId = SecurityUtil.currentUserId();
        return customerRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for the current user."));
    }

    private ComplaintResponse toResponse(Complaint complaint) {
        List<RemarkResponse> remarks = complaint.getRemarks().stream()
                .map(r -> RemarkResponse.builder()
                        .remark(r.getRemark())
                        .statusAtTime(r.getStatusAtTime())
                        .updatedBy(r.getUpdatedBy())
                        .createdAt(r.getCreatedAt())
                        .build())
                .toList();

        return ComplaintResponse.builder()
                .id(complaint.getId())
                .complaintNumber(complaint.getComplaintNumber())
                .consumerNumber(complaint.getConsumer().getConsumerNumber())
                .customerName(complaint.getConsumer().getCustomer().getFullName())
                .complaintType(complaint.getComplaintType())
                .category(complaint.getCategory())
                .description(complaint.getDescription())
                .preferredContactMethod(complaint.getPreferredContactMethod())
                .contactDetails(complaint.getContactDetails())
                .status(complaint.getStatus())
                .assignedTo(complaint.getAssignedTo())
                .resolutionDueAt(complaint.getResolutionDueAt())
                .createdAt(complaint.getCreatedAt())
                .updatedAt(complaint.getUpdatedAt())
                .remarks(remarks)
                .build();
    }
}
