package com.mamaruo.hospitalinquiry.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.mamaruo.hospitalinquiry.entity.dto.MessageDto;
import com.mamaruo.hospitalinquiry.service.MessageService;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    // 获取问诊的所有消息（用于初始化聊天界面）
    @GetMapping("/inquiry/{inquiryId}")
    public List<MessageDto> getMessagesByInquiry(@PathVariable Integer inquiryId) {
        return messageService.getMessagesByInquiryId(inquiryId);
    }

    // 获取问诊的新消息（用于轮询，作为 WebSocket 的备用方案）
    @GetMapping("/inquiry/{inquiryId}/new")
    public List<MessageDto> getNewMessages(
            @PathVariable Integer inquiryId,
            @RequestParam Long afterId) {
        return messageService.getNewMessages(inquiryId, afterId);
    }
}
