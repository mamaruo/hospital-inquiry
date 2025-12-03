package com.mamaruo.hospitalinquiry.entity.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank String mobile,
    @NotBlank String password
) {

}