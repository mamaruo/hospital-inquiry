package com.mamaruo.hospitalinquiry.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import com.mamaruo.hospitalinquiry.entity.Role;
import com.mamaruo.hospitalinquiry.entity.User;
import com.mamaruo.hospitalinquiry.entity.dto.UserResponse;
import com.mamaruo.hospitalinquiry.repository.UserRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 获取所有患者
    @GetMapping("/patients")
    public List<UserResponse> getAllPatients() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.PATIENT)
                .map(UserResponse::fromUser)
                .toList();
    }

    // 获取所有医生用户
    @GetMapping("/doctors")
    public List<UserResponse> getAllDoctors() {
        return userRepository.findAll().stream()
                .filter(u -> u.getRole() == Role.DOCTOR)
                .map(UserResponse::fromUser)
                .toList();
    }

    // 获取所有用户
    @GetMapping("/users")
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::fromUser)
                .toList();
    }

    // 搜索用户
    @GetMapping("/users/search")
    public List<UserResponse> searchUsers(@RequestParam String keyword) {
        return userRepository.findAll().stream()
                .filter(u -> (u.getMobile() != null && u.getMobile().contains(keyword)) 
                        || (u.getName() != null && u.getName().contains(keyword)))
                .map(UserResponse::fromUser)
                .toList();
    }

    // 启用/禁用用户
    @PutMapping("/users/{id}/toggle-status")
    public UserResponse toggleUserStatus(@PathVariable Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setEnabled(!user.getEnabled());
        return UserResponse.fromUser(userRepository.save(user));
    }

    // 重置用户密码
    @PutMapping("/users/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(
            @PathVariable Integer id,
            @RequestBody ResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    // 创建管理员账号
    @PostMapping("/create-admin")
    public UserResponse createAdmin(@RequestBody CreateAdminRequest request) {
        if (userRepository.findByMobile(request.mobile()).isPresent()) {
            throw new IllegalArgumentException("该手机号已被注册");
        }
        
        User user = new User();
        user.setMobile(request.mobile());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setRole(Role.ADMIN);
        
        return UserResponse.fromUser(userRepository.save(user));
    }

    private record ResetPasswordRequest(String newPassword) {}
    private record CreateAdminRequest(String mobile, String password, String name) {}
}
