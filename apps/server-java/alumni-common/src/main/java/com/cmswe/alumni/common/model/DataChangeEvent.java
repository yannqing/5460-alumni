package com.cmswe.alumni.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Canal 数据变更事件（企业级标准）
 *
 * <p>用于 Canal 监听 MySQL Binlog 后，通过 Kafka 传递给消费者
 *
 * <p>设计参考：阿里巴巴 Canal 官方推荐模型
 *
 * @author CNI Alumni System
 * @since 2025-12-16
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataChangeEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件唯一ID（用于幂等性保证）
     */
    private String eventId;

    /**
     * 数据库名称
     */
    private String database;

    /**
     * 表名称
     */
    private String table;

    /**
     * 事件类型（INSERT/UPDATE/DELETE）
     */
    private EventType eventType;

    /**
     * 变更前的数据（UPDATE/DELETE时有值）
     */
    private Map<String, Object> beforeData;

    /**
     * 变更后的数据（INSERT/UPDATE时有值）
     */
    private Map<String, Object> afterData;

    /**
     * 主键ID（用于快速定位）
     */
    private Long primaryKey;

    /**
     * 主键字段名称（如：wx_id, alumni_id）
     */
    private String primaryKeyName;

    /**
     * Binlog 文件名
     */
    private String binlogFile;

    /**
     * Binlog 位置
     */
    private Long binlogPosition;

    /**
     * 执行时间（MySQL 服务器时间）
     */
    private Long executeTime;

    /**
     * 事件创建时间（Canal 客户端时间）
     */
    private LocalDateTime createTime;

    /**
     * 是否为DDL语句（true-DDL false-DML）
     */
    private Boolean isDdl;

    /**
     * SQL语句（可选，用于调试）
     */
    private String sql;

    /**
     * 数据变更类型枚举
     */
    public enum EventType {
        /**
         * 插入
         */
        INSERT,

        /**
         * 更新
         */
        UPDATE,

        /**
         * 删除
         */
        DELETE,

        /**
         * DDL操作（CREATE/ALTER/DROP等）
         */
        DDL,

        /**
         * 其他
         */
        OTHER
    }

    /**
     * 获取变更后的字段值
     *
     * @param fieldName 字段名
     * @return 字段值
     */
    public Object getAfterValue(String fieldName) {
        return afterData != null ? afterData.get(fieldName) : null;
    }

    /**
     * 获取变更前的字段值
     *
     * @param fieldName 字段名
     * @return 字段值
     */
    public Object getBeforeValue(String fieldName) {
        return beforeData != null ? beforeData.get(fieldName) : null;
    }

    /**
     * 判断指定字段是否发生变化
     *
     * @param fieldName 字段名
     * @return true-变化 false-未变化
     */
    public boolean isFieldChanged(String fieldName) {
        if (eventType != EventType.UPDATE) {
            return false;
        }

        Object before = getBeforeValue(fieldName);
        Object after = getAfterValue(fieldName);

        if (before == null && after == null) {
            return false;
        }

        if (before == null || after == null) {
            return true;
        }

        return !before.equals(after);
    }
}
