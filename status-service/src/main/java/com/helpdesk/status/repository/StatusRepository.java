package com.helpdesk.status.repository;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.helpdesk.status.exception.StatusServiceException;
import com.helpdesk.status.model.StatusHistory;
import com.helpdesk.status.model.TicketStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StatusRepository {

    private final Firestore firestore;

    @Value("${firestore.collection.status}")
    private String collectionName;

    public StatusHistory save(StatusHistory statusHistory) {
        try {
            log.info("Saving status update for ticket: {}", statusHistory.getTicketId());

            DocumentReference docRef = firestore.collection(collectionName)
                    .document(statusHistory.getStatusId());

            ApiFuture<WriteResult> result = docRef.set(convertToMap(statusHistory));

            WriteResult writeResult = result.get();
            log.info("Status saved successfully at: {}", writeResult.getUpdateTime());

            return statusHistory;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error saving status: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new StatusServiceException("Failed to save status", e);
        }
    }

    public Optional<StatusHistory> findById(String statusId) {
        try {
            log.info("Finding status by ID: {}", statusId);

            DocumentReference docRef = firestore.collection(collectionName).document(statusId);
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                StatusHistory status = documentToStatusHistory(document);
                log.info("Status found: {}", statusId);
                return Optional.of(status);
            }

            log.warn("Status not found: {}", statusId);
            return Optional.empty();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding status: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new StatusServiceException("Failed to find status", e);
        }
    }
    public List<StatusHistory> findByTicketId(String ticketId) {
        try {
            log.info("Finding status history for ticket: {}", ticketId);

            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
                    .whereEqualTo("ticketId", ticketId)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<StatusHistory> statusHistory = new ArrayList<>();

            for (QueryDocumentSnapshot document : documents) {
                statusHistory.add(documentToStatusHistory(document));
            }

            log.info("Found {} status updates for ticket: {}", statusHistory.size(), ticketId);
            return statusHistory;

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding status history: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new StatusServiceException("Failed to find status history", e);
        }
    }

    public Optional<StatusHistory> findCurrentStatusByTicketId(String ticketId) {
        try {
            log.info("Finding current status for ticket: {}", ticketId);

            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
                    .whereEqualTo("ticketId", ticketId)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .limit(1)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            if (!documents.isEmpty()) {
                StatusHistory status = documentToStatusHistory(documents.get(0));
                log.info("Current status found for ticket {}: {}", ticketId, status.getStatus());
                return Optional.of(status);
            }

            log.warn("No status found for ticket: {}", ticketId);
            return Optional.empty();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding current status: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new StatusServiceException("Failed to find current status", e);
        }
    }

    public Map<TicketStatus, Long> getStatusSummaryByDate(LocalDate date) {
        try {
            log.info("Getting status summary for date: {}", date);

            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
                    .whereGreaterThanOrEqualTo("updatedAt", localDateTimeToDate(startOfDay))
                    .whereLessThan("updatedAt", localDateTimeToDate(endOfDay))
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();

            Map<TicketStatus, Long> summary = new HashMap<>();
            for (TicketStatus status : TicketStatus.values()) {
                summary.put(status, 0L);
            }

            Map<String, TicketStatus> latestStatusPerTicket = new HashMap<>();

            for (QueryDocumentSnapshot document : documents) {
                StatusHistory status = documentToStatusHistory(document);
                String ticketId = status.getTicketId();

                if (!latestStatusPerTicket.containsKey(ticketId) ||
                        status.getUpdatedAt().isAfter(
                                documents.stream()
                                        .filter(d -> d.getString("ticketId").equals(ticketId))
                                        .map(this::documentToStatusHistory)
                                        .findFirst()
                                        .get()
                                        .getUpdatedAt()
                        )) {
                    latestStatusPerTicket.put(ticketId, status.getStatus());
                }
            }

            for (TicketStatus status : latestStatusPerTicket.values()) {
                summary.put(status, summary.get(status) + 1);
            }

            log.info("Status summary for {}: {}", date, summary);
            return summary;

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting status summary: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new StatusServiceException("Failed to get status summary", e);
        }
    }


    public List<StatusHistory> findAll() {
        try {
            log.info("Finding all status updates");

            ApiFuture<QuerySnapshot> future = firestore.collection(collectionName)
                    .orderBy("updatedAt", Query.Direction.DESCENDING)
                    .get();

            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            List<StatusHistory> statusUpdates = new ArrayList<>();

            for (QueryDocumentSnapshot document : documents) {
                statusUpdates.add(documentToStatusHistory(document));
            }

            log.info("Found {} total status updates", statusUpdates.size());
            return statusUpdates;

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding all status updates: {}", e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new StatusServiceException("Failed to find all status updates", e);
        }
    }


    private StatusHistory documentToStatusHistory(DocumentSnapshot document) {
        return StatusHistory.builder()
                .statusId(document.getId())
                .ticketId(document.getString("ticketId"))
                .status(TicketStatus.valueOf(document.getString("status")))
                .updatedBy(document.getString("updatedBy"))
                .updatedAt(dateToLocalDateTime(document.getDate("updatedAt")))
                .comments(document.getString("comments"))
                .build();
    }

    private Map<String, Object> convertToMap(StatusHistory statusHistory) {
        Map<String, Object> map = new HashMap<>();
        map.put("statusId", statusHistory.getStatusId());
        map.put("ticketId", statusHistory.getTicketId());
        map.put("status", statusHistory.getStatus().name());
        map.put("updatedBy", statusHistory.getUpdatedBy());
        map.put("updatedAt", localDateTimeToDate(statusHistory.getUpdatedAt()));
        map.put("comments", statusHistory.getComments());
        return map;
    }

    private Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}