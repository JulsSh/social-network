package ru.skillbox.socialnetwork.dialog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import ru.skillbox.socialnetwork.dialog.dto.MessageDto;
import ru.skillbox.socialnetwork.dialog.dto.MessageRequest;
import ru.skillbox.socialnetwork.dialog.dto.MessageSocketDto;
import ru.skillbox.socialnetwork.dialog.service.DialogService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper mapper;
    private final DialogService dialogService;
    private final Map<UUID, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        UUID userId = (UUID) session.getAttributes().get("userId");
        sessions.put(userId, session);
        log.info("WebSocket connected, session {}", session.getAttributes());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        UUID userId = (UUID) session.getAttributes().get("userId");
        sessions.remove(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        MessageRequest request = mapper.readValue(message.getPayload(), MessageRequest.class);
        UUID senderId = (UUID) session.getAttributes().get("userId");
        MessageDto saved = dialogService.saveMessage(senderId, request.recipientId(), request.data().messageText());
        MessageSocketDto payload = new MessageSocketDto(saved.id(), saved.dialogId(), saved.time(), saved.messageText(),
                saved.conversationPartner1(), saved.conversationPartner2(),saved.readStatus(), saved.stubDate(), saved.date(),
                senderId, request.data().clientId());
        String json = mapper.writeValueAsString(Map.of("type", "MESSAGE", "recipientId", request.recipientId(), "data", payload));
        WebSocketSession recipientSession = sessions.get(request.recipientId());
        if (recipientSession != null && recipientSession.isOpen()) {
            recipientSession.sendMessage(new TextMessage(json));
        }
        WebSocketSession senderSession =  sessions.get(senderId);
        if(senderSession !=null && senderSession.isOpen()){
            senderSession.sendMessage(new TextMessage(json));
        }
        log.info("WebSocket message: {}", message.getPayload());
    }
}