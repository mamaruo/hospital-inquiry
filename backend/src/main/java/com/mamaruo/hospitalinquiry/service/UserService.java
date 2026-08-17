package com.mamaruo.hospitalinquiry.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mamaruo.hospitalinquiry.entity.Role;
import com.mamaruo.hospitalinquiry.entity.User;
import com.mamaruo.hospitalinquiry.entity.dto.ChangePasswordRequest;
import com.mamaruo.hospitalinquiry.entity.dto.ForgotPasswordRequest;
import com.mamaruo.hospitalinquiry.entity.dto.LoginRequest;
import com.mamaruo.hospitalinquiry.entity.dto.LoginResponse;
import com.mamaruo.hospitalinquiry.entity.dto.SignupRequest;
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

    public UserResponse signup(SignupRequest request) {
        // 检查手机号是否已存在
        if (userRepo.findByMobile(request.mobile()).isPresent()) {
            throw new IllegalArgumentException("该手机号已被注册");
        }

        // 检查身份证号是否已存在
        if (userRepo.findByIdCard(request.idCard()).isPresent()) {
            throw new IllegalArgumentException("该身份证号已被注册");
        }

        User user = new User();
        user.setName(request.name());
        user.setMobile(request.mobile());
        user.setIdCard(request.idCard());
        user.setPassword(passwordEncoder.encode(request.password()));
        // 注册仅允许创建患者账户，防止客户端伪造角色
        user.setRole(Role.PATIENT);
        user.setEnabled(true);
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

    public UserResponse getCurrentUser(Integer userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return UserResponse.fromUser(user);
    }

    public UserResponse changePassword(Integer userId, ChangePasswordRequest request) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (!passwordEncoder.matches(request.oldPassword(), user.getPassword())) {
            throw new BadCredentialsException("原密码错误");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        return UserResponse.fromUser(userRepo.save(user));
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        // 统一返回"信息不匹配"，避免泄露是手机号还是身份证不正确
        User user = userRepo.findByMobile(request.mobile())
            .filter(u -> u.getIdCard() != null
                && u.getIdCard().equalsIgnoreCase(request.idCard()))
            .orElseThrow(() -> new IllegalArgumentException("信息不匹配，无法重置密码"));

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);
    }
}