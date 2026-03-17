package com.cmswe.alumni.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户隐私设置初始化事件
 * <p>
 * 用于用户注册后异步初始化用户隐私设置
 *
 * @author CMSWE
 * @since 2026-03-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrivacyInitEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件ID（用于幂等性校验）
     */
    private String eventId;

    /**
     * 微信用户ID
     */
    private Long wxId;

    /**
     * 事件创建时间
     */
    private LocalDateTime createTime;

    /**
     * 事件来源（如：user-register）
     */
    private String source;
}
