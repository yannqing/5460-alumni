package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationMemberService;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.common.entity.AlumniAssociationMember;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationMapper;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationMemberMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class AlumniAssociationMemberImpl extends ServiceImpl<AlumniAssociationMemberMapper, AlumniAssociationMember> implements AlumniAssociationMemberService {

    @Resource
    private UserService userService;

    @Resource
    private AlumniAssociationMapper alumniAssociationMapper;

    // TODO 代码重复，注意优化
    @Override
    public boolean insertAlumniAssociationMember(Long wxId, Long alumniAssociationId) {
        Optional.ofNullable(wxId)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        Optional.ofNullable(alumniAssociationId)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        Optional.ofNullable(userService.getById(wxId))
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        Optional.ofNullable(alumniAssociationMapper.selectById(alumniAssociationId))
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        AlumniAssociationMember alumniAssociationMember = new AlumniAssociationMember();
        alumniAssociationMember.setWxId(wxId);
        alumniAssociationMember.setAlumniAssociationId(alumniAssociationId);
        alumniAssociationMember.setRoleOrId(1L);      // TODO 这里是会长角色的枚举，后面需要调整
        alumniAssociationMember.setJoinTime(LocalDateTime.now());

        boolean saveResult = this.save(alumniAssociationMember);

        log.info("给校友会 id={} 新增一个会长 id={} ", wxId, alumniAssociationId);

        return saveResult;
    }

    @Override
    public List<AlumniAssociationMember> getAlumniAssociationMemberByAlumniAssociationId(Long alumniAssociationId) {
        Optional.ofNullable(alumniAssociationId)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        return this.getBaseMapper().selectList(
                new LambdaQueryWrapper<AlumniAssociationMember>()
                        .eq(AlumniAssociationMember::getAlumniAssociationId, alumniAssociationId)
        );
    }
}
