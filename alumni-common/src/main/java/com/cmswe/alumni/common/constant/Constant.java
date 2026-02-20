package com.cmswe.alumni.common.constant;

import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 常量类
 */
@Data
@Configuration
public class Constant {

    public static String[] anonymousConstant = {
            "/login",
            "/favicon.ico",
            "/ws/user-status",
            "/v3/api-docs",
            "/api/resume/privacy-files",
            "/users"
    };

    public static String[] anonymousMatch = {
            "/auth/**",
       "/doc.html/**",
       "/webjars/**",
            "/file/**",
            "/v3/**",
            "/test/**",
            "/api/wechat/**",
            "/upload/**",
            "/druid/**",
            "/ws/**",
            "/actuator/**",
    };

    // 图片类型文件的一级类型
    public static final String IMAGE_FILE_TYPE = "image";

    // 简历类型文件的一级类型
    public static final String RESUME_FILE_TYPE = "resume";

    // 音频类型文件的一级类型
    public static final String AUDIO_FILE_TYPE = "audio";

    // 允许的图片文件扩展名
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico", "tiff", "tif"
    ));
    
    // 允许的简历文件扩展名
    public static final Set<String> ALLOWED_RESUME_EXTENSIONS = new HashSet<>(Arrays.asList(
            "md", "pdf", "docx", "doc", "txt"
    ));
    
    // 允许的音频文件扩展名
    public static final Set<String> ALLOWED_AUDIO_EXTENSIONS = new HashSet<>(Arrays.asList(
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a", "opus"
    ));

    public static final Integer UPLOAD_FILE_COUNT = 3;

    public static final Integer adminUserId = 1;

    public static List<String> anonymousConstantList = Arrays.asList(anonymousConstant);

    public static Boolean isMatch(String path) {
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        List<Boolean> list = Arrays.stream(anonymousMatch).map((pattern) -> antPathMatcher.match(pattern, path)).toList();
        return list.contains(true);
    }
}