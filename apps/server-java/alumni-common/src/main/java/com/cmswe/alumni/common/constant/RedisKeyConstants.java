package com.cmswe.alumni.common.constant;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisKeyConstants {

    // 用户在线状态：online:users:{userId}
    public final static String USER_ONLINE_STATUS = "online:users:";

    // 用户在线状态：online:users:set
    public final static String USER_ONLINE_SETS = "online:users:set";
}
