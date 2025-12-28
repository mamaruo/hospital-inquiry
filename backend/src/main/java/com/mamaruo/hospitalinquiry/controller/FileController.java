package com.mamaruo.hospitalinquiry.controller;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.mamaruo.hospitalinquiry.entity.DoctorProfile;
import com.mamaruo.hospitalinquiry.repository.DoctorProfileRepository;
import com.mamaruo.hospitalinquiry.service.FileService;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService fileService;
    private final DoctorProfileRepository doctorProfileRepository;

    public FileController(FileService fileService, DoctorProfileRepository doctorProfileRepository) {
        this.fileService = fileService;
        this.doctorProfileRepository = doctorProfileRepository;
    }

    @PostMapping("/upload")
    public UploadResponse uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String filename = fileService.saveFile(file);
        return new UploadResponse(filename, "/api/files/" + filename);
    }

    @GetMapping("/{filename}")
    public ResponseEntity<byte[]> getFile(@PathVariable String filename) throws IOException {
        byte[] data = fileService.getFile(filename);
        MediaType mediaType = determineMediaType(filename);
        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(data);
    }

    // 获取医生头像
    @GetMapping("/{doctorId}/photo")
    public ResponseEntity<byte[]> getDoctorPhoto(@PathVariable Integer doctorId) throws IOException {
        DoctorProfile doctor = doctorProfileRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("医生不存在"));
        
        if (doctor.getPhotoPath() == null) {
            return ResponseEntity.notFound().build();
        }

        byte[] data = fileService.getDoctorPhoto(doctor.getPhotoPath());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(data);
    }

    private MediaType determineMediaType(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else if (lower.endsWith(".webp")) {
            return MediaType.valueOf("image/webp");
        }
        return MediaType.IMAGE_JPEG;
    }

    private record UploadResponse(String filename, String url) {}
}
