package com.mamaruo.hospitalinquiry.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.mamaruo.hospitalinquiry.entity.Inquiry;
import com.mamaruo.hospitalinquiry.entity.InquiryStatus;

public interface InquiryRepository extends JpaRepository<Inquiry, Integer> {
    // 根据问诊人所属用户ID查询
    @Query("SELECT i FROM Inquiry i WHERE i.patientProfile.user.id = :userId")
    List<Inquiry> findByPatientUserId(@Param("userId") Integer userId);
    
    // 根据医生用户ID查询
    @Query("SELECT i FROM Inquiry i WHERE i.doctor.user.id = :doctorUserId")
    List<Inquiry> findByDoctorUserId(@Param("doctorUserId") Integer doctorUserId);
    
    // 根据医生用户ID和状态查询
    @Query("SELECT i FROM Inquiry i WHERE i.doctor.user.id = :doctorUserId AND i.status = :status")
    List<Inquiry> findByDoctorUserIdAndStatus(@Param("doctorUserId") Integer doctorUserId, @Param("status") InquiryStatus status);
    
    // 根据问诊人ID查询
    List<Inquiry> findByPatientProfileId(Integer patientProfileId);
    
    // 根据医生档案ID查询
    List<Inquiry> findByDoctorId(Integer doctorId);
    
    // 根据状态查询
    List<Inquiry> findByStatus(InquiryStatus status);
}
