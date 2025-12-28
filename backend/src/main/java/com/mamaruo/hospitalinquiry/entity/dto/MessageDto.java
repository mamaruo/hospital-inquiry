package com.mamaruo.hospitalinquiry.entity.dto;

import java.time.LocalDateTime;
import com.mamaruo.hospitalinquiry.entity.MessageType;

public record MessageDto(
    Long id,
    Integer inquiryId,
    Integer senderId,
    String senderName,
    String senderRole,
    MessageType type,
    String content,
    LocalDateTime createdAt
) {}
