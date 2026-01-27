package com.helpdesk.ticket.service;

import com.helpdesk.ticket.config.SlaConfig;
import com.helpdesk.ticket.model.Ticket;
import com.helpdesk.ticket.model.TicketPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlaService {

    private final SlaConfig slaConfig;

    public LocalDateTime calculateSlaDueDate(Ticket ticket) {
        int slaHours = slaConfig.getHoursForPriority(ticket.getPriority());
        LocalDateTime dueDate = ticket.getCreatedAt().plusHours(slaHours);

        log.debug("SLA due date calculated for ticket {}: {} (Priority: {}, SLA: {} hours)",
                ticket.getTicketId(), dueDate, ticket.getPriority(), slaHours);

        return dueDate;
    }


    public boolean isSlaViolated(Ticket ticket) {
        if (ticket.getSlaDueDate() == null) {
            return false;
        }

        boolean violated = LocalDateTime.now().isAfter(ticket.getSlaDueDate());

        if (violated && !ticket.isSlaViolated()) {
            log.warn("SLA VIOLATION detected for ticket {}: Due date {} passed",
                    ticket.getTicketId(), ticket.getSlaDueDate());
        }

        return violated;
    }

    public Duration getRemainingTime(Ticket ticket) {
        if (ticket.getSlaDueDate() == null) {
            return Duration.ZERO;
        }

        Duration remaining = Duration.between(LocalDateTime.now(), ticket.getSlaDueDate());
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }


    public Duration getOverdueTime(Ticket ticket) {
        if (ticket.getSlaDueDate() == null || !ticket.isSlaViolated()) {
            return Duration.ZERO;
        }

        return Duration.between(ticket.getSlaDueDate(), LocalDateTime.now());
    }

    public boolean isCritical(Ticket ticket) {
        if (ticket.isSlaViolated()) {
            return true;
        }

        Duration remaining = getRemainingTime(ticket);
        return remaining.toHours() < 2;
    }

    public String getSlaStatusMessage(Ticket ticket) {
        if (ticket.isSlaViolated()) {
            Duration overdue = getOverdueTime(ticket);
            return String.format("SLA VIOLATED: Overdue by %d hours %d minutes",
                    overdue.toHours(), overdue.toMinutesPart());
        }

        Duration remaining = getRemainingTime(ticket);
        if (isCritical(ticket)) {
            return String.format("CRITICAL: %d hours %d minutes remaining",
                    remaining.toHours(), remaining.toMinutesPart());
        }

        return String.format("On track: %d hours %d minutes remaining",
                remaining.toHours(), remaining.toMinutesPart());
    }
}