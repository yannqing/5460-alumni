package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.LocalPlatformMember;

public interface LocalPlatformMemberService extends IService<LocalPlatformMember> {

    /**
     * 给校处会新增一个会长（创建的时候所用）
     *
     * @param wxId
     * @param platformId
     * @return
     */
    boolean insertLocalPlatformMember(Long wxId, Long platformId);
}
