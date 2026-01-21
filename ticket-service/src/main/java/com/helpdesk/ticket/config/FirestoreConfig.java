package com.helpdesk.ticket.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
@Configuration
public class FirestoreConfig {

    @Value("${google.cloud.project-id}")
    private String projectId;

    @Value("${firestore.emulator.enabled:true}")
    private boolean emulatorEnabled;

    @Bean
    public Firestore firestore() throws IOException {

        if (emulatorEnabled) {
            log.info("Initializing Firestore with EMULATOR for project: {}", projectId);

            // Set emulator host BEFORE creating any Firestore instances
            String emulatorHost = "localhost:8086";
            System.setProperty("FIRESTORE_EMULATOR_HOST", emulatorHost);

            // Use FirestoreOptions directly for emulator (simpler than Firebase SDK)
            FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                    .setProjectId(projectId)
                    .setEmulatorHost(emulatorHost)
                    .build();

            return firestoreOptions.getService();

        } else {
            log.info("Initializing Firestore with SERVICE ACCOUNT for project: {}", projectId);

            FileInputStream serviceAccount = new FileInputStream("serviceAccountKey.json");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId(projectId)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            return FirestoreClient.getFirestore();
        }
    }
}