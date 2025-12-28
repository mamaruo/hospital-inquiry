package com.mamaruo.hospitalinquiry.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mamaruo.hospitalinquiry.entity.Department;
import com.mamaruo.hospitalinquiry.entity.dto.DepartmentDto;
import com.mamaruo.hospitalinquiry.repository.DepartmentRepository;

@Service
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public DepartmentDto getDepartmentById(Integer id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("科室不存在"));
        return toDto(dept);
    }

    @Transactional
    public DepartmentDto createDepartment(String name, String description) {
        if (departmentRepository.existsByName(name)) {
            throw new IllegalArgumentException("科室名称已存在");
        }
        Department dept = new Department(name);
        dept.setDescription(description);
        return toDto(departmentRepository.save(dept));
    }

    @Transactional
    public DepartmentDto updateDepartment(Integer id, String name, String description) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("科室不存在"));
        
        // 检查名称是否重复（排除自身）
        departmentRepository.findByName(name).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new IllegalArgumentException("科室名称已存在");
            }
        });
        
        dept.setName(name);
        dept.setDescription(description);
        return toDto(departmentRepository.save(dept));
    }

    @Transactional
    public void deleteDepartment(Integer id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("科室不存在"));
        if (!dept.getDoctors().isEmpty()) {
            throw new IllegalArgumentException("该科室下还有医生，无法删除");
        }
        departmentRepository.delete(dept);
    }

    public Department getOrCreateDepartment(String name) {
        return departmentRepository.findByName(name)
                .orElseGet(() -> {
                    Department dept = new Department(name);
                    return departmentRepository.save(dept);
                });
    }

    private DepartmentDto toDto(Department dept) {
        return new DepartmentDto(
                dept.getId(),
                dept.getName(),
                dept.getDescription(),
                dept.getDoctors() != null ? dept.getDoctors().size() : 0
        );
    }
}
