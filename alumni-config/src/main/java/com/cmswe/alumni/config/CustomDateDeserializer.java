package com.cmswe.alumni.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description: 自定义日期反序列化器，处理各种日期格式
 * @author: yannqing
 * @create: 2025-04-17 16:00
 * @from: <更多资料：yannqing.com>
 **/
@Component
public class CustomDateDeserializer extends JsonDeserializer<Date> {

    private static final String[] DATE_FORMATS = {
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy/MM/dd HH:mm:ss",
        "yyyy/MM/dd",
        "yyyy.MM.dd HH:mm:ss",
        "yyyy.MM.dd",
        "yyyyMMdd",
        "yyyyMMddHHmmss"
    };

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String dateAsString = jsonParser.getText();
        if (dateAsString == null || dateAsString.trim().isEmpty()) {
            return null;
        }

        dateAsString = dateAsString.trim();
        
        // 尝试每一种支持的日期格式进行解析
        for (String format : DATE_FORMATS) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                dateFormat.setLenient(true); // 设置为宽松模式以提高兼容性
                return dateFormat.parse(dateAsString);
            } catch (ParseException ignored) {
                // 忽略解析错误，尝试下一个格式
            }
        }
        
        throw new IOException("无法将字符串 '" + dateAsString + "' 解析为日期，尝试的格式有: " + String.join(", ", DATE_FORMATS));
    }
} 