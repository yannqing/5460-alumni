package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.LocalPlatformMemberService;
import com.cmswe.alumni.api.association.LocalPlatformService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.common.entity.LocalPlatformMember;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.service.association.mapper.LocalPlatformMapper;
import com.cmswe.alumni.service.association.mapper.LocalPlatformMemberMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class LocalPlatformMemberImpl extends ServiceImpl<LocalPlatformMemberMapper, LocalPlatformMember> implements LocalPlatformMemberService {

    @Resource
    private UserService userService;

    @Resource
    private LocalPlatformMapper localPlatformMapper;

    @Override
    public boolean insertLocalPlatformMember(Long wxId, Long platformId) {
        Optional.ofNullable(wxId)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        Optional.ofNullable(platformId)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        Optional.ofNullable(userService.getById(wxId))
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        Optional.ofNullable(localPlatformMapper.selectById(platformId))
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        LocalPlatformMember localPlatformMember = new LocalPlatformMember();
        localPlatformMember.setWxId(wxId);
        localPlatformMember.setLocalPlatformId(platformId);
        localPlatformMember.setRoleOrId(1L);      // TODO 这里需要修改，这里应该是会长角色的枚举
        localPlatformMember.setJoinTime(LocalDateTime.now());


        boolean saveResult = this.save(localPlatformMember);

        log.info("给校处会 id={} 新增一个会长 id={}", platformId, wxId);

        return saveResult;
    }
}
