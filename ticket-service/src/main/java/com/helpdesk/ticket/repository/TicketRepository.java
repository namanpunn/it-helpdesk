package com.helpdesk.ticket.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.helpdesk.ticket.exception.TicketServiceException;
import com.helpdesk.ticket.model.Ticket;
import com.helpdesk.ticket.model.TicketPriority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TicketRepository {

    private final Firestore firestore;

    @Value("${firestore.collection.tickets}")
    private String collectionName;


    public Ticket save(Ticket ticket) {
        try {
            log.info("Saving ticket: {}", ticket.getTicketId());

            DocumentReference docRef = firestore.collection(collectionName)
                    .document(ticket.getTicketId());

            ApiFuture<WriteResult> result = docRef.set(convertToMap(ticket));

            WriteResult writeResult = result.get();
            log.info("Ticket saved successfully at: {}", writeResult.getUpdateTime());

            return ticket;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error saving ticket: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new TicketServiceException("Failed to save ticket", e);
        }
    }


    public Optional<Ticket> findById(String ticketId) {
        try {
            log.info("Finding ticket by ID: {}", ticketId);

            DocumentReference docRef = firestore.collection(collectionName).document(ticketId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                Ticket ticket = documentToTicket(document);
                log.info("Ticket found: {}", ticketId);
                return Optional.of(ticket);
            }

            log.warn("Ticket not found: {}", ticketId);
            return Optional.empty();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding ticket: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new TicketServiceException("Failed to find ticket", e);
        }
    }

    public List<Ticket> findByEmployeeId(String employeeId) {
        try {
            log.info("Finding tickets for employee: {}", employeeId);

            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
                    .whereEqualTo("employeeId", employeeId)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Ticket> tickets = new ArrayList<>();

            for (QueryDocumentSnapshot document : documents) {
                tickets.add(documentToTicket(document));
            }

            log.info("Found {} tickets for employee: {}", tickets.size(), employeeId);
            return tickets;

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding tickets by employee: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new TicketServiceException("Failed to find tickets by employee", e);
        }
    }

    public List<Ticket> findByPriority(TicketPriority priority) {
        try {
            log.info("Finding tickets with priority: {}", priority);

            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
                    .whereEqualTo("priority", priority.name())
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Ticket> tickets = new ArrayList<>();

            for (QueryDocumentSnapshot document : documents) {
                tickets.add(documentToTicket(document));
            }

            log.info("Found {} tickets with priority: {}", tickets.size(), priority);
            return tickets;

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding tickets by priority: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new TicketServiceException("Failed to find tickets by priority", e);
        }
    }

    public List<Ticket> findAll() {
        try {
            log.info("Finding all tickets");

            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName).get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<Ticket> tickets = new ArrayList<>();

            for (QueryDocumentSnapshot document : documents) {
                tickets.add(documentToTicket(document));
            }

            log.info("Found {} total tickets", tickets.size());
            return tickets;

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding all tickets: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new TicketServiceException("Failed to find all tickets", e);
        }
    }


    public void delete(String ticketId) {
        try {
            log.info("Deleting ticket: {}", ticketId);

            ApiFuture<WriteResult> writeResult = firestore.collection(collectionName)
                    .document(ticketId)
                    .delete();

            writeResult.get();
            log.info("Ticket deleted: {}", ticketId);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error deleting ticket: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new TicketServiceException("Failed to delete ticket", e);
        }
    }


    private Ticket documentToTicket(DocumentSnapshot document) {
        return Ticket.builder()
                .ticketId(document.getId())
                .employeeId(document.getString("employeeId"))
                .employeeName(document.getString("employeeName"))
                .category(com.helpdesk.ticket.model.TicketCategory.valueOf(document.getString("category")))
                .description(document.getString("description"))
                .priority(TicketPriority.valueOf(document.getString("priority")))
                .createdAt(dateToLocalDateTime(document.getDate("createdAt")))
                .createdBy(document.getString("createdBy"))
                .build();
    }

    private Object convertToMap(Ticket ticket) {
        return new java.util.HashMap<String, Object>() {{
            put("ticketId", ticket.getTicketId());
            put("employeeId", ticket.getEmployeeId());
            put("employeeName", ticket.getEmployeeName());
            put("category", ticket.getCategory().name());
            put("description", ticket.getDescription());
            put("priority", ticket.getPriority().name());
            put("createdAt", localDateTimeToDate(ticket.getCreatedAt()));
            put("createdBy", ticket.getCreatedBy());
        }};
    }

    private Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}