package com.helpdesk.ticket.service;

import com.helpdesk.ticket.dto.CreateTicketRequest;
import com.helpdesk.ticket.exception.TicketNotFoundException;
import com.helpdesk.ticket.model.Ticket;
import com.helpdesk.ticket.model.TicketPriority;
import com.helpdesk.ticket.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final SlaService slaService;
    
    public Ticket createTicket(CreateTicketRequest request) {
        log.info("Creating ticket for employee: {}", request.getEmployeeId());

        Ticket ticket = Ticket.builder()
                .ticketId(Ticket.generateTicketId())
                .employeeId(request.getEmployeeId())
                .employeeName(request.getEmployeeName())
                .category(request.getCategory())
                .description(request.getDescription())
                .priority(request.getPriority())
                .createdAt(LocalDateTime.now())
                .createdBy(request.getEmployeeName())
                .slaViolated(false)
                .build();

        LocalDateTime slaDueDate = slaService.calculateSlaDueDate(ticket);
        ticket.setSlaDueDate(slaDueDate);

        Ticket savedTicket = ticketRepository.save(ticket);

        log.info("Ticket created successfully: {} with SLA due date: {}",
                savedTicket.getTicketId(), slaDueDate);

        if (ticket.getPriority() == TicketPriority.HIGH) {
            log.warn("HIGH PRIORITY ticket created: {} - Must be resolved within 24 hours (Due: {})",
                    ticket.getTicketId(), slaDueDate);
        }

        return savedTicket;
    }


    public Ticket getTicketById(String ticketId) {
        log.info("Fetching ticket: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));

        updateSlaStatus(ticket);

        return ticket;
    }

    public List<Ticket> getTicketsByEmployeeId(String employeeId) {
        log.info("Fetching tickets for employee: {}", employeeId);

        List<Ticket> tickets = ticketRepository.findByEmployeeId(employeeId);

        tickets.forEach(this::updateSlaStatus);

        return tickets;
    }

    public List<Ticket> getTicketsByPriority(TicketPriority priority) {
        log.info("Fetching tickets with priority: {}", priority);

        List<Ticket> tickets = ticketRepository.findByPriority(priority);
        tickets.forEach(this::updateSlaStatus);

        return tickets;
    }

    public List<Ticket> getAllTickets() {
        log.info("Fetching all tickets");
        List<Ticket> tickets = ticketRepository.findAll();
        tickets.forEach(this::updateSlaStatus);
        return tickets;
    }


    public List<Ticket> getSlaViolatedTickets() {
        log.info("Fetching SLA violated tickets");

        List<Ticket> allTickets = ticketRepository.findAll();

        return allTickets.stream()
                .peek(this::updateSlaStatus)
                .filter(Ticket::isSlaViolated)
                .collect(Collectors.toList());
    }


    public List<Ticket> getCriticalTickets() {
        log.info("Fetching critical tickets (near SLA breach)");

        List<Ticket> allTickets = ticketRepository.findAll();

        return allTickets.stream()
                .peek(this::updateSlaStatus)
                .filter(ticket -> !ticket.isSlaViolated())
                .filter(slaService::isCritical)
                .collect(Collectors.toList());
    }

    public void deleteTicket(String ticketId) {
        log.info("Deleting ticket: {}", ticketId);
        getTicketById(ticketId);
        ticketRepository.delete(ticketId);
        log.info("Ticket deleted successfully: {}", ticketId);
    }


    private void updateSlaStatus(Ticket ticket) {
        boolean currentlyViolated = slaService.isSlaViolated(ticket);

        if (currentlyViolated && !ticket.isSlaViolated()) {
            ticket.setSlaViolated(true);
            ticket.setSlaViolatedAt(LocalDateTime.now());
            ticketRepository.save(ticket);

            log.error("SLA VIOLATION: Ticket {} exceeded due date {}",
                    ticket.getTicketId(), ticket.getSlaDueDate());
        }
    }
}