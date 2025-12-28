package com.mamaruo.hospitalinquiry.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.mamaruo.hospitalinquiry.entity.PatientProfile;
import com.mamaruo.hospitalinquiry.entity.User;

public interface PatientProfileRepository extends JpaRepository<PatientProfile, Integer> {
    List<PatientProfile> findByUser(User user);
    List<PatientProfile> findByUserId(Integer userId);
}
