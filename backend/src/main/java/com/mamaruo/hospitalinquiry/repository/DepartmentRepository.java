package com.mamaruo.hospitalinquiry.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mamaruo.hospitalinquiry.entity.Department;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Optional<Department> findByName(String name);
    boolean existsByName(String name);
}
