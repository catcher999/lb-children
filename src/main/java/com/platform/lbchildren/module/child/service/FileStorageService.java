package com.platform.lbchildren.module.child.service;

import com.platform.lbchildren.common.BusinessException;
import com.platform.lbchildren.common.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * 文件存储服务：将上传文件保存到本地磁盘，并返回可访问的 URL
 */
@Service
public class FileStorageService {

    /** 允许上传的图片扩展名 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    /** 单文件大小上限（字节） */
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    @Value("${app.upload-base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 保存文件，返回访问 URL，如 http://localhost:8080/uploads/album/xxx.jpg
     *
     * @param file   上传文件
     * @param subDir 子目录，如 album / diary
     */
    public String store(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件不能为空");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "文件大小不能超过 10MB");
        }
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "仅支持图片文件: " + ALLOWED_EXTENSIONS);
        }

        try {
            String filename = UUID.randomUUID() + "." + ext;
            Path dir = Paths.get(uploadDir, subDir);
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());
            return baseUrl + "/uploads/" + subDir + "/" + filename;
        } catch (IOException e) {
            throw new BusinessException(ResultCode.SYSTEM_ERROR.getCode(), "文件保存失败");
        }
    }
}
