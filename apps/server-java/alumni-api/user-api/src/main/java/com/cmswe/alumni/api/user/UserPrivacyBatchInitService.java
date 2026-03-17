package com.cmswe.alumni.api.user;

import com.cmswe.alumni.common.vo.BatchInitResultVo;

/**
 * 用户隐私设置批量初始化服务接口
 *
 * @author CMSWE
 * @since 2026-03-17
 */
public interface UserPrivacyBatchInitService {

    /**
     * 批量初始化所有用户的隐私设置
     * <p>
     * 查询所有未初始化隐私设置的用户，并为他们初始化隐私设置
     *
     * @return 批量初始化结果
     */
    BatchInitResultVo batchInitAllUsers();
}
