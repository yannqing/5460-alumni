package com.cmswe.alumni.api.user;

import java.util.Map;

public interface WechatApiService {

    Map<String, Object> getPhoneNumber(String phoneCode);

    Map<String, String> getSessionInfo(String code);
}
