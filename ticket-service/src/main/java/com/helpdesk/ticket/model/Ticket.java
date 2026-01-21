package com.helpdesk.ticket.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    private String ticketId;
    private String employeeId;
    private String employeeName;
    private TicketCategory category;
    private String description;
    private TicketPriority priority;
    private LocalDateTime createdAt;
    private String createdBy;
}