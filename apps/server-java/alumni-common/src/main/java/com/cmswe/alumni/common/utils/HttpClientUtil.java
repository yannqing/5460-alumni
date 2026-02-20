package com.cmswe.alumni.common.utils;

import com.cmswe.alumni.common.exception.HttpClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 企业级 WebClient 工具类
 * 提供统一的 HTTP 请求封装
 */
@Slf4j
@Component
public class HttpClientUtil {

    private final WebClient webClient;
    private final WebClient customWebClient;

    // 默认超时时间
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    @Autowired
    public HttpClientUtil(WebClient webClient, @Qualifier("customWebClient") WebClient customWebClient) {
        this.webClient = webClient;
        this.customWebClient = customWebClient;
    }

    /**
     * GET 请求 - 返回字符串
     */
    public String get(String url) {
        return get(url, String.class, null, null);
    }

    /**
     * GET 请求 - 指定返回类型
     */
    public <T> T get(String url, Class<T> responseType) {
        return get(url, responseType, null, null);
    }

    /**
     * GET 请求 - 带请求头
     */
    public <T> T get(String url, Class<T> responseType, Map<String, String> headers) {
        return get(url, responseType, headers, null);
    }

    /**
     * GET 请求 - 完整参数
     */
    public <T> T get(String url, Class<T> responseType, Map<String, String> headers, Duration timeout) {
        return executeRequest(() -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.get().uri(url);
            if (headers != null) {
                headers.forEach(requestSpec::header);
            }
            return requestSpec.retrieve()
                    .bodyToMono(responseType)
                    .timeout(timeout != null ? timeout : DEFAULT_TIMEOUT);
        }, "GET", url);
    }

    /**
     * GET 请求 - 返回复杂类型（支持泛型）
     */
    public <T> T get(String url, ParameterizedTypeReference<T> responseType) {
        return get(url, responseType, null, null);
    }

    /**
     * GET 请求 - 返回复杂类型，带请求头
     */
    public <T> T get(String url, ParameterizedTypeReference<T> responseType, Map<String, String> headers, Duration timeout) {
        return executeRequest(() -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.get().uri(url);
            if (headers != null) {
                headers.forEach(requestSpec::header);
            }
            return requestSpec.retrieve()
                    .bodyToMono(responseType)
                    .timeout(timeout != null ? timeout : DEFAULT_TIMEOUT);
        }, "GET", url);
    }

    /**
     * POST 请求 - JSON 请求体
     */
    public <T> T post(String url, Object requestBody, Class<T> responseType) {
        return post(url, requestBody, responseType, null, null);
    }

    /**
     * POST 请求 - JSON 请求体，带请求头
     */
    public <T> T post(String url, Object requestBody, Class<T> responseType, Map<String, String> headers) {
        return post(url, requestBody, responseType, headers, null);
    }

    /**
     * POST 请求 - 完整参数
     */
    public <T> T post(String url, Object requestBody, Class<T> responseType, Map<String, String> headers, Duration timeout) {
        return executeRequest(() -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody));
            
            if (headers != null) {
                headers.forEach(requestSpec::header);
            }
            
            return requestSpec.retrieve()
                    .bodyToMono(responseType)
                    .timeout(timeout != null ? timeout : DEFAULT_TIMEOUT);
        }, "POST", url);
    }

    /**
     * POST 请求 - 表单数据
     */
    public <T> T postForm(String url, MultiValueMap<String, String> formData, Class<T> responseType) {
        return postForm(url, formData, responseType, null, null);
    }

    /**
     * POST 请求 - 表单数据，完整参数
     */
    public <T> T postForm(String url, MultiValueMap<String, String> formData, Class<T> responseType, 
                         Map<String, String> headers, Duration timeout) {
        return executeRequest(() -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(formData));
            
            if (headers != null) {
                headers.forEach(requestSpec::header);
            }
            
            return requestSpec.retrieve()
                    .bodyToMono(responseType)
                    .timeout(timeout != null ? timeout : DEFAULT_TIMEOUT);
        }, "POST", url);
    }

    /**
     * PUT 请求
     */
    public <T> T put(String url, Object requestBody, Class<T> responseType) {
        return put(url, requestBody, responseType, null, null);
    }

    /**
     * PUT 请求 - 完整参数
     */
    public <T> T put(String url, Object requestBody, Class<T> responseType, Map<String, String> headers, Duration timeout) {
        return executeRequest(() -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.put()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody));
            
            if (headers != null) {
                headers.forEach(requestSpec::header);
            }
            
            return requestSpec.retrieve()
                    .bodyToMono(responseType)
                    .timeout(timeout != null ? timeout : DEFAULT_TIMEOUT);
        }, "PUT", url);
    }

    /**
     * DELETE 请求
     */
    public <T> T delete(String url, Class<T> responseType) {
        return delete(url, responseType, null, null);
    }

    /**
     * DELETE 请求 - 完整参数
     */
    public <T> T delete(String url, Class<T> responseType, Map<String, String> headers, Duration timeout) {
        return executeRequest(() -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.delete().uri(url);
            if (headers != null) {
                headers.forEach(requestSpec::header);
            }
            return requestSpec.retrieve()
                    .bodyToMono(responseType)
                    .timeout(timeout != null ? timeout : DEFAULT_TIMEOUT);
        }, "DELETE", url);
    }

    /**
     * PATCH 请求
     */
    public <T> T patch(String url, Object requestBody, Class<T> responseType) {
        return patch(url, requestBody, responseType, null, null);
    }

    /**
     * PATCH 请求 - 完整参数
     */
    public <T> T patch(String url, Object requestBody, Class<T> responseType, Map<String, String> headers, Duration timeout) {
        return executeRequest(() -> {
            WebClient.RequestHeadersSpec<?> requestSpec = webClient.patch()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody));
            
            if (headers != null) {
                headers.forEach(requestSpec::header);
            }
            
            return requestSpec.retrieve()
                    .bodyToMono(responseType)
                    .timeout(timeout != null ? timeout : DEFAULT_TIMEOUT);
        }, "PATCH", url);
    }

    /**
     * 自定义请求构建器
     */
    public WebClient.RequestHeadersUriSpec<?> customRequest() {
        return customWebClient.method(null);
    }

    /**
     * 使用自定义 WebClient
     */
    public <T> T executeWithCustomClient(String url, Consumer<WebClient.RequestHeadersSpec<?>> requestCustomizer, 
                                        Class<T> responseType, Duration timeout) {
        return executeRequest(() -> {
            WebClient.RequestHeadersSpec<?> requestSpec = customWebClient.get().uri(url);
            requestCustomizer.accept(requestSpec);
            return requestSpec.retrieve()
                    .bodyToMono(responseType)
                    .timeout(timeout != null ? timeout : DEFAULT_TIMEOUT);
        }, "CUSTOM", url);
    }

    /**
     * 执行请求的通用方法，包含异常处理和日志记录
     */
    private <T> T executeRequest(java.util.function.Supplier<Mono<T>> requestSupplier, String method, String url) {
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("发起HTTP请求: {} {}", method, url);
            
            T result = requestSupplier.get()
                    .doOnSuccess(response -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.info("HTTP请求成功: {} {} - 耗时: {}ms", method, url, duration);
                    })
                    .doOnError(error -> {
                        long duration = System.currentTimeMillis() - startTime;
                        log.error("HTTP请求失败: {} {} - 耗时: {}ms, 错误: {}", method, url, duration, error.getMessage());
                    })
                    .block();
            
            return result;
            
        } catch (WebClientResponseException e) {
            String errorMessage = String.format("HTTP请求响应错误: %s %s - 状态码: %d, 响应体: %s", 
                    method, url, e.getStatusCode().value(), e.getResponseBodyAsString());
            log.error(errorMessage, e);
            throw new HttpClientException(errorMessage, e, e.getStatusCode().value());
            
        } catch (Exception e) {
            String errorMessage = String.format("HTTP请求异常: %s %s - 错误: %s", method, url, e.getMessage());
            log.error(errorMessage, e);
            throw new HttpClientException(errorMessage, e);
        }
    }

    /**
     * 构建认证头
     */
    public static Map<String, String> bearerToken(String token) {
        return Map.of(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    /**
     * 构建基础认证头
     */
    public static Map<String, String> basicAuth(String username, String password) {
        String auth = username + ":" + password;
        String encodedAuth = java.util.Base64.getEncoder().encodeToString(auth.getBytes());
        return Map.of(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
    }

    /**
     * 构建通用请求头
     */
    public static Map<String, String> commonHeaders() {
        return Map.of(
                HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE,
                HttpHeaders.USER_AGENT, "Alumni-System-HttpClient/1.0"
        );
    }
}