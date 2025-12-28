package com.mamaruo.hospitalinquiry.entity.dto;

import com.mamaruo.hospitalinquiry.entity.Role;
import com.mamaruo.hospitalinquiry.entity.User;

public record UserResponse(
    Integer id,
    String name,
    String mobile,
    String idCard,
    Role role,
    Boolean enabled
) {
    public static UserResponse fromUser(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getMobile(),
            user.getIdCard(),
            user.getRole(),
            user.getEnabled()
        );
    }
}
