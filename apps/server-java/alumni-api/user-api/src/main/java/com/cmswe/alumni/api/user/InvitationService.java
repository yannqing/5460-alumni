package com.cmswe.alumni.api.user;

import com.cmswe.alumni.common.dto.ConfirmInvitationDto;
import com.cmswe.alumni.common.vo.InvitationMyListVo;
import com.cmswe.alumni.common.vo.InvitationRankVo;
import com.cmswe.alumni.common.vo.InvitationRecordItemVo;
import com.cmswe.alumni.common.vo.InviteeRegisterCheckVo;
import com.cmswe.alumni.common.vo.PosterTemplateItemVo;

import java.util.List;

/**
 * 邀请服务接口
 */
public interface InvitationService {

    /**
     * 确认邀请
     * 1. 写入邀请记录表
     * 2. 邀请人积分+1
     * 3. 记录积分变化
     *
     * @param dto 邀请人和被邀请人wxid
     * @return 是否成功
     */
    boolean confirmInvitation(ConfirmInvitationDto dto);

    /**
     * 检查被邀请人是否已注册
     * 入参 wxid：若在邀请记录表中作为被邀请人存在，则用 wxid 查 wx_user_info 的 nickname、name；
     * 若均已填写，则将对应邀请记录的 is_register 更新为 1。
     *
     * @param wxId 被邀请人 wxid
     * @return 是否为被邀请用户、是否已注册
     */
    InviteeRegisterCheckVo checkInviteeRegistration(String wxId);

    /**
     * 查看自己的邀请列表（作为邀请人）
     *
     * @param inviterWxId 邀请人 wxid
     * @return 邀请人数、排名、邀请记录列表
     */
    InvitationMyListVo getMyInvitationList(String inviterWxId);

    /**
     * 查看邀请排行榜
     * 1. 自己的邀请人数及排名 2. 全部邀请排行（wxid、排行、邀请人数、头像、姓名、学校）
     *
     * @param myWxId 当前用户 wxid（用于展示“我的”人数与排名，可为 null）
     * @return 我的统计 + 完整排行榜列表
     */
    InvitationRankVo getInvitationRank(String myWxId);

    /**
     * 展示邀请模版列表（type=0 邀请模板），仅返回 id、url
     *
     * @return 模版列表
     */
    List<PosterTemplateItemVo> getInvitationPosterTemplates();
}
