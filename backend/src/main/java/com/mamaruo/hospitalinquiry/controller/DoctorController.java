package com.mamaruo.hospitalinquiry.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.mamaruo.hospitalinquiry.entity.dto.CreateDoctorRequest;
import com.mamaruo.hospitalinquiry.entity.dto.DoctorDto;
import com.mamaruo.hospitalinquiry.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // 公开接口 - 获取可用医生列表
    @GetMapping("/public")
    public List<DoctorDto> getAvailableDoctors() {
        return doctorService.getAvailableDoctors();
    }

    // 公开接口 - 按科室获取医生
    @GetMapping("/public/department/{departmentId}")
    public List<DoctorDto> getDoctorsByDepartment(@PathVariable Integer departmentId) {
        return doctorService.getDoctorsByDepartment(departmentId);
    }

    // 公开接口 - 获取单个医生详情
    @GetMapping("/public/{id}")
    public DoctorDto getDoctorById(@PathVariable Integer id) {
        return doctorService.getDoctorById(id);
    }

    // 需要认证的接口
    @GetMapping
    public List<DoctorDto> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/user/{userId}")
    public DoctorDto getDoctorByUserId(@PathVariable Integer userId) {
        return doctorService.getDoctorByUserId(userId);
    }

    @PostMapping
    public DoctorDto createDoctor(@RequestBody CreateDoctorRequest request) {
        return doctorService.createDoctor(request);
    }

    @PutMapping("/{id}")
    public DoctorDto updateDoctor(
            @PathVariable Integer id,
            @RequestBody UpdateDoctorRequest request) {
        return doctorService.updateDoctor(
                id, 
                request.departmentId(), 
                request.title(), 
                request.expertise(), 
                request.available()
        );
    }

    private record UpdateDoctorRequest(
            Integer departmentId,
            String title,
            String expertise,
            Boolean available
    ) {}
}
