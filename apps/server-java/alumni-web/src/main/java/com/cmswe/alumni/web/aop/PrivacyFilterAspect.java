package com.cmswe.alumni.web.aop;

import com.cmswe.alumni.api.user.UserPrivacySettingService;
import com.cmswe.alumni.common.entity.UserPrivacySetting;
import com.cmswe.alumni.redis.utils.RedisCache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 通用隐私过滤切面
 * 根据数据库中的隐私设置自动过滤任何对象中的敏感字段
 *
 * @author system
 */
@Slf4j
@Aspect
@Component
public class PrivacyFilterAspect {

    @Resource
    private UserPrivacySettingService userPrivacySettingService;

    @Resource
    private RedisCache redisCache;

    /**
     * Redis缓存键前缀
     */
    private static final String PRIVACY_CACHE_KEY_PREFIX = "privacy:user:";

    /**
     * Redis缓存过期时间（分钟）
     */
    private static final int CACHE_EXPIRE_MINUTES = 30;

    @Around("@annotation(privacyFilter)")
    public Object handlePrivacy(ProceedingJoinPoint joinPoint, PrivacyFilter privacyFilter) throws Throwable {
        // 1. 检查是否启用隐私过滤
        if (!privacyFilter.enabled()) {
            return joinPoint.proceed();
        }

        // 2. 执行原方法
        Object result = joinPoint.proceed();
        if (result == null) {
            return null;
        }

        // 3. 递归过滤结果对象中的隐私字段
        // 这个方法会自动从每个对象中提取用户ID，并应用对应的隐私设置
        filterPrivacyFields(result, privacyFilter.userIdField());

        return result;
    }

    /**
     * 递归过滤对象中的隐私字段
     * 支持普通对象、集合、Map等各种类型
     *
     * @param obj 要过滤的对象
     * @param userIdFieldName 用户ID字段名
     */
    private void filterPrivacyFields(Object obj, String userIdFieldName) {
        if (obj == null) {
            return;
        }

        // 已处理对象集合，防止循环引用
        Set<Object> processed = new HashSet<>();
        // 用户隐私设置缓存，避免重复查询数据库
        Map<Long, Set<String>> privacyCache = new HashMap<>();

        filterPrivacyFieldsRecursive(obj, userIdFieldName, privacyCache, processed);
    }

    /**
     * 递归过滤隐私字段的内部实现
     *
     * @param obj             要过滤的对象
     * @param userIdFieldName 用户ID字段名
     * @param privacyCache    隐私设置缓存
     * @param processed       已处理对象集合
     */
    private void filterPrivacyFieldsRecursive(Object obj, String userIdFieldName,
                                              Map<Long, Set<String>> privacyCache, Set<Object> processed) {
        if (obj == null) {
            return;
        }

        // 防止循环引用
        if (processed.contains(obj)) {
            return;
        }

        Class<?> clazz = obj.getClass();

        // 跳过基本类型、包装类型、字符串等
        if (isSimpleType(clazz)) {
            return;
        }

        processed.add(obj);

        // 处理集合类型
        if (obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            for (Object item : collection) {
                filterPrivacyFieldsRecursive(item, userIdFieldName, privacyCache, processed);
            }
            return;
        }

        // 处理Map类型
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Object value : map.values()) {
                filterPrivacyFieldsRecursive(value, userIdFieldName, privacyCache, processed);
            }
            return;
        }

        // 处理数组类型
        if (clazz.isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object item = java.lang.reflect.Array.get(obj, i);
                filterPrivacyFieldsRecursive(item, userIdFieldName, privacyCache, processed);
            }
            return;
        }

        // 处理普通对象
        filterObjectFields(obj, userIdFieldName, privacyCache, processed);
    }

    /**
     * 过滤普通对象的字段
     *
     * @param obj 要过滤的对象
     * @param userIdFieldName 用户ID字段名
     * @param privacyCache 隐私设置缓存
     * @param processed 已处理对象集合
     */
    private void filterObjectFields(Object obj, String userIdFieldName,
                                    Map<Long, Set<String>> privacyCache, Set<Object> processed) {
        Class<?> clazz = obj.getClass();

        // 获取所有字段（包括父类字段）
        List<Field> allFields = getAllFields(clazz);

        // 1. 先尝试从对象中提取用户ID
        Long userId = extractUserId(obj, userIdFieldName, allFields);
        Set<String> hiddenFieldCodes = null;

        if (userId != null) {
            // 2. 从缓存或数据库获取该用户的隐私设置
            hiddenFieldCodes = privacyCache.get(userId);
            if (hiddenFieldCodes == null) {
                hiddenFieldCodes = loadUserPrivacySettings(userId);
                privacyCache.put(userId, hiddenFieldCodes);
            }
        }

        // 3. 遍历所有字段，应用隐私过滤和递归处理
        for (Field field : allFields) {
            try {
                // 跳过静态字段和序列化ID
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                        "serialVersionUID".equals(field.getName())) {
                    continue;
                }

                field.setAccessible(true);
                Object fieldValue = field.get(obj);

                if (fieldValue == null) {
                    continue;
                }

                // 如果字段名在隐藏列表中，设置为null
                if (hiddenFieldCodes != null && hiddenFieldCodes.contains(field.getName())) {
                    field.set(obj, null);
                    log.debug("用户 {} 的字段 {} 已被隐私设置隐藏", userId, field.getName());
                    continue;
                }

                // 递归处理嵌套对象
                filterPrivacyFieldsRecursive(fieldValue, userIdFieldName, privacyCache, processed);

            } catch (Exception e) {
                log.warn("过滤字段 {} 时发生异常: {}", field.getName(), e.getMessage());
            }
        }
    }

    /**
     * 从对象中提取用户ID
     *
     * @param obj             对象
     * @param userIdFieldName 用户ID字段名
     * @param allFields       所有字段
     * @return 用户ID，如果未找到则返回null
     */
    private Long extractUserId(Object obj, String userIdFieldName, List<Field> allFields) {
        try {
            for (Field field : allFields) {
                if (field.getName().equals(userIdFieldName)) {
                    field.setAccessible(true);
                    Object value = field.get(obj);
                    if (value instanceof Long) {
                        return (Long) value;
                    } else if (value instanceof Integer) {
                        return ((Integer) value).longValue();
                    } else if (value instanceof String) {
                        return Long.parseLong((String) value);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            log.debug("从对象 {} 中提取用户ID字段 {} 失败: {}",
                    obj.getClass().getSimpleName(), userIdFieldName, e.getMessage());
        }
        return null;
    }

    /**
     * 加载用户的隐私设置（带 Redis 缓存）
     *
     * @param userId 用户ID
     * @return 需要隐藏的字段代码集合
     */
    private Set<String> loadUserPrivacySettings(Long userId) {
        String cacheKey = PRIVACY_CACHE_KEY_PREFIX + userId;

        try {
            // 1. 先从 Redis 缓存中获取
            Set<String> cachedSettings = redisCache.getCacheSet(cacheKey);
            if (cachedSettings != null && !cachedSettings.isEmpty()) {
                log.debug("从 Redis 缓存加载用户 {} 的隐私设置: {}", userId, cachedSettings);
                return cachedSettings;
            }

            // 2. 缓存未命中，从数据库查询
            Set<String> hiddenFieldCodes = new HashSet<>();
            List<UserPrivacySetting> privacySettings = userPrivacySettingService.getByUserId(userId);

            if (privacySettings != null && !privacySettings.isEmpty()) {
                for (UserPrivacySetting setting : privacySettings) {
                    // visibility=0 表示不可见
                    if (setting.getVisibility() != null && setting.getVisibility() == 0) {
                        hiddenFieldCodes.add(setting.getFieldCode());
                    }
                }

                // 3. 将结果缓存到 Redis（即使是空集合也缓存，避免缓存穿透）
                redisCache.setCacheSet(cacheKey, hiddenFieldCodes);
                redisCache.expire(cacheKey, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

                log.info("从数据库加载用户 {} 的隐私设置并缓存到 Redis，隐藏字段: {}", userId, hiddenFieldCodes);
            } else {
                // 4. 用户没有隐私设置，缓存空集合（防止缓存穿透）
                redisCache.setCacheSet(cacheKey, hiddenFieldCodes);
                redisCache.expire(cacheKey, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                log.debug("用户 {} 没有隐私设置，已缓存空集合", userId);
            }

            return hiddenFieldCodes;

        } catch (Exception e) {
            log.error("加载用户 {} 的隐私设置失败", userId, e);
            return new HashSet<>();
        }
    }

    /**
     * 清除用户的隐私设置缓存
     * 当用户修改隐私设置时，应该调用此方法清除缓存
     *
     * @param userId 用户ID
     */
    public void clearUserPrivacyCache(Long userId) {
        String cacheKey = PRIVACY_CACHE_KEY_PREFIX + userId;
        redisCache.deleteObject(cacheKey);
        log.info("已清除用户 {} 的隐私设置缓存", userId);
    }

    /**
     * 获取类的所有字段（包括父类）
     */
    private List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 判断是否为简单类型（不需要递归处理）
     */
    private boolean isSimpleType(Class<?> clazz) {
        return clazz.isPrimitive() ||
                clazz.equals(String.class) ||
                clazz.equals(Boolean.class) ||
                clazz.equals(Byte.class) ||
                clazz.equals(Short.class) ||
                clazz.equals(Integer.class) ||
                clazz.equals(Long.class) ||
                clazz.equals(Float.class) ||
                clazz.equals(Double.class) ||
                clazz.equals(Character.class) ||
                Number.class.isAssignableFrom(clazz) ||
                clazz.isEnum() ||
                clazz.getName().startsWith("java.time.") ||
                clazz.getName().startsWith("java.util.Date");
    }
}