package ru.skillbox.socialnetwork.dialog.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.skillbox.socialnetwork.dialog.dto.NotificationEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

    public void send(NotificationEvent event) {
        kafkaTemplate.send("NOTIFICATION", event.getUserId().toString(), event);
        log.info("Notification sent: {}", event);
    }
}
