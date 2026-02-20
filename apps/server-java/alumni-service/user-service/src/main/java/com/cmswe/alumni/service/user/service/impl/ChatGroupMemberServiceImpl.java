package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.ChatGroupMemberService;
import com.cmswe.alumni.common.dto.ChatMemberDTO;
import com.cmswe.alumni.common.entity.ChatGroupMember;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.service.user.mapper.ChatGroupMemberMapper;
import com.cmswe.alumni.service.user.mapper.WxUserInfoMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class ChatGroupMemberServiceImpl extends ServiceImpl<ChatGroupMemberMapper, ChatGroupMember>
    implements ChatGroupMemberService {

    @Resource
    private ChatGroupMemberMapper chatGroupMemberMapper;

    @Resource
    private WxUserInfoMapper  wxUserInfoMapper;
//    @Override
//    public PageResult memberList(ChatMemberParam chatMemberParam) {
//        Page<ChatGroupMember> page = new Page<>(chatMemberParam.getPageNo(),chatMemberParam.getPageSize());
//        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<ChatGroupMember>().eq(ChatGroupMember::getChatGroupId, chatMemberParam.getChatGroupId());
//
//        Page<ChatGroupMember> chatGroupMemberPage = chatGroupMemberMapper.selectPage(page, queryWrapper);
//        PageResult pageResult = new PageResult();
//        //todo 处理返回的ChatGroupMember 对象
//        pageResult.setTotal(chatGroupMemberPage.getTotal());
//        pageResult.setRecords(chatGroupMemberPage.getRecords());
//        return pageResult;
//    }
    @Override
    public boolean addMemberToGroupByGroupId(ChatMemberDTO chatMemberDTO) {
        WxUserInfo wxUserInfo = wxUserInfoMapper.selectById(chatMemberDTO.getUserId());
        ChatGroupMember chatGroupMember = new ChatGroupMember();
        chatGroupMember.setUserId(Long.valueOf(chatMemberDTO.getUserId()));
        chatGroupMember.setChatGroupId(Long.valueOf(chatMemberDTO.getChatGroupId()));
        chatGroupMember.setGroupName(wxUserInfo.getNickname());
        int result = chatGroupMemberMapper.insert(chatGroupMember);
        return result > 0;
    }

    @Override
    @Transactional
    public boolean addMemberListToGroupByGroupId(ChatMemberDTO chatMemberDTO) {
        String chatGroupId = chatMemberDTO.getChatGroupId();
        List<String> userIds = chatMemberDTO.getUserIds();
        if (userIds.isEmpty()) return false;
        List<String> newUserIds = userIds.stream()
                .filter(userId -> {
                    boolean exists = chatGroupMemberMapper.exists(new LambdaQueryWrapper<ChatGroupMember>()
                            .eq(ChatGroupMember::getUserId, userId)
                            .eq(ChatGroupMember::getChatGroupId, chatGroupId));
                    return !exists;
                })
                .toList();
        if (newUserIds.isEmpty()) return false;
        List<ChatGroupMember> chatGroupMemberList = queryUserInfo(newUserIds).stream().map(user -> {
            ChatGroupMember chatGroupMember = new ChatGroupMember();
            chatGroupMember.setChatGroupId(Long.valueOf(chatGroupId));
            chatGroupMember.setGroupName(user.getNickname());
            chatGroupMember.setUserId(user.getWxId());
            return chatGroupMember;
        }).toList();
        return super.saveBatch(chatGroupMemberList);
    }

    @Override
    public boolean deleteMemberByChatGroupId(String groupId) {
        int result = chatGroupMemberMapper.delete(new LambdaQueryWrapper<ChatGroupMember>().eq(ChatGroupMember::getChatGroupId, groupId));
        return result>0;
    }

    @Override
    public List<ChatGroupMember> getGroupMemberByGroupId(String groupId) {
        return chatGroupMemberMapper.selectList(new LambdaQueryWrapper<ChatGroupMember>().eq(ChatGroupMember::getChatGroupId, groupId));
    }

    @Override
    public boolean isMemberExists(String groupId, String userId) {
        LambdaQueryWrapper<ChatGroupMember> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ChatGroupMember::getUserId, userId)
                .eq(ChatGroupMember::getChatGroupId, groupId);
        return this.count(queryWrapper) > 0;
    }

    private List<WxUserInfo> queryUserInfo(List<String> userIds){
        return wxUserInfoMapper.selectBatchIds(userIds);
    }


}




