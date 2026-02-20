package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.api.user.ChatConversationService;
import com.cmswe.alumni.common.entity.ChatConversation;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.enums.SourceType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.ConversationItemVo;
import com.cmswe.alumni.service.user.mapper.ChatConversationMapper;
import com.cmswe.alumni.service.user.mapper.WxUserInfoMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天会话服务实现类
 *
 * @author CMSWE
 * @since 2025-12-10
 */
@Slf4j
@Service
public class ChatConversationServiceImpl implements ChatConversationService {

    @Resource
    private ChatConversationMapper chatConversationMapper;

    @Resource
    private WxUserInfoMapper wxUserInfoMapper;

    @Override
    public List<ConversationItemVo> getConversationList(Long userId) {
        try {
            // 直接从会话表查询，性能优秀
            List<ChatConversation> conversations = chatConversationMapper.selectUserConversations(userId, 50);

            if (conversations == null || conversations.isEmpty()) {
                return new ArrayList<>();
            }

            // 构建会话列表VO
            List<ConversationItemVo> result = new ArrayList<>();
            for (ChatConversation conversation : conversations) {
                ConversationItemVo vo = buildConversationVo(conversation);
                if (vo != null) {
                    result.add(vo);
                }
            }

            return result;

        } catch (Exception e) {
            log.error("[ChatConversationService] 获取会话列表失败 - UserId: {}, Error: {}",
                    userId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "获取会话列表失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatConversation getOrCreateConversation(Long userId, Long peerId, SourceType conversationType) {
        try {
            // 先查询是否存在
            ChatConversation conversation = chatConversationMapper.selectOrCreateConversation(
                    userId, peerId, conversationType);

            if (conversation != null) {
                return conversation;
            }

            // 不存在则创建新会话
            conversation = new ChatConversation();
            conversation.setWxId(userId);
            conversation.setPeerId(peerId);
            conversation.setConversationType(conversationType);
            conversation.setUnreadCount(0);
            conversation.setIsPinned(false);
            conversation.setIsMuted(false);
            conversation.setIsDeleted(false);
            conversation.setIsHidden(false);
            conversation.setMentionCount(0);

            chatConversationMapper.insert(conversation);

            log.info("[ChatConversationService] 创建新会话 - UserId: {}, PeerId: {}, Type: {}",
                    userId, peerId, conversationType);

            return conversation;

        } catch (Exception e) {
            log.error("[ChatConversationService] 获取或创建会话失败 - UserId: {}, PeerId: {}, Error: {}",
                    userId, peerId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "会话创建失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateLastMessage(Long userId, Long peerId, SourceType conversationType,
                                 Long lastMessageId, String lastMessageContent,
                                 LocalDateTime lastMessageTime, Long lastMessageFromId) {
        try {
            // 确保会话存在
            getOrCreateConversation(userId, peerId, conversationType);

            // 更新最后消息
            int rows = chatConversationMapper.updateLastMessage(
                    userId, peerId, conversationType,
                    lastMessageId, lastMessageContent, lastMessageTime, lastMessageFromId);

            if (rows > 0) {
                log.debug("[ChatConversationService] 更新会话最后消息 - UserId: {}, PeerId: {}, MessageId: {}",
                        userId, peerId, lastMessageId);
            }

        } catch (Exception e) {
            log.error("[ChatConversationService] 更新最后消息失败 - UserId: {}, PeerId: {}, Error: {}",
                    userId, peerId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "更新会话失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementUnreadCount(Long userId, Long peerId, SourceType conversationType, Integer count) {
        try {
            // 确保会话存在
            getOrCreateConversation(userId, peerId, conversationType);

            // 增加未读数
            int rows = chatConversationMapper.incrementUnreadCount(userId, peerId, conversationType, count);

            if (rows > 0) {
                log.debug("[ChatConversationService] 增加未读数 - UserId: {}, PeerId: {}, Count: {}",
                        userId, peerId, count);
            }

        } catch (Exception e) {
            log.error("[ChatConversationService] 增加未读数失败 - UserId: {}, PeerId: {}, Error: {}",
                    userId, peerId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "更新未读数失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearUnreadCount(Long userId, Long peerId, SourceType conversationType) {
        try {
            int rows = chatConversationMapper.clearUnreadCount(userId, peerId, conversationType);

            if (rows > 0) {
                log.debug("[ChatConversationService] 清空未读数 - UserId: {}, PeerId: {}",
                        userId, peerId);
            }

        } catch (Exception e) {
            log.error("[ChatConversationService] 清空未读数失败 - UserId: {}, PeerId: {}, Error: {}",
                    userId, peerId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "清空未读数失败");
        }
    }

    @Override
    public void updatePinnedStatus(Long conversationId, Boolean isPinned) {
        try {
            int rows = chatConversationMapper.updatePinnedStatus(conversationId, isPinned);

            if (rows > 0) {
                log.info("[ChatConversationService] 更新置顶状态 - ConversationId: {}, IsPinned: {}",
                        conversationId, isPinned);
            }

        } catch (Exception e) {
            log.error("[ChatConversationService] 更新置顶状态失败 - ConversationId: {}, Error: {}",
                    conversationId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "更新置顶状态失败");
        }
    }

    @Override
    public void updateMutedStatus(Long conversationId, Boolean isMuted) {
        try {
            int rows = chatConversationMapper.updateMutedStatus(conversationId, isMuted);

            if (rows > 0) {
                log.info("[ChatConversationService] 更新免打扰状态 - ConversationId: {}, IsMuted: {}",
                        conversationId, isMuted);
            }

        } catch (Exception e) {
            log.error("[ChatConversationService] 更新免打扰状态失败 - ConversationId: {}, Error: {}",
                    conversationId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "更新免打扰状态失败");
        }
    }

    @Override
    public void deleteConversation(Long conversationId) {
        try {
            int rows = chatConversationMapper.deleteConversation(conversationId);

            if (rows > 0) {
                log.info("[ChatConversationService] 删除会话 - ConversationId: {}", conversationId);
            }

        } catch (Exception e) {
            log.error("[ChatConversationService] 删除会话失败 - ConversationId: {}, Error: {}",
                    conversationId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "删除会话失败");
        }
    }

    @Override
    public void saveDraft(Long conversationId, String draftContent) {
        try {
            int rows = chatConversationMapper.saveDraft(conversationId, draftContent);

            if (rows > 0) {
                log.debug("[ChatConversationService] 保存草稿 - ConversationId: {}", conversationId);
            }

        } catch (Exception e) {
            log.error("[ChatConversationService] 保存草稿失败 - ConversationId: {}, Error: {}",
                    conversationId, e.getMessage(), e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "保存草稿失败");
        }
    }

    @Override
    public Integer getTotalUnreadCount(Long userId) {
        try {
            Integer count = chatConversationMapper.countTotalUnread(userId);
            return count != null ? count : 0;

        } catch (Exception e) {
            log.error("[ChatConversationService] 获取总未读数失败 - UserId: {}, Error: {}",
                    userId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * 构建会话VO
     */
    private ConversationItemVo buildConversationVo(ChatConversation conversation) {
        try {
            ConversationItemVo vo = new ConversationItemVo();
            vo.setConversationId(String.valueOf(conversation.getConversationId()));
            vo.setPeerId(String.valueOf(conversation.getPeerId()));
            vo.setConversationType(conversation.getConversationType());
            vo.setLastMessageContent(conversation.getLastMessageContent());
            vo.setLastMessageTime(conversation.getLastMessageTime());
            vo.setUnreadCount(conversation.getUnreadCount());
            vo.setIsPinned(conversation.getIsPinned());
            vo.setIsMuted(conversation.getIsMuted());
            vo.setDraftContent(conversation.getDraftContent());

            // 查询对方用户信息
            WxUserInfo peerUserInfo = wxUserInfoMapper.selectOne(
                    new LambdaQueryWrapper<WxUserInfo>()
                            .eq(WxUserInfo::getWxId, conversation.getPeerId())
            );

            if (peerUserInfo != null) {
                vo.setPeerNickname(peerUserInfo.getNickname());
                vo.setPeerAvatar(peerUserInfo.getAvatarUrl());
            }

            return vo;

        } catch (Exception e) {
            log.error("[ChatConversationService] 构建会话VO失败 - ConversationId: {}, Error: {}",
                    conversation.getConversationId(), e.getMessage(), e);
            return null;
        }
    }
}
