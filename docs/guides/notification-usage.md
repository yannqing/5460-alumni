# 企业级消息通知系统使用文档

## 📋 目录

- [系统概述](#系统概述)
- [核心特性](#核心特性)
- [系统架构](#系统架构)
- [快速开始](#快速开始)
- [使用示例](#使用示例)
- [最佳实践](#最佳实践)
- [常见问题](#常见问题)

---

## 系统概述

基于 **企业级标准** 重新设计和优化的消息通知系统，支持 P2P 消息、群聊消息、系统通知、组织通知和业务通知的统一管理和处理。

### 核心特性

✅ **统一消息模型** - 所有消息类型使用统一的数据模型 `UnifiedMessage`
✅ **策略模式** - 根据消息类别自动选择对应的生产者
✅ **责任链模式** - 消息消费采用责任链模式，灵活处理多个步骤
✅ **消息幂等性** - 基于 Redis 实现消息去重，防止重复消费
✅ **死信队列** - 失败消息自动发送到死信队列，支持人工介入
✅ **离线消息** - 自动检测用户在线状态，离线用户消息存储到 Redis
✅ **WebSocket 推送** - 在线用户实时接收消息推送
✅ **数据库持久化** - 所有消息和通知持久化存储
✅ **Redis 缓存** - 未读消息计数、最近消息等缓存优化

---

## 系统架构

### 架构图

```
┌─────────────────────────────────────────────────────────────────────┐
│                          业务层                                      │
│  (Controller / Service)                                              │
└────────────────────────┬────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   UnifiedMessageService (门面)                       │
│  ┌──────────┬──────────┬──────────┬──────────┬──────────┐          │
│  │  P2P     │  Group   │ System   │  Org     │ Business │          │
│  │ Producer │ Producer │ Producer │ Producer │ Producer │          │
│  └──────────┴──────────┴──────────┴──────────┴──────────┘          │
└────────────────────────┬────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────────┐
│                         Kafka Topics                                 │
│  • user.message.p2p                                                  │
│  • group.message.chat                                                │
│  • system.notification                                               │
│  • organization.notification                                         │
│  • business.notification                                             │
└────────────────────────┬────────────────────────────────────────────┘
                         │
                         ▼
┌─────────────────────────────────────────────────────────────────────┐
│                   UnifiedMessageConsumer                             │
│                   (责任链模式处理)                                    │
│  ┌──────────────────────────────────────────────────────────────┐  │
│  │  1. WebSocketPushHandler      → 推送给在线用户                │  │
│  │  2. DatabasePersistHandler    → 保存到数据库                  │  │
│  │  3. RedisCacheHandler         → 更新Redis缓存                 │  │
│  │  4. OfflineMessageHandler     → 离线消息存储                  │  │
│  └──────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────┘
```

### 核心组件

| 组件                         | 职责                              |
| ---------------------------- | --------------------------------- |
| `UnifiedMessage`             | 统一消息传输对象                  |
| `UnifiedMessageService`      | 消息服务门面，对外提供统一接口    |
| `MessageProducer` (策略)     | 消息生产者接口，支持多种实现      |
| `MessageHandler` (责任链)    | 消息处理器接口，支持链式处理      |
| `MessageIdempotentService`   | 消息幂等性服务，防止重复消费      |
| `DeadLetterQueueService`     | 死信队列服务，处理失败消息        |
| `UnifiedMessageConsumer`     | 统一消息消费者，消费 Kafka 消息   |

---

## 快速开始

### 1. 依赖注入

在你的业务类中注入 `UnifiedMessageService`：

```java
@Service
public class YourBusinessService {

    @Autowired
    private UnifiedMessageService messageService;

    // ... 你的业务逻辑
}
```

### 2. 发送消息

```java
// 发送 P2P 消息
messageService.sendP2PMessage(
    fromUserId,
    fromUsername,
    toUserId,
    "你好，这是一条测试消息"
);

// 发送群聊消息
messageService.sendGroupMessage(
    fromUserId,
    fromUsername,
    groupId,
    "大家好！"
);

// 发送系统通知
messageService.sendSystemNotification(
    userId,
    NotificationType.MEMBER_UPGRADE,
    "会员升级",
    "恭喜您的会员等级已提升至VIP"
);
```

---

## 使用示例

### 示例1：用户发送消息给用户（P2P）

```java
@Service
public class ChatService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 用户发送私聊消息
     */
    public boolean sendPrivateMessage(Long fromUserId, String fromUsername,
                                      Long toUserId, String content) {
        return messageService.sendP2PMessage(fromUserId, fromUsername, toUserId, content);
    }
}
```

**特点：**
- ✅ 自动推送给在线用户
- ✅ 离线用户消息存储到 Redis
- ✅ 保存到数据库
- ✅ 更新未读消息计数

---

### 示例2：用户发送消息到群聊

```java
@Service
public class GroupChatService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 发送群聊消息
     */
    public boolean sendGroupMessage(Long userId, String username,
                                    Long groupId, String content) {
        return messageService.sendGroupMessage(userId, username, groupId, content);
    }
}
```

**特点：**
- ✅ 自动推送给群内所有在线成员
- ✅ 离线成员消息存储
- ✅ 保存到数据库

---

### 示例3：系统发送通知

#### 3.1 发送给单个用户

```java
@Service
public class MemberService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 会员升级通知
     */
    public void notifyMemberUpgrade(Long userId) {
        messageService.sendSystemNotification(
            userId,
            NotificationType.MEMBER_UPGRADE,
            "会员升级",
            "恭喜您的会员等级已提升至VIP"
        );
    }

    /**
     * 优惠券到期提醒
     */
    public void notifyCouponExpiring(Long userId, String couponName) {
        messageService.sendSystemNotification(
            userId,
            NotificationType.COUPON_EXPIRING,
            "优惠券到期提醒",
            String.format("您的优惠券【%s】将在3天后过期，请尽快使用", couponName)
        );
    }
}
```

#### 3.2 广播给所有用户

```java
@Service
public class SystemAnnouncementService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 发布系统公告
     */
    public void publishSystemAnnouncement(String title, String content) {
        messageService.broadcastSystemNotification(
            NotificationType.SYSTEM_ANNOUNCEMENT,
            title,
            content
        );
    }
}
```

#### 3.3 批量发送给多个用户

```java
@Service
public class CouponService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 批量发放优惠券通知
     */
    public void notifyBatchCouponIssued(List<Long> userIds, String couponName) {
        messageService.batchSendSystemNotification(
            userIds,
            NotificationType.COUPON_ISSUED,
            "优惠券发放",
            String.format("您已获得优惠券【%s】", couponName)
        );
    }
}
```

---

### 示例4：组织通知

```java
@Service
public class OrganizationService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 组织发布活动通知
     */
    public void publishEventNotification(Long organizationId, String organizationName,
                                         List<Long> followerIds, Long eventId,
                                         String eventTitle, String eventSummary) {
        messageService.sendOrganizationNotification(
            organizationId,
            organizationName,
            followerIds,
            NotificationType.ORGANIZATION_EVENT,
            "活动发布 - " + eventTitle,
            eventSummary,
            eventId,
            "EVENT"
        );
    }

    /**
     * 组织发布公告
     */
    public void publishAnnouncement(Long organizationId, String organizationName,
                                    List<Long> followerIds, String title, String content) {
        messageService.sendOrganizationNotification(
            organizationId,
            organizationName,
            followerIds,
            NotificationType.ORGANIZATION_ANNOUNCEMENT,
            title,
            content,
            null,
            "ANNOUNCEMENT"
        );
    }
}
```

---

### 示例5：业务通知

#### 5.1 用户关注通知

```java
@Service
public class FollowService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 用户关注通知
     */
    public void notifyUserFollow(Long fromUserId, String fromUsername, Long toUserId) {
        messageService.sendFollowNotification(fromUserId, fromUsername, toUserId);
    }
}
```

#### 5.2 评论通知

```java
@Service
public class CommentService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 评论通知
     */
    public void notifyComment(Long fromUserId, String fromUsername,
                             Long toUserId, Long commentId, String content) {
        messageService.sendCommentNotification(
            fromUserId,
            fromUsername,
            toUserId,
            commentId,
            content
        );
    }
}
```

#### 5.3 点赞通知

```java
@Service
public class LikeService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 点赞通知
     */
    public void notifyLike(Long fromUserId, String fromUsername,
                          Long toUserId, Long targetId, String targetType) {
        messageService.sendLikeNotification(
            fromUserId,
            fromUsername,
            toUserId,
            targetId,
            targetType
        );
    }
}
```

#### 5.4 群主转移通知

```java
@Service
public class GroupManagementService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 群主转移通知
     */
    public void notifyGroupOwnerTransfer(Long groupId, Long oldOwnerId,
                                         Long newOwnerId, String newOwnerName) {
        messageService.sendGroupOwnerTransferNotification(
            groupId,
            oldOwnerId,
            newOwnerId,
            newOwnerName
        );
    }
}
```

---

### 示例6：高级用法 - 自定义消息

```java
@Service
public class CustomMessageService {

    @Autowired
    private UnifiedMessageService messageService;

    /**
     * 发送自定义消息
     */
    public void sendCustomMessage() {
        UnifiedMessage message = UnifiedMessage.builder()
            .category(MessageCategory.SYSTEM)
            .messageType("CUSTOM_TYPE")
            .fromId(0L)
            .fromType("SYSTEM")
            .fromName("系统")
            .toId(123456L)
            .toType("USER")
            .title("自定义标题")
            .content("自定义内容")
            .priority(MessagePriority.HIGH)
            .needPersist(true)
            .needPush(true)
            .needOfflineStore(true)
            .build();

        messageService.sendMessage(message);
    }
}
```

---

## 最佳实践

### 1. 消息类别选择

| 场景                  | 消息类别        | 说明                    |
| --------------------- | --------------- | ----------------------- |
| 用户私聊              | `P2P`           | 点对点消息              |
| 群组聊天              | `GROUP`         | 群聊消息                |
| 系统级通知            | `SYSTEM`        | 系统公告、会员提醒等    |
| 组织发布通知          | `ORGANIZATION`  | 组织活动、公告等        |
| 业务交互通知          | `BUSINESS`      | 关注、点赞、评论等      |

### 2. 消息优先级设置

```java
MessagePriority.LOW     // 点赞通知等
MessagePriority.NORMAL  // 普通消息、评论通知等
MessagePriority.HIGH    // 系统通知、群主转移等
MessagePriority.URGENT  // 账号安全、支付相关等
```

### 3. 性能优化建议

- ✅ **批量发送**：对于需要发送给多个用户的通知，使用 `batchSendMessages` 方法
- ✅ **异步发送**：默认使用异步发送，提升性能
- ✅ **控制推送频率**：点赞等低优先级通知可以设置 `needPush=false`，减少打扰

### 4. 监控和告警

- 监控 Kafka 消息积压
- 监控死信队列消息数量
- 监控消息处理失败率
- 设置告警阈值

---

## 常见问题

### Q1: 如何确保消息不重复消费？

**A**: 系统使用 `MessageIdempotentService` 基于 Redis 实现消息幂等性，每个消息在消费前都会检查是否已处理过。

### Q2: 离线消息如何处理？

**A**: 离线用户的消息会自动存储到 Redis，默认保留 7 天。用户上线时，系统会自动推送所有离线消息。

### Q3: 消息处理失败怎么办？

**A**: 失败的消息会自动发送到死信队列（`message.dlq` 或 `notification.dlq`），支持后续人工介入处理。

### Q4: 如何自定义消息处理逻辑？

**A**: 可以实现 `MessageHandler` 接口，并将其加入到责任链中。

### Q5: 支持哪些通知类型？

**A**: 系统预定义了 20+ 种通知类型，参见 `NotificationType` 枚举。也支持自定义通知类型。

---

## 联系我们

如有问题或建议，请联系开发团队：

- **邮箱**: dev@cmswe.alumni.com
- **文档**: [内部Wiki](http://wiki.cmswe.alumni.com)

---

**© 2025 CMSWE Alumni Platform. All Rights Reserved.**
