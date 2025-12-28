package com.mamaruo.hospitalinquiry.entity.dto;

public record CreateInquiryRequest(
    Integer patientProfileId,
    Integer doctorId,
    String symptomDescription
) {}
