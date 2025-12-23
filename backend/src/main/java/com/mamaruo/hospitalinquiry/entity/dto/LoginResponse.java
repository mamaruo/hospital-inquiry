package com.mamaruo.hospitalinquiry.entity.dto;

public record LoginResponse(
    String token,
    UserResponse user
) {
}
