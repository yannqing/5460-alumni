package com.cmswe.alumni.web.merchant;

import com.cmswe.alumni.api.system.MerchantService;
import com.cmswe.alumni.api.user.OrganizeArchiRoleService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.*;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.vo.MerchantApprovalVo;
import com.cmswe.alumni.common.vo.MerchantListVo;
import com.cmswe.alumni.common.vo.OrganizeArchiRoleVo;
import com.cmswe.alumni.common.vo.PageVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户管理后台 Controller
 * 用于管理员管理商户
 *
 * @author CNI Alumni System
 */
@Tag(name = "商户后台管理", description = "商户后台管理相关接口（管理员）")
@Slf4j
@RestController
@RequestMapping("/merchant-management")
public class MerchantManagementController {

    @Resource
    private MerchantService merchantService;

    @Resource
    private OrganizeArchiRoleService organizeArchiRoleService;

    /**
     * 管理员审批商户入驻申请
     *
     * @param securityUser 当前登录的管理员
     * @param approveDto   审批信息
     * @return 审批结果
     */
    @PostMapping("/approve")
    @Operation(summary = "审批商户入驻申请")
    public BaseResponse<Boolean> approveMerchant(
            @AuthenticationPrincipal SecurityUser securityUser,
            @Valid @RequestBody ApproveMerchantDto approveDto) {
        Long reviewerId = securityUser.getWxUser().getWxId();

        log.info("管理员审批商户入驻申请 - 审核人ID: {}, 商户ID: {}, 审核状态: {}",
                reviewerId, approveDto.getMerchantId(), approveDto.getReviewStatus());

        boolean result = merchantService.approveMerchant(reviewerId, approveDto);

        if (result) {
            String message = approveDto.getReviewStatus() == 1 ? "审核通过" : "审核驳回";
            log.info("商户审批成功 - 审核人ID: {}, 商户ID: {}, 结果: {}",
                    reviewerId, approveDto.getMerchantId(), message);
            return ResultUtils.success(Code.SUCCESS, true, message + "成功");
        } else {
            log.error("商户审批失败 - 审核人ID: {}, 商户ID: {}",
                    reviewerId, approveDto.getMerchantId());
            return ResultUtils.failure(Code.FAILURE, false, "审批失败");
        }
    }

    /**
     * 分页查询商户审批记录
     *
     * @param queryDto 查询条件
     * @return 审批记录列表
     */
    @GetMapping("/approval/records")
    @Operation(summary = "分页查询商户审批记录")
    public BaseResponse<PageVo<MerchantApprovalVo>> listApprovalRecords(@Valid QueryMerchantApprovalDto queryDto) {
        log.info("管理员查询商户审批记录 - 条件: {}", queryDto);
        PageVo<MerchantApprovalVo> result = merchantService.selectApprovalRecordsByPage(queryDto);
        return ResultUtils.success(result);
    }

    /**
     * 查询本人负责的商户列表
     *
     * @param securityUser 当前登录用户
     * @param current      当前页（默认1）
     * @param size         每页大小（默认10）
     * @return 商户列表分页数据
     */
    @GetMapping("/my-merchants")
    @Operation(summary = "查询本人负责的商户列表")
    public BaseResponse<PageVo<MerchantListVo>> getMyManagedMerchants(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        Long wxId = securityUser.getWxUser().getWxId();
        log.info("查询本人负责的商户列表 - 用户ID: {}, 当前页: {}, 每页大小: {}",
                wxId, current, size);

        PageVo<MerchantListVo> pageVo = merchantService.getMyManagedMerchants(wxId, current, size);

        log.info("查询本人负责的商户列表成功 - 用户ID: {}, 总记录数: {}",
                wxId, pageVo.getTotal());

        return ResultUtils.success(Code.SUCCESS, pageVo, "查询成功");
    }

    /**
     * 新增商户组织架构角色
     *
     * @param addDto 新增请求参数
     * @return 返回新增结果
     */
    @PostMapping("/role/add")
    @Operation(summary = "新增商户组织架构角色")
    public BaseResponse<Boolean> addOrganizeArchiRole(@Valid @RequestBody AddOrganizeArchiRoleDto addDto) {
        log.info("新增商户组织架构角色，组织 ID: {}, 角色名: {}", addDto.getOrganizeId(), addDto.getRoleOrName());

        // 后端指定组织类型为商户（2）
        addDto.setOrganizeType(2);

        boolean result = organizeArchiRoleService.addOrganizeArchiRole(addDto);

        log.info("新增商户组织架构角色成功，组织 ID: {}, 角色名: {}", addDto.getOrganizeId(), addDto.getRoleOrName());
        return ResultUtils.success(Code.SUCCESS, result, "新增成功");
    }

    /**
     * 更新商户组织架构角色
     *
     * @param updateDto 更新请求参数
     * @return 返回更新结果
     */
    @PutMapping("/role/update")
    @Operation(summary = "更新商户组织架构角色")
    public BaseResponse<Boolean> updateOrganizeArchiRole(@Valid @RequestBody UpdateOrganizeArchiRoleDto updateDto) {
        log.info("更新商户组织架构角色，角色 ID: {}, 组织 ID: {}", updateDto.getRoleOrId(), updateDto.getOrganizeId());

        boolean result = organizeArchiRoleService.updateOrganizeArchiRole(updateDto);

        log.info("更新商户组织架构角色成功，角色 ID: {}, 组织 ID: {}", updateDto.getRoleOrId(), updateDto.getOrganizeId());
        return ResultUtils.success(Code.SUCCESS, result, "更新成功");
    }

    /**
     * 删除商户组织架构角色
     *
     * @param deleteDto 删除请求参数
     * @return 返回删除结果
     */
    @DeleteMapping("/role/delete")
    @Operation(summary = "删除商户组织架构角色")
    public BaseResponse<Boolean> deleteOrganizeArchiRole(@Valid @RequestBody DeleteOrganizeArchiRoleDto deleteDto) {
        log.info("删除商户组织架构角色，角色 ID: {}, 组织 ID: {}", deleteDto.getRoleOrId(), deleteDto.getOrganizeId());

        boolean result = organizeArchiRoleService.deleteOrganizeArchiRole(
                deleteDto.getRoleOrId(),
                deleteDto.getOrganizeId());

        log.info("删除商户组织架构角色成功，角色 ID: {}, 组织 ID: {}", deleteDto.getRoleOrId(), deleteDto.getOrganizeId());
        return ResultUtils.success(Code.SUCCESS, result, "删除成功");
    }

    /**
     * 查询商户组织架构角色列表（树形结构）
     *
     * @param queryDto 查询请求参数
     * @return 返回角色树形列表
     */
    @PostMapping("/role/list")
    @Operation(summary = "查询商户组织架构角色列表（树形结构）")
    public BaseResponse<List<OrganizeArchiRoleVo>> getOrganizeArchiRoleList(
            @Valid @RequestBody QueryOrganizeArchiRoleListDto queryDto) {
        log.info("查询商户组织架构角色树，组织 ID: {}", queryDto.getOrganizeId());

        // 后端指定组织类型为商户（2）
        queryDto.setOrganizeType(2);

        List<OrganizeArchiRoleVo> roleTree = organizeArchiRoleService.getOrganizeArchiRoleTree(
                queryDto.getOrganizeId(),
                queryDto.getOrganizeType(),
                queryDto.getRoleOrName(),
                queryDto.getStatus());

        log.info("查询商户组织架构角色树成功，组织 ID: {}, 根节点数: {}", queryDto.getOrganizeId(), roleTree.size());
        return ResultUtils.success(Code.SUCCESS, roleTree, "查询成功");
    }
}
