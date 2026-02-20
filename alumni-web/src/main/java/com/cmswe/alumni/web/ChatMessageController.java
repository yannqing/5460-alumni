package com.cmswe.alumni.web;

import com.cmswe.alumni.api.user.ChatMessageService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.QueryChatHistoryDto;
import com.cmswe.alumni.common.dto.QueryNotificationListDto;
import com.cmswe.alumni.common.dto.SendMessageDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.ChatMessageVo;
import com.cmswe.alumni.common.vo.ConversationItemVo;
import com.cmswe.alumni.common.vo.NotificationVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天消息控制器
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Tag(name = "聊天消息服务")
@RestController
@RequestMapping("/chat")
public class ChatMessageController {

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private com.cmswe.alumni.api.user.ChatConversationService chatConversationService;

    @Resource
    private com.cmswe.alumni.service.user.service.NotificationService notificationService;

    @PostMapping("/send")
    @Operation(summary = "发送消息")
    public BaseResponse<Long> sendMessage(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody SendMessageDto sendMessageDto) {

        Long messageId = chatMessageService.sendMessage(
                securityUser.getWxUser().getWxId(),
                sendMessageDto
        );

        return ResultUtils.success(Code.SUCCESS, messageId, "发送成功");
    }

    @PostMapping("/history")
    @Operation(summary = "获取聊天历史记录")
    public BaseResponse<PageVo<ChatMessageVo>> getChatHistory(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody QueryChatHistoryDto queryDto) {

        PageVo<ChatMessageVo> pageVo = chatMessageService.getChatHistory(
                securityUser.getWxUser().getWxId(),
                queryDto
        );

        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    @GetMapping("/conversations")
    @Operation(summary = "获取会话列表")
    public BaseResponse<List<ConversationItemVo>> getConversationList(
            @AuthenticationPrincipal SecurityUser securityUser) {

        List<ConversationItemVo> conversations = chatMessageService.getConversationList(
                securityUser.getWxUser().getWxId()
        );

        return ResultUtils.success(Code.SUCCESS, conversations, "查询成功");
    }

    @PutMapping("/read/{otherWxId}")
    @Operation(summary = "标记消息为已读")
    public BaseResponse<Integer> markAsRead(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long otherWxId) {

        Integer count = chatMessageService.markMessagesAsRead(
                securityUser.getWxUser().getWxId(),
                otherWxId
        );

        return ResultUtils.success(Code.SUCCESS, count, "标记成功");
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息总数")
    public BaseResponse<Integer> getUnreadCount(
            @AuthenticationPrincipal SecurityUser securityUser) {

        Integer count = chatMessageService.getUnreadCount(
                securityUser.getWxUser().getWxId()
        );

        return ResultUtils.success(Code.SUCCESS, count, "查询成功");
    }

    @DeleteMapping("/recall/{messageId}")
    @Operation(summary = "撤回消息")
    public BaseResponse<?> recallMessage(
            @AuthenticationPrincipal SecurityUser securityUser,
            @PathVariable Long messageId) {

        boolean success = chatMessageService.recallMessage(
                securityUser.getWxUser().getWxId(),
                messageId
        );

        if (success) {
            return ResultUtils.success(Code.SUCCESS, null, "撤回成功");
        }
        return ResultUtils.failure(Code.FAILURE, null, "撤回失败");
    }

    @PutMapping("/conversation/{conversationId}/pin")
    @Operation(summary = "置顶/取消置顶会话")
    public BaseResponse<?> updatePinnedStatus(
            @PathVariable Long conversationId,
            @RequestParam Boolean isPinned) {

        chatConversationService.updatePinnedStatus(conversationId, isPinned);
        return ResultUtils.success(Code.SUCCESS, null, isPinned ? "置顶成功" : "取消置顶成功");
    }

    @PutMapping("/conversation/{conversationId}/mute")
    @Operation(summary = "免打扰/取消免打扰")
    public BaseResponse<?> updateMutedStatus(
            @PathVariable Long conversationId,
            @RequestParam Boolean isMuted) {

        chatConversationService.updateMutedStatus(conversationId, isMuted);
        return ResultUtils.success(Code.SUCCESS, null, isMuted ? "免打扰已开启" : "免打扰已关闭");
    }

    @DeleteMapping("/conversation/{conversationId}")
    @Operation(summary = "删除会话")
    public BaseResponse<?> deleteConversation(
            @PathVariable Long conversationId) {

        chatConversationService.deleteConversation(conversationId);
        return ResultUtils.success(Code.SUCCESS, null, "删除成功");
    }

    @PutMapping("/conversation/{conversationId}/draft")
    @Operation(summary = "保存草稿")
    public BaseResponse<?> saveDraft(
            @PathVariable Long conversationId,
            @RequestParam String draftContent) {

        chatConversationService.saveDraft(conversationId, draftContent);
        return ResultUtils.success(Code.SUCCESS, null, "草稿已保存");
    }

    @PostMapping("/notifications")
    @Operation(summary = "获取通知列表")
    public BaseResponse<PageVo<NotificationVo>> getNotificationList(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody QueryNotificationListDto queryDto) {

        PageVo<NotificationVo> pageVo = notificationService.getNotificationListPage(
                securityUser.getWxUser().getWxId(),
                queryDto
        );

        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    @GetMapping("/unread/total")
    @Operation(summary = "获取所有未读消息总数（包括聊天消息和通知消息）")
    public BaseResponse<Integer> getTotalUnreadCount(
            @AuthenticationPrincipal SecurityUser securityUser) {

        Long wxId = securityUser.getWxUser().getWxId();

        // 1. 获取聊天消息未读数
        Integer chatUnreadCount = chatMessageService.getUnreadCount(wxId);

        // 2. 获取通知消息未读数
        int notificationUnreadCount = notificationService.getUnreadCount(wxId);

        // 3. 计算总未读数
        Integer totalUnreadCount = chatUnreadCount + notificationUnreadCount;

        return ResultUtils.success(Code.SUCCESS, totalUnreadCount, "查询成功");
    }

    @PutMapping("/notifications/read")
    @Operation(summary = "标记通知为已读")
    public BaseResponse<Integer> markNotificationAsRead(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(required = false) Long notificationId) {

        Long wxId = securityUser.getWxUser().getWxId();

        // 如果notificationId为null，则标记所有通知为已读
        if (notificationId == null) {
            int count = notificationService.markAllAsRead(wxId);
            return ResultUtils.success(Code.SUCCESS, count, "全部标记已读成功");
        } else {
            // 标记单个通知为已读
            boolean success = notificationService.markAsRead(notificationId, wxId);
            if (success) {
                return ResultUtils.success(Code.SUCCESS, 1, "标记已读成功");
            } else {
                return ResultUtils.failure(Code.FAILURE, 0, "标记已读失败");
            }
        }
    }
}
