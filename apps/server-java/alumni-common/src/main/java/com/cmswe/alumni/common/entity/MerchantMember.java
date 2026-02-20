package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商户成员关系表
 * @TableName merchant_member
 */
@TableName(value = "merchant_member")
@Data
public class MerchantMember implements Serializable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 校友ID
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 商户ID
     */
    @TableField(value = "merchant_id")
    private Long merchantId;

    /**
     * 店铺ID
     */
    @TableField(value = "shop_id")
    private Long shopId;

    /**
     * 成员架构角色ID
     */
    @TableField(value = "role_or_id")
    private Long roleOrId;

    /**
     * 加入时间
     */
    @TableField(value = "join_time")
    private LocalDateTime joinTime;

    /**
     * 状态：0-退出 1-正常
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(value = "is_delete")
    private Integer isDelete;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
