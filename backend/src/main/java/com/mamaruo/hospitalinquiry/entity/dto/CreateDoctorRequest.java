package com.mamaruo.hospitalinquiry.entity.dto;

public record CreateDoctorRequest(
    String mobile,
    String password,
    String name,
    Integer departmentId,
    String title,
    String expertise
) {}
