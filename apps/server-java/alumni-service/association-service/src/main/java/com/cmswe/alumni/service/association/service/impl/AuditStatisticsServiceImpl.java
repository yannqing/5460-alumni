package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.api.association.AuditStatisticsService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.common.entity.AlumniAssociationApplication;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApplication;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApply;
import com.cmswe.alumni.common.entity.AlumniHeadquarters;
import com.cmswe.alumni.common.vo.AuditStatisticsVo;
import com.cmswe.alumni.common.vo.ManagedOrganizationListVo;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationApplicationMapper;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationJoinApplicationMapper;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationJoinApplyMapper;
import com.cmswe.alumni.service.association.mapper.AlumniHeadquartersMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 审核待办统计服务实现类
 */
@Slf4j
@Service
public class AuditStatisticsServiceImpl implements AuditStatisticsService {

    @Resource
    private AlumniHeadquartersMapper alumniHeadquartersMapper;

    @Resource
    private AlumniAssociationApplicationMapper alumniAssociationApplicationMapper;

    @Resource
    private AlumniAssociationJoinApplyMapper alumniAssociationJoinApplyMapper;

    @Resource
    private AlumniAssociationJoinApplicationMapper alumniAssociationJoinApplicationMapper;

    @Resource
    private UserService userService;

    @Override
    public AuditStatisticsVo getAuditTodoStatistics(Long wxId) {
        Map<String, Integer> counts = new HashMap<>();

        // 1. 获取用户管理的组织
        List<ManagedOrganizationListVo> managedOrgs = userService.getManagedOrganizations(wxId, null);
        
        // 提取管理的校友会ID和校促会ID
        Set<Long> managedAlumniIds = managedOrgs.stream()
                .filter(org -> org.getType() == 0)
                .map(ManagedOrganizationListVo::getId)
                .collect(Collectors.toSet());
        
        Set<Long> managedPlatformIds = managedOrgs.stream()
                .filter(org -> org.getType() == 1)
                .map(ManagedOrganizationListVo::getId)
                .collect(Collectors.toSet());

        // 2. 统计各个模块的待办数量
        
        // 2.1 审核校友总会 (SYSTEM_GENERAL_ALUMNI_ASSOCIATION_AUDIT)
        // 通常只有系统管理员能看到，这里直接查询所有待审核
        LambdaQueryWrapper<AlumniHeadquarters> headquartersWrapper = new LambdaQueryWrapper<>();
        headquartersWrapper.eq(AlumniHeadquarters::getApprovalStatus, 0)
                .ne(AlumniHeadquarters::getActiveStatus, 0);
        counts.put("SYSTEM_GENERAL_ALUMNI_ASSOCIATION_AUDIT", Math.toIntExact(alumniHeadquartersMapper.selectCount(headquartersWrapper)));

        // 2.2 校友会审核 - 系统级别 (SYSTEM_ALUMNI_ASSOCIATION_APPLICATION)
        LambdaQueryWrapper<AlumniAssociationApplication> sysAppWrapper = new LambdaQueryWrapper<>();
        sysAppWrapper.eq(AlumniAssociationApplication::getApplicationStatus, 0);
        counts.put("SYSTEM_ALUMNI_ASSOCIATION_APPLICATION", Math.toIntExact(alumniAssociationApplicationMapper.selectCount(sysAppWrapper)));

        // 2.3 校友会审核 - 城市/校促会级别 (LOCAL_PLATFORM_ALUMNI_ASSOCIATION_APPLICATION)
        // 用户要求城市下面的校友会审核不参与统计
        counts.put("LOCAL_PLATFORM_ALUMNI_ASSOCIATION_APPLICATION", 0);

        // 2.4 校友会认证 (SYSTEM_ALUMNI_ASSOCIATION_CERTIFICATION) - 对应校友会申请加入校促会
        if (!managedPlatformIds.isEmpty()) {
            LambdaQueryWrapper<AlumniAssociationJoinApply> certWrapper = new LambdaQueryWrapper<>();
            certWrapper.eq(AlumniAssociationJoinApply::getStatus, 0)
                    .in(AlumniAssociationJoinApply::getPlatformId, managedPlatformIds);
            counts.put("SYSTEM_ALUMNI_ASSOCIATION_CERTIFICATION", Math.toIntExact(alumniAssociationJoinApplyMapper.selectCount(certWrapper)));
        } else {
            counts.put("SYSTEM_ALUMNI_ASSOCIATION_CERTIFICATION", 0);
        }

        // 2.5 加入审核 (ALUMNI_ASSOCIATION_JOIN_REVIEW) - 用户申请加入校友会
        if (!managedAlumniIds.isEmpty()) {
            LambdaQueryWrapper<AlumniAssociationJoinApplication> joinWrapper = new LambdaQueryWrapper<>();
            joinWrapper.eq(AlumniAssociationJoinApplication::getApplicationStatus, 0)
                    .eq(AlumniAssociationJoinApplication::getApplicantType, 1) // 1-用户
                    .in(AlumniAssociationJoinApplication::getAlumniAssociationId, managedAlumniIds);
            counts.put("ALUMNI_ASSOCIATION_JOIN_REVIEW", Math.toIntExact(alumniAssociationJoinApplicationMapper.selectCount(joinWrapper)));
        } else {
            counts.put("ALUMNI_ASSOCIATION_JOIN_REVIEW", 0);
        }

        return AuditStatisticsVo.builder()
                .todoCounts(counts)
                .build();
    }
}
