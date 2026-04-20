package com.cmswe.alumni.service.association.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cmswe.alumni.api.association.AuditStatisticsService;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.api.search.ShopService;
import com.cmswe.alumni.api.system.MerchantService;
import com.cmswe.alumni.common.entity.AlumniAssociationApplication;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApplication;
import com.cmswe.alumni.common.entity.AlumniAssociationJoinApply;
import com.cmswe.alumni.common.entity.AlumniHeadquarters;
import com.cmswe.alumni.common.entity.Merchant;
import com.cmswe.alumni.common.entity.Shop;
import com.cmswe.alumni.common.vo.AuditStatisticsVo;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationApplicationMapper;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationJoinApplicationMapper;
import com.cmswe.alumni.service.association.mapper.AlumniAssociationJoinApplyMapper;
import com.cmswe.alumni.service.association.mapper.AlumniHeadquartersMapper;
import com.cmswe.alumni.common.entity.Role;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

    @Resource
    private RoleService roleService;

    @Resource
    private MerchantService merchantService;

    @Resource
    private ShopService shopService;

    @Override
    public AuditStatisticsVo getAuditTodoStatistics(Long wxId) {
        Map<String, Integer> counts = new HashMap<>();

        // 1. 查询当前用户角色，判断是否为系统管理员
        boolean isSystemAdmin = false;
        if (wxId != null) {
            List<Role> userRoles = roleService.getRolesByUserId(wxId);
            isSystemAdmin = userRoles.stream()
                    .anyMatch(role -> "SYSTEM_SUPER_ADMIN".equals(role.getRoleCode()));
        }

        // 2. 统计各个模块的待办数量

        // 2.1 审核校友总会 (SYSTEM_GENERAL_ALUMNI_ASSOCIATION_AUDIT)
        // 仅系统管理员统计待办数量，其它用户不显示
        if (isSystemAdmin) {
            LambdaQueryWrapper<AlumniHeadquarters> headquartersWrapper = new LambdaQueryWrapper<>();
            headquartersWrapper.eq(AlumniHeadquarters::getApprovalStatus, 0)
                    .ne(AlumniHeadquarters::getActiveStatus, 0);
            counts.put("SYSTEM_GENERAL_ALUMNI_ASSOCIATION_AUDIT",
                    Math.toIntExact(alumniHeadquartersMapper.selectCount(headquartersWrapper)));
        } else {
            counts.put("SYSTEM_GENERAL_ALUMNI_ASSOCIATION_AUDIT", 0);
        }

        // 2.2 校友会审核 - 系统级别 (SYSTEM_ALUMNI_ASSOCIATION_APPLICATION)
        // 仅系统管理员统计待办数量，其它用户不显示
        if (isSystemAdmin) {
            LambdaQueryWrapper<AlumniAssociationApplication> sysAppWrapper = new LambdaQueryWrapper<>();
            sysAppWrapper.eq(AlumniAssociationApplication::getApplicationStatus, 0);
            counts.put("SYSTEM_ALUMNI_ASSOCIATION_APPLICATION",
                    Math.toIntExact(alumniAssociationApplicationMapper.selectCount(sysAppWrapper)));
        } else {
            counts.put("SYSTEM_ALUMNI_ASSOCIATION_APPLICATION", 0);
        }

        // 2.3 校友会审核 - 城市/校促会级别 (LOCAL_PLATFORM_ALUMNI_ASSOCIATION_APPLICATION)
        // 用户要求城市下面的校友会审核不参与统计
        counts.put("LOCAL_PLATFORM_ALUMNI_ASSOCIATION_APPLICATION", 0);

        // 2.4 校友会认证 (SYSTEM_ALUMNI_ASSOCIATION_CERTIFICATION) - 对应校友会申请加入校促会
        // 仅统计通过 RoleUser 分配管理的校促会下的认证申请，不统计系统管理员的「全部组织」
        Set<Long> platformIdsByRole = userService.getManagedPlatformIdsByRole(wxId);
        if (!platformIdsByRole.isEmpty()) {
            LambdaQueryWrapper<AlumniAssociationJoinApply> certWrapper = new LambdaQueryWrapper<>();
            certWrapper.eq(AlumniAssociationJoinApply::getStatus, 0)
                    .in(AlumniAssociationJoinApply::getPlatformId, platformIdsByRole);
            counts.put("SYSTEM_ALUMNI_ASSOCIATION_CERTIFICATION", Math.toIntExact(alumniAssociationJoinApplyMapper.selectCount(certWrapper)));
        } else {
            counts.put("SYSTEM_ALUMNI_ASSOCIATION_CERTIFICATION", 0);
        }

        // 2.5～2.7 校友会维度待办：仅 RoleUser 绑定为该校友会管理员的才统计（含超级管理员亦须具备该校友会管理权限）
        Set<Long> alumniIdsByRole = userService.getManagedAlumniAssociationIdsByRole(wxId);
        if (!alumniIdsByRole.isEmpty()) {
            LambdaQueryWrapper<AlumniAssociationJoinApplication> joinWrapper = new LambdaQueryWrapper<>();
            joinWrapper.eq(AlumniAssociationJoinApplication::getApplicationStatus, 0)
                    .eq(AlumniAssociationJoinApplication::getApplicantType, 1) // 1-用户
                    .in(AlumniAssociationJoinApplication::getAlumniAssociationId, alumniIdsByRole);
            counts.put("ALUMNI_ASSOCIATION_JOIN_REVIEW", Math.toIntExact(alumniAssociationJoinApplicationMapper.selectCount(joinWrapper)));

            // 2.6 商户审核 (ALUMNI_ASSOCIATION_MERCHANT_AUDIT)：待审核商户，且所属校友会在管理范围内
            LambdaQueryWrapper<Merchant> merchantPendingWrapper = new LambdaQueryWrapper<>();
            merchantPendingWrapper.eq(Merchant::getReviewStatus, 0);
            if (!alumniIdsByRole.isEmpty()) {
                String sql = alumniIdsByRole.stream()
                        .map(id -> "(alumni_association_id = CAST(" + id + " AS CHAR) OR JSON_CONTAINS(alumni_association_id, CAST(" + id + " AS CHAR)))")
                        .collect(Collectors.joining(" OR "));
                merchantPendingWrapper.apply("(" + sql + ")");
            }
            counts.put("ALUMNI_ASSOCIATION_MERCHANT_AUDIT",
                    Math.toIntExact(merchantService.count(merchantPendingWrapper)));

            // 2.7 店铺审核 (ALUMNI_ASSOCIATION_SHOP_AUDIT)：待审核店铺（review_status=0），且所属商户的校友会在管理范围内
            LambdaQueryWrapper<Merchant> scopedMerchantQuery = new LambdaQueryWrapper<>();
            scopedMerchantQuery.select(Merchant::getMerchantId);
            if (!alumniIdsByRole.isEmpty()) {
                String sql = alumniIdsByRole.stream()
                        .map(id -> "(alumni_association_id = CAST(" + id + " AS CHAR) OR JSON_CONTAINS(alumni_association_id, CAST(" + id + " AS CHAR)))")
                        .collect(Collectors.joining(" OR "));
                scopedMerchantQuery.apply("(" + sql + ")");
            }
            List<Long> scopedMerchantIds = merchantService.list(scopedMerchantQuery)
                    .stream()
                    .map(Merchant::getMerchantId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            if (scopedMerchantIds.isEmpty()) {
                counts.put("ALUMNI_ASSOCIATION_SHOP_AUDIT", 0);
            } else {
                LambdaQueryWrapper<Shop> shopPendingWrapper = new LambdaQueryWrapper<>();
                shopPendingWrapper.eq(Shop::getReviewStatus, 0)
                        .in(Shop::getMerchantId, scopedMerchantIds);
                counts.put("ALUMNI_ASSOCIATION_SHOP_AUDIT",
                        Math.toIntExact(shopService.count(shopPendingWrapper)));
            }
        } else {
            counts.put("ALUMNI_ASSOCIATION_JOIN_REVIEW", 0);
            counts.put("ALUMNI_ASSOCIATION_MERCHANT_AUDIT", 0);
            counts.put("ALUMNI_ASSOCIATION_SHOP_AUDIT", 0);
        }

        return AuditStatisticsVo.builder()
                .todoCounts(counts)
                .build();
    }
}
