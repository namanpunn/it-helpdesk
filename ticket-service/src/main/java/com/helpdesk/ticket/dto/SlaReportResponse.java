package com.helpdesk.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlaReportResponse {
    private int totalTickets;
    private int violatedCount;
    private int criticalCount;
    private int onTrackCount;
    private double violationRate;
    private List<SlaStatusResponse> violatedTickets;
    private List<SlaStatusResponse> criticalTickets;
}