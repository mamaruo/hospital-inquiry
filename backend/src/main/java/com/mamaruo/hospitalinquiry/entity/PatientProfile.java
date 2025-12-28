package com.mamaruo.hospitalinquiry.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * 问诊人实体（一个患者账号可以有多个问诊人）
 */
@Entity
@Table(name = "patient_profiles")
public class PatientProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // 所属患者账号

    @Column(nullable = false)
    private String name;  // 问诊人姓名

    @Column(nullable = false)
    private String gender;  // 性别

    private LocalDate birthDate;  // 出生日期

    @Column(columnDefinition = "TEXT")
    private String medicalHistory;  // 病史

    public PatientProfile() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }
}
