package com.helpdesk.status.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusHistory {

    @DocumentId
    private String statusId;

    private String ticketId;
    private TicketStatus status;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String comments;

    public static String generateStatusId() {
        return "STS-" + System.currentTimeMillis();
    }
}