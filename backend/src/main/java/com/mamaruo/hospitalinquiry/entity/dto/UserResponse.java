package com.mamaruo.hospitalinquiry.entity.dto;

import com.mamaruo.hospitalinquiry.entity.User;

public record UserResponse(
    Integer id,
    String name,
    String mobile,
    String idCard
) {
    public static UserResponse fromUser(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getMobile(),
            user.getIdCard()
        );
    }
}
