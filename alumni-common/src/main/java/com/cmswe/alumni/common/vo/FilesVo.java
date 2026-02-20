package com.cmswe.alumni.common.vo;

import com.cmswe.alumni.common.entity.Files;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "文件响应内容")
public class FilesVo implements Serializable {

    /**
     * 文件id（雪花❄️）
     */
    @Schema(description = "文件id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileId;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long wxId;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    private String sessionId;

    /**
     * 文件类型：pdf/word/image/text...
     */
    @Schema(description = "文件类型：pdf/word/image/text...")
    private String fileType;

    /**
     * 显示名称（可编辑）
     */
    @Schema(description = "显示名称（可编辑）")
    private String displayName;

    /**
     * 文件扩展名
     */
    @Schema(description = "文件扩展名")
    private String fileExtension;

    /**
     * 可访问文件路径
     */
    @Schema(description = "可访问文件路径")
    private String fileUrl;

    /**
     * 文件存储路径（不含域名）
     */
    @Schema(description = "文件存储路径")
    private String filePath;

    /**
     * 缩略图URL
     */
    @Schema(description = "缩略图URL")
    private String thumbnailUrl;

    /**
     * 文件大小（字节）
     */
    @Schema(description = "文件大小")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long fileSize;

    /**
     * 访问次数
     */
    @Schema(description = "访问次数")
    private Integer accessCount;

    /**
     * 下载次数
     */
    @Schema(description = "下载次数")
    private Integer downloadCount;

    /**
     * 预览次数
     */
    @Schema(description = "预览次数")
    private Integer previewCount;

    /**
     * 分享次数
     */
    @Schema(description = "分享次数")
    private Integer shareCount;

    /**
     * 状态：0-上传中，1-成功，2-失败
     */
    @Schema(description = "状态：0-上传中，1-成功，2-失败")
    private Integer status;

    @Serial
    private static final long serialVersionUID = 1L;

    public static FilesVo objToVo(Files file) {
        if (file == null) {
            throw new BusinessException(ErrorType.SYSTEM_ERROR);
        }
        FilesVo vo = new FilesVo();
        BeanUtils.copyProperties(file, vo);
        return vo;
    }
}
