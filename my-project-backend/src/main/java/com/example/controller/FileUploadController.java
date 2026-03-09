package com.example.controller;

import com.example.entity.RestBean;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class FileUploadController {

    // 上传文件存储目录
    private static final String UPLOAD_DIR = "uploads";

    /**
     * 上传图片
     */
    @PostMapping("/upload")
    public RestBean<String> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // 验证文件
            if (file.isEmpty()) {
                return RestBean.failure(400, "文件为空");
            }

            // 获取文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));

            // 验证文件类型
            if (!isImageFile(fileExtension)) {
                return RestBean.failure(400, "只支持上传 JPG、PNG 格式的图片");
            }

            // 验证文件大小（2MB）
            if (file.getSize() > 2 * 1024 * 1024) {
                return RestBean.failure(400, "文件大小不能超过 2MB");
            }

            // 获取项目根目录
            String projectPath = new File("").getAbsolutePath();
            String uploadDirPath = projectPath + File.separator + UPLOAD_DIR;
            System.out.println("上传目录路径: " + uploadDirPath);

            // 创建上传目录
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
                System.out.println("创建uploads目录");
            }

            // 生成新的文件名
            String newFilename = generateFilename(originalFilename);
            Path filePath = Paths.get(uploadDirPath, newFilename);
            System.out.println("文件保存路径: " + filePath);

            // 保存文件
            file.transferTo(filePath.toFile());
            System.out.println("文件上传成功: " + newFilename);

            // 返回文件访问URL
            String fileUrl = "/uploads/" + newFilename;

            return RestBean.success(fileUrl);

        } catch (IOException e) {
            return RestBean.failure(500, "文件上传失败: " + e.getMessage());
        }
    }

    /**
     * 判断是否为图片文件
     */
    private boolean isImageFile(String extension) {
        return ".jpg".equalsIgnoreCase(extension) ||
               ".jpeg".equalsIgnoreCase(extension) ||
               ".png".equalsIgnoreCase(extension) ||
               ".JPG".equalsIgnoreCase(extension) ||
               ".JPEG".equalsIgnoreCase(extension) ||
               ".PNG".equalsIgnoreCase(extension);
    }

    /**
     * 生成新的文件名
     */
    private String generateFilename(String originalFilename) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timestamp = now.format(formatter);

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return uuid + "_" + timestamp + extension;
    }
}
