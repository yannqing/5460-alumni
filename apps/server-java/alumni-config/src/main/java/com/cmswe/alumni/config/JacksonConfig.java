package com.cmswe.alumni.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * @description: Jackson配置类
 * @author: yannqing
 * @create: 2025-04-17 15:00
 * @from: <更多资料：yannqing.com>
 **/
@Configuration
public class JacksonConfig {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final String TIME_ZONE = "GMT+8";

    @Bean
    @Primary
    public ObjectMapper objectMapper(CustomDateDeserializer customDateDeserializer) {
        // 使用Builder构建基本配置
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json()
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .timeZone(TIME_ZONE)
                .dateFormat(new SimpleDateFormat(DATE_FORMAT))
                .build();
        
        // 配置ObjectMapper
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        objectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        
        // 处理Java8的日期时间类型
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
        
        // 序列化器保持不变
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
        
        // 自定义反序列化器，支持时间戳和字符串格式
        javaTimeModule.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
            @Override
            public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                    // 处理时间戳（毫秒）
                    long timestamp = p.getLongValue();
                    return LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(timestamp), 
                        ZoneId.systemDefault()
                    );
                } else if (p.hasToken(JsonToken.VALUE_STRING)) {
                    // 处理字符串格式
                    String dateString = p.getText();
                    if (dateString == null || dateString.trim().isEmpty()) {
                        return null;
                    }
                    try {
                        // 首先尝试标准格式
                        return LocalDateTime.parse(dateString.trim(), formatter);
                    } catch (DateTimeParseException e) {
                        // 如果标准格式失败，尝试ISO格式
                        try {
                            return LocalDateTime.parse(dateString.trim());
                        } catch (DateTimeParseException e2) {
                            throw new IOException("无法解析日期字符串: " + dateString, e2);
                        }
                    }
                } else {
                    throw new IOException("无法反序列化LocalDateTime，不支持的数据类型: " + p.getCurrentToken());
                }
            }
        });
        
        objectMapper.registerModule(javaTimeModule);
        
        // 注册自定义日期反序列化器
        SimpleModule dateModule = new SimpleModule();
        dateModule.addDeserializer(Date.class, customDateDeserializer);
        objectMapper.registerModule(dateModule);
        
        return objectMapper;
    }
} 