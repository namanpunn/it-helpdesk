package com.helpdesk.status.service;

import com.helpdesk.status.dto.TicketStatusResponse;
import com.helpdesk.status.dto.UpdateStatusRequest;
import com.helpdesk.status.exception.StatusNotFoundException;
import com.helpdesk.status.model.StatusHistory;
import com.helpdesk.status.model.StatusSummary;
import com.helpdesk.status.model.TicketStatus;
import com.helpdesk.status.repository.StatusRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatusService {

    private final StatusRepository statusRepository;


    public StatusHistory updateStatus(UpdateStatusRequest request) {
        log.info("Updating status for ticket: {} to {}", request.getTicketId(), request.getStatus());

        StatusHistory statusHistory = StatusHistory.builder()
                .statusId(StatusHistory.generateStatusId())
                .ticketId(request.getTicketId())
                .status(request.getStatus())
                .updatedBy(request.getUpdatedBy())
                .updatedAt(LocalDateTime.now())
                .comments(request.getComments())
                .build();

        StatusHistory savedStatus = statusRepository.save(statusHistory);
        log.info("Status updated successfully for ticket: {}", request.getTicketId());

        return savedStatus;
    }

    public StatusHistory getCurrentStatus(String ticketId) {
        log.info("Fetching current status for ticket: {}", ticketId);

        return statusRepository.findCurrentStatusByTicketId(ticketId)
                .orElseThrow(() -> new StatusNotFoundException(
                        "No status found for ticket: " + ticketId));
    }

    public TicketStatusResponse getTicketStatusHistory(String ticketId) {
        log.info("Fetching complete status history for ticket: {}", ticketId);

        List<StatusHistory> history = statusRepository.findByTicketId(ticketId);

        if (history.isEmpty()) {
            throw new StatusNotFoundException("No status history found for ticket: " + ticketId);
        }

        return TicketStatusResponse.builder()
                .ticketId(ticketId)
                .currentStatus(history.get(0).getStatus()) // First one is latest (DESC order)
                .statusHistory(history)
                .totalUpdates(history.size())
                .build();
    }

    public StatusSummary getDailySummary(LocalDate date) {
        log.info("Fetching daily summary for date: {}", date);

        Map<TicketStatus, Long> summary = statusRepository.getStatusSummaryByDate(date);

        StatusSummary statusSummary = new StatusSummary(date);
        statusSummary.setOpenCount(summary.getOrDefault(TicketStatus.OPEN, 0L));
        statusSummary.setInProgressCount(summary.getOrDefault(TicketStatus.IN_PROGRESS, 0L));
        statusSummary.setResolvedCount(summary.getOrDefault(TicketStatus.RESOLVED, 0L));
        statusSummary.setClosedCount(summary.getOrDefault(TicketStatus.CLOSED, 0L));

        long total = statusSummary.getOpenCount() +
                statusSummary.getInProgressCount() +
                statusSummary.getResolvedCount() +
                statusSummary.getClosedCount();

        statusSummary.setTotalCount(total);

        log.info("Daily summary generated: {} total tickets", total);
        return statusSummary;
    }


    public List<StatusHistory> getAllStatusUpdates() {
        log.info("Fetching all status updates");
        return statusRepository.findAll();
    }
}