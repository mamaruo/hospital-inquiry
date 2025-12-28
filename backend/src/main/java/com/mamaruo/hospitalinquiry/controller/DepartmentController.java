package com.mamaruo.hospitalinquiry.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mamaruo.hospitalinquiry.entity.dto.DepartmentDto;
import com.mamaruo.hospitalinquiry.service.DepartmentService;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    public List<DepartmentDto> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @GetMapping("/{id}")
    public DepartmentDto getDepartmentById(@PathVariable Integer id) {
        return departmentService.getDepartmentById(id);
    }

    @PostMapping
    public DepartmentDto createDepartment(@RequestBody DepartmentRequest request) {
        return departmentService.createDepartment(request.name(), request.description());
    }

    @PutMapping("/{id}")
    public DepartmentDto updateDepartment(@PathVariable Integer id, @RequestBody DepartmentRequest request) {
        return departmentService.updateDepartment(id, request.name(), request.description());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Integer id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    private record DepartmentRequest(String name, String description) {}
}
