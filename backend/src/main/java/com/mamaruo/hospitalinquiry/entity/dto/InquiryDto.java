package com.mamaruo.hospitalinquiry.entity.dto;

import java.time.LocalDateTime;
import com.mamaruo.hospitalinquiry.entity.InquiryStatus;

public record InquiryDto(
    Integer id,
    PatientProfileDto patientProfile,
    DoctorDto doctor,
    String symptomDescription,
    InquiryStatus status,
    LocalDateTime createdAt,
    LocalDateTime acceptedAt,
    LocalDateTime completedAt
) {}
