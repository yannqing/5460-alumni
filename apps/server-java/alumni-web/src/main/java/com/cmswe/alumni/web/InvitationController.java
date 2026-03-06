package com.cmswe.alumni.web;

import com.cmswe.alumni.api.user.InvitationService;
import com.cmswe.alumni.auth.SecurityUser;
import com.cmswe.alumni.common.constant.Code;
import com.cmswe.alumni.common.dto.ConfirmInvitationDto;
import com.cmswe.alumni.common.utils.BaseResponse;
import com.cmswe.alumni.common.utils.ResultUtils;
import com.cmswe.alumni.common.utils.WechatMiniUtil;
import com.cmswe.alumni.common.vo.InvitationMyListVo;
import com.cmswe.alumni.common.vo.InvitationQrVo;
import com.cmswe.alumni.common.vo.InvitationRankVo;
import com.cmswe.alumni.common.vo.InvitationRecordItemVo;
import com.cmswe.alumni.common.vo.InviteeRegisterCheckVo;
import com.cmswe.alumni.common.vo.PosterTemplateItemVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 邀请相关接口
 */
@Tag(name = "邀请")
@RestController
@RequestMapping("/invitation")
public class InvitationController {

    @Resource
    private WechatMiniUtil wechatMiniUtil;

    @Resource
    private InvitationService invitationService;

    @Value("${wechat.mini.app-id}")
    private String appId;

    /**
     * 生成邀请二维码
     * 二维码为微信小程序码，扫码后打开小程序首页并带上邀请人wxid（scene参数）
     * 小程序 app.js onShow 中会根据 query.scene 调用 login(scene) 完成邀请绑定
     *
     * @param securityUser 当前登录用户（邀请人）
     * @return 包含 Base64 二维码、wxid、appId 的响应
     */
    @GetMapping("/qrcode")
    @Operation(summary = "生成邀请二维码")
    public BaseResponse<InvitationQrVo> generateInvitationQrcode(
            @AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null || securityUser.getWxUser() == null) {
            return ResultUtils.failure(Code.FAILURE, null, "请先登录");
        }

        Long wxId = securityUser.getWxUser().getWxId();
        // scene 最大32字符，wxId 转字符串传入，扫码后小程序可通过 query.scene 获取
        String scene = String.valueOf(wxId);
        String page = "pages/index/index";
        int width = 280;

        String qrCodeBase64 = wechatMiniUtil.createWxaCodeUnlimit(scene, page, width);

        InvitationQrVo vo = InvitationQrVo.builder()
                .wxId(wxId)
                .appId(appId)
                .qrCodeBase64(qrCodeBase64)
                .build();

        return ResultUtils.success(Code.SUCCESS, vo, "生成成功");
    }

    /**
     * 确认邀请
     * 首次登录后，被邀请人携带 inviterWxId、inviteeWxId 调用此接口完成邀请确认。
     * 会：1. 写入邀请记录表 2. 邀请人积分+1 3. 记录积分变化
     *
     * @param dto          邀请人wxid、被邀请人wxid
     * @param securityUser 当前登录用户（需为被邀请人本人）
     */
    @PostMapping("/confirm")
    @Operation(summary = "确认邀请")
    public BaseResponse<Boolean> confirmInvitation(
            @Valid @RequestBody ConfirmInvitationDto dto,
            @AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null || securityUser.getWxUser() == null) {
            return ResultUtils.failure(Code.FAILURE, null, "请先登录");
        }
        Long currentWxId = securityUser.getWxUser().getWxId();
        if (!String.valueOf(currentWxId).equals(dto.getInviteeWxId())) {
            return ResultUtils.failure(Code.FAILURE, null, "仅被邀请人本人可确认邀请");
        }
        boolean success = invitationService.confirmInvitation(dto);
        return ResultUtils.success(Code.SUCCESS, success, "确认成功");
    }

    /**
     * 检查被邀请人是否已注册
     * 入参 wxid：若在邀请记录表中作为被邀请人存在，则查 wx_user_info 的 nickname、name 是否填写；
     * 若均已填写，则将对应邀请记录的 is_register 更新为 1（已注册）。
     *
     * @param wxId 被邀请人 wxid
     */
    @GetMapping("/check-register")
    @Operation(summary = "检查被邀请人是否已注册")
    public BaseResponse<InviteeRegisterCheckVo> checkInviteeRegister(@RequestParam String wxId) {
        InviteeRegisterCheckVo vo = invitationService.checkInviteeRegistration(wxId);
        return ResultUtils.success(Code.SUCCESS, vo, "查询成功");
    }

    /**
     * 查看自己的邀请列表（入参为邀请人 wxid）
     *
     * @param wxId 邀请人 wxid
     */
    @GetMapping("/my-list")
    @Operation(summary = "查看自己的邀请列表")
    public BaseResponse<InvitationMyListVo> getMyInvitationList(@RequestParam String wxId) {
        InvitationMyListVo vo = invitationService.getMyInvitationList(wxId);
        return ResultUtils.success(Code.SUCCESS, vo, "查询成功");
    }

    /**
     * 查看邀请排行榜
     * 1. 自己的邀请人数及排名  2. 全部邀请排行（wxid、排行、邀请人数、头像、姓名、学校）
     *
     * @param wxId 当前用户 wxid（用于展示“我的”人数与排名，不传则仅返回排行榜）
     */
    @GetMapping("/rank")
    @Operation(summary = "查看邀请排行榜")
    public BaseResponse<InvitationRankVo> getInvitationRank(@RequestParam(required = false) String wxId) {
        InvitationRankVo vo = invitationService.getInvitationRank(wxId);
        return ResultUtils.success(Code.SUCCESS, vo, "查询成功");
    }

    /**
     * 展示邀请模版列表（无入参，查询 type=0 邀请模板，输出 id、url）
     */
    @GetMapping("/poster-templates")
    @Operation(summary = "展示邀请模版列表")
    public BaseResponse<List<PosterTemplateItemVo>> getInvitationPosterTemplates() {
        List<PosterTemplateItemVo> list = invitationService.getInvitationPosterTemplates();
        return ResultUtils.success(Code.SUCCESS, list, "查询成功");
    }
}
