package com.mamaruo.hospitalinquiry.entity.dto;

import com.mamaruo.hospitalinquiry.entity.MessageType;

public record SendMessageRequest(
    Integer inquiryId,
    MessageType type,
    String content
) {}
