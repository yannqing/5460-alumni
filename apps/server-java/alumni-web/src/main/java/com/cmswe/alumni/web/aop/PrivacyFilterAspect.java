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
        String methodName = joinPoint.getSignature().getName();
        log.debug("==== @PrivacyFilter AOP 开始执行 - 方法: {} ====", methodName);

        // 1. 检查是否启用隐私过滤
        if (!privacyFilter.enabled()) {
            log.debug("隐私过滤未启用，跳过过滤 - 方法: {}", methodName);
            return joinPoint.proceed();
        }

        // 2. 执行原方法
        Object result = joinPoint.proceed();
        if (result == null) {
            log.debug("方法返回值为空，跳过过滤 - 方法: {}", methodName);
            return null;
        }

        log.debug("开始过滤隐私字段 - 方法: {}, 返回类型: {}, userIdField: {}",
                methodName, result.getClass().getSimpleName(), privacyFilter.userIdField());

        // 3. 递归过滤结果对象中的隐私字段
        // 这个方法会自动从每个对象中提取用户ID，并应用对应的隐私设置
        filterPrivacyFields(result, privacyFilter.userIdField());

        log.debug("==== @PrivacyFilter AOP 执行完成 - 方法: {} ====", methodName);
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

        // 批量预热：递归查找所有集合，提取所有用户ID，批量加载隐私设置
        long startTime = System.currentTimeMillis();
        List<Long> allUserIds = extractAllUserIds(obj, userIdFieldName, new HashSet<>());

        if (!allUserIds.isEmpty()) {
            log.debug("从结果对象中提取到 {} 个用户ID，开始批量加载隐私设置", allUserIds.size());
            batchLoadPrivacySettings(allUserIds, privacyCache);
            long duration = System.currentTimeMillis() - startTime;
            log.info("批量加载隐私设置完成 - 用户数: {}, 耗时: {}ms", allUserIds.size(), duration);
        }

        filterPrivacyFieldsRecursive(obj, userIdFieldName, privacyCache, processed);
    }

    /**
     * 递归提取对象中的所有用户ID
     *
     * @param obj 对象
     * @param userIdFieldName 用户ID字段名
     * @param processed 已处理对象集合
     * @return 用户ID列表
     */
    private List<Long> extractAllUserIds(Object obj, String userIdFieldName, Set<Object> processed) {
        List<Long> userIds = new ArrayList<>();

        if (obj == null || processed.contains(obj)) {
            return userIds;
        }

        Class<?> clazz = obj.getClass();

        // 跳过简单类型
        if (isSimpleType(clazz)) {
            return userIds;
        }

        processed.add(obj);

        // 处理集合
        if (obj instanceof Collection) {
            Collection<?> collection = (Collection<?>) obj;
            for (Object item : collection) {
                Long userId = extractUserIdFromObject(item, userIdFieldName);
                if (userId != null) {
                    userIds.add(userId);
                }
                // 递归处理嵌套对象
                userIds.addAll(extractAllUserIds(item, userIdFieldName, processed));
            }
            return userIds;
        }

        // 处理Map
        if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            for (Object value : map.values()) {
                userIds.addAll(extractAllUserIds(value, userIdFieldName, processed));
            }
            return userIds;
        }

        // 处理数组
        if (clazz.isArray()) {
            int length = java.lang.reflect.Array.getLength(obj);
            for (int i = 0; i < length; i++) {
                Object item = java.lang.reflect.Array.get(obj, i);
                userIds.addAll(extractAllUserIds(item, userIdFieldName, processed));
            }
            return userIds;
        }

        // 处理普通对象：提取用户ID并递归处理字段
        Long userId = extractUserIdFromObject(obj, userIdFieldName);
        if (userId != null) {
            userIds.add(userId);
        }

        // 递归处理对象的所有字段
        List<Field> allFields = getAllFields(clazz);
        for (Field field : allFields) {
            try {
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                        "serialVersionUID".equals(field.getName())) {
                    continue;
                }
                field.setAccessible(true);
                Object fieldValue = field.get(obj);
                if (fieldValue != null) {
                    userIds.addAll(extractAllUserIds(fieldValue, userIdFieldName, processed));
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }

        return userIds;
    }

    /**
     * 批量加载隐私设置到内存缓存
     *
     * @param userIds 用户ID列表
     * @param privacyCache 隐私设置缓存
     */
    private void batchLoadPrivacySettings(List<Long> userIds, Map<Long, Set<String>> privacyCache) {
        if (userIds == null || userIds.isEmpty()) {
            return;
        }

        try {
            // 构建所有的 cache key
            List<String> cacheKeys = new ArrayList<>();
            Map<String, Long> keyToUserIdMap = new HashMap<>();
            for (Long userId : userIds) {
                if (!privacyCache.containsKey(userId)) {
                    String cacheKey = PRIVACY_CACHE_KEY_PREFIX + userId;
                    cacheKeys.add(cacheKey);
                    keyToUserIdMap.put(cacheKey, userId);
                }
            }

            if (cacheKeys.isEmpty()) {
                return;
            }

            // 批量从 Redis 读取（一次 Pipeline 操作）
            Map<String, Set<String>> cacheResults = null;
            List<String> corruptedKeys = new ArrayList<>();

            try {
                cacheResults = redisCache.batchGetCacheSet(cacheKeys);
                log.info("批量从 Redis 读取隐私设置 - 请求数: {}, 返回数: {}", cacheKeys.size(), cacheResults.size());
            } catch (Exception e) {
                log.error("批量从 Redis 读取隐私设置失败，可能存在数据类型错误 - 错误: {}", e.getMessage());
                // 如果批量读取失败，逐个尝试读取以找出损坏的key
                cacheResults = new HashMap<>();
                for (String cacheKey : cacheKeys) {
                    try {
                        Set<String> result = redisCache.getCacheSet(cacheKey);
                        if (result != null) {
                            cacheResults.put(cacheKey, result);
                        }
                    } catch (Exception ex) {
                        Long userId = keyToUserIdMap.get(cacheKey);
                        log.warn("用户 {} 的 Redis 缓存数据损坏 - 错误: {}", userId, ex.getMessage());
                        corruptedKeys.add(cacheKey);
                    }
                }
            }

            // 删除损坏的缓存key
            if (!corruptedKeys.isEmpty()) {
                log.warn("发现 {} 个损坏的缓存key，正在清理", corruptedKeys.size());
                for (String corruptedKey : corruptedKeys) {
                    try {
                        redisCache.deleteObject(corruptedKey);
                        log.info("已删除损坏的缓存: {}", corruptedKey);
                    } catch (Exception e) {
                        log.error("删除损坏缓存失败: {}", corruptedKey, e);
                    }
                }
            }

            // 检查 Redis 中哪些 key 实际存在
            Map<String, Boolean> existsMap = redisCache.batchHasKey(cacheKeys);

            // 收集缓存未命中的用户ID（需要从数据库加载）
            List<Long> uncachedUserIds = new ArrayList<>();

            // 加载到内存缓存
            for (Map.Entry<String, Set<String>> entry : cacheResults.entrySet()) {
                String cacheKey = entry.getKey();
                Set<String> cachedSettings = entry.getValue();
                Long userId = keyToUserIdMap.get(cacheKey);

                // 判断 key 是否真实存在（区分 "key 不存在" 和 "key 存在但值为空"）
                Boolean keyExists = existsMap.get(cacheKey);

                log.debug("处理用户 {} 的缓存 - keyExists: {}, cachedSettings: {}", userId, keyExists, cachedSettings);

                if (Boolean.TRUE.equals(keyExists)) {
                    // key 存在，先过滤掉空字符串占位符
                    cachedSettings.remove("");

                    // 再判断是否为空
                    if (!cachedSettings.isEmpty()) {
                        // 缓存命中且有隐藏字段
                        privacyCache.put(userId, cachedSettings);
                        log.debug("用户 {} 从 Redis 加载隐私设置成功 - 隐藏字段: {}", userId, cachedSettings);
                    } else {
                        // 缓存命中但没有隐藏字段（用户所有字段都是可见的）
                        privacyCache.put(userId, new HashSet<>());
                        log.debug("用户 {} 的 Redis 缓存表示无隐藏字段（所有字段可见）", userId);
                    }
                } else {
                    // key 不存在，加入待加载列表
                    uncachedUserIds.add(userId);
                    log.debug("用户 {} 的隐私设置在 Redis 中不存在，需要从数据库加载", userId);
                }
            }

            // 将损坏的key对应的用户也加入待加载列表
            for (String corruptedKey : corruptedKeys) {
                Long userId = keyToUserIdMap.get(corruptedKey);
                if (userId != null) {
                    uncachedUserIds.add(userId);
                    log.debug("用户 {} 的缓存已损坏，加入待加载列表", userId);
                }
            }

            // 批量从数据库加载缓存未命中的用户隐私设置
            if (!uncachedUserIds.isEmpty()) {
                log.info("缓存未命中用户数: {}，开始从数据库批量加载", uncachedUserIds.size());
                long dbStartTime = System.currentTimeMillis();

                // 批量查询数据库
                long queryStartTime = System.currentTimeMillis();
                Map<Long, List<UserPrivacySetting>> dbSettingsMap = userPrivacySettingService.batchGetByUserIds(uncachedUserIds);
                long queryDuration = System.currentTimeMillis() - queryStartTime;
                log.info("数据库批量查询完成 - 耗时: {}ms", queryDuration);

                // 处理查询结果并写入内存缓存
                long processStartTime = System.currentTimeMillis();
                Map<String, Set<String>> redisDataMap = new HashMap<>();

                for (Long userId : uncachedUserIds) {
                    List<UserPrivacySetting> settings = dbSettingsMap.getOrDefault(userId, new ArrayList<>());

                    Set<String> hiddenFieldCodes = new HashSet<>();
                    for (UserPrivacySetting setting : settings) {
                        if (setting.getVisibility() != null && setting.getVisibility() == 0) {
                            hiddenFieldCodes.add(setting.getFieldCode());
                        }
                    }

                    // 写入内存缓存
                    privacyCache.put(userId, hiddenFieldCodes);

                    // 准备 Redis 数据（延迟批量写入）
                    String cacheKey = PRIVACY_CACHE_KEY_PREFIX + userId;
                    redisDataMap.put(cacheKey, hiddenFieldCodes);

                    log.debug("从数据库加载用户 {} 的隐私设置 - 记录数: {}, 隐藏字段: {}",
                            userId, settings.size(), hiddenFieldCodes);
                }
                long processDuration = System.currentTimeMillis() - processStartTime;
                log.info("数据处理完成 - 耗时: {}ms", processDuration);

                // 批量写入 Redis（使用 Pipeline）
                long redisStartTime = System.currentTimeMillis();
                redisCache.batchSetCacheSet(redisDataMap, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                long redisDuration = System.currentTimeMillis() - redisStartTime;
                log.info("Redis 批量写入完成 - 耗时: {}ms", redisDuration);

                long dbDuration = System.currentTimeMillis() - dbStartTime;
                log.info("批量从数据库加载隐私设置完成 - 用户数: {}, 总耗时: {}ms (查询: {}ms, 处理: {}ms, Redis: {}ms)",
                        uncachedUserIds.size(), dbDuration, queryDuration, processDuration, redisDuration);
            }

        } catch (Exception e) {
            log.error("批量加载隐私设置失败", e);
        }
    }

    /**
     * 批量预热隐私设置缓存
     * 从集合中提取所有用户ID，批量加载到内存缓存
     *
     * @param collection 对象集合
     * @param userIdFieldName 用户ID字段名
     * @param privacyCache 隐私设置缓存
     */

    /**
     * 从对象中提取用户ID
     *
     * @param obj 对象
     * @param userIdFieldName 用户ID字段名
     * @return 用户ID，如果未找到则返回null
     */
    private Long extractUserIdFromObject(Object obj, String userIdFieldName) {
        if (obj == null) {
            return null;
        }

        try {
            Class<?> clazz = obj.getClass();
            List<Field> allFields = getAllFields(clazz);

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
            Set<String> cachedSettings = null;
            boolean needReloadFromDb = false;

            try {
                cachedSettings = redisCache.getCacheSet(cacheKey);
                log.info("尝试从 Redis 加载用户 {} 的隐私设置 - 缓存结果: {}", userId, cachedSettings);
            } catch (Exception e) {
                // Redis类型错误或反序列化失败，删除损坏的key并重新加载
                log.warn("用户 {} 的 Redis 缓存数据损坏（可能是数据类型错误或反序列化失败），将删除并重新加载 - 错误: {}", userId, e.getMessage());
                try {
                    redisCache.deleteObject(cacheKey);
                    log.info("已删除用户 {} 的损坏缓存", userId);
                } catch (Exception deleteEx) {
                    log.error("删除用户 {} 的损坏缓存失败", userId, deleteEx);
                }
                needReloadFromDb = true;
            }

            if (!needReloadFromDb && cachedSettings != null && !cachedSettings.isEmpty()) {
                // 过滤掉空字符串占位符
                cachedSettings.remove("");
                log.info("从 Redis 缓存加载用户 {} 的隐私设置: {} (过滤空字符串后)", userId, cachedSettings);
                return cachedSettings;
            }

            log.info("用户 {} 的隐私设置缓存未命中或为空，从数据库加载", userId);

            // 2. 缓存未命中，从数据库查询
            Set<String> hiddenFieldCodes = new HashSet<>();
            List<UserPrivacySetting> privacySettings = userPrivacySettingService.getByUserId(userId);

            log.info("从数据库查询用户 {} 的隐私设置 - 记录数: {}", userId,
                    privacySettings != null ? privacySettings.size() : 0);

            if (privacySettings != null && !privacySettings.isEmpty()) {
                int hiddenCount = 0;
                for (UserPrivacySetting setting : privacySettings) {
                    log.debug("隐私设置 - userId: {}, fieldCode: {}, visibility: {}",
                            userId, setting.getFieldCode(), setting.getVisibility());
                    // visibility=0 表示不可见
                    if (setting.getVisibility() != null && setting.getVisibility() == 0) {
                        hiddenFieldCodes.add(setting.getFieldCode());
                        hiddenCount++;
                    }
                }

                // 3. 将结果缓存到 Redis（即使是空集合也缓存，避免缓存穿透）
                // 使用优化版本的 setCacheSet，一次操作完成写入和设置过期时间
                redisCache.setCacheSet(cacheKey, hiddenFieldCodes, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);

                log.info("从数据库加载用户 {} 的隐私设置并缓存到 Redis - 总记录: {}, 隐藏字段数: {}, 隐藏字段: {}",
                        userId, privacySettings.size(), hiddenCount, hiddenFieldCodes);
            } else {
                // 4. 用户没有隐私设置，缓存空集合（防止缓存穿透）
                redisCache.setCacheSet(cacheKey, hiddenFieldCodes, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
                log.warn("用户 {} 没有隐私设置记录，已缓存空集合", userId);
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