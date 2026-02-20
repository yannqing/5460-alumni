package com.cmswe.alumni.redis.utils;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

@SuppressWarnings(value = { "unchecked", "rawtypes" })
@Component
public class RedisCache
{
    @Resource
    public RedisTemplate redisTemplate;

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     */
    public <T> void setCacheObject(final String key, final T value)
    {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 缓存基本的对象，Integer、String、实体类等
     *
     * @param key 缓存的键值
     * @param value 缓存的值
     * @param timeout 时间
     * @param timeUnit 时间颗粒度
     */
    public <T> void setCacheObject(final String key, final T value, final Integer timeout, final TimeUnit timeUnit)
    {
        redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout)
    {
        return expire(key, timeout, TimeUnit.SECONDS);
    }

    /**
     * 设置有效时间
     *
     * @param key Redis键
     * @param timeout 超时时间
     * @param unit 时间单位
     * @return true=设置成功；false=设置失败
     */
    public boolean expire(final String key, final long timeout, final TimeUnit unit)
    {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获得缓存的基本对象。
     *
     * @param key 缓存键值
     * @return 缓存键值对应的数据
     */
    public <T> T getCacheObject(final String key)
    {
        ValueOperations<String, T> operation = redisTemplate.opsForValue();
        return operation.get(key);
    }

    /**
     * 删除单个对象
     *
     * @param key redis key
     */
    public boolean deleteObject(final String key)
    {
        return redisTemplate.delete(key);
    }

    /**
     * 删除集合对象
     *
     * @param collection 多个对象
     * @return 返回删除结果
     */
    public long deleteObject(final Collection collection)
    {
        return redisTemplate.delete(collection);
    }

    /**
     * 缓存List数据
     *
     * @param key 缓存的键值
     * @param dataList 待缓存的List数据
     * @return 缓存的对象
     */
    public <T> long setCacheList(final String key, final List<T> dataList)
    {
        Long count = redisTemplate.opsForList().rightPushAll(key, dataList);
        return count == null ? 0 : count;
    }

    /**
     * 获得缓存的list对象
     *
     * @param key 缓存的键值
     * @return 缓存键值对应的数据
     */
    public <T> List<T> getCacheList(final String key)
    {
        return redisTemplate.opsForList().range(key, 0, -1);
    }

    /**
     * 缓存Set
     *
     * @param key 缓存键值
     * @param dataSet 缓存的数据
     * @return 缓存数据的对象
     */
    public <T> BoundSetOperations<String, T> setCacheSet(final String key, final Set<T> dataSet)
    {
        BoundSetOperations<String, T> setOperation = redisTemplate.boundSetOps(key);
        Iterator<T> it = dataSet.iterator();
        while (it.hasNext())
        {
            setOperation.add(it.next());
        }
        return setOperation;
    }

    /**
     * 获得缓存的set
     *
     * @param key key
     * @return 返回结果
     */
    public <T> Set<T> getCacheSet(final String key)
    {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 缓存Map
     *
     * @param key key
     * @param dataMap 要删除的 map
     */
    public <T> void setCacheMap(final String key, final Map<String, T> dataMap)
    {
        if (dataMap != null) {
            redisTemplate.opsForHash().putAll(key, dataMap);
        }
    }

    /**
     * 获得缓存的Map
     *
     * @param key key
     * @return 返回结果
     */
    public <T> Map<String, T> getCacheMap(final String key)
    {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 往Hash中存入数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @param value 值
     */
    public <T> void setCacheMapValue(final String key, final String hKey, final T value)
    {
        redisTemplate.opsForHash().put(key, hKey, value);
    }

    /**
     * 获取Hash中的数据
     *
     * @param key Redis键
     * @param hKey Hash键
     * @return Hash中的对象
     */
    public <T> T getCacheMapValue(final String key, final String hKey)
    {
        HashOperations<String, String, T> opsForHash = redisTemplate.opsForHash();
        return opsForHash.get(key, hKey);
    }

    /**
     * 删除Hash中的数据
     *
     * @param key key
     * @param hkey hkey
     */
    public void delCacheMapValue(final String key, final String hkey)
    {
        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.delete(key, hkey);
    }

    /**
     * 获取多个Hash中的数据
     *
     * @param key Redis键
     * @param hKeys Hash键集合
     * @return Hash对象集合
     */
    public <T> List<T> getMultiCacheMapValue(final String key, final Collection<Object> hKeys)
    {
        return redisTemplate.opsForHash().multiGet(key, hKeys);
    }

    /**
     * 获得缓存的基本对象列表
     *
     * @param pattern 字符串前缀
     * @return 对象列表
     */
    public Collection<String> keys(final String pattern)
    {
        return redisTemplate.keys(pattern);
    }

    // ==================== 增强方法（用于在线状态管理） ====================

    /**
     * 检查 Key 是否存在
     *
     * @param key Redis 键
     * @return true=存在；false=不存在
     */
    public boolean hasKey(final String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 向 Set 中添加元素（原子操作）
     *
     * @param key    Redis 键
     * @param values 要添加的值
     * @return 成功添加的元素数量
     */
    public <T> Long addToSet(final String key, T... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 从 Set 中移除元素（原子操作）
     *
     * @param key    Redis 键
     * @param values 要移除的值
     * @return 成功移除的元素数量
     */
    public <T> Long removeFromSet(final String key, T... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 获取 Set 的大小
     *
     * @param key Redis 键
     * @return Set 的元素数量
     */
    public Long getSetSize(final String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 向 List 右侧添加元素
     *
     * @param key   Redis 键
     * @param value 要添加的值
     * @return List 的新长度
     */
    public <T> Long rightPushToList(final String key, final T value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 向 List 左侧添加元素
     *
     * @param key   Redis 键
     * @param value 要添加的值
     * @return List 的新长度
     */
    public <T> Long leftPushToList(final String key, final T value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 从 List 获取指定范围的元素
     *
     * @param key   Redis 键
     * @param start 起始索引（0 开始）
     * @param end   结束索引（-1 表示最后一个）
     * @return 元素列表
     */
    public <T> List<T> getListRange(final String key, final long start, final long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取 List 的大小
     *
     * @param key Redis 键
     * @return List 的元素数量
     */
    public Long getListSize(final String key) {
        return redisTemplate.opsForList().size(key);
    }

    /**
     * 原子递增
     *
     * @param key Redis 键
     * @return 递增后的值
     */
    public Long increment(final String key) {
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * 原子递增（指定步长）
     *
     * @param key   Redis 键
     * @param delta 步长
     * @return 递增后的值
     */
    public Long incrementBy(final String key, final long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 原子递减
     *
     * @param key Redis 键
     * @return 递减后的值
     */
    public Long decrement(final String key) {
        return redisTemplate.opsForValue().decrement(key);
    }

    /**
     * 原子递减（指定步长）
     *
     * @param key   Redis 键
     * @param delta 步长
     * @return 递减后的值
     */
    public Long decrementBy(final String key, final long delta) {
        return redisTemplate.opsForValue().decrement(key, delta);
    }

    // ==================== 业务专用方法 ====================

    /**
     * 设置用户在线状态（带过期时间）
     *
     * @param userId   用户ID
     * @param value    状态值
     * @param timeout  过期时间
     * @param timeUnit 时间单位
     */
    public <T> void setUserOnlineStatus(final Long userId, final T value, final Integer timeout, final TimeUnit timeUnit) {
        String key = "online:users:" + userId;
        setCacheObject(key, value, timeout, timeUnit);
    }

    /**
     * 删除用户在线状态
     *
     * @param userId 用户ID
     * @return true=删除成功；false=删除失败
     */
    public boolean deleteUserOnlineStatus(final Long userId) {
        String key = "online:users:" + userId;
        return deleteObject(key);
    }

    /**
     * 检查用户是否在线
     *
     * @param userId 用户ID
     * @return true=在线；false=离线
     */
    public boolean isUserOnline(final Long userId) {
        String key = "online:users:" + userId;
        return hasKey(key);
    }

    /**
     * 添加用户到在线集合（原子操作）
     *
     * @param userId 用户ID
     * @return 成功添加的元素数量
     */
    public Long addUserToOnlineSet(final Long userId) {
        String key = "online:users:set";
        return addToSet(key, userId);
    }

    /**
     * 从在线集合移除用户（原子操作）
     *
     * @param userId 用户ID
     * @return 成功移除的元素数量
     */
    public Long removeUserFromOnlineSet(final Long userId) {
        String key = "online:users:set";
        return removeFromSet(key, userId);
    }

    /**
     * 获取所有在线用户
     *
     * @return 在线用户ID集合
     */
    public <T> Set<T> getAllOnlineUsers() {
        String key = "online:users:set";
        return getCacheSet(key);
    }

    /**
     * 获取在线用户数量
     *
     * @return 在线用户数
     */
    public Long getOnlineUserCount() {
        String key = "online:users:set";
        return getSetSize(key);
    }

    /**
     * 添加离线消息
     *
     * @param userId     用户ID
     * @param message    消息内容
     * @param expireDays 过期天数
     * @return List 的新长度
     */
    public <T> Long addOfflineMessage(final Long userId, final T message, final int expireDays) {
        String key = "offline:message:" + userId;
        Long size = rightPushToList(key, message);
        expire(key, expireDays, TimeUnit.DAYS);
        return size;
    }

    /**
     * 添加离线通知
     *
     * @param userId       用户ID
     * @param notification 通知内容
     * @param expireDays   过期天数
     * @return List 的新长度
     */
    public <T> Long addOfflineNotification(final Long userId, final T notification, final int expireDays) {
        String key = "offline:notification:" + userId;
        Long size = rightPushToList(key, notification);
        expire(key, expireDays, TimeUnit.DAYS);
        return size;
    }

    /**
     * 获取并清除离线消息
     *
     * @param userId 用户ID
     * @return 离线消息列表
     */
    public <T> List<T> getAndClearOfflineMessages(final Long userId) {
        String key = "offline:message:" + userId;
        List<T> messages = getCacheList(key);
        if (messages != null && !messages.isEmpty()) {
            deleteObject(key);
        }
        return messages;
    }

    /**
     * 获取并清除离线通知
     *
     * @param userId 用户ID
     * @return 离线通知列表
     */
    public <T> List<T> getAndClearOfflineNotifications(final Long userId) {
        String key = "offline:notification:" + userId;
        List<T> notifications = getCacheList(key);
        if (notifications != null && !notifications.isEmpty()) {
            deleteObject(key);
        }
        return notifications;
    }
}
