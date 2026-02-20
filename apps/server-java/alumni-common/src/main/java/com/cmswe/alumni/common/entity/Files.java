package com.cmswe.alumni.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件表
 * @TableName files
 */
@TableName(value = "files")
@Data
public class Files implements Serializable {

    /**
     * 文件id（雪花❄️）
     */
    @TableId(value = "file_id", type = IdType.ASSIGN_ID)
    private Long fileId;

    /**
     * 用户ID
     */
    @TableField(value = "wx_id")
    private Long wxId;

    /**
     * 会话ID
     */
    @TableField(value = "session_id")
    private String sessionId;

    /**
     * 文件类型：pdf/word/image/text...
     */
    @TableField(value = "file_type")
    private String fileType;

    /**
     * 文件名
     */
    @TableField(value = "file_name")
    private String fileName;

    /**
     * 原始文件名（上传时的名称）
     */
    @TableField(value = "original_name")
    private String originalName;

    /**
     * 显示名称（可编辑）
     */
    @TableField(value = "display_name")
    private String displayName;

    /**
     * 文件扩展名
     */
    @TableField(value = "file_extension")
    private String fileExtension;

    /**
     * 存储类型：OSS/MinIO/S3/Local/FTP等
     */
    @TableField(value = "storage_type")
    private String storageType;

    /**
     * 可访问文件路径
     */
    @TableField(value = "file_url")
    private String fileUrl;

    /**
     * 文件存储路径（不含域名）
     */
    @TableField(value = "file_path")
    private String filePath;

    /**
     * 缩略图URL
     */
    @TableField(value = "thumbnail_url")
    private String thumbnailUrl;

    /**
     * 文件大小（字节）
     */
    @TableField(value = "file_size")
    private Long fileSize;

    /**
     * 文件MD5（用于去重）
     */
    @TableField(value = "file_md5")
    private String fileMd5;

    /**
     * 文件哈希（SHA-256）
     */
    @TableField(value = "file_hash")
    private String fileHash;

    /**
     * MIME 类型
     */
    @TableField(value = "mime_type")
    private String mimeType;

    /**
     * 访问次数
     */
    @TableField(value = "access_count")
    private Integer accessCount;

    /**
     * 下载次数
     */
    @TableField(value = "download_count")
    private Integer downloadCount;

    /**
     * 预览次数
     */
    @TableField(value = "preview_count")
    private Integer previewCount;

    /**
     * 分享次数
     */
    @TableField(value = "share_count")
    private Integer shareCount;

    /**
     * 状态：0-上传中，1-成功，2-失败
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 权限级别：0-公开，1-私有，2-受保护，3-机密
     */
    @TableField(value = "permission_level")
    private Integer permissionLevel;

    /**
     * 上传IP地址（支持IPv6）
     */
    @TableField(value = "upload_ip")
    private String uploadIp;

    /**
     * 创建时间
     */
    @TableField(value = "created_time")
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(value = "updated_time")
    private LocalDateTime updatedTime;

    /**
     * 是否删除（0-未删除，1-已删除）
     */
    @TableLogic
    @TableField(value = "is_deleted")
    private Integer isDeleted;

    @Serial
    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
