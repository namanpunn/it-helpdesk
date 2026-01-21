package com.helpdesk.status.controller;

import com.helpdesk.status.dto.ApiResponse;
import com.helpdesk.status.dto.TicketStatusResponse;
import com.helpdesk.status.dto.UpdateStatusRequest;
import com.helpdesk.status.model.StatusHistory;
import com.helpdesk.status.model.StatusSummary;
import com.helpdesk.status.service.StatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/status")
@RequiredArgsConstructor
@Tag(name = "Status Management", description = "APIs for managing ticket status updates and reporting")
public class StatusController {

    private final StatusService statusService;

    @PostMapping("/update")
    @Operation(summary = "Update ticket status", description = "Updates the status of a ticket and creates a history entry")
    public ResponseEntity<ApiResponse<StatusHistory>> updateStatus(
            @Valid @RequestBody UpdateStatusRequest request) {

        log.info("Received request to update status for ticket: {}", request.getTicketId());

        StatusHistory statusHistory = statusService.updateStatus(request);

        ApiResponse<StatusHistory> response = ApiResponse.success(
                "Ticket status updated successfully",
                statusHistory
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get current ticket status", description = "Retrieves the current status of a specific ticket")
    public ResponseEntity<ApiResponse<StatusHistory>> getCurrentStatus(
            @Parameter(description = "Ticket ID", example = "TKT-1737456789012")
            @PathVariable String ticketId) {

        log.info("Received request to fetch current status for ticket: {}", ticketId);

        StatusHistory status = statusService.getCurrentStatus(ticketId);

        ApiResponse<StatusHistory> response = ApiResponse.success(
                "Current status retrieved successfully",
                status
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ticketId}/history")
    @Operation(summary = "Get ticket status history", description = "Retrieves complete status update history for a ticket")
    public ResponseEntity<ApiResponse<TicketStatusResponse>> getStatusHistory(
            @Parameter(description = "Ticket ID", example = "TKT-1737456789012")
            @PathVariable String ticketId) {

        log.info("Received request to fetch status history for ticket: {}", ticketId);

        TicketStatusResponse history = statusService.getTicketStatusHistory(ticketId);

        ApiResponse<TicketStatusResponse> response = ApiResponse.success(
                "Status history retrieved successfully",
                history
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/{date}")
    @Operation(summary = "Get daily status summary", description = "Retrieves count of tickets by status for a specific date")
    public ResponseEntity<ApiResponse<StatusSummary>> getDailySummary(
            @Parameter(description = "Date in format YYYY-MM-DD", example = "2026-01-21")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        log.info("Received request to fetch daily summary for date: {}", date);

        StatusSummary summary = statusService.getDailySummary(date);

        ApiResponse<StatusSummary> response = ApiResponse.success(
                "Daily summary retrieved successfully",
                summary
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary/today")
    @Operation(summary = "Get today's status summary", description = "Retrieves count of tickets by status for today")
    public ResponseEntity<ApiResponse<StatusSummary>> getTodaySummary() {

        LocalDate today = LocalDate.now();
        StatusSummary summary = statusService.getDailySummary(today);

        ApiResponse<StatusSummary> response = ApiResponse.success(
                "Today's summary retrieved successfully",
                summary
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all status updates", description = "Retrieves all status updates across all tickets")
    public ResponseEntity<ApiResponse<List<StatusHistory>>> getAllStatusUpdates() {

        log.info("Received request to fetch all status updates");

        List<StatusHistory> statusUpdates = statusService.getAllStatusUpdates();

        ApiResponse<List<StatusHistory>> response = ApiResponse.success(
                String.format("Found %d status update(s)", statusUpdates.size()),
                statusUpdates
        );

        return ResponseEntity.ok(response);
    }}