package com.mamaruo.hospitalinquiry.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mamaruo.hospitalinquiry.entity.Inquiry;
import com.mamaruo.hospitalinquiry.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByInquiryOrderByCreatedAtAsc(Inquiry inquiry);
    List<Message> findByInquiryIdOrderByCreatedAtAsc(Integer inquiryId);
    
    // 获取某条消息之后的新消息
    List<Message> findByInquiryIdAndIdGreaterThanOrderByCreatedAtAsc(Integer inquiryId, Long afterId);
}
