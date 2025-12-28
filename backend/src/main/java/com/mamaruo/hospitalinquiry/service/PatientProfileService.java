package com.mamaruo.hospitalinquiry.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mamaruo.hospitalinquiry.entity.PatientProfile;
import com.mamaruo.hospitalinquiry.entity.User;
import com.mamaruo.hospitalinquiry.entity.dto.CreatePatientProfileRequest;
import com.mamaruo.hospitalinquiry.entity.dto.PatientProfileDto;
import com.mamaruo.hospitalinquiry.repository.PatientProfileRepository;
import com.mamaruo.hospitalinquiry.repository.UserRepository;

@Service
public class PatientProfileService {
    private final PatientProfileRepository patientProfileRepository;
    private final UserRepository userRepository;

    public PatientProfileService(PatientProfileRepository patientProfileRepository, 
            UserRepository userRepository) {
        this.patientProfileRepository = patientProfileRepository;
        this.userRepository = userRepository;
    }

    public List<PatientProfileDto> getProfilesByUserId(Integer userId) {
        return patientProfileRepository.findByUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public PatientProfileDto getProfileById(Integer id) {
        PatientProfile profile = patientProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("问诊人不存在"));
        return toDto(profile);
    }

    @Transactional
    public PatientProfileDto createProfile(Integer userId, CreatePatientProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        PatientProfile profile = new PatientProfile();
        profile.setUser(user);
        profile.setName(request.name());
        profile.setGender(request.gender());
        profile.setBirthDate(request.birthDate());
        profile.setMedicalHistory(request.medicalHistory());

        return toDto(patientProfileRepository.save(profile));
    }

    @Transactional
    public PatientProfileDto updateProfile(Integer id, Integer userId, CreatePatientProfileRequest request) {
        PatientProfile profile = patientProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("问诊人不存在"));
        
        // 验证问诊人属于当前用户
        if (!profile.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("无权修改此问诊人");
        }

        profile.setName(request.name());
        profile.setGender(request.gender());
        profile.setBirthDate(request.birthDate());
        profile.setMedicalHistory(request.medicalHistory());

        return toDto(patientProfileRepository.save(profile));
    }

    @Transactional
    public void deleteProfile(Integer id, Integer userId) {
        PatientProfile profile = patientProfileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("问诊人不存在"));
        
        // 验证问诊人属于当前用户
        if (!profile.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此问诊人");
        }

        patientProfileRepository.delete(profile);
    }

    public PatientProfileDto toDto(PatientProfile profile) {
        return new PatientProfileDto(
                profile.getId(),
                profile.getName(),
                profile.getGender(),
                profile.getBirthDate(),
                profile.getMedicalHistory()
        );
    }
}
