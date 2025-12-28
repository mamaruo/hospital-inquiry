package com.mamaruo.hospitalinquiry.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.mamaruo.hospitalinquiry.entity.UserPrincipal;
import com.mamaruo.hospitalinquiry.entity.dto.CreatePatientProfileRequest;
import com.mamaruo.hospitalinquiry.entity.dto.PatientProfileDto;
import com.mamaruo.hospitalinquiry.service.PatientProfileService;

@RestController
@RequestMapping("/api/patient-profiles")
public class PatientProfileController {
    private final PatientProfileService patientProfileService;

    public PatientProfileController(PatientProfileService patientProfileService) {
        this.patientProfileService = patientProfileService;
    }

    @GetMapping
    public List<PatientProfileDto> getMyProfiles(@AuthenticationPrincipal UserPrincipal principal) {
        return patientProfileService.getProfilesByUserId(principal.getId());
    }

    @GetMapping("/{id}")
    public PatientProfileDto getProfileById(@PathVariable Integer id) {
        return patientProfileService.getProfileById(id);
    }

    @PostMapping
    public PatientProfileDto createProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreatePatientProfileRequest request) {
        return patientProfileService.createProfile(principal.getId(), request);
    }

    @PutMapping("/{id}")
    public PatientProfileDto updateProfile(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody CreatePatientProfileRequest request) {
        return patientProfileService.updateProfile(id, principal.getId(), request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProfile(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserPrincipal principal) {
        patientProfileService.deleteProfile(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
