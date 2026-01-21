package com.helpdesk.ticket.dto;

import com.helpdesk.ticket.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    private boolean success;
    private String message;
    private Ticket data;
}