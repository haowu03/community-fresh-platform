package com.fresh.modules.common.controller;

import com.fresh.common.result.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * 管理端-通用接口：文件上传
 */
@Slf4j
@RestController
@RequestMapping("/admin/common")
@Tag(name = "通用模块", description = "管理端-文件上传等通用接口")
public class CommonController {

    /** 本地文件上传目录（配置项 fresh.upload-path） */
    @Value("${fresh.upload-path}")
    private String uploadPath;

    /**
     * 文件上传：存储到本地目录，文件名 UUID 化保留扩展名
     *
     * @param file 上传文件
     * @return 可访问路径 /upload/{filename}
     */
    @PostMapping("/upload")
    @Operation(summary = "文件上传")
    public R<String> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return R.error("上传文件不能为空");
        }
        try {
            // 原始文件名与扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            // UUID 重命名，防止文件名冲突与路径注入
            String fileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 确保上传目录存在
            File dir = new File(uploadPath);
            if (!dir.exists() && !dir.mkdirs()) {
                log.error("创建上传目录失败: {}", dir.getAbsolutePath());
                return R.error("文件上传失败");
            }

            // 转存文件
            File dest = new File(dir, fileName);
            file.transferTo(dest.getAbsoluteFile());
            log.info("文件上传成功: {}", dest.getAbsolutePath());

            // 返回可访问路径（由静态资源映射 /upload/** 提供访问）
            return R.success("/upload/" + fileName);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return R.error("文件上传失败");
        }
    }
}
