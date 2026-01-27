package com.helpdesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaStatusResponse {
    private String ticketId;
    private LocalDateTime createdAt;
    private LocalDateTime slaDueDate;
    private boolean slaViolated;
    private LocalDateTime slaViolatedAt;
    private String slaStatus;
    private long remainingHours;
    private long remainingMinutes;
    private boolean critical;
}