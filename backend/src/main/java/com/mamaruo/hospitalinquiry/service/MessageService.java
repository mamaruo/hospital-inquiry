package com.mamaruo.hospitalinquiry.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mamaruo.hospitalinquiry.entity.Inquiry;
import com.mamaruo.hospitalinquiry.entity.Message;
import com.mamaruo.hospitalinquiry.entity.MessageType;
import com.mamaruo.hospitalinquiry.entity.User;
import com.mamaruo.hospitalinquiry.entity.dto.MessageDto;
import com.mamaruo.hospitalinquiry.repository.InquiryRepository;
import com.mamaruo.hospitalinquiry.repository.MessageRepository;
import com.mamaruo.hospitalinquiry.repository.UserRepository;

@Service
public class MessageService {
    private final MessageRepository messageRepository;
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    public MessageService(
            MessageRepository messageRepository,
            InquiryRepository inquiryRepository,
            UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.inquiryRepository = inquiryRepository;
        this.userRepository = userRepository;
    }

    public List<MessageDto> getMessagesByInquiryId(Integer inquiryId) {
        return messageRepository.findByInquiryIdOrderByCreatedAtAsc(inquiryId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<MessageDto> getNewMessages(Integer inquiryId, Long afterId) {
        return messageRepository.findByInquiryIdAndIdGreaterThanOrderByCreatedAtAsc(inquiryId, afterId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDto saveMessage(Integer inquiryId, Integer senderId, MessageType type, String content) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("问诊不存在"));
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        Message message = new Message();
        message.setInquiry(inquiry);
        message.setSender(sender);
        message.setType(type);
        message.setContent(content);

        return toDto(messageRepository.save(message));
    }

    public MessageDto toDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getInquiry().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getSender().getRole().name(),
                message.getType(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
