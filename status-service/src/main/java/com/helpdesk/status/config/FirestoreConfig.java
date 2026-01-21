package com.helpdesk.status.config;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FirestoreConfig {

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${firestore.emulator.enabled:true}")
    private boolean emulatorEnabled;

    @Bean
    public Firestore firestore() {

        if (emulatorEnabled) {
            log.info("Initializing Firestore with EMULATOR for project: {}", projectId);

            String emulatorHost = "localhost:8086";
            System.setProperty("FIRESTORE_EMULATOR_HOST", emulatorHost);

            FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                    .setProjectId(projectId)
                    .setEmulatorHost(emulatorHost)
                    .build();

            return firestoreOptions.getService();
        } else {
            log.info("Initializing Firestore with SERVICE ACCOUNT for project: {}", projectId);

            FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                    .setProjectId(projectId)
                    .build();

            return firestoreOptions.getService();
        }
    }
}