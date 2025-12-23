package com.mamaruo.hospitalinquiry.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mamaruo.hospitalinquiry.entity.User;
import com.mamaruo.hospitalinquiry.entity.dto.LoginRequest;
import com.mamaruo.hospitalinquiry.entity.dto.LoginResponse;
import com.mamaruo.hospitalinquiry.entity.dto.UserResponse;
import com.mamaruo.hospitalinquiry.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserService(
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        AuthenticationManager authenticationManager,
        JwtService jwtService
    ) {
        this.userRepo = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public UserResponse signup(User user) {
        // 检查手机号是否已存在
        if (userRepo.findByMobile(user.getMobile()).isPresent()) {
            throw new IllegalArgumentException("该手机号已被注册");
        }
        
        // 检查身份证号是否已存在
        if (user.getIdCard() != null && userRepo.findByIdCard(user.getIdCard()).isPresent()) {
            throw new IllegalArgumentException("该身份证号已被注册");
        }
        
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepo.save(user);
        return UserResponse.fromUser(savedUser);
    }

    public LoginResponse verify(LoginRequest request) {
        try {
            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.mobile(), request.password()));

            if (auth.isAuthenticated()) {
                String token = jwtService.generateToken(request.mobile());
                User user = userRepo.findByMobile(request.mobile())
                    .orElseThrow(() -> new IllegalStateException("用户不存在"));
                return new LoginResponse(token, UserResponse.fromUser(user));
            } else {
                throw new BadCredentialsException("认证失败");
            }
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("手机号或密码错误");
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("登录失败：" + e.getMessage());
        }
    }
}