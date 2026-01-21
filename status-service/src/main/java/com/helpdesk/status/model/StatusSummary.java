package com.helpdesk.status.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusSummary {

    private LocalDate date;
    private long openCount;
    private long inProgressCount;
    private long resolvedCount;
    private long closedCount;
    private long totalCount;

    public StatusSummary(LocalDate date) {
        this.date = date;
        this.openCount = 0;
        this.inProgressCount = 0;
        this.resolvedCount = 0;
        this.closedCount = 0;
        this.totalCount = 0;
    }
}