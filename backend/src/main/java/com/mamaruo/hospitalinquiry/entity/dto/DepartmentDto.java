package com.mamaruo.hospitalinquiry.entity.dto;

public record DepartmentDto(
    Integer id,
    String name,
    String description,
    Integer doctorCount
) {}
