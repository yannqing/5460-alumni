package com.cmswe.alumni.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvc配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Value("${file.upload-common-url}")
    private String uploadCommonPath;

    @Value("${file.upload-prefix-url}")
    private String uploadPrefixPath;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 添加Swagger UI资源映射
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/swagger-ui/");

        // 添加Knife4j资源映射
        registry.addResourceHandler("/doc.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

        // 添加文件上传资源映射 - 统一使用项目根目录下的upload文件夹
        registry.addResourceHandler(uploadPrefixPath + "/**")
                .addResourceLocations("file:" + uploadCommonPath + "/");

        // 添加默认产品图片映射
        registry.addResourceHandler("/default/**")
                .addResourceLocations("file:" + uploadCommonPath + "/default/");
    }
}