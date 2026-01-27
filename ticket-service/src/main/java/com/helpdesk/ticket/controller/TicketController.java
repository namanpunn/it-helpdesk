package com.helpdesk.ticket.controller;

import com.helpdesk.ticket.dto.ApiResponse;
import com.helpdesk.ticket.dto.CreateTicketRequest;
import com.helpdesk.ticket.model.Ticket;
import com.helpdesk.ticket.model.TicketPriority;
import com.helpdesk.ticket.service.SlaService;
import com.helpdesk.ticket.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.helpdesk.ticket.dto.SlaReportResponse;
import com.helpdesk.ticket.dto.SlaStatusResponse;
import java.time.Duration;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket Management", description = "APIs for managing IT helpdesk tickets")
public class TicketController {

    private final TicketService ticketService;
    private final SlaService slaService;

    @PostMapping("/create")
    @Operation(summary = "Create a new ticket", description = "Creates a new IT helpdesk ticket")
    public ResponseEntity<ApiResponse<Ticket>> createTicket(
            @Valid @RequestBody CreateTicketRequest request) {

        log.info("Received request to create ticket for employee: {}", request.getEmployeeId());

        Ticket ticket = ticketService.createTicket(request);

        ApiResponse<Ticket> response = ApiResponse.success(
                "Ticket created successfully",
                ticket
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ticketId}")
    @Operation(summary = "Get ticket by ID", description = "Retrieves a specific ticket by its ID")
    public ResponseEntity<ApiResponse<Ticket>> getTicketById(
            @Parameter(description = "Ticket ID", example = "TKT-1737456789012")
            @PathVariable String ticketId) {

        log.info("Received request to fetch ticket: {}", ticketId);

        Ticket ticket = ticketService.getTicketById(ticketId);

        ApiResponse<Ticket> response = ApiResponse.success(
                "Ticket retrieved successfully",
                ticket
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/employee/{employeeId}")
    @Operation(summary = "Get tickets by employee ID", description = "Retrieves all tickets for a specific employee")
    public ResponseEntity<ApiResponse<List<Ticket>>> getTicketsByEmployeeId(
            @Parameter(description = "Employee ID", example = "EMP001")
            @PathVariable String employeeId) {

        log.info("Received request to fetch tickets for employee: {}", employeeId);

        List<Ticket> tickets = ticketService.getTicketsByEmployeeId(employeeId);

        ApiResponse<List<Ticket>> response = ApiResponse.success(
                String.format("Found %d ticket(s) for employee", tickets.size()),
                tickets
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Get tickets by priority", description = "Retrieves all tickets with a specific priority level")
    public ResponseEntity<ApiResponse<List<Ticket>>> getTicketsByPriority(
            @Parameter(description = "Priority level", example = "HIGH")
            @PathVariable TicketPriority priority) {

        log.info("Received request to fetch tickets with priority: {}", priority);

        List<Ticket> tickets = ticketService.getTicketsByPriority(priority);

        ApiResponse<List<Ticket>> response = ApiResponse.success(
                String.format("Found %d ticket(s) with priority %s", tickets.size(), priority),
                tickets
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all tickets", description = "Retrieves all tickets in the system")
    public ResponseEntity<ApiResponse<List<Ticket>>> getAllTickets() {

        log.info("Received request to fetch all tickets");

        List<Ticket> tickets = ticketService.getAllTickets();

        ApiResponse<List<Ticket>> response = ApiResponse.success(
                String.format("Found %d total ticket(s)", tickets.size()),
                tickets
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{ticketId}")
    @Operation(summary = "Delete a ticket", description = "Deletes a ticket by its ID")
    public ResponseEntity<ApiResponse<Void>> deleteTicket(
            @Parameter(description = "Ticket ID", example = "TKT-1737456789012")
            @PathVariable String ticketId) {

        log.info("Received request to delete ticket: {}", ticketId);

        ticketService.deleteTicket(ticketId);

        ApiResponse<Void> response = ApiResponse.success(
                "Ticket deleted successfully",
                null
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sla/violated")
    @Operation(summary = "Get SLA violated tickets", description = "Retrieves all tickets that violated SLA")
    public ResponseEntity<ApiResponse<List<Ticket>>> getSlaViolatedTickets() {

        log.info("Received request to fetch SLA violated tickets");

        List<Ticket> tickets = ticketService.getSlaViolatedTickets();

        ApiResponse<List<Ticket>> response = ApiResponse.success(
                String.format("Found %d SLA violated ticket(s)", tickets.size()),
                tickets
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sla/critical")
    @Operation(summary = "Get critical tickets", description = "Retrieves tickets at risk of SLA violation (less than 2 hours)")
    public ResponseEntity<ApiResponse<List<Ticket>>> getCriticalTickets() {

        log.info("Received request to fetch critical tickets");

        List<Ticket> tickets = ticketService.getCriticalTickets();

        ApiResponse<List<Ticket>> response = ApiResponse.success(
                String.format("Found %d critical ticket(s)", tickets.size()),
                tickets
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/sla/report")
    @Operation(summary = "Get SLA report", description = "Retrieves comprehensive SLA compliance report")
    public ResponseEntity<ApiResponse<SlaReportResponse>> getSlaReport() {

        log.info("Received request to generate SLA report");

        List<Ticket> allTickets = ticketService.getAllTickets();
        List<Ticket> violatedTickets = ticketService.getSlaViolatedTickets();
        List<Ticket> criticalTickets = ticketService.getCriticalTickets();

        // Build SLA status responses
        List<SlaStatusResponse> violatedResponses = violatedTickets.stream()
                .map(this::buildSlaStatus)
                .toList();

        List<SlaStatusResponse> criticalResponses = criticalTickets.stream()
                .map(this::buildSlaStatus)
                .toList();

        int onTrackCount = allTickets.size() - violatedTickets.size() - criticalTickets.size();
        double violationRate = allTickets.isEmpty() ? 0 :
                (double) violatedTickets.size() / allTickets.size() * 100;

        SlaReportResponse report = SlaReportResponse.builder()
                .totalTickets(allTickets.size())
                .violatedCount(violatedTickets.size())
                .criticalCount(criticalTickets.size())
                .onTrackCount(Math.max(0, onTrackCount))
                .violationRate(violationRate)
                .violatedTickets(violatedResponses)
                .criticalTickets(criticalResponses)
                .build();

        ApiResponse<SlaReportResponse> response = ApiResponse.success(
                "SLA report generated successfully",
                report
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ticketId}/sla")
    @Operation(summary = "Get ticket SLA status", description = "Get detailed SLA status for a specific ticket")
    public ResponseEntity<ApiResponse<SlaStatusResponse>> getTicketSlaStatus(
            @PathVariable String ticketId) {

        log.info("Received request to fetch SLA status for ticket: {}", ticketId);

        Ticket ticket = ticketService.getTicketById(ticketId);
        SlaStatusResponse slaStatus = buildSlaStatus(ticket);

        ApiResponse<SlaStatusResponse> response = ApiResponse.success(
                "SLA status retrieved successfully",
                slaStatus
        );

        return ResponseEntity.ok(response);
    }

    // Helper method to build SLA status
    private SlaStatusResponse buildSlaStatus(Ticket ticket) {
        Duration remaining = slaService.getRemainingTime(ticket);

        return SlaStatusResponse.builder()
                .ticketId(ticket.getTicketId())
                .createdAt(ticket.getCreatedAt())
                .slaDueDate(ticket.getSlaDueDate())
                .slaViolated(ticket.isSlaViolated())
                .slaViolatedAt(ticket.getSlaViolatedAt())
                .slaStatus(slaService.getSlaStatusMessage(ticket))
                .remainingHours(remaining.toHours())
                .remainingMinutes(remaining.toMinutesPart())
                .critical(slaService.isCritical(ticket))
                .build();
    }
}