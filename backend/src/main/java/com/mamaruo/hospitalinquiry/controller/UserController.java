package com.mamaruo.hospitalinquiry.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mamaruo.hospitalinquiry.entity.User;
import com.mamaruo.hospitalinquiry.entity.dto.LoginRequest;
import com.mamaruo.hospitalinquiry.entity.dto.LoginResponse;
import com.mamaruo.hospitalinquiry.entity.dto.UserResponse;
import com.mamaruo.hospitalinquiry.service.UserService;


@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserResponse signup(@RequestBody User user) {
        return userService.signup(user);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.verify(request);
    }
}