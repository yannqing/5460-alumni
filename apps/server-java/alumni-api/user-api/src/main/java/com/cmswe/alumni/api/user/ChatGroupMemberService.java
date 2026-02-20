package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.ChatMemberDTO;
import com.cmswe.alumni.common.entity.ChatGroupMember;
import com.cmswe.alumni.common.model.ChatMemberParam;

import java.util.List;


public interface ChatGroupMemberService extends IService<ChatGroupMember> {
    /**
     * 根据聊天群号分组查询群成员列表
     * @param chatMemberParam
     * @return
     */
//    PageResult memberList(ChatMemberParam chatMemberParam);

    /**
     * 根据用户Id建立聊天群-用户关系
     * @param chatMemberDTO
     * @return
     */
    boolean addMemberToGroupByGroupId(ChatMemberDTO chatMemberDTO);

    /**
     * 根据用户Ids建立聊天群-用户关系
     * @param chatMemberDTO
     * @return
     */
    boolean addMemberListToGroupByGroupId(ChatMemberDTO chatMemberDTO);

    /**
     * 根据群组Id解除聊天群-用户关系
     * @param groupId
     * @return
     */
    boolean deleteMemberByChatGroupId(String groupId);

    /**
     * 根据GroupId查询聊天群对应的成员关系列表
     * @param groupId
     * @return
     */
    List<ChatGroupMember> getGroupMemberByGroupId(String groupId);

    /**
     * 根据GroupId查询userId是否存在于群组内
     * @param groupId
     * @param userId
     * @return
     */
    boolean isMemberExists(String groupId, String userId);
}
