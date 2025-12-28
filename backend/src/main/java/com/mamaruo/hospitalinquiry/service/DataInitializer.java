package com.mamaruo.hospitalinquiry.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mamaruo.hospitalinquiry.entity.Role;
import com.mamaruo.hospitalinquiry.entity.User;
import com.mamaruo.hospitalinquiry.repository.UserRepository;

/**
 * 数据初始化器 - 启动时从 doctors.json 导入医生数据，并创建管理员账号
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final DoctorService doctorService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public DataInitializer(DoctorService doctorService, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, ObjectMapper objectMapper) {
        this.doctorService = doctorService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        createAdminAccount();
        loadDoctorsFromJson();
    }

    /**
     * 创建管理员账号（如果不存在）
     */
    private void createAdminAccount() {
        String adminMobile = "19212345678";
        if (userRepository.findByMobile(adminMobile).isEmpty()) {
            User admin = new User();
            admin.setMobile(adminMobile);
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setName("系统管理员");
            admin.setIdCard("000000000000000000");
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("已创建管理员账号: admin / admin123");
        } else {
            System.out.println("管理员账号已存在");
        }
    }

    private void loadDoctorsFromJson() {
        try {
            ClassPathResource resource = new ClassPathResource("doctors.json");
            InputStream inputStream = resource.getInputStream();
            
            List<DoctorJsonData> doctors = objectMapper.readValue(
                    inputStream, 
                    new TypeReference<List<DoctorJsonData>>() {}
            );

            System.out.println("开始导入医生数据，共 " + doctors.size() + " 条记录...");

            for (DoctorJsonData doctor : doctors) {
                try {
                    doctorService.createDoctorFromJson(
                            doctor.name(),
                            doctor.department(),
                            doctor.title(),
                            doctor.expertise(),
                            doctor.photoPath()
                    );
                    System.out.println("导入医生: " + doctor.name());
                } catch (Exception e) {
                    System.out.println("跳过医生 " + doctor.name() + ": " + e.getMessage());
                }
            }

            System.out.println("医生数据导入完成！");
        } catch (IOException e) {
            System.out.println("无法读取 doctors.json 文件: " + e.getMessage());
        }
    }

    // 用于解析 JSON 的内部记录类
    private record DoctorJsonData(
            String name,
            String department,
            String title,
            String expertise,
            String photo_path  // JSON 中使用下划线命名
    ) {
        public String photoPath() {
            return photo_path;
        }
    }
}