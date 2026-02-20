package com.cmswe.alumni.api.user;

import com.cmswe.alumni.common.dto.WxInitRequest;
import com.cmswe.alumni.common.vo.WxInitResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    WxInitResponse wxInit(WxInitRequest wxInitRequest, HttpServletRequest request) throws JsonProcessingException;
}
