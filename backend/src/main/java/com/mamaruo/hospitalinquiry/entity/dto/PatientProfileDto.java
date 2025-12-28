package com.mamaruo.hospitalinquiry.entity.dto;

import java.time.LocalDate;

public record PatientProfileDto(
    Integer id,
    String name,
    String gender,
    LocalDate birthDate,
    String medicalHistory
) {}
