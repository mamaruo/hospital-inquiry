package com.mamaruo.hospitalinquiry.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mamaruo.hospitalinquiry.entity.MessageType;
import com.mamaruo.hospitalinquiry.entity.dto.MessageDto;
import com.mamaruo.hospitalinquiry.service.InquiryService;
import com.mamaruo.hospitalinquiry.service.JwtService;
import com.mamaruo.hospitalinquiry.service.MessageService;
import com.mamaruo.hospitalinquiry.repository.UserRepository;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final MessageService messageService;
    private final InquiryService inquiryService;

    // 存储 WebSocket 会话: inquiryId -> Map<userId, session>
    private final Map<Integer, Map<Integer, WebSocketSession>> inquirySessions = new ConcurrentHashMap<>();
    // 存储会话与用户的映射: sessionId -> {userId, inquiryId}
    private final Map<String, SessionInfo> sessionInfoMap = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(
            ObjectMapper objectMapper,
            JwtService jwtService,
            UserRepository userRepository,
            MessageService messageService,
            InquiryService inquiryService) {
        this.objectMapper = objectMapper;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.messageService = messageService;
        this.inquiryService = inquiryService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 从 query 参数中获取 token 和 inquiryId
        String query = session.getUri().getQuery();
        Map<String, String> params = parseQueryParams(query);
        
        String token = params.get("token");
        String inquiryIdStr = params.get("inquiryId");

        if (token == null || inquiryIdStr == null) {
            session.close(CloseStatus.BAD_DATA.withReason("缺少必要参数"));
            return;
        }

        try {
            // 验证 token
            String mobile = jwtService.extractMobile(token);
            var userOpt = userRepository.findByMobile(mobile);
            if (userOpt.isEmpty()) {
                session.close(CloseStatus.BAD_DATA.withReason("用户不存在"));
                return;
            }

            Integer userId = userOpt.get().getId();
            Integer inquiryId = Integer.parseInt(inquiryIdStr);

            // 验证用户是否有权访问该问诊
            if (!inquiryService.canAccessInquiry(inquiryId, userId)) {
                session.close(CloseStatus.BAD_DATA.withReason("无权访问该问诊"));
                return;
            }

            // 注册会话
            sessionInfoMap.put(session.getId(), new SessionInfo(userId, inquiryId));
            inquirySessions.computeIfAbsent(inquiryId, k -> new ConcurrentHashMap<>())
                    .put(userId, session);

            System.out.println("WebSocket 连接建立: userId=" + userId + ", inquiryId=" + inquiryId);

            // 发送连接成功消息
            sendMessage(session, new WebSocketResponse("connected", "连接成功", null));

        } catch (Exception e) {
            session.close(CloseStatus.BAD_DATA.withReason("认证失败: " + e.getMessage()));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        SessionInfo sessionInfo = sessionInfoMap.get(session.getId());
        if (sessionInfo == null) {
            return;
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(message.getPayload());
            String type = jsonNode.has("type") ? jsonNode.get("type").asText() : "message";

            if ("message".equals(type)) {
                String content = jsonNode.get("content").asText();
                String msgType = jsonNode.has("msgType") ? jsonNode.get("msgType").asText() : "TEXT";
                
                // 保存消息到数据库
                MessageDto savedMessage = messageService.saveMessage(
                        sessionInfo.inquiryId(),
                        sessionInfo.userId(),
                        MessageType.valueOf(msgType),
                        content
                );

                // 广播消息给该问诊的所有参与者
                broadcastToInquiry(sessionInfo.inquiryId(), new WebSocketResponse("message", null, savedMessage));
            }
        } catch (Exception e) {
            sendMessage(session, new WebSocketResponse("error", "消息处理失败: " + e.getMessage(), null));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        SessionInfo sessionInfo = sessionInfoMap.remove(session.getId());
        if (sessionInfo != null) {
            Map<Integer, WebSocketSession> sessions = inquirySessions.get(sessionInfo.inquiryId());
            if (sessions != null) {
                sessions.remove(sessionInfo.userId());
                if (sessions.isEmpty()) {
                    inquirySessions.remove(sessionInfo.inquiryId());
                }
            }
            System.out.println("WebSocket 连接关闭: userId=" + sessionInfo.userId() + ", inquiryId=" + sessionInfo.inquiryId());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.err.println("WebSocket 传输错误: " + exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }

    private void broadcastToInquiry(Integer inquiryId, WebSocketResponse response) {
        Map<Integer, WebSocketSession> sessions = inquirySessions.get(inquiryId);
        if (sessions != null) {
            sessions.values().forEach(session -> {
                try {
                    sendMessage(session, response);
                } catch (IOException e) {
                    System.err.println("发送消息失败: " + e.getMessage());
                }
            });
        }
    }

    private void sendMessage(WebSocketSession session, WebSocketResponse response) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(response)));
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> params = new ConcurrentHashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    params.put(pair[0], pair[1]);
                }
            }
        }
        return params;
    }

    private record SessionInfo(Integer userId, Integer inquiryId) {}
    
    private record WebSocketResponse(String type, String message, Object data) {}
}
