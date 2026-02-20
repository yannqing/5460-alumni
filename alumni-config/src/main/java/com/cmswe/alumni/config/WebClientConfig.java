package com.cmswe.alumni.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * WebClient 配置类
 * 提供企业级 HTTP 客户端配置
 */
@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${webclient.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${webclient.timeout.read:30000}")
    private int readTimeout;

    @Value("${webclient.timeout.write:30000}")
    private int writeTimeout;

    @Value("${webclient.max-memory-size:16777216}")
    private int maxMemorySize;

    /**
     * 默认 WebClient Bean
     */
    @Bean
    public WebClient webClient() {
        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient()))
                .exchangeStrategies(exchangeStrategies());
        addFilters(builder);
        return builder.build();
    }

    /**
     * 自定义名称的 WebClient Bean（用于特定服务）
     */
    @Bean("customWebClient")
    public WebClient customWebClient() {
        WebClient.Builder builder = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient()))
                .exchangeStrategies(exchangeStrategies());
        addFilters(builder);
        return builder.build();
    }

    /**
     * HTTP 客户端配置
     */
    private HttpClient httpClient() {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS))
                );
    }

    /**
     * 交换策略配置（内存限制等）
     */
    private ExchangeStrategies exchangeStrategies() {
        return ExchangeStrategies.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxMemorySize))
                .build();
    }

    /**
     * 添加过滤器
     */
    private void addFilters(org.springframework.web.reactive.function.client.WebClient.Builder builder) {
        builder
                .filter(logRequest())
                .filter(logResponse())
                .filter(errorHandling());
    }

    /**
     * 请求日志过滤器
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("HTTP Request: {} {}", clientRequest.method(), clientRequest.url());
                clientRequest.headers().forEach((name, values) -> 
                    log.debug("Request Header: {}={}", name, values)
                );
            }
            return Mono.just(clientRequest);
        });
    }

    /**
     * 响应日志过滤器
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("HTTP Response: {}", clientResponse.statusCode());
                clientResponse.headers().asHttpHeaders().forEach((name, values) -> 
                    log.debug("Response Header: {}={}", name, values)
                );
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * 错误处理过滤器
     */
    private ExchangeFilterFunction errorHandling() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                log.error("HTTP Error Response: {} {}", 
                    clientResponse.statusCode().value(), 
                    clientResponse.statusCode());
            }
            return Mono.just(clientResponse);
        });
    }
}