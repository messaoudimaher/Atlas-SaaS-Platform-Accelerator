package com.atlas.org.adapter.out.messaging;

import com.atlas.shared.messaging.config.RabbitMQConfig;
import com.atlas.shared.messaging.outbox.OutboxEvent;
import com.atlas.shared.messaging.outbox.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxEventPublisherScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisherScheduler.class);

    private final OutboxEventRepository outboxEventRepo;
    private final RabbitTemplate rabbitTemplate;

    public OutboxEventPublisherScheduler(
            OutboxEventRepository outboxEventRepo,
            RabbitTemplate rabbitTemplate) {
        this.outboxEventRepo = outboxEventRepo;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepo.findTop50ByStatusOrderByCreatedAtAsc(OutboxEvent.Status.PENDING);

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Found {} pending outbox events to publish", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                String routingKey = resolveRoutingKey(event.getEventType());
                
                log.info("Publishing outbox event [{}] to exchange [{}] with routing key [{}]", 
                        event.getId(), RabbitMQConfig.EVENTS_EXCHANGE, routingKey);

                rabbitTemplate.convertAndSend(RabbitMQConfig.EVENTS_EXCHANGE, routingKey, event.getPayload());

                event.markProcessed();
                outboxEventRepo.save(event);
                log.debug("Outbox event [{}] marked as PROCESSED", event.getId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event [{}]: {}", event.getId(), e.getMessage(), e);
                event.markFailed();
                outboxEventRepo.save(event);
            }
        }
    }

    private String resolveRoutingKey(String eventType) {
        return switch (eventType) {
            case "OrganizationCreated" -> RabbitMQConfig.ROUTING_KEY_ORG_CREATED;
            case "WorkspaceCreated" -> RabbitMQConfig.ROUTING_KEY_WORKSPACE_CREATED;
            default -> eventType.toLowerCase().replace("event", "");
        };
    }
}
