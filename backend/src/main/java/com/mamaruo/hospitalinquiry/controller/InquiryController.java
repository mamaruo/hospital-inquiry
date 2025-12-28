package com.mamaruo.hospitalinquiry.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.mamaruo.hospitalinquiry.entity.UserPrincipal;
import com.mamaruo.hospitalinquiry.entity.dto.CreateInquiryRequest;
import com.mamaruo.hospitalinquiry.entity.dto.InquiryDto;
import com.mamaruo.hospitalinquiry.service.InquiryService;

@RestController
@RequestMapping("/api/inquiries")
public class InquiryController {
    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    // 患者获取自己的问诊列表
    @GetMapping("/patient")
    public List<InquiryDto> getPatientInquiries(@AuthenticationPrincipal UserPrincipal principal) {
        return inquiryService.getInquiriesByPatientUserId(principal.getId());
    }

    // 医生获取自己的所有问诊
    @GetMapping("/doctor")
    public List<InquiryDto> getDoctorInquiries(@AuthenticationPrincipal UserPrincipal principal) {
        return inquiryService.getInquiriesByDoctorUserId(principal.getId());
    }

    // 医生获取待接诊列表
    @GetMapping("/doctor/pending")
    public List<InquiryDto> getDoctorPendingInquiries(@AuthenticationPrincipal UserPrincipal principal) {
        return inquiryService.getPendingInquiriesByDoctorUserId(principal.getId());
    }

    // 医生获取进行中的问诊
    @GetMapping("/doctor/in-progress")
    public List<InquiryDto> getDoctorInProgressInquiries(@AuthenticationPrincipal UserPrincipal principal) {
        return inquiryService.getInProgressInquiriesByDoctorUserId(principal.getId());
    }

    // 获取单个问诊详情
    @GetMapping("/{id}")
    public InquiryDto getInquiryById(@PathVariable Integer id) {
        return inquiryService.getInquiryById(id);
    }

    // 患者创建问诊
    @PostMapping
    public InquiryDto createInquiry(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreateInquiryRequest request) {
        return inquiryService.createInquiry(principal.getId(), request);
    }

    // 医生接受问诊
    @PostMapping("/{id}/accept")
    public InquiryDto acceptInquiry(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return inquiryService.acceptInquiry(id, principal.getId());
    }

    // 医生结束问诊
    @PostMapping("/{id}/complete")
    public InquiryDto completeInquiry(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        return inquiryService.completeInquiry(id, principal.getId());
    }
}
