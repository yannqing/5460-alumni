package com.cmswe.alumni.api.user;

import java.util.Map;

public interface WechatApiService {

    Map<String, Object> getPhoneNumber(String phoneCode);

    Map<String, String> getSessionInfo(String code);

    /**
     * 生成小程序码
     * @param page 小程序页面路径（如 pages/alumni-association/apply/apply）
     * @param scene 场景参数（如 id=123456）
     * @return 包含小程序码图片URL的结果
     */
    Map<String, Object> generateMiniProgramQrcode(String page, String scene);
}
