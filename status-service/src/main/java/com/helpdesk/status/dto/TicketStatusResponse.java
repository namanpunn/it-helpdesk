package com.helpdesk.status.dto;

import com.helpdesk.status.model.StatusHistory;
import com.helpdesk.status.model.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketStatusResponse {

    private String ticketId;
    private TicketStatus currentStatus;
    private List<StatusHistory> statusHistory;
    private int totalUpdates;
}