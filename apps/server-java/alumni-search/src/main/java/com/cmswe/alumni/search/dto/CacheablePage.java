package com.cmswe.alumni.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;
import java.util.List;

/**
 * 可缓存的分页结果包装类
 * 用于解决 Spring PageImpl 无法被 FastJson 序列化的问题
 *
 * @author CNI Alumni System
 * @since 2026-03-25
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheablePage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> content;

    /**
     * 当前页码（从1开始）
     */
    private int pageNum;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 总记录数
     */
    private long totalElements;

    /**
     * 从 Spring Page 创建
     */
    public static <T> CacheablePage<T> fromPage(Page<T> page, int pageNum) {
        return new CacheablePage<>(
                page.getContent(),
                pageNum,
                page.getSize(),
                page.getTotalElements()
        );
    }

    /**
     * 转换为 Spring Page
     */
    public Page<T> toPage() {
        return new PageImpl<>(
                content,
                PageRequest.of(pageNum - 1, pageSize),
                totalElements
        );
    }
}
