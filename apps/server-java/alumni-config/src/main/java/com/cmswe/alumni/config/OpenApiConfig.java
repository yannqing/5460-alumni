package com.cmswe.alumni.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.GlobalOpenApiCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Knife4j 配置
 *
 * 功能:
 * 1. 配置 API 文档基本信息
 * 2. 配置全局请求头参数(Token、签名等)
 * 3. 配置安全认证方案
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 基础配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                // 基本信息
                .info(new Info()
                        .title("CNI Alumni API 文档")
                        .version("v1.0.0")
                        .description("校友系统 RESTful API 接口文档")
                        .contact(new Contact()
                                .name("CNI Alumni 开发团队")
                                .email("dev@cni-alumni.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))

                // 组件配置(安全方案)
                .components(new Components()
                        // JWT Token 认证
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("请输入 JWT Token(不需要加 'Bearer ' 前缀)")
                                .name("Authorization"))

                        // 签名验证 - Timestamp
                        .addSecuritySchemes("X-Timestamp", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Timestamp")
                                .description("请求时间戳(毫秒),示例: 1701331200000"))

                        // 签名验证 - Nonce
                        .addSecuritySchemes("X-Nonce", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Nonce")
                                .description("随机字符串(UUID),示例: a1b2c3d4-e5f6-7890-abcd-ef1234567890"))

                        // 签名验证 - Signature
                        .addSecuritySchemes("X-Signature", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-Signature")
                                .description("请求签名(HMAC-SHA256),详见签名验证文档")))

                // 全局应用安全方案
                .addSecurityItem(new SecurityRequirement()
                        .addList("Bearer"))
                .addSecurityItem(new SecurityRequirement()
                        .addList("X-Timestamp")
                        .addList("X-Nonce")
                        .addList("X-Signature"));
    }

    /**
     * 全局参数配置
     * 为所有接口添加全局请求头参数,并自动填充示例值
     */
    @Bean
    public GlobalOpenApiCustomizer globalOpenApiCustomizer() {
        return openApi -> {
            // 添加全局请求头参数
            if (openApi.getPaths() != null) {
                openApi.getPaths().forEach((path, pathItem) -> {
                    pathItem.readOperations().forEach(operation -> {
                        // JWT Token (使用真实格式的示例值)
                        operation.addParametersItem(new HeaderParameter()
                                .name("token")
                                .description("JWT Token - 用户登录后获取,开发环境可不填")
                                .required(false)
                                .schema(new StringSchema()
                                        .example("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoidGVzdCJ9.abc123")
                                        ._default("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoidGVzdCJ9.abc123")));

                        // 签名验证 - Timestamp (自动生成当前时间戳)
                        operation.addParametersItem(new HeaderParameter()
                                .name("X-Timestamp")
                                .description("请求时间戳(毫秒) - 当前时间,±5分钟内有效")
                                .required(false)
                                .schema(new StringSchema()
                                        .example(String.valueOf(System.currentTimeMillis()))
                                        ._default(String.valueOf(System.currentTimeMillis()))));

                        // 签名验证 - Nonce (使用开发模式的特殊值)
                        operation.addParametersItem(new HeaderParameter()
                                .name("X-Nonce")
                                .description("随机字符串 - 开发环境可使用 'mock' 跳过签名验证,生产环境必须使用 UUID")
                                .required(false)
                                .schema(new StringSchema()
                                        .example("mock")
                                        ._default("mock")));

                        // 签名验证 - Signature (开发模式可省略)
                        operation.addParametersItem(new HeaderParameter()
                                .name("X-Signature")
                                .description("请求签名 - 使用 nonce='mock' 时可省略,否则需要用 HMAC-SHA256 计算")
                                .required(false)
                                .schema(new StringSchema()
                                        .example("")
                                        ._default("")));
                    });
                });
            }
        };
    }

    /**
     * 分组 API - 用户相关
     */
    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("1. 用户模块")
                .pathsToMatch("/user/**", "/auth/**")
                .build();
    }

    /**
     * 分组 API - 母校相关
     */
    @Bean
    public GroupedOpenApi schoolApi() {
        return GroupedOpenApi.builder()
                .group("2. 母校模块")
                .pathsToMatch("/school/**")
                .build();
    }

    /**
     * 分组 API - 校友会相关
     */
    @Bean
    public GroupedOpenApi associationApi() {
        return GroupedOpenApi.builder()
                .group("3. 校友会模块")
                .pathsToMatch("/association/**")
                .build();
    }

    /**
     * 分组 API - 系统管理
     */
    @Bean
    public GroupedOpenApi systemApi() {
        return GroupedOpenApi.builder()
                .group("4. 系统管理")
                .pathsToMatch("/system/**", "/file/**")
                .build();
    }

    /**
     * 分组 API - 全部接口
     */
    @Bean
    public GroupedOpenApi allApi() {
        return GroupedOpenApi.builder()
                .group("0. 全部接口")
                .pathsToMatch("/**")
                .build();
    }
}
