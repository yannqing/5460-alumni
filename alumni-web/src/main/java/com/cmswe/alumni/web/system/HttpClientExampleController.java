package com.cmswe.alumni.web.system;

import com.cmswe.alumni.common.utils.HttpClientUtil;
import com.cmswe.alumni.common.utils.ResultUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

/**
 * HttpClientUtil 使用示例
 * 演示企业级 WebClient 工具类的各种用法
 */
@Tag(name = "HttpClientUtil 使用示例")
@Slf4j
@RestController
@RequestMapping("/api/http-example")
public class HttpClientExampleController {

    @Resource
    private HttpClientUtil httpClientUtil;

    /**
     * 示例1：简单 GET 请求
     */
    @GetMapping("/simple-get")
    public Object simpleGet() {
        try {
            // 简单的字符串响应
            String result = httpClientUtil.get("https://httpbin.org/get");
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("简单GET请求失败", e);
            return ResultUtils.failure(500, "请求失败: " + e.getMessage());
        }
    }

    /**
     * 示例2：带请求头的 GET 请求
     */
    @GetMapping("/get-with-headers")
    public Object getWithHeaders() {
        try {
            // 构建请求头
            Map<String, String> headers = Map.of(
                    "User-Agent", "Alumni-System/1.0",
                    "X-Custom-Header", "custom-value"
            );
            
            // 发起请求，指定返回类型为 Map
            Map result = httpClientUtil.get(
                    "https://httpbin.org/headers", 
                    Map.class, 
                    headers
            );
            
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("带请求头的GET请求失败", e);
            return ResultUtils.failure(500, "请求失败: " + e.getMessage());
        }
    }

    /**
     * 示例3：POST 请求发送 JSON 数据
     */
    @GetMapping("/post-json")
    public Object postJson() {
        try {
            // 构建请求体
            Map<String, Object> requestBody = Map.of(
                    "name", "张三",
                    "age", 25,
                    "email", "zhangsan@example.com"
            );
            
            // 发起 POST 请求
            Map result = httpClientUtil.post(
                    "https://httpbin.org/post",
                    requestBody,
                    Map.class
            );
            
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("POST JSON请求失败", e);
            return ResultUtils.failure(500, "请求失败: " + e.getMessage());
        }
    }

    /**
     * 示例4：带认证的请求
     */
    @GetMapping("/auth-request")
    public Object authRequest() {
        try {
            // 使用 Bearer Token 认证
            Map<String, String> authHeaders = HttpClientUtil.bearerToken("your-token-here");
            
            Map result = httpClientUtil.get(
                    "https://httpbin.org/bearer",
                    Map.class,
                    authHeaders
            );
            
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("认证请求失败", e);
            return ResultUtils.failure(500, "请求失败: " + e.getMessage());
        }
    }

    /**
     * 示例5：自定义超时时间
     */
    @GetMapping("/timeout-request")
    public Object timeoutRequest() {
        try {
            // 设置5秒超时
            String result = httpClientUtil.get(
                    "https://httpbin.org/delay/3",
                    String.class,
                    null,
                    Duration.ofSeconds(5)
            );
            
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("超时请求失败", e);
            return ResultUtils.failure(500, "请求失败: " + e.getMessage());
        }
    }

    /**
     * 示例6：处理复杂返回类型（泛型）
     */
    @GetMapping("/complex-type")
    public Object complexType() {
        try {
            // 使用 ParameterizedTypeReference 处理复杂类型
            ParameterizedTypeReference<Map<String, Object>> responseType = 
                    new ParameterizedTypeReference<Map<String, Object>>() {};
            
            Map<String, Object> result = httpClientUtil.get(
                    "https://httpbin.org/json",
                    responseType
            );
            
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("复杂类型请求失败", e);
            return ResultUtils.failure(500, "请求失败: " + e.getMessage());
        }
    }

    /**
     * 示例7：PUT 请求
     */
    @GetMapping("/put-request")
    public Object putRequest() {
        try {
            Map<String, Object> updateData = Map.of(
                    "status", "updated",
                    "timestamp", System.currentTimeMillis()
            );
            
            Map result = httpClientUtil.put(
                    "https://httpbin.org/put",
                    updateData,
                    Map.class
            );
            
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("PUT请求失败", e);
            return ResultUtils.failure(500, "请求失败: " + e.getMessage());
        }
    }

    /**
     * 示例8：DELETE 请求
     */
    @GetMapping("/delete-request")
    public Object deleteRequest() {
        try {
            Map result = httpClientUtil.delete(
                    "https://httpbin.org/delete",
                    Map.class
            );
            
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("DELETE请求失败", e);
            return ResultUtils.failure(500, "请求失败: " + e.getMessage());
        }
    }

    /**
     * 示例9：组合使用 - 获取用户信息后更新状态
     */
    @GetMapping("/composite-request")
    public Object compositeRequest() {
        try {
            // 第一步：获取用户信息
            Map userInfo = httpClientUtil.get(
                    "https://httpbin.org/json",
                    Map.class
            );
            
            // 第二步：根据用户信息更新状态
            Map<String, Object> updateData = Map.of(
                    "user_info", userInfo,
                    "last_login", System.currentTimeMillis(),
                    "status", "active"
            );
            
            Map updateResult = httpClientUtil.post(
                    "https://httpbin.org/post",
                    updateData,
                    Map.class,
                    HttpClientUtil.commonHeaders(),
                    Duration.ofSeconds(10)
            );
            
            return ResultUtils.success(Map.of(
                    "user_info", userInfo,
                    "update_result", updateResult
            ));
            
        } catch (Exception e) {
            log.error("组合请求失败", e);
            return ResultUtils.failure(500, "请求失败: " + e.getMessage());
        }
    }
}