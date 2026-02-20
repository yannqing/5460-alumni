package com.cmswe.alumni.common.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 通用分页响应数据
 * @param <T> 数据类型
 */
@Data
@Schema(description = "分页响应数据")
public class PageVo<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "数据列表")
    private List<T> records;

    @Schema(description = "总记录数")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long total;

    @Schema(description = "当前页码")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long current;

    @Schema(description = "每页大小")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long size;

    @Schema(description = "总页数")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pages;

    @Schema(description = "是否有上一页")
    private Boolean hasPrevious;

    @Schema(description = "是否有下一页")
    private Boolean hasNext;

    public PageVo() {}

    public PageVo(List<T> records, Long total, Long current, Long size) {
        this.records = records;
        this.total = total;
        this.current = current;
        this.size = size;
        this.pages = size > 0 ? (total + size - 1) / size : 0;
        this.hasPrevious = current > 1;
        this.hasNext = current < pages;
    }

    /**
     * 从 MyBatis Plus Page 对象转换
     * @param page MyBatis Plus 分页对象
     * @param <T> 数据类型
     * @return PageVo对象
     */
    public static <T> PageVo<T> of(com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> page) {
        return new PageVo<>(
            page.getRecords(),
            page.getTotal(),
            page.getCurrent(),
            page.getSize()
        );
    }
}