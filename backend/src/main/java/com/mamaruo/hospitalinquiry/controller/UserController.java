package com.mamaruo.hospitalinquiry.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.mamaruo.hospitalinquiry.entity.UserPrincipal;
import com.mamaruo.hospitalinquiry.entity.dto.ChangePasswordRequest;
import com.mamaruo.hospitalinquiry.entity.dto.ForgotPasswordRequest;
import com.mamaruo.hospitalinquiry.entity.dto.LoginRequest;
import com.mamaruo.hospitalinquiry.entity.dto.LoginResponse;
import com.mamaruo.hospitalinquiry.entity.dto.SignupRequest;
import com.mamaruo.hospitalinquiry.entity.dto.UserResponse;
import com.mamaruo.hospitalinquiry.service.UserService;

import jakarta.validation.Valid;


@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserResponse signup(@Valid @RequestBody SignupRequest request) {
        return userService.signup(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.verify(request);
    }

    // 获取当前登录用户信息
    @GetMapping("/api/users/me")
    public UserResponse getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        return userService.getCurrentUser(principal.getId());
    }

    // 修改当前登录用户密码
    @PutMapping("/api/users/me/password")
    public UserResponse changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        return userService.changePassword(principal.getId(), request);
    }

    // 忘记密码：通过手机号+身份证号重置密码（公开接口）
    @PostMapping("/api/users/forgot-password")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userService.forgotPassword(request);
    }
}
