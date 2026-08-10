package com.atlas.notification.adapter.in.messaging;

import com.atlas.notification.adapter.out.persistence.ProcessedEventJpaEntity;
import com.atlas.notification.adapter.out.persistence.ProcessedEventJpaRepository;
import com.atlas.shared.messaging.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@Configuration
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);

    private final ProcessedEventJpaRepository processedEventRepo;
    private final ObjectMapper objectMapper;

    public NotificationEventListener(
            ProcessedEventJpaRepository processedEventRepo,
            ObjectMapper objectMapper) {
        this.processedEventRepo = processedEventRepo;
        this.objectMapper = objectMapper;
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(RabbitMQConfig.NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMQConfig.DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMQConfig.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orgCreatedBinding(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(eventsExchange)
                .with("organization.#");
    }

    @Bean
    public Binding workspaceCreatedBinding(Queue notificationQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(eventsExchange)
                .with("workspace.#");
    }

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    @Transactional
    public void onMessage(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            
            if (!root.has("eventId")) {
                log.warn("Received message without eventId, skipping: {}", payload);
                return;
            }

            UUID eventId = UUID.fromString(root.get("eventId").asText());
            String eventType = root.has("eventType") ? root.get("eventType").asText() : "UNKNOWN";

            // Idempotency check: Skip duplicate messages
            if (processedEventRepo.existsById(eventId)) {
                log.info("Duplicate event detected [{}], skipping processing (Idempotency guaranteed)", eventId);
                return;
            }

            log.info("Processing notification for event [{}] of type [{}]: payload={}", eventId, eventType, payload);

            // Execute notification business logic (e.g. Email dispatch / webhook)
            sendNotification(eventType, root);

            // Mark event as processed in database
            processedEventRepo.save(new ProcessedEventJpaEntity(eventId, eventType));
            log.info("Event [{}] successfully processed and recorded in processed_events", eventId);

        } catch (Exception e) {
            log.error("Error processing notification message: {}", payload, e);
            throw new RuntimeException("Message processing failed, triggering retry/DLQ routing", e);
        }
    }

    private void sendNotification(String eventType, JsonNode payload) {
        switch (eventType) {
            case "OrganizationCreated" -> {
                String orgName = payload.has("name") ? payload.get("name").asText() : "N/A";
                log.info("📧 [NOTIFICATION] Dispatching Welcome Email for newly registered Organization: '{}'", orgName);
            }
            case "WorkspaceCreated" -> {
                String wsName = payload.has("name") ? payload.get("name").asText() : "N/A";
                log.info("📧 [NOTIFICATION] Dispatching Workspace Provisioned Email for Workspace: '{}'", wsName);
            }
            default -> log.info("📢 [NOTIFICATION] Generic event notification dispatched for: {}", eventType);
        }
    }
}
