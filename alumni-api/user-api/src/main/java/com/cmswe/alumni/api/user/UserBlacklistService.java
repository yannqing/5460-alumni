package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.BlacklistDto;
import com.cmswe.alumni.common.entity.UserBlacklist;

/**
 * 用户黑名单服务接口
 */
public interface UserBlacklistService extends IService<UserBlacklist> {

    /**
     * 拉黑用户
     * @param wxId 用户 id
     * @param blacklistDto 拉黑信息
     * @return 是否成功
     */
    boolean blockUser(Long wxId, BlacklistDto blacklistDto);

    /**
     * 取消拉黑
     * @param wxId 用户 id
     * @param blockedWxId 被拉黑用户 ID
     * @return 是否成功
     */
    boolean unblockUser(Long wxId, Long blockedWxId);

    /**
     * 检查是否已拉黑
     * @param wxId 用户 id
     * @param blockedWxId 被拉黑用户 ID
     * @return 是否已拉黑
     */
    boolean isBlocked(Long wxId, Long blockedWxId);
}
