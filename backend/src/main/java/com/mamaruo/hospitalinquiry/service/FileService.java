package com.mamaruo.hospitalinquiry.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private static final String DEFAULT_AVATAR = "default.svg";

    @Value("${app.upload.dir:C:/hospital-uploads}")
    private String uploadDir;

    public String saveFile(MultipartFile file) throws IOException {
        // 确保上传目录存在
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 生成唯一文件名
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID().toString() + extension;

        // 保存文件
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return newFilename;
    }

    public byte[] getFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("文件不存在");
        }
        return Files.readAllBytes(filePath);
    }

    /**
     * 医生头像及其实际生效的文件名（用于判定 Content-Type）。
     */
    public record DoctorPhoto(byte[] data, String filename) {}

    public DoctorPhoto getDoctorPhoto(String photoPath) {
        if (photoPath == null || photoPath.isEmpty()) {
            throw new IllegalArgumentException("照片路径为空");
        }
        Path filePath = Paths.get(photoPath);
        if (filePath.isAbsolute() && Files.exists(filePath)) {
            try {
                return new DoctorPhoto(Files.readAllBytes(filePath), filePath.getFileName().toString());
            } catch (IOException e) {
                throw new IllegalArgumentException("照片读取失败");
            }
        }
        // 相对文件名（或换机器后失效的绝对路径）从打包资源中按文件名解析
        String filename = filePath.getFileName().toString();
        byte[] bundled = readBundledAvatar(filename);
        if (bundled != null) {
            return new DoctorPhoto(bundled, filename);
        }
        byte[] fallback = readBundledAvatar(DEFAULT_AVATAR);
        if (fallback != null) {
            return new DoctorPhoto(fallback, DEFAULT_AVATAR);
        }
        throw new IllegalArgumentException("照片不存在");
    }

    private byte[] readBundledAvatar(String filename) {
        try (InputStream in = new ClassPathResource("static/avatars/" + filename).getInputStream()) {
            return in.readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }

    public void deleteFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadDir).resolve(filename);
        Files.deleteIfExists(filePath);
    }
}
