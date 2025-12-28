package com.mamaruo.hospitalinquiry.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mamaruo.hospitalinquiry.entity.Department;
import com.mamaruo.hospitalinquiry.entity.DoctorProfile;
import com.mamaruo.hospitalinquiry.entity.Role;
import com.mamaruo.hospitalinquiry.entity.User;
import com.mamaruo.hospitalinquiry.entity.dto.CreateDoctorRequest;
import com.mamaruo.hospitalinquiry.entity.dto.DoctorDto;
import com.mamaruo.hospitalinquiry.repository.DepartmentRepository;
import com.mamaruo.hospitalinquiry.repository.DoctorProfileRepository;
import com.mamaruo.hospitalinquiry.repository.UserRepository;

@Service
public class DoctorService {
    private final DoctorProfileRepository doctorProfileRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DoctorService(
            DoctorProfileRepository doctorProfileRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.doctorProfileRepository = doctorProfileRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<DoctorDto> getAllDoctors() {
        return doctorProfileRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<DoctorDto> getAvailableDoctors() {
        return doctorProfileRepository.findByAvailableTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<DoctorDto> getDoctorsByDepartment(Integer departmentId) {
        return doctorProfileRepository.findByDepartmentIdAndAvailableTrue(departmentId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DoctorDto getDoctorById(Integer id) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("医生不存在"));
        return toDto(profile);
    }

    public DoctorDto getDoctorByUserId(Integer userId) {
        DoctorProfile profile = doctorProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("医生档案不存在"));
        return toDto(profile);
    }

    @Transactional
    public DoctorDto createDoctor(CreateDoctorRequest request) {
        // 检查手机号是否已存在
        if (userRepository.findByMobile(request.mobile()).isPresent()) {
            throw new IllegalArgumentException("该手机号已被注册");
        }

        Department dept = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new IllegalArgumentException("科室不存在"));

        // 创建用户账号
        User user = new User();
        user.setMobile(request.mobile());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setRole(Role.DOCTOR);
        user = userRepository.save(user);

        // 创建医生档案
        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setDepartment(dept);
        profile.setTitle(request.title());
        profile.setExpertise(request.expertise());
        profile.setAvailable(true);

        return toDto(doctorProfileRepository.save(profile));
    }

    @Transactional
    public DoctorDto createDoctorFromJson(String name, String departmentName, String title, 
            String expertise, String photoPath) {
        // 获取或创建科室
        Department dept = departmentRepository.findByName(departmentName)
                .orElseGet(() -> {
                    Department newDept = new Department(departmentName);
                    return departmentRepository.save(newDept);
                });

        // 生成手机号（使用名字哈希值作为后缀，确保唯一性）
        String mobile = "138" + String.format("%08d", Math.abs(name.hashCode()) % 100000000);
        
        // 检查手机号是否已存在，如果存在则跳过
        if (userRepository.findByMobile(mobile).isPresent()) {
            DoctorProfile existing = doctorProfileRepository.findAll().stream()
                    .filter(d -> d.getUser().getName().equals(name))
                    .findFirst()
                    .orElse(null);
            if (existing != null) {
                return toDto(existing);
            }
            // 如果手机号重复但不是同一个医生，则修改手机号
            mobile = "139" + String.format("%08d", Math.abs((name + System.currentTimeMillis()).hashCode()) % 100000000);
        }

        // 创建用户账号
        User user = new User();
        user.setMobile(mobile);
        user.setPassword(passwordEncoder.encode("123456")); // 默认密码
        user.setName(name);
        user.setRole(Role.DOCTOR);
        user = userRepository.save(user);

        // 创建医生档案
        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setDepartment(dept);
        profile.setTitle(title);
        profile.setExpertise(expertise);
        profile.setPhotoPath(photoPath);
        profile.setAvailable(true);

        return toDto(doctorProfileRepository.save(profile));
    }

    @Transactional
    public DoctorDto updateDoctor(Integer id, Integer departmentId, String title, 
            String expertise, Boolean available) {
        DoctorProfile profile = doctorProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("医生不存在"));

        if (departmentId != null) {
            Department dept = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new IllegalArgumentException("科室不存在"));
            profile.setDepartment(dept);
        }
        if (title != null) {
            profile.setTitle(title);
        }
        if (expertise != null) {
            profile.setExpertise(expertise);
        }
        if (available != null) {
            profile.setAvailable(available);
        }

        return toDto(doctorProfileRepository.save(profile));
    }

    public DoctorDto toDto(DoctorProfile profile) {
        return new DoctorDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getName(),
                profile.getUser().getMobile(),
                profile.getDepartment().getName(),
                profile.getDepartment().getId(),
                profile.getTitle(),
                profile.getExpertise(),
                profile.getPhotoPath() != null ? "/api/files/" + profile.getId() + "/photo" : null,
                profile.getAvailable()
        );
    }
}
