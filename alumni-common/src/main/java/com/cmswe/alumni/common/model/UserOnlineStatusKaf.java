package com.cmswe.alumni.common.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "UserOnlineStatusKaf", description = "用户在 Kafka 中的在线状态处理以及存储格式")
public class UserOnlineStatusKaf implements Serializable {

    /**
     * 用户id
     */
    private Long wxId;

    /**
     * 动作类型: ONLINE, OFFLINE, HEARTBEAT
     */
    private StatusAction action;

    /**
     * 服务器实例ID
     */
    private String serverId;

    /**
     * 客户端IP
     */
    private String clientIp;

    /**
     * 设备ID
     */
    private String deviceId;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 额外信息
     */
    private Map<String, Object> metadata;

    public enum StatusAction {
        ONLINE,      // 上线
        OFFLINE,     // 下线
        HEARTBEAT    // 心跳
    }

    @Serial
    private static final long serialVersionUID = 1L;
}
