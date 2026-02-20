package com.cmswe.alumni.service.user.service.message.handler;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.enums.MessageCategory;
import com.cmswe.alumni.common.model.UnifiedMessage;
import com.cmswe.alumni.kafka.handler.AbstractMessageHandler;
import com.cmswe.alumni.service.user.mapper.WxUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 校友状态更新处理器（责任链模式）
 *
 * <p>功能：当校友会加入申请审核通过时，更新用户的 isAlumni 字段为 1
 *
 * <p>触发条件：
 * <ul>
 *   <li>消息类别为 BUSINESS（业务通知）</li>
 *   <li>消息类型为 ALUMNI_APPLICATION_APPROVED（校友会申请审核通过）</li>
 *   <li>relatedType 为 ASSOCIATION（校友会相关）</li>
 * </ul>
 *
 * @author CMSWE
 * @since 2025-01-26
 */
@Slf4j
@Component
public class AlumniStatusUpdateHandler extends AbstractMessageHandler<UnifiedMessage> {

    private final WxUserMapper wxUserMapper;

    public AlumniStatusUpdateHandler(WxUserMapper wxUserMapper) {
        this.wxUserMapper = wxUserMapper;
    }

    @Override
    public String getHandlerName() {
        return "AlumniStatusUpdateHandler";
    }

    @Override
    public int getOrder() {
        // 在数据库持久化之后执行，但在 Redis 缓存更新之前
        return 25;
    }

    @Override
    protected boolean doHandle(UnifiedMessage message) {
        // 1. 检查是否为业务通知
        if (message.getCategory() != MessageCategory.BUSINESS) {
            log.debug("[AlumniStatusUpdateHandler] 非业务通知，跳过处理 - Category: {}", message.getCategory());
            return true;
        }

        // 2. 检查消息类型是否为校友会申请审核通过
        if (!"ALUMNI_APPLICATION_APPROVED".equals(message.getMessageType())) {
            log.debug("[AlumniStatusUpdateHandler] 非校友会申请审核通过消息，跳过处理 - MessageType: {}",
                    message.getMessageType());
            return true;
        }

        // 3. 检查关联类型是否为校友会
        if (!"ASSOCIATION".equals(message.getRelatedType())) {
            log.debug("[AlumniStatusUpdateHandler] 非校友会相关消息，跳过处理 - RelatedType: {}",
                    message.getRelatedType());
            return true;
        }

        // 4. 获取接收方用户ID（申请通过的用户）
        Long wxId = message.getToId();
        if (wxId == null || wxId == 0) {
            log.warn("[AlumniStatusUpdateHandler] 接收方用户ID为空，无法更新校友状态 - MessageId: {}",
                    message.getMessageId());
            return false;
        }

        try {
            // 5. 查询用户当前的 isAlumni 状态
            WxUser wxUser = wxUserMapper.selectById(wxId);

            if (wxUser == null) {
                log.warn("[AlumniStatusUpdateHandler] 用户不存在，无法更新校友状态 - WxId: {}", wxId);
                return false;
            }

            // 6. 检查当前状态，如果已经是校友则无需更新
            if (wxUser.getIsAlumni() != null && wxUser.getIsAlumni() == 1) {
                log.debug("[AlumniStatusUpdateHandler] 用户已是校友，无需更新 - WxId: {}", wxId);
                return true;
            }

            // 7. 更新用户的 isAlumni 字段为 1
            LambdaUpdateWrapper<WxUser> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(WxUser::getWxId, wxId)
                    .set(WxUser::getIsAlumni, 1);

            int updateCount = wxUserMapper.update(null, updateWrapper);

            if (updateCount > 0) {
                log.info("[AlumniStatusUpdateHandler] 校友状态更新成功 - WxId: {}, isAlumni: 0 -> 1", wxId);
                return true;
            } else {
                log.warn("[AlumniStatusUpdateHandler] 校友状态更新失败 - WxId: {}, UpdateCount: {}",
                        wxId, updateCount);
                return false;
            }

        } catch (Exception e) {
            log.error("[AlumniStatusUpdateHandler] 更新校友状态异常 - WxId: {}, Error: {}",
                    wxId, e.getMessage(), e);
            return false;
        }
    }
}
