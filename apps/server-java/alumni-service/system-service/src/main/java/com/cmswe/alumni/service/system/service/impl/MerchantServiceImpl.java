package com.cmswe.alumni.service.system.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.association.AlumniAssociationService;
import com.cmswe.alumni.api.search.ShopService;
import com.cmswe.alumni.api.system.MerchantService;
import com.cmswe.alumni.api.user.OrganizeArchiRoleService;
import com.cmswe.alumni.api.user.RoleService;
import com.cmswe.alumni.api.user.RoleUserService;
import com.cmswe.alumni.api.user.UnifiedMessageApiService;
import com.cmswe.alumni.common.constant.CommonConstant;
import com.cmswe.alumni.common.dto.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import com.cmswe.alumni.common.entity.AlumniAssociation;
import com.cmswe.alumni.common.entity.Merchant;
import com.cmswe.alumni.common.entity.MerchantMember;
import com.cmswe.alumni.common.entity.OrganizeArchiRole;
import com.cmswe.alumni.common.entity.Role;
import com.cmswe.alumni.common.entity.RoleUser;
import com.cmswe.alumni.common.entity.Shop;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.enums.NotificationType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniAssociationListVo;
import com.cmswe.alumni.common.vo.MerchantDetailVo;
import com.cmswe.alumni.common.vo.MerchantListVo;
import com.cmswe.alumni.common.vo.MerchantApprovalVo;
import com.cmswe.alumni.common.vo.SuperAdminMerchantApprovalVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.common.vo.ShopListVo;
import com.cmswe.alumni.common.vo.MerchantMemberVo;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.api.user.UserService;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.service.system.mapper.MerchantMemberMapper;
import com.cmswe.alumni.service.system.mapper.SystemMerchantMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 商户 Service 实现类
 *
 * @author CNI Alumni System
 */
@Slf4j
@Service
public class MerchantServiceImpl extends ServiceImpl<SystemMerchantMapper, Merchant> implements MerchantService {

    @Resource
    private UnifiedMessageApiService unifiedMessageApiService;

    @Resource
    private RoleService roleService;

    @Resource
    private RoleUserService roleUserService;

    @Resource
    private OrganizeArchiRoleService organizeArchiRoleService;

    @Resource
    private MerchantMemberMapper merchantMemberMapper;

    @Resource
    private AlumniAssociationService alumniAssociationService;

    @Lazy
    @Resource
    private ShopService shopService;

    @Resource
    private UserService userService;

    @Resource
    private WxUserInfoService wxUserInfoService;

    private List<Long> parseAssociationIds(String associationIdStr) {
        if (StringUtils.isBlank(associationIdStr)) {
            return new ArrayList<>();
        }
        String trimmed = associationIdStr.trim();
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            try {
                return JSON.parseArray(trimmed, Long.class);
            } catch (Exception e) {
                log.error("解析校友会ID数组失败: {}", trimmed, e);
            }
        }
        // 尝试作为单个 ID 处理
        try {
            Long id = Long.parseLong(trimmed);
            return new ArrayList<>(Collections.singletonList(id));
        } catch (NumberFormatException e) {
            log.warn("校友会ID字段格式非数字且非数组: {}", trimmed);
        }
        return new ArrayList<>();
    }

    /**
     * 分页查询商户列表
     *
     * @param queryMerchantListDto 查询条件
     * @return 分页结果
     */
    @Override
    public PageVo<MerchantListVo> selectByPage(QueryMerchantListDto queryMerchantListDto) {
        // 1.参数校验
        Optional.ofNullable(queryMerchantListDto)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        // 2.获取参数
        String merchantName = queryMerchantListDto.getMerchantName();
        Integer merchantType = queryMerchantListDto.getMerchantType();
        Integer memberTier = queryMerchantListDto.getMemberTier();
        String businessCategory = queryMerchantListDto.getBusinessCategory();
        String contactPhone = queryMerchantListDto.getContactPhone();
        String legalPerson = queryMerchantListDto.getLegalPerson();
        Integer isAlumniCertified = queryMerchantListDto.getIsAlumniCertified();
        Long alumniAssociationId = queryMerchantListDto.getAlumniAssociationId();
        int current = queryMerchantListDto.getCurrent();
        int pageSize = queryMerchantListDto.getPageSize();
        String sortField = queryMerchantListDto.getSortField();
        String sortOrder = queryMerchantListDto.getSortOrder();

        // 3.构建查询条件（强制只查询启用且审核通过的商户）
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                // 强制条件：只返回启用状态（status=1）且审核通过（reviewStatus=1）的商户
                .eq(Merchant::getStatus, 1)
                .eq(Merchant::getReviewStatus, 1)
                // 其他可选查询条件
                .like(StringUtils.isNotBlank(merchantName), Merchant::getMerchantName, merchantName)
                .eq(merchantType != null, Merchant::getMerchantType, merchantType)
                .eq(memberTier != null, Merchant::getMemberTier, memberTier)
                .like(StringUtils.isNotBlank(businessCategory), Merchant::getBusinessCategory, businessCategory)
                .like(StringUtils.isNotBlank(contactPhone), Merchant::getContactPhone, contactPhone)
                .like(StringUtils.isNotBlank(legalPerson), Merchant::getLegalPerson, legalPerson)
                .eq(isAlumniCertified != null, Merchant::getIsAlumniCertified, isAlumniCertified);

        if (alumniAssociationId != null) {
            queryWrapper.apply("(alumni_association_id = CAST({0} AS CHAR) OR JSON_CONTAINS(alumni_association_id, CAST({0} AS CHAR)))", alumniAssociationId);
        }

        // 4.添加排序
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            switch (sortField) {
                case "ratingScore":
                    queryWrapper.orderBy(true, isAsc, Merchant::getRatingScore);
                    break;
                case "createTime":
                    queryWrapper.orderBy(true, isAsc, Merchant::getCreateTime);
                    break;
                case "shopCount":
                    queryWrapper.orderBy(true, isAsc, Merchant::getShopCount);
                    break;
                case "memberTier":
                    queryWrapper.orderBy(true, isAsc, Merchant::getMemberTier);
                    break;
                default:
                    queryWrapper.orderByDesc(Merchant::getCreateTime);
                    break;
            }
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc(Merchant::getCreateTime);
        }

        // 5.执行分页查询
        Page<Merchant> merchantPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 6.转换为VO（处理Long类型精度丢失问题）
        List<MerchantListVo> list = merchantPage.getRecords().stream().map(merchant -> {
            MerchantListVo merchantListVo = MerchantListVo.objToVo(merchant);
            // 将Long转换为String，避免前端精度丢失
            merchantListVo.setMerchantId(String.valueOf(merchant.getMerchantId()));
            if (merchant.getUserId() != null) {
                merchantListVo.setUserId(String.valueOf(merchant.getUserId()));
            }
            if (merchant.getAlumniAssociationId() != null) {
                merchantListVo.setAlumniAssociationId(String.valueOf(merchant.getAlumniAssociationId()));
            }
            return merchantListVo;
        }).toList();

        log.info("分页查询商户列表，当前页：{}，每页数量：{}，总记录数：{}", current, pageSize, merchantPage.getTotal());

        // 7.转换结果并返回
        Page<MerchantListVo> resultPage = new Page<MerchantListVo>(current, pageSize, merchantPage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    public MerchantDetailVo getPendingMerchantByIdAndUserId(Long merchantId, Long wxId) {
        // 1. 参数校验
        if (merchantId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "商户ID不能为空");
        }
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }

        log.info("查询本人待审核/审核失败商户申请 - 商户ID: {}, 用户ID: {}", merchantId, wxId);

        // 2. 查询商户（0-待审核 2-审核失败，均可回填后修改/重新提交）
        Merchant merchant = this.getOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getMerchantId, merchantId)
                        .eq(Merchant::getUserId, wxId)
                        .in(Merchant::getReviewStatus, java.util.Arrays.asList(0, 2))
        );

        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "未找到待审核或审核失败的商户申请");
        }

        log.info("查询本人商户申请成功 - 商户ID: {}, 商户名称: {}", merchantId, merchant.getMerchantName());

        // 3. 转换为 VO
        return MerchantDetailVo.objToVo(merchant);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePendingMerchantApplication(Long wxId, Long merchantId, ApplyMerchantDto applyDto) {
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }
        if (merchantId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "商户ID不能为空");
        }
        Optional.ofNullable(applyDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户申请不存在");
        }
        if (!wxId.equals(merchant.getUserId())) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "无权修改该申请");
        }
        if (merchant.getReviewStatus() == null || merchant.getReviewStatus() != 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "仅待审核的申请可修改");
        }

        BeanUtils.copyProperties(applyDto, merchant);
        merchant.setMerchantId(merchantId);
        merchant.setUserId(wxId);
        merchant.setReviewStatus(0);
        merchant.setReviewerId(null);
        merchant.setReviewReason(null);
        merchant.setReviewTime(null);
        merchant.setIsAlumniCertified(applyDto.getMerchantType() != null && applyDto.getMerchantType() == 1 ? 1 : 0);
        merchant.setUpdateTime(LocalDateTime.now());
        
        // 处理关联校友会 ID (转换为 JSON 数组)
        if (applyDto.getAlumniAssociationId() != null) {
            merchant.setAlumniAssociationId(JSON.toJSONString(Collections.singletonList(applyDto.getAlumniAssociationId())));
        } else {
            merchant.setAlumniAssociationId(null);
        }

        log.info("用户更新待审核商户申请 - 用户ID: {}, 商户ID: {}, 商户名称: {}", wxId, merchantId, merchant.getMerchantName());
        boolean ok = this.updateById(merchant);
        if (!ok) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新申请失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelPendingMerchantApplication(Long wxId, Long merchantId) {
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }
        if (merchantId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "商户ID不能为空");
        }
        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户申请不存在");
        }
        if (!wxId.equals(merchant.getUserId())) {
            throw new BusinessException(ErrorType.FORBIDDEN_ERROR, "无权撤销该申请");
        }
        if (merchant.getReviewStatus() == null || merchant.getReviewStatus() != 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "仅待审核的申请可撤销");
        }
        merchant.setReviewStatus(3);
        merchant.setReviewerId(null);
        merchant.setReviewReason(null);
        merchant.setReviewTime(null);
        log.info("用户撤销待审核商户申请 - 用户ID: {}, 商户ID: {}", wxId, merchantId);
        boolean ok = this.updateById(merchant);
        if (!ok) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "撤销失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyMerchant(Long wxId, ApplyMerchantDto applyDto) {
        // 1. 参数校验
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }
        Optional.ofNullable(applyDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        log.info("用户提交商户入驻申请 - 用户ID: {}, 商户名称: {}", wxId, applyDto.getMerchantName());

        // 2. 检查用户是否已有待审核的申请
        long pendingCount = this.count(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getUserId, wxId)
                        .eq(Merchant::getReviewStatus, 0) // 0-待审核
        );

        if (pendingCount > 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "您已有待审核的商户申请，请耐心等待审核结果");
        }

        // 3. 构建商户实体
        Merchant merchant = new Merchant();
        BeanUtils.copyProperties(applyDto, merchant);
        merchant.setUserId(wxId);
        merchant.setReviewStatus(0); // 0-待审核
        merchant.setStatus(0); // 0-禁用（待审核通过后启用）
        merchant.setMemberTier(1); // 1-基础版
        merchant.setShopCount(0);
        merchant.setTotalCouponIssued(0L);
        merchant.setTotalCouponVerified(0L);
        merchant.setRatingCount(0);
        merchant.setIsAlumniCertified(applyDto.getMerchantType() == 1 ? 1 : 0);
        
        // 处理关联校友会 ID (转换为 JSON 数组)
        if (applyDto.getAlumniAssociationId() != null) {
            merchant.setAlumniAssociationId(JSON.toJSONString(Collections.singletonList(applyDto.getAlumniAssociationId())));
        }

        // 4. 保存到数据库
        boolean saveResult = this.save(merchant);
        if (!saveResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "提交申请失败");
        }

        log.info("商户申请提交成功 - 商户ID: {}, 商户名称: {}", merchant.getMerchantId(), merchant.getMerchantName());

        // 5. 发送通知给用户
        sendApplicationNotification(wxId, merchant.getMerchantName());

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveMerchant(Long reviewerId, ApproveMerchantDto approveDto) {
        // 1. 参数校验
        if (reviewerId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "审核人ID不能为空");
        }
        Optional.ofNullable(approveDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        Long merchantId = approveDto.getMerchantId();
        Integer reviewStatus = approveDto.getReviewStatus();
        String reviewReason = approveDto.getReviewReason();

        // 校验审核状态
        if (reviewStatus != 1 && reviewStatus != 2) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "审核状态只能为1（审核通过）或2（审核失败）");
        }

        // 审核失败时，审核原因不能为空
        if (reviewStatus == 2 && StringUtils.isBlank(reviewReason)) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "审核失败时必须填写审核原因");
        }

        log.info("管理员审批商户入驻申请 - 审核人ID: {}, 商户ID: {}, 审核状态: {}", reviewerId, merchantId, reviewStatus);

        // 2. 查询商户
        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户不存在");
        }

        // 3. 检查商户状态（只能审核待审核状态的商户）
        if (merchant.getReviewStatus() != 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该商户申请已经审核过了");
        }

        // 4. 更新商户审核信息
        merchant.setReviewStatus(reviewStatus);
        merchant.setReviewReason(reviewReason);
        merchant.setReviewerId(reviewerId);
        merchant.setReviewTime(LocalDateTime.now());

        // 审核通过时，启用商户
        if (reviewStatus == 1) {
            merchant.setStatus(1); // 1-启用
            merchant.setCertifiedTime(LocalDateTime.now());
        }

        boolean updateResult = this.updateById(merchant);
        if (!updateResult) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "审核操作失败");
        }

        log.info("商户审核完成 - 商户ID: {}, 审核结果: {}", merchantId, reviewStatus == 1 ? "通过" : "失败");

        // 5. 审核通过时，为申请人分配商户管理员角色
        if (reviewStatus == 1) {
            assignMerchantAdminRole(merchant.getUserId(), merchantId);
        }

        // 6. 发送审核结果通知给用户
        sendApprovalNotification(merchant.getUserId(), merchantId, merchant.getMerchantName(), reviewStatus,
                reviewReason);

        return true;
    }

    /**
     * 为申请人分配商户管理员角色
     *
     * @param wxId       申请人用户ID
     * @param merchantId 商户ID
     */
    private void assignMerchantAdminRole(Long wxId, Long merchantId) {
        try {
            log.info("开始为申请人分配商户管理员角色 - 用户ID: {}, 商户ID: {}", wxId, merchantId);

            // 1. 根据角色代码查询角色
            Role merchantAdminRole = roleService.getOne(
                    new LambdaQueryWrapper<Role>()
                            .eq(Role::getRoleCode, "ORGANIZE_MERCHANT_ADMIN")
                            .eq(Role::getStatus, 1) // 1-启用
            );

            if (merchantAdminRole == null) {
                log.error("未找到商户管理员角色 - roleCode: ORGANIZE_MERCHANT_ADMIN");
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "系统角色配置错误，未找到商户管理员角色");
            }

            // 2. 检查用户是否已有该商户的管理员角色
            RoleUser existingRoleUser = roleUserService.getOne(
                    new LambdaQueryWrapper<RoleUser>()
                            .eq(RoleUser::getWxId, wxId)
                            .eq(RoleUser::getRoleId, merchantAdminRole.getRoleId())
                            .eq(RoleUser::getType, 3) // 3-商户
                            .eq(RoleUser::getOrganizeId, merchantId));

            if (existingRoleUser != null) {
                log.info("用户已拥有该商户的管理员角色 - 用户ID: {}, 商户ID: {}", wxId, merchantId);
                return;
            }

            // 3. 创建用户角色关联记录
            RoleUser roleUser = new RoleUser();
            roleUser.setWxId(wxId);
            roleUser.setRoleId(merchantAdminRole.getRoleId());
            roleUser.setType(3); // 3-商户
            roleUser.setOrganizeId(merchantId);
            roleUser.setCreateTime(LocalDateTime.now());
            roleUser.setUpdateTime(LocalDateTime.now());

            boolean insertResult = roleUserService.save(roleUser);
            if (!insertResult) {
                log.error("分配商户管理员角色失败 - 用户ID: {}, 商户ID: {}", wxId, merchantId);
                throw new BusinessException(ErrorType.OPERATION_ERROR, "分配商户管理员角色失败");
            }

            log.info("成功为申请人分配商户管理员角色 - 用户ID: {}, 商户ID: {}, 角色ID: {}",
                    wxId, merchantId, merchantAdminRole.getRoleId());
        } catch (BusinessException e) {
            // 业务异常直接抛出，让事务回滚
            throw e;
        } catch (Exception e) {
            log.error("分配商户管理员角色时发生异常 - 用户ID: {}, 商户ID: {}", wxId, merchantId, e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "分配商户管理员角色时发生系统错误");
        }
    }

    private static final String SHOP_ADMIN_ROLE_CODE = "ORGANIZE_SHOP_ADMIN";

    /**
     * 为用户分配门店管理员角色（role_user：type=4 门店，organize_id=门店ID）
     */
    private void assignShopAdminRole(Long wxId, Long shopId) {
        try {
            log.info("开始分配门店管理员角色 - 用户ID: {}, 门店ID: {}", wxId, shopId);

            Role shopAdminRole = roleService.getOne(
                    new LambdaQueryWrapper<Role>()
                            .eq(Role::getRoleCode, SHOP_ADMIN_ROLE_CODE)
                            .eq(Role::getStatus, 1));

            if (shopAdminRole == null) {
                log.error("未找到门店管理员角色 - roleCode: {}", SHOP_ADMIN_ROLE_CODE);
                throw new BusinessException(ErrorType.SYSTEM_ERROR, "系统角色配置错误，未找到门店管理员角色");
            }

            RoleUser existing = roleUserService.getOne(
                    new LambdaQueryWrapper<RoleUser>()
                            .eq(RoleUser::getWxId, wxId)
                            .eq(RoleUser::getRoleId, shopAdminRole.getRoleId())
                            .eq(RoleUser::getType, 4)
                            .eq(RoleUser::getOrganizeId, shopId));

            if (existing != null) {
                log.info("用户已是该门店管理员 - 用户ID: {}, 门店ID: {}", wxId, shopId);
                return;
            }

            RoleUser roleUser = new RoleUser();
            roleUser.setWxId(wxId);
            roleUser.setRoleId(shopAdminRole.getRoleId());
            roleUser.setType(4);
            roleUser.setOrganizeId(shopId);
            roleUser.setCreateTime(LocalDateTime.now());
            roleUser.setUpdateTime(LocalDateTime.now());

            boolean saved = roleUserService.save(roleUser);
            if (!saved) {
                log.error("分配门店管理员角色失败 - 用户ID: {}, 门店ID: {}", wxId, shopId);
                throw new BusinessException(ErrorType.OPERATION_ERROR, "分配门店管理员角色失败");
            }

            log.info("成功分配门店管理员角色 - 用户ID: {}, 门店ID: {}, 角色ID: {}",
                    wxId, shopId, shopAdminRole.getRoleId());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("分配门店管理员角色异常 - 用户ID: {}, 门店ID: {}", wxId, shopId, e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "分配门店管理员角色时发生系统错误");
        }
    }

    /**
     * 移除用户在指定门店的门店管理员角色（与成员退出时一并清理）
     */
    private void revokeShopAdminRoleIfPresent(Long wxId, Long shopId) {
        if (shopId == null) {
            return;
        }
        try {
            Role shopAdminRole = roleService.getOne(
                    new LambdaQueryWrapper<Role>()
                            .eq(Role::getRoleCode, SHOP_ADMIN_ROLE_CODE)
                            .eq(Role::getStatus, 1));
            if (shopAdminRole == null) {
                return;
            }
            roleUserService.remove(
                    new LambdaQueryWrapper<RoleUser>()
                            .eq(RoleUser::getWxId, wxId)
                            .eq(RoleUser::getRoleId, shopAdminRole.getRoleId())
                            .eq(RoleUser::getType, 4)
                            .eq(RoleUser::getOrganizeId, shopId));
            log.info("已移除门店管理员角色 - 用户ID: {}, 门店ID: {}", wxId, shopId);
        } catch (Exception e) {
            log.error("移除门店管理员角色失败 - 用户ID: {}, 门店ID: {}", wxId, shopId, e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "移除门店管理员角色失败");
        }
    }

    /**
     * 移除用户在指定商户下所有门店的门店管理员角色，避免店铺切换/脏数据导致权限残留
     */
    private void revokeShopAdminRolesByMerchant(Long wxId, Long merchantId) {
        if (wxId == null || merchantId == null) {
            return;
        }
        try {
            Role shopAdminRole = roleService.getOne(
                    new LambdaQueryWrapper<Role>()
                            .eq(Role::getRoleCode, SHOP_ADMIN_ROLE_CODE)
                            .eq(Role::getStatus, 1));
            if (shopAdminRole == null) {
                return;
            }

            List<Long> merchantShopIds = shopService.list(
                            new LambdaQueryWrapper<Shop>().eq(Shop::getMerchantId, merchantId))
                    .stream()
                    .map(Shop::getShopId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toList());
            if (merchantShopIds.isEmpty()) {
                return;
            }

            roleUserService.remove(
                    new LambdaQueryWrapper<RoleUser>()
                            .eq(RoleUser::getWxId, wxId)
                            .eq(RoleUser::getRoleId, shopAdminRole.getRoleId())
                            .eq(RoleUser::getType, 4)
                            .in(RoleUser::getOrganizeId, merchantShopIds));
            log.info("已移除用户在商户下所有门店管理员角色 - 用户ID: {}, 商户ID: {}, 门店数: {}",
                    wxId, merchantId, merchantShopIds.size());
        } catch (Exception e) {
            log.error("按商户移除门店管理员角色失败 - 用户ID: {}, 商户ID: {}", wxId, merchantId, e);
            throw new BusinessException(ErrorType.SYSTEM_ERROR, "移除门店管理员角色失败");
        }
    }

    private boolean hasShopAdminRole(Long wxId, Long shopId) {
        if (wxId == null || shopId == null) {
            return false;
        }
        Role shopAdminRole = roleService.getOne(
                new LambdaQueryWrapper<Role>()
                        .eq(Role::getRoleCode, SHOP_ADMIN_ROLE_CODE)
                        .eq(Role::getStatus, 1));
        if (shopAdminRole == null) {
            return false;
        }
        return roleUserService.lambdaQuery()
                .eq(RoleUser::getWxId, wxId)
                .eq(RoleUser::getRoleId, shopAdminRole.getRoleId())
                .eq(RoleUser::getType, 4)
                .eq(RoleUser::getOrganizeId, shopId)
                .count() > 0;
    }

    /**
     * 发送商户申请提交通知
     *
     * @param wxId         用户ID
     * @param merchantName 商户名称
     */
    private void sendApplicationNotification(Long wxId, String merchantName) {
        try {
            String title = "商户入驻申请已提交";
            String content = "您的【" + merchantName + "】商户入驻申请已经成功提交，请等待审核";

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    wxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    null,
                    "MERCHANT_APPLICATION");

            if (success) {
                log.info("商户申请通知发送成功 - 用户ID: {}, 商户名称: {}", wxId, merchantName);
            } else {
                log.warn("商户申请通知发送失败 - 用户ID: {}, 商户名称: {}", wxId, merchantName);
            }
        } catch (Exception e) {
            log.error("发送商户申请通知时发生异常 - 用户ID: {}, 商户名称: {}", wxId, merchantName, e);
        }
    }

    /**
     * 发送商户审核结果通知
     *
     * @param wxId         用户ID
     * @param merchantId   商户ID
     * @param merchantName 商户名称
     * @param reviewStatus 审核状态（1-通过，2-失败）
     * @param reviewReason 审核原因
     */
    private void sendApprovalNotification(Long wxId, Long merchantId, String merchantName, Integer reviewStatus,
            String reviewReason) {
        try {
            String title;
            String content;

            if (reviewStatus == 1) {
                // 审核通过
                title = "商户入驻申请已通过";
                content = "恭喜，您的【" + merchantName + "】商户入驻申请已经审核通过！您现在可以开始管理您的商户和门店了";
            } else {
                // 审核失败
                title = "商户入驻申请未通过";
                content = "很抱歉，您的【" + merchantName + "】商户入驻申请未通过审核";
                if (StringUtils.isNotBlank(reviewReason)) {
                    content += "。原因：" + reviewReason;
                }
                content += "。您可以修改后重新提交申请";
            }

            boolean success = unifiedMessageApiService.sendSystemNotification(
                    wxId,
                    NotificationType.SYSTEM_ANNOUNCEMENT,
                    title,
                    content,
                    merchantId,
                    "MERCHANT_APPROVAL");

            if (success) {
                log.info("商户审核通知发送成功 - 用户ID: {}, 商户ID: {}, 商户名称: {}, 审核结果: {}", wxId, merchantId, merchantName,
                        reviewStatus == 1 ? "通过" : "失败");
            } else {
                log.warn("商户审核通知发送失败 - 用户ID: {}, 商户ID: {}, 商户名称: {}", wxId, merchantId, merchantName);
            }
        } catch (Exception e) {
            log.error("发送商户审核通知时发生异常 - 用户ID: {}, 商户ID: {}, 商户名称: {}", wxId, merchantId, merchantName, e);
        }
    }

    @Override
    public PageVo<MerchantApprovalVo> selectApprovalRecordsByPage(QueryMerchantApprovalDto queryDto) {
        // 1.参数校验
        Optional.ofNullable(queryDto)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        // 2.获取参数
        String merchantName = queryDto.getMerchantName();
        Integer reviewStatus = queryDto.getReviewStatus();
        Integer merchantType = queryDto.getMerchantType();
        Long alumniAssociationId = queryDto.getAlumniAssociationId();
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();
        String sortField = queryDto.getSortField();
        String sortOrder = queryDto.getSortOrder();

        // 3.构建查询条件
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.isNotBlank(merchantName), Merchant::getMerchantName, merchantName)
                .eq(reviewStatus != null, Merchant::getReviewStatus, reviewStatus)
                .eq(merchantType != null, Merchant::getMerchantType, merchantType)
                .ne(Merchant::getReviewStatus, 3); // 3-已撤销，审批列表不展示

        if (alumniAssociationId != null) {
            queryWrapper.apply("(alumni_association_id = CAST({0} AS CHAR) OR JSON_CONTAINS(alumni_association_id, CAST({0} AS CHAR)))", alumniAssociationId);
        }

        // 4.添加排序
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            if ("createTime".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, Merchant::getCreateTime);
            } else if ("reviewTime".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, Merchant::getReviewTime);
            } else {
                queryWrapper.orderByDesc(Merchant::getCreateTime);
            }
        } else {
            queryWrapper.orderByDesc(Merchant::getCreateTime);
        }

        // 5.执行分页查询
        Page<Merchant> merchantPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 6.转换为VO
        List<MerchantApprovalVo> list = merchantPage.getRecords().stream()
                .map(MerchantApprovalVo::objToVo)
                .toList();

        log.info("分页查询商户审批记录，当前页：{}，每页数量：{}，总记录数：{}", current, pageSize, merchantPage.getTotal());

        // 7.转换结果并返回
        Page<MerchantApprovalVo> resultPage = new Page<MerchantApprovalVo>(current, pageSize, merchantPage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    public PageVo<SuperAdminMerchantApprovalVo> selectAllApprovalRecordsByPage(QueryMerchantApprovalDto queryDto) {
        // 1.参数校验
        Optional.ofNullable(queryDto)
                .orElseThrow(() -> new BusinessException(ErrorType.SYSTEM_ERROR));

        // 2.获取参数
        String merchantName = queryDto.getMerchantName();
        Integer reviewStatus = queryDto.getReviewStatus();
        Integer merchantType = queryDto.getMerchantType();
        int current = queryDto.getCurrent();
        int pageSize = queryDto.getPageSize();
        String sortField = queryDto.getSortField();
        String sortOrder = queryDto.getSortOrder();

        // 3.构建查询条件 (超级管理员查看所有，不按校友会过滤)
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper
                .like(StringUtils.isNotBlank(merchantName), Merchant::getMerchantName, merchantName)
                .eq(reviewStatus != null, Merchant::getReviewStatus, reviewStatus)
                .eq(merchantType != null, Merchant::getMerchantType, merchantType)
                .ne(Merchant::getReviewStatus, 3); // 3-已撤销，审批列表不展示

        // 4.添加排序
        if (StringUtils.isNotBlank(sortField)) {
            boolean isAsc = CommonConstant.SORT_ORDER_ASC.equals(sortOrder);
            if ("createTime".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, Merchant::getCreateTime);
            } else if ("reviewTime".equals(sortField)) {
                queryWrapper.orderBy(true, isAsc, Merchant::getReviewTime);
            } else {
                queryWrapper.orderByDesc(Merchant::getCreateTime);
            }
        } else {
            queryWrapper.orderByDesc(Merchant::getCreateTime);
        }

        // 5.执行分页查询
        Page<Merchant> merchantPage = this.page(new Page<>(current, pageSize), queryWrapper);

        // 6.转换为VO (并填充申请人/审核人显示名)
        List<SuperAdminMerchantApprovalVo> list = merchantPage.getRecords().stream()
                .map(merchant -> {
                    SuperAdminMerchantApprovalVo vo = SuperAdminMerchantApprovalVo.objToVo(merchant);
                    vo.setApplicantName(resolveUserDisplayName(merchant.getUserId()));
                    vo.setReviewerName(resolveUserDisplayName(merchant.getReviewerId()));
                    return vo;
                })
                .toList();

        log.info("超级管理员分页查询所有商户审批记录，当前页：{}，每页数量：{}，总记录数：{}", current, pageSize, merchantPage.getTotal());

        // 7.转换结果并返回
        Page<SuperAdminMerchantApprovalVo> resultPage = new Page<SuperAdminMerchantApprovalVo>(current, pageSize, merchantPage.getTotal())
                .setRecords(list);
        return PageVo.of(resultPage);
    }

    @Override
    public MerchantApprovalVo getApprovalRecordByMerchantId(Long merchantId) {
        if (merchantId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "商户ID不能为空");
        }

        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户不存在");
        }

        MerchantApprovalVo vo = MerchantApprovalVo.objToVo(merchant);
        vo.setApplicantName(resolveUserDisplayName(merchant.getUserId()));
        vo.setReviewerName(resolveUserDisplayName(merchant.getReviewerId()));

        if (StringUtils.isNotBlank(merchant.getAlumniAssociationId())) {
            List<Long> associationIds = parseAssociationIds(merchant.getAlumniAssociationId());
            if (!associationIds.isEmpty()) {
                // 设置已加入的列表
                List<AlumniAssociationListVo> joinedList = new ArrayList<>();
                for (Long assocId : associationIds) {
                    AlumniAssociation association = alumniAssociationService.getById(assocId);
                    if (association != null) {
                        joinedList.add(AlumniAssociationListVo.objToVo(association));
                    }
                }
                vo.setJoinedAssociations(joinedList);
                
                // 设置第一个为主展示
                if (!joinedList.isEmpty()) {
                    vo.setAlumniAssociation(joinedList.get(0));
                }
            }
        }

        log.info("查询商户入驻申请详情 - merchantId: {}, reviewStatus: {}", merchantId, merchant.getReviewStatus());
        return vo;
    }

    /**
     * 用户展示名：优先真实姓名，否则微信昵称
     */
    private String resolveUserDisplayName(Long wxId) {
        if (wxId == null) {
            return null;
        }
        WxUserInfo info = wxUserInfoService.getOne(
                new LambdaQueryWrapper<WxUserInfo>()
                        .eq(WxUserInfo::getWxId, wxId)
                        .last("LIMIT 1"));
        if (info == null) {
            return null;
        }
        if (StringUtils.isNotBlank(info.getName())) {
            return info.getName();
        }
        return info.getNickname();
    }

    @Override
    public MerchantDetailVo getMerchantDetailById(Long merchantId) {
        // 1. 参数校验
        if (merchantId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "商户ID不能为空");
        }

        log.info("查询商户详情 - 商户ID: {}", merchantId);

        // 2. 查询商户（只查询启用状态的商户）
        Merchant merchant = this.getOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getMerchantId, merchantId)
                        .eq(Merchant::getStatus, 1) // 1-启用
                        .eq(Merchant::getReviewStatus, 1) // 1-审核通过
        );

        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户不存在或未启用");
        }

        // 3. 转换为 VO
        MerchantDetailVo merchantDetailVo = MerchantDetailVo.objToVo(merchant);

        // 4. 如果有关联的校友会，查询校友会信息
        if (StringUtils.isNotBlank(merchant.getAlumniAssociationId())) {
            List<Long> associationIds = parseAssociationIds(merchant.getAlumniAssociationId());
            if (!associationIds.isEmpty()) {
                List<AlumniAssociationListVo> joinedList = new ArrayList<>();
                for (Long assocId : associationIds) {
                    AlumniAssociation association = alumniAssociationService.getById(assocId);
                    if (association != null) {
                        joinedList.add(AlumniAssociationListVo.objToVo(association));
                    }
                }
                merchantDetailVo.setJoinedAssociations(joinedList);
                
                // 取第一个作为主展示
                if (!joinedList.isEmpty()) {
                    merchantDetailVo.setAlumniAssociation(joinedList.get(0));
                }
            }
        }

        // 5. 查询商户下的门店列表（只返回已审核通过的门店）
        List<Shop> shops = shopService.list(
                new LambdaQueryWrapper<Shop>()
                        .eq(Shop::getMerchantId, merchantId)
                        .eq(Shop::getReviewStatus, 1) // 1-审核通过
                        .orderByDesc(Shop::getShopType) // 总店排在前面
                        .orderByDesc(Shop::getCreateTime));

        if (!shops.isEmpty()) {
            List<ShopListVo> shopListVos = shops.stream()
                    .map(ShopListVo::objToVo)
                    .collect(Collectors.toList());
            merchantDetailVo.setShops(shopListVos);
        }

        log.info("查询商户详情成功 - 商户ID: {}, 商户名称: {}, 门店数量: {}",
                merchantId, merchant.getMerchantName(), shops.size());

        return merchantDetailVo;
    }

    @Override
    public PageVo<MerchantListVo> getMyManagedMerchants(Long wxId, Long current, Long size) {
        // 1. 参数校验
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL);
        }
        if (current == null || current < 1) {
            current = 1L;
        }
        if (size == null || size < 1) {
            size = 10L;
        }

        // 2. 判断是否是系统超级管理员：超级管理员可查看所有商户
        List<Role> userRoles = roleService.getRolesByUserId(wxId);
        boolean isSystemSuperAdmin = userRoles.stream()
                .anyMatch(role -> "SYSTEM_SUPER_ADMIN".equals(role.getRoleCode()));
        if (isSystemSuperAdmin) {
            log.info("系统超级管理员查询商户列表 - 用户ID: {}, 当前页: {}, 每页大小: {}", wxId, current, size);

            Page<Merchant> page = new Page<>(current, size);
            Page<Merchant> merchantPage = this.lambdaQuery()
                    // 仅返回审核通过的商户，避免未审核商户出现在管理列表
                    .eq(Merchant::getReviewStatus, 1)
                    .orderByDesc(Merchant::getCreateTime)
                    .page(page);

            List<MerchantListVo> merchantListVos = merchantPage.getRecords().stream()
                    .map(merchant -> {
                        MerchantListVo vo = MerchantListVo.objToVo(merchant);
                        vo.setMerchantId(String.valueOf(merchant.getMerchantId()));
                        if (merchant.getUserId() != null) {
                            vo.setUserId(String.valueOf(merchant.getUserId()));
                        }
                        if (merchant.getAlumniAssociationId() != null) {
                            vo.setAlumniAssociationId(String.valueOf(merchant.getAlumniAssociationId()));
                        }
                        return vo;
                    })
                    .collect(Collectors.toList());

            Page<MerchantListVo> voPage = new Page<>(current, size, merchantPage.getTotal());
            voPage.setRecords(merchantListVos);

            log.info("系统超级管理员查询商户列表成功 - 用户ID: {}, 找到{}个商户", wxId, merchantPage.getTotal());
            return PageVo.of(voPage);
        }

        // 3. 根据角色代码查询角色ID
        Role role = roleService.getRoleByCodeInner("ORGANIZE_MERCHANT_ADMIN");
        if (role == null) {
            log.warn("商户管理员角色不存在，roleCode: ORGANIZE_MERCHANT_ADMIN");
            // 返回空列表
            Page<MerchantListVo> emptyPage = new Page<>(current, size, 0);
            return PageVo.of(emptyPage);
        }

        // 4. 查询用户拥有该角色的所有组织ID（商户ID）
        List<RoleUser> roleUsers = roleUserService.lambdaQuery()
                .eq(RoleUser::getWxId, wxId)
                .eq(RoleUser::getRoleId, role.getRoleId())
                .eq(RoleUser::getType, 3) // 类型3代表商户
                .list();

        if (roleUsers.isEmpty()) {
            log.info("用户没有负责的商户 - 用户ID: {}", wxId);
            // 返回空列表
            Page<MerchantListVo> emptyPage = new Page<>(current, size, 0);
            return PageVo.of(emptyPage);
        }

        // 5. 提取商户ID列表
        List<Long> merchantIds = roleUsers.stream()
                .map(RoleUser::getOrganizeId)
                .distinct()
                .collect(Collectors.toList());

        log.info("查询用户负责的商户列表 - 用户ID: {}, 商户数量: {}", wxId, merchantIds.size());

        // 6. 分页查询商户列表
        Page<Merchant> page = new Page<>(current, size);
        Page<Merchant> merchantPage = this.lambdaQuery()
                .in(Merchant::getMerchantId, merchantIds)
                // 仅返回审核通过的商户，避免未审核商户出现在管理列表
                .eq(Merchant::getReviewStatus, 1)
                .orderByDesc(Merchant::getCreateTime)
                .page(page);

        // 7. 转换为 VO
        List<MerchantListVo> merchantListVos = merchantPage.getRecords().stream()
                .map(merchant -> {
                    MerchantListVo vo = MerchantListVo.objToVo(merchant);
                    // ID 转换为 String 避免精度丢失
                    vo.setMerchantId(String.valueOf(merchant.getMerchantId()));
                    if (merchant.getUserId() != null) {
                        vo.setUserId(String.valueOf(merchant.getUserId()));
                    }
                    if (merchant.getAlumniAssociationId() != null) {
                        vo.setAlumniAssociationId(String.valueOf(merchant.getAlumniAssociationId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());

        // 8. 构建分页结果
        Page<MerchantListVo> voPage = new Page<>(current, size, merchantPage.getTotal());
        voPage.setRecords(merchantListVos);

        log.info("查询用户负责的商户列表成功 - 用户ID: {}, 找到{}个商户", wxId, merchantPage.getTotal());

        return PageVo.of(voPage);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantDetailVo updateMerchantInfo(Long wxId, UpdateMerchantDto dto) {
        if (wxId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空");
        }
        Optional.ofNullable(dto).orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        Long merchantId = dto.getMerchantId();
        assertUserCanManageMerchant(wxId, merchantId);

        Merchant merchant = this.getById(merchantId);
        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户不存在");
        }
        if (merchant.getReviewStatus() == null || merchant.getReviewStatus() != 1) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "仅审核通过的商户可修改资料");
        }
        if (merchant.getStatus() != null && merchant.getStatus() == 2) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "已注销商户不可修改资料");
        }
        if (merchant.getStatus() == null || merchant.getStatus() != 1) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "商户未启用，暂不可修改资料");
        }

        if (!hasAnyUpdatableFieldInDto(dto)) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "请至少填写一项需要修改的字段");
        }

        if (dto.getContactEmail() != null && StringUtils.isNotBlank(dto.getContactEmail())) {
            String em = dto.getContactEmail().trim();
            if (!em.matches("^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "邮箱格式不正确");
            }
        }

        if (dto.getMerchantName() != null) {
            if (StringUtils.isBlank(dto.getMerchantName())) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "商户名称不能为空");
            }
            merchant.setMerchantName(dto.getMerchantName().trim());
        }
        if (dto.getMerchantType() != null) {
            if (dto.getMerchantType() != 1 && dto.getMerchantType() != 2) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "商户类型只能为1（校友商铺）或2（普通商铺）");
            }
            merchant.setMerchantType(dto.getMerchantType());
            merchant.setIsAlumniCertified(dto.getMerchantType() == 1 ? 1 : 0);
        }
        if (dto.getBusinessLicense() != null) {
            merchant.setBusinessLicense(
                    StringUtils.isBlank(dto.getBusinessLicense()) ? null : dto.getBusinessLicense().trim());
        }
        if (dto.getUnifiedSocialCreditCode() != null) {
            merchant.setUnifiedSocialCreditCode(
                    StringUtils.isBlank(dto.getUnifiedSocialCreditCode()) ? null : dto.getUnifiedSocialCreditCode().trim());
        }
        if (dto.getLegalPerson() != null) {
            merchant.setLegalPerson(StringUtils.isBlank(dto.getLegalPerson()) ? null : dto.getLegalPerson().trim());
        }
        if (dto.getLegalPersonId() != null) {
            merchant.setLegalPersonId(StringUtils.isBlank(dto.getLegalPersonId()) ? null : dto.getLegalPersonId().trim());
        }
        if (dto.getContactPhone() != null) {
            merchant.setContactPhone(StringUtils.isBlank(dto.getContactPhone()) ? null : dto.getContactPhone().trim());
        }
        if (dto.getContactEmail() != null) {
            merchant.setContactEmail(StringUtils.isBlank(dto.getContactEmail()) ? null : dto.getContactEmail().trim());
        }
        if (dto.getBusinessScope() != null) {
            merchant.setBusinessScope(dto.getBusinessScope());
        }
        if (dto.getBusinessCategory() != null) {
            merchant.setBusinessCategory(
                    StringUtils.isBlank(dto.getBusinessCategory()) ? null : dto.getBusinessCategory().trim());
        }
        if (dto.getLogo() != null) {
            merchant.setLogo(StringUtils.isBlank(dto.getLogo()) ? null : dto.getLogo().trim());
        }
        if (dto.getBackgroundImage() != null) {
            merchant.setBackgroundImage(dto.getBackgroundImage());
        }
        if (dto.getAlumniAssociationId() != null) {
            merchant.setAlumniAssociationId(JSON.toJSONString(Collections.singletonList(dto.getAlumniAssociationId())));
        }
        if (merchant.getMerchantType() != null && merchant.getMerchantType() == 2) {
            merchant.setAlumniAssociationId(null);
        }

        int effectiveType = merchant.getMerchantType() != null ? merchant.getMerchantType() : 0;
        String associationIdStr = merchant.getAlumniAssociationId();
        if (effectiveType == 1) {
            if (StringUtils.isBlank(associationIdStr)) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "校友商铺必须关联有效校友会");
            }
            List<Long> associationIds = parseAssociationIds(associationIdStr);
            if (associationIds.isEmpty()) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "校友商铺必须关联有效校友会");
            }
            AlumniAssociation assoc = alumniAssociationService.getById(associationIds.get(0));
            if (assoc == null) {
                throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "校友会不存在");
            }
        }

        merchant.setUpdateTime(LocalDateTime.now());
        boolean ok = this.updateById(merchant);
        if (!ok) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新失败");
        }
        log.info("商户资料更新成功 - operatorWxId: {}, merchantId: {}", wxId, merchantId);
        return getMerchantDetailById(merchantId);
    }

    private static boolean hasAnyUpdatableFieldInDto(UpdateMerchantDto dto) {
        return dto.getMerchantName() != null
                || dto.getMerchantType() != null
                || dto.getBusinessLicense() != null
                || dto.getUnifiedSocialCreditCode() != null
                || dto.getLegalPerson() != null
                || dto.getLegalPersonId() != null
                || dto.getContactPhone() != null
                || dto.getContactEmail() != null
                || dto.getBusinessScope() != null
                || dto.getBusinessCategory() != null
                || dto.getLogo() != null
                || dto.getBackgroundImage() != null
                || dto.getAlumniAssociationId() != null;
    }

    /**
     * 校验当前用户是否为系统超管，或该商户的「商户管理员」角色持有者
     */
    private void assertUserCanManageMerchant(Long wxId, Long merchantId) {
        if (merchantId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "商户ID不能为空");
        }
        List<Role> userRoles = roleService.getRolesByUserId(wxId);
        boolean isSystemSuperAdmin = userRoles.stream()
                .anyMatch(role -> "SYSTEM_SUPER_ADMIN".equals(role.getRoleCode()));
        if (isSystemSuperAdmin) {
            return;
        }
        Role role = roleService.getRoleByCodeInner("ORGANIZE_MERCHANT_ADMIN");
        if (role == null) {
            throw new BusinessException(ErrorType.NO_AUTH_ERROR, "无权限操作该商户");
        }
        long cnt = roleUserService.lambdaQuery()
                .eq(RoleUser::getWxId, wxId)
                .eq(RoleUser::getRoleId, role.getRoleId())
                .eq(RoleUser::getType, 3)
                .eq(RoleUser::getOrganizeId, merchantId)
                .count();
        if (cnt == 0) {
            throw new BusinessException(ErrorType.NO_AUTH_ERROR, "无权限操作该商户");
        }
    }

    @Override
    public List<MerchantMemberVo> getMerchantMembers(Long merchantId) {
        // 1. 参数校验
        if (merchantId == null) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "商户ID不能为空");
        }

        log.info("查询商户成员列表 - 商户ID: {}", merchantId);

        // 2. 查询商户成员列表（只返回 status = 1 的成员）
        List<MerchantMember> members = merchantMemberMapper.selectList(
                new LambdaQueryWrapper<MerchantMember>()
                        .eq(MerchantMember::getMerchantId, merchantId)
                        .eq(MerchantMember::getStatus, 1)
                        .orderByDesc(MerchantMember::getJoinTime));

        if (members.isEmpty()) {
            log.info("该商户暂无成员 - 商户ID: {}", merchantId);
            return List.of();
        }

        // 3. 转换为 VO，填充用户信息、角色信息和店铺信息
        List<MerchantMemberVo> memberVos = members.stream().map(member -> {
            MerchantMemberVo vo = MerchantMemberVo.builder()
                    .id(member.getId())
                    .wxId(member.getWxId())
                    .merchantId(member.getMerchantId())
                    .shopId(member.getShopId())
                    .roleOrId(member.getRoleOrId())
                    .position(member.getPosition())
                    .joinTime(member.getJoinTime())
                    .status(member.getStatus())
                    .build();

            // 填充用户信息
            if (member.getWxId() != null) {
                WxUserInfo userInfo = wxUserInfoService.getOne(new LambdaQueryWrapper<WxUserInfo>()
                        .eq(WxUserInfo::getWxId, member.getWxId()));
                if (userInfo != null) {
                    vo.setNickname(userInfo.getNickname());
                    vo.setName(userInfo.getName());
                    vo.setAvatarUrl(userInfo.getAvatarUrl());
                    vo.setGender(userInfo.getGender());
                }
            }

            // 填充角色信息
            if (member.getRoleOrId() != null) {
                OrganizeArchiRole role = organizeArchiRoleService.getById(member.getRoleOrId());
                if (role != null) {
                    vo.setRoleName(role.getRoleOrName());
                }
            }

            // 填充店铺信息
            if (member.getShopId() != null) {
                Shop shop = shopService.getById(member.getShopId());
                if (shop != null) {
                    vo.setShopName(shop.getShopName());
                }
            }
            vo.setIsShopAdmin(hasShopAdminRole(member.getWxId(), member.getShopId()));

            return vo;
        }).collect(Collectors.toList());

        log.info("查询商户成员列表成功 - 商户ID: {}, 成员数量: {}", merchantId, memberVos.size());

        return memberVos;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addMerchantMember(AddMerchantMemberDto addDto) {
        // 1. 参数校验
        Optional.ofNullable(addDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        Long merchantId;
        Long wxId;
        try {
            merchantId = Long.parseLong(addDto.getMerchantId().trim());
            wxId = Long.parseLong(addDto.getWxId().trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "商户ID或用户ID格式不正确");
        }

        Long roleOrId = null;
        if (StringUtils.isNotBlank(addDto.getRoleOrId())) {
            try {
                roleOrId = Long.parseLong(addDto.getRoleOrId().trim());
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "角色ID格式不正确");
            }
        }

        Long shopId = null;
        if (StringUtils.isNotBlank(addDto.getShopId())) {
            try {
                shopId = Long.parseLong(addDto.getShopId().trim());
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "店铺ID格式不正确");
            }
        }

        String position = addDto.getPosition() != null ? addDto.getPosition().trim() : "";
        if (StringUtils.isBlank(position)) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "职务不能为空");
        }

        boolean setAsShopAdmin = Boolean.TRUE.equals(addDto.getSetAsShopAdmin());
        if (setAsShopAdmin && shopId == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "设为门店管理员时需先选择店铺");
        }

        log.info("添加商户成员 - 商户ID: {}, 用户ID: {}, 角色ID: {}, 职务: {}, 设为门店管理员: {}",
                merchantId, wxId, roleOrId, position, setAsShopAdmin);

        // 2. 校验商户是否存在且已审核通过
        Merchant merchant = this.getOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getMerchantId, merchantId)
                        .eq(Merchant::getStatus, 1)
                        .eq(Merchant::getReviewStatus, 1));

        if (merchant == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "商户不存在或未审核通过");
        }

        // 3. 校验用户是否存在
        WxUser wxUser = userService.getById(wxId);
        if (wxUser == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "用户不存在");
        }

        // 3.1 若传了店铺 ID，校验店铺属于该商户
        if (shopId != null) {
            Shop shop = shopService.getById(shopId);
            if (shop == null || shop.getMerchantId() == null || !shop.getMerchantId().equals(merchantId)) {
                throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "店铺不存在或不属于该商户");
            }
        }

        // 4. 若传了架构角色 ID，则校验角色是否存在且属于该商户
        if (roleOrId != null) {
            OrganizeArchiRole role = organizeArchiRoleService.getOne(
                    new LambdaQueryWrapper<OrganizeArchiRole>()
                            .eq(OrganizeArchiRole::getRoleOrId, roleOrId)
                            .eq(OrganizeArchiRole::getOrganizeId, merchantId)
                            .eq(OrganizeArchiRole::getOrganizeType, 3) // 3-商户
                            .eq(OrganizeArchiRole::getStatus, 1));

            if (role == null) {
                throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "角色不存在或不属于该商户");
            }
        }

        // 5. 检查用户是否已是该商户的成员
        MerchantMember existingMember = merchantMemberMapper.selectOne(
                new LambdaQueryWrapper<MerchantMember>()
                        .eq(MerchantMember::getWxId, wxId)
                        .eq(MerchantMember::getMerchantId, merchantId)
                        .eq(MerchantMember::getStatus, 1));

        if (existingMember != null) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "该用户已是商户成员");
        }

        // 6. 创建商户成员记录
        MerchantMember merchantMember = new MerchantMember();
        merchantMember.setWxId(wxId);
        merchantMember.setMerchantId(merchantId);
        merchantMember.setShopId(shopId);
        merchantMember.setRoleOrId(roleOrId);
        merchantMember.setPosition(position);
        merchantMember.setJoinTime(LocalDateTime.now());
        merchantMember.setStatus(1);
        merchantMember.setCreateTime(LocalDateTime.now());
        merchantMember.setUpdateTime(LocalDateTime.now());

        int insertResult = merchantMemberMapper.insert(merchantMember);
        if (insertResult <= 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "添加商户成员失败");
        }

        if (setAsShopAdmin) {
            assignShopAdminRole(wxId, shopId);
        }

        log.info("添加商户成员成功 - 商户ID: {}, 用户ID: {}, 角色ID: {}, 职务: {}, 门店管理员: {}",
                merchantId, wxId, roleOrId, position, setAsShopAdmin);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateMerchantMemberRole(UpdateMerchantMemberRoleDto updateDto) {
        // 1. 参数校验
        Optional.ofNullable(updateDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        Long merchantId;
        Long wxId;
        try {
            merchantId = Long.parseLong(updateDto.getMerchantId().trim());
            wxId = Long.parseLong(updateDto.getWxId().trim());
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "商户ID或用户ID格式不正确");
        }

        Long newRoleOrId = null;
        if (StringUtils.isNotBlank(updateDto.getRoleOrId())) {
            try {
                newRoleOrId = Long.parseLong(updateDto.getRoleOrId().trim());
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "角色ID格式不正确");
            }
        }

        Long newShopId = null;
        if (StringUtils.isNotBlank(updateDto.getShopId())) {
            try {
                newShopId = Long.parseLong(updateDto.getShopId().trim());
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorType.ARGS_ERROR, "店铺ID格式不正确");
            }
        }

        String position = updateDto.getPosition() != null ? updateDto.getPosition().trim() : "";
        if (StringUtils.isBlank(position)) {
            throw new BusinessException(ErrorType.ARGS_NOT_NULL, "职务不能为空");
        }
        boolean setAsShopAdmin = Boolean.TRUE.equals(updateDto.getSetAsShopAdmin());

        log.info("更新商户成员 - 商户ID: {}, 用户ID: {}, 新角色ID: {}, 新店铺ID: {}, 职务: {}, 设为门店管理员: {}",
                merchantId, wxId, newRoleOrId, newShopId, position, setAsShopAdmin);

        // 2. 若传了架构角色 ID，则校验角色是否存在且属于该商户
        if (newRoleOrId != null) {
            OrganizeArchiRole newRole = organizeArchiRoleService.getOne(
                    new LambdaQueryWrapper<OrganizeArchiRole>()
                            .eq(OrganizeArchiRole::getRoleOrId, newRoleOrId)
                            .eq(OrganizeArchiRole::getOrganizeId, merchantId)
                            .eq(OrganizeArchiRole::getOrganizeType, 3) // 3-商户
                            .eq(OrganizeArchiRole::getStatus, 1));

            if (newRole == null) {
                throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "角色不存在或不属于该商户");
            }
        }

        // 3. 查询成员记录
        MerchantMember member = merchantMemberMapper.selectOne(
                new LambdaQueryWrapper<MerchantMember>()
                        .eq(MerchantMember::getWxId, wxId)
                        .eq(MerchantMember::getMerchantId, merchantId)
                        .eq(MerchantMember::getStatus, 1));

        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该用户不是商户成员");
        }
        Long oldShopId = member.getShopId();
        boolean wasShopAdmin = hasShopAdminRole(wxId, oldShopId);

        // 4. 若传了店铺 ID，校验店铺属于该商户
        if (newShopId != null) {
            Shop shop = shopService.getById(newShopId);
            if (shop == null || shop.getMerchantId() == null || !shop.getMerchantId().equals(merchantId)) {
                throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "店铺不存在或不属于该商户");
            }
        }
        Long targetShopId = newShopId != null ? newShopId : oldShopId;
        if (setAsShopAdmin && targetShopId == null) {
            throw new BusinessException(ErrorType.ARGS_ERROR, "设为门店管理员时需先选择店铺");
        }

        // 5. 更新职务；架构角色、店铺仅在入参提供时修改
        member.setPosition(position);
        if (newRoleOrId != null) {
            member.setRoleOrId(newRoleOrId);
        }
        if (newShopId != null) {
            member.setShopId(newShopId);
        }
        member.setUpdateTime(LocalDateTime.now());

        int updateResult = merchantMemberMapper.updateById(member);
        if (updateResult <= 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "更新商户成员失败");
        }

        // 6. 同步门店管理员权限（三种情况）
        if (setAsShopAdmin) {
            if (wasShopAdmin && oldShopId != null && targetShopId != null && !oldShopId.equals(targetShopId)) {
                revokeShopAdminRoleIfPresent(wxId, oldShopId);
                assignShopAdminRole(wxId, targetShopId);
            } else if (!wasShopAdmin) {
                assignShopAdminRole(wxId, targetShopId);
            } else if (targetShopId != null) {
                // 避免历史脏数据：如果勾选管理员，则确保目标店权限一定存在
                assignShopAdminRole(wxId, targetShopId);
            }
        } else if (wasShopAdmin) {
            revokeShopAdminRoleIfPresent(wxId, oldShopId);
        }

        log.info("更新商户成员成功 - 商户ID: {}, 用户ID: {}, 角色ID: {}, 店铺ID: {}, 职务: {}, 门店管理员: {}",
                merchantId, wxId, newRoleOrId, targetShopId, position, setAsShopAdmin);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMerchantMember(DeleteMerchantMemberDto deleteDto) {
        // 1. 参数校验
        Optional.ofNullable(deleteDto)
                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

        long merchantId = Long.parseLong(deleteDto.getMerchantId().trim());
        long wxId = Long.parseLong(deleteDto.getWxId().trim());

        log.info("删除商户成员 - 商户ID: {}, 用户ID: {}", merchantId, wxId);

        // 2. 查询成员记录
        MerchantMember member = merchantMemberMapper.selectOne(
                new LambdaQueryWrapper<MerchantMember>()
                        .eq(MerchantMember::getWxId, wxId)
                        .eq(MerchantMember::getMerchantId, merchantId)
                        .eq(MerchantMember::getStatus, 1));

        if (member == null) {
            throw new BusinessException(ErrorType.NOT_FOUND_ERROR, "该用户不是商户成员");
        }

        revokeShopAdminRolesByMerchant(wxId, merchantId);

        // 3. 更新状态为退出（软删除）
        member.setStatus(0); // 0-退出
        member.setUpdateTime(LocalDateTime.now());

        int updateResult = merchantMemberMapper.updateById(member);
        if (updateResult <= 0) {
            throw new BusinessException(ErrorType.OPERATION_ERROR, "删除商户成员失败");
        }

        log.info("删除商户成员成功 - 商户ID: {}, 用户ID: {}", merchantId, wxId);

        return true;
    }
}
