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

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;

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
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket created successfully: {}", savedTicket.getTicketId());

        return savedTicket;
    }


    public Ticket getTicketById(String ticketId) {
        log.info("Fetching ticket: {}", ticketId);

        return ticketRepository.findById(ticketId)
                .orElseThrow(() -> new TicketNotFoundException("Ticket not found with ID: " + ticketId));
    }


    public List<Ticket> getTicketsByEmployeeId(String employeeId) {
        log.info("Fetching tickets for employee: {}", employeeId);

        List<Ticket> tickets = ticketRepository.findByEmployeeId(employeeId);

        if (tickets.isEmpty()) {
            log.warn("No tickets found for employee: {}", employeeId);
        }

        return tickets;
    }

    public List<Ticket> getTicketsByPriority(TicketPriority priority) {
        log.info("Fetching tickets with priority: {}", priority);

        List<Ticket> tickets = ticketRepository.findByPriority(priority);

        if (tickets.isEmpty()) {
            log.warn("No tickets found with priority: {}", priority);
        }

        return tickets;
    }

    public List<Ticket> getAllTickets() {
        log.info("Fetching all tickets");
        return ticketRepository.findAll();
    }

    public void deleteTicket(String ticketId) {
        log.info("Deleting ticket: {}", ticketId);

        getTicketById(ticketId);

        ticketRepository.delete(ticketId);
        log.info("Ticket deleted successfully: {}", ticketId);
    }
}