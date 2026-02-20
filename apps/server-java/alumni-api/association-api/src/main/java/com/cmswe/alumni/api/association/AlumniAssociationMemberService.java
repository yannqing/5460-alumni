package com.cmswe.alumni.api.association;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.entity.AlumniAssociationMember;
import com.cmswe.alumni.common.entity.LocalPlatformMember;

import java.util.List;

public interface AlumniAssociationMemberService extends IService<AlumniAssociationMember> {

    /**
     * 给校友会新增一个会长（创建的时候所用，目前是测试接口在用）
     *
     * @param wxId
     * @param alumniAssociationId
     * @return
     */
    boolean insertAlumniAssociationMember(Long wxId, Long alumniAssociationId);

    /**
     * 查询校友会下所有的成员列表
     *
     * @param alumniAssociationId 校友会 id
     * @return 返回列表
     */
    List<AlumniAssociationMember> getAlumniAssociationMemberByAlumniAssociationId(Long alumniAssociationId);
}
