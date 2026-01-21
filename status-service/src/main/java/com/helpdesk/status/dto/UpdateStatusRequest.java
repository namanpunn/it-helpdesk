package com.helpdesk.status.dto;

import com.helpdesk.status.model.TicketStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {

    @NotBlank(message = "Ticket ID is required")
    private String ticketId;

    @NotNull(message = "Status is required")
    private TicketStatus status;

    @NotBlank(message = "Updated by is required")
    @Size(min = 3, max = 100, message = "Updated by must be between 3 and 100 characters")
    private String updatedBy;

    @Size(max = 500, message = "Comments cannot exceed 500 characters")
    private String comments;
}