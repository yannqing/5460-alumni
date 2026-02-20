package com.cmswe.alumni.auth.filter;

import com.alibaba.fastjson2.JSON;
import com.cmswe.alumni.auth.config.SignatureConfig;
import com.cmswe.alumni.auth.signature.SignatureValidator;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.constant.Constant;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 请求签名验证过滤器
 *
 * 优先级：1（最高，在所有过滤器之前执行）
 * 位置：Spring Security 过滤链之前
 *
 * 功能：
 * 1. 提取请求参数（URL参数、Header、JSON Body）
 * 2. 调用 SignatureValidator 验证签名
 * 3. 验证失败返回 401 错误
 * 4. 验证成功继续执行后续过滤器
 */
@Slf4j
@Component
@Order(1)  // 优先级最高，在 JwtAuthenticationTokenFilter 之前执行
public class RequestSignatureFilter extends OncePerRequestFilter {

    @Resource
    private SignatureValidator signatureValidator;

    @Resource
    private SignatureConfig signatureConfig;

    @Resource
    private ObjectMapper objectMapper;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.debug("[签名过滤器] 请求进入: {} {}", method, path);

        // 0. 检查是否启用签名验证
        if (!signatureConfig.getEnabled()) {
            log.debug("[签名过滤器] 签名验证已禁用，直接放行");
            filterChain.doFilter(request, response);
            return;
        }

        // 1. 检查是否在白名单中
        if (isWhitelisted(path)) {
            log.debug("[签名过滤器] 白名单路径，跳过验证: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        // 2. 包装请求（支持多次读取 Body）
        ContentCachingRequestWrapper wrappedRequest =
            new ContentCachingRequestWrapper(request);

        // 3. 提取所有参数
        Map<String, String> params = extractAllParams(wrappedRequest);

        log.debug("[签名过滤器] 提取参数: {}", params);

        // 4. 检查是否为开发模式（特殊nonce跳过验证）
        if (signatureConfig.getDevMode()) {
            String nonce = params.get("nonce");
            if (nonce != null && signatureConfig.getDevModeNonces().contains(nonce)) {
                log.warn("[签名过滤器] ⚠️ 开发模式：检测到特殊nonce=\"{}\", 跳过签名验证", nonce);
                filterChain.doFilter(wrappedRequest, response);
                return;
            }
        }

        // 5. 验证签名
        boolean valid = signatureValidator.validateSignature(params);

        if (!valid) {
            // 签名验证失败
            log.warn("[签名过滤器] 签名验证失败: {} {}", method, path);
            sendErrorResponse(response, Code.SIGNATURE_VERIFY_FAILURE, "请求签名验证失败");
            return;
        }

        // 6. 验证通过，继续执行
        log.debug("[签名过滤器] 签名验证通过: {} {}", method, path);
        filterChain.doFilter(wrappedRequest, response);
    }

    /**
     * 提取所有请求参数
     *
     * 支持3种方式：
     * 1. URL 查询参数（GET 请求）
     * 2. HTTP Header（推荐）
     * 3. JSON Body（POST 请求）
     */
    private Map<String, String> extractAllParams(HttpServletRequest request)
            throws IOException {

        Map<String, String> params = new HashMap<>();

        // 方式1: 提取 URL 参数
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });

        // 方式2: 提取 Header 中的签名参数（推荐）
        String timestamp = request.getHeader("X-Timestamp");
        String nonce = request.getHeader("X-Nonce");
        String signature = request.getHeader("X-Signature");

        if (timestamp != null) params.put("timestamp", timestamp);
        if (nonce != null) params.put("nonce", nonce);
        if (signature != null) params.put("signature", signature);

        // 方式3: 提取 JSON Body（仅 POST 请求）
        if ("POST".equalsIgnoreCase(request.getMethod())
            && request.getContentType() != null
            && request.getContentType().contains("application/json")) {

            try {
                ContentCachingRequestWrapper wrapper =
                    (ContentCachingRequestWrapper) request;
                byte[] content = wrapper.getContentAsByteArray();

                if (content.length > 0) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> jsonBody =
                        objectMapper.readValue(content, Map.class);

                    jsonBody.forEach((key, value) -> {
                        if (value != null) {
                            params.put(key, value.toString());
                        }
                    });
                }
            } catch (Exception e) {
                log.error("[签名过滤器] JSON Body 解析失败", e);
            }
        }

        return params;
    }

    /**
     * 检查路径是否在白名单中
     * 使用 Constant 中定义的匿名访问路径
     */
    private boolean isWhitelisted(String path) {
        // 检查精确匹配的路径
        if (Constant.anonymousConstantList.contains(path)) {
            return true;
        }

        // 检查模式匹配的路径（使用通配符）
        return Arrays.stream(Constant.anonymousMatch)
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 返回错误响应
     */
    private void sendErrorResponse(HttpServletResponse response,
                                   int statusCode,
                                   String message) throws IOException {

        response.setStatus(200);  // HTTP状态码设为200，业务错误码在body中
        response.setContentType("application/json;charset=UTF-8");

        // 使用你们项目的统一响应格式
        String json = JSON.toJSONString(
            ResultUtils.failure(statusCode, null, message)
        );

        response.getWriter().write(json);
        response.getWriter().flush();
    }
}
