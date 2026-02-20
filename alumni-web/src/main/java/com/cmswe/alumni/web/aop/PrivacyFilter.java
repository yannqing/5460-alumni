package com.cmswe.alumni.web.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 通用隐私过滤注解
 * 根据数据库中的用户隐私设置自动过滤返回结果中的敏感字段
 *
 * 使用方法：
 * 1. 在需要过滤隐私的接口方法上添加此注解
 * 2. 指定返回对象中用户ID的字段名（默认为"wxId"）
 * 3. 系统会自动从返回对象中提取用户ID，查询隐私设置并过滤字段
 * 4. 所有visibility=0的字段会被设置为null
 *
 * 特性：
 * - 支持任意返回类型（VO、DTO、Entity等）
 * - 支持单个对象和列表（每个对象应用各自的隐私设置）
 * - 支持嵌套对象、集合、Map等复杂结构
 * - 基于field_code字段匹配，自动过滤所有匹配的字段
 *
 * 示例：
 * <pre>
 * // 单个用户详情
 * @PrivacyFilter(userIdField = "wxId")
 * public UserDetailVo getUserDetail(Long userId) { ... }
 *
 * // 用户列表（每个用户应用各自的隐私设置）
 * @PrivacyFilter(userIdField = "wxId")
 * public List<UserVo> getUserList() { ... }
 * </pre>
 *
 * @author system
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PrivacyFilter {

    /**
     * 是否启用隐私过滤
     * 默认为true
     */
    boolean enabled() default true;

    /**
     * 返回对象中用户ID的字段名
     * 系统会自动从返回对象中查找此字段，提取用户ID并应用对应的隐私设置
     * 默认为"wxId"，可以指定为"userId"、"id"等
     *
     * 示例：
     * - userIdField = "wxId" -> 从返回对象的 wxId 字段获取用户ID
     * - userIdField = "userId" -> 从返回对象的 userId 字段获取用户ID
     */
    String userIdField() default "wxId";
}