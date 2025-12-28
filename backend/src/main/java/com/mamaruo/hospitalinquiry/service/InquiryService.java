package com.mamaruo.hospitalinquiry.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mamaruo.hospitalinquiry.entity.DoctorProfile;
import com.mamaruo.hospitalinquiry.entity.Inquiry;
import com.mamaruo.hospitalinquiry.entity.InquiryStatus;
import com.mamaruo.hospitalinquiry.entity.PatientProfile;
import com.mamaruo.hospitalinquiry.entity.dto.CreateInquiryRequest;
import com.mamaruo.hospitalinquiry.entity.dto.InquiryDto;
import com.mamaruo.hospitalinquiry.repository.DoctorProfileRepository;
import com.mamaruo.hospitalinquiry.repository.InquiryRepository;
import com.mamaruo.hospitalinquiry.repository.PatientProfileRepository;

@Service
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final PatientProfileRepository patientProfileRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorService doctorService;
    private final PatientProfileService patientProfileService;

    public InquiryService(
            InquiryRepository inquiryRepository,
            PatientProfileRepository patientProfileRepository,
            DoctorProfileRepository doctorProfileRepository,
            DoctorService doctorService,
            PatientProfileService patientProfileService) {
        this.inquiryRepository = inquiryRepository;
        this.patientProfileRepository = patientProfileRepository;
        this.doctorProfileRepository = doctorProfileRepository;
        this.doctorService = doctorService;
        this.patientProfileService = patientProfileService;
    }

    // 患者获取自己的问诊列表
    public List<InquiryDto> getInquiriesByPatientUserId(Integer userId) {
        return inquiryRepository.findByPatientUserId(userId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 医生获取自己的问诊列表
    public List<InquiryDto> getInquiriesByDoctorUserId(Integer doctorUserId) {
        return inquiryRepository.findByDoctorUserId(doctorUserId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 医生获取待接诊列表
    public List<InquiryDto> getPendingInquiriesByDoctorUserId(Integer doctorUserId) {
        return inquiryRepository.findByDoctorUserIdAndStatus(doctorUserId, InquiryStatus.PENDING).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // 医生获取进行中的问诊
    public List<InquiryDto> getInProgressInquiriesByDoctorUserId(Integer doctorUserId) {
        return inquiryRepository.findByDoctorUserIdAndStatus(doctorUserId, InquiryStatus.IN_PROGRESS).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public InquiryDto getInquiryById(Integer id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("问诊不存在"));
        return toDto(inquiry);
    }

    @Transactional
    public InquiryDto createInquiry(Integer userId, CreateInquiryRequest request) {
        PatientProfile patientProfile = patientProfileRepository.findById(request.patientProfileId())
                .orElseThrow(() -> new IllegalArgumentException("问诊人不存在"));
        
        // 验证问诊人属于当前用户
        if (!patientProfile.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("无权使用此问诊人创建问诊");
        }

        DoctorProfile doctor = doctorProfileRepository.findById(request.doctorId())
                .orElseThrow(() -> new IllegalArgumentException("医生不存在"));

        if (!doctor.getAvailable()) {
            throw new IllegalArgumentException("该医生暂不接诊");
        }

        Inquiry inquiry = new Inquiry();
        inquiry.setPatientProfile(patientProfile);
        inquiry.setDoctor(doctor);
        inquiry.setSymptomDescription(request.symptomDescription());
        inquiry.setStatus(InquiryStatus.PENDING);
        inquiry.setCreatedAt(LocalDateTime.now());

        return toDto(inquiryRepository.save(inquiry));
    }

    // 医生接受问诊
    @Transactional
    public InquiryDto acceptInquiry(Integer inquiryId, Integer doctorUserId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("问诊不存在"));
        
        // 验证是否是该医生的问诊
        if (!inquiry.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new IllegalArgumentException("无权操作此问诊");
        }

        if (inquiry.getStatus() != InquiryStatus.PENDING) {
            throw new IllegalArgumentException("该问诊状态不是待接诊");
        }

        inquiry.setStatus(InquiryStatus.IN_PROGRESS);
        inquiry.setAcceptedAt(LocalDateTime.now());

        return toDto(inquiryRepository.save(inquiry));
    }

    // 医生结束问诊
    @Transactional
    public InquiryDto completeInquiry(Integer inquiryId, Integer doctorUserId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("问诊不存在"));
        
        // 验证是否是该医生的问诊
        if (!inquiry.getDoctor().getUser().getId().equals(doctorUserId)) {
            throw new IllegalArgumentException("无权操作此问诊");
        }

        if (inquiry.getStatus() != InquiryStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("该问诊状态不是进行中");
        }

        inquiry.setStatus(InquiryStatus.COMPLETED);
        inquiry.setCompletedAt(LocalDateTime.now());

        return toDto(inquiryRepository.save(inquiry));
    }

    // 验证用户是否有权访问该问诊
    public boolean canAccessInquiry(Integer inquiryId, Integer userId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId).orElse(null);
        if (inquiry == null) {
            return false;
        }
        return inquiry.getPatientProfile().getUser().getId().equals(userId) 
                || inquiry.getDoctor().getUser().getId().equals(userId);
    }

    private InquiryDto toDto(Inquiry inquiry) {
        return new InquiryDto(
                inquiry.getId(),
                patientProfileService.toDto(inquiry.getPatientProfile()),
                doctorService.toDto(inquiry.getDoctor()),
                inquiry.getSymptomDescription(),
                inquiry.getStatus(),
                inquiry.getCreatedAt(),
                inquiry.getAcceptedAt(),
                inquiry.getCompletedAt()
        );
    }
}
