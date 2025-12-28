package com.mamaruo.hospitalinquiry.entity.dto;

import java.time.LocalDate;

public record CreatePatientProfileRequest(
    String name,
    String gender,
    LocalDate birthDate,
    String medicalHistory
) {}
