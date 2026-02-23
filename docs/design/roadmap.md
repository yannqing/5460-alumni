# 后续扩展


## 扩展 AI 对话

目前是，只支持一个用户对应一个会话，不能多，而与 AI 聊天是要支持多个会话的，所以需要修改

实现方案：
在现有设计基础上，添加 会话组ID 概念，不破坏现有逻辑。

数据库优化（最小改动）

```sql
-- 给 chat_conversation 表添加一个字段
ALTER TABLE `chat_conversation`
ADD COLUMN `session_id` VARCHAR(64) DEFAULT NULL COMMENT
'会话组ID（用于区分同一对方的多个会话，如AI对话的不同主题）',
ADD INDEX `idx_user_peer_session` (`wx_id`, `peer_id`, `session_id`, `conversation_type`) COMMENT
'用户-对方-会话组查询';

-- 删除旧的唯一索引（因为现在允许同一用户和对方有多个会话）
ALTER TABLE `chat_conversation`
DROP INDEX `uk_user_peer_type`;

-- 添加新的唯一索引（包含 session_id）
ALTER TABLE `chat_conversation`
ADD UNIQUE INDEX `uk_user_peer_type_session` (`wx_id`, `peer_id`, `conversation_type`, `session_id`) COMMENT
'用户会话唯一索引（支持多会话）';
```

使用场景举例

-- 场景1：普通聊天（兼容现有逻辑，session_id 为 NULL）
wx_id=1, peer_id=2, conversation_type='USER', session_id=NULL  -- 默认会话

-- 场景2：AI 对话 - 不同主题的会话
wx_id=1, peer_id=AI_BOT_ID, conversation_type='USER', session_id='topic_travel'      -- 旅游主题
wx_id=1, peer_id=AI_BOT_ID, conversation_type='USER', session_id='topic_coding'      -- 编程主题
wx_id=1, peer_id=AI_BOT_ID, conversation_type='USER', session_id='topic_health'      -- 健康主题

-- 场景3：客服会话 - 不同工单
wx_id=1, peer_id=CUSTOMER_SERVICE_ID, conversation_type='USER', session_id='order_12345'  -- 订单咨询
wx_id=1, peer_id=CUSTOMER_SERVICE_ID, conversation_type='USER', session_id='refund_67890' -- 退款咨询

