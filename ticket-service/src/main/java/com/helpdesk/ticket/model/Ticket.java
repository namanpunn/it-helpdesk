package com.helpdesk.ticket.model;

import com.google.cloud.firestore.annotation.DocumentId;
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

    @DocumentId
    private String ticketId;

    private String employeeId;
    private String employeeName;
    private TicketCategory category;
    private String description;
    private TicketPriority priority;
    private LocalDateTime createdAt;
    private String createdBy;

    private LocalDateTime slaDueDate;
    private boolean slaViolated;
    private LocalDateTime slaViolatedAt;

    public static String generateTicketId() {
        return "TKT-" + System.currentTimeMillis();
    }

    public boolean isOverdue() {
        if (slaDueDate == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(slaDueDate);
    }
}