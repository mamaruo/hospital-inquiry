package com.mamaruo.hospitalinquiry.entity.dto;

public record DoctorDto(
    Integer id,
    Integer userId,
    String name,
    String mobile,
    String departmentName,
    Integer departmentId,
    String title,
    String expertise,
    String photoUrl,
    Boolean available
) {}
