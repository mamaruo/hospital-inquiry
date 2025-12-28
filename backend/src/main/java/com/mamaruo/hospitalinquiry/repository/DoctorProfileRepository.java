package com.mamaruo.hospitalinquiry.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mamaruo.hospitalinquiry.entity.Department;
import com.mamaruo.hospitalinquiry.entity.DoctorProfile;
import com.mamaruo.hospitalinquiry.entity.User;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Integer> {
    Optional<DoctorProfile> findByUser(User user);
    Optional<DoctorProfile> findByUserId(Integer userId);
    List<DoctorProfile> findByDepartment(Department department);
    List<DoctorProfile> findByDepartmentId(Integer departmentId);
    List<DoctorProfile> findByAvailableTrue();
    List<DoctorProfile> findByDepartmentIdAndAvailableTrue(Integer departmentId);
}
