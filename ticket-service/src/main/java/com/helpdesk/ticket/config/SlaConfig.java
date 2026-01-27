package com.helpdesk.ticket.config;

import com.helpdesk.ticket.model.TicketPriority;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "sla")
public class SlaConfig {

    private Map<String, Integer> hours = new HashMap<>();

    public int getHoursForPriority(TicketPriority priority) {
        return hours.getOrDefault(priority.name(), getDefaultHours(priority));
    }

    private int getDefaultHours(TicketPriority priority) {
        return switch (priority) {
            case HIGH -> 24;      
            case MEDIUM -> 72;
            case LOW -> 168;
        };
    }
}