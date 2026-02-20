package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.QueryNearbyDto;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.vo.NearbyAlumniVo;
import com.cmswe.alumni.common.vo.PageVo;

public interface WxUserInfoService extends IService<WxUserInfo> {

    /**
     * 根据地理位置查询附近校友（分页）
     *
     * @param queryDto 查询参数（包含经纬度、半径、分页等）
     * @return 附近校友分页列表
     * @deprecated 请使用 {@link #getNearbyAlumni(QueryNearbyDto, Long)} 以获取关注状态
     */
    @Deprecated
    PageVo<NearbyAlumniVo> getNearbyAlumni(QueryNearbyDto queryDto);

    /**
     * 根据地理位置查询附近校友（分页，包含关注状态）
     *
     * @param queryDto      查询参数（包含经纬度、半径、分页等）
     * @param currentUserId 当前用户ID（用于查询关注状态，可为null）
     * @return 附近校友分页列表（包含关注状态）
     */
    PageVo<NearbyAlumniVo> getNearbyAlumni(QueryNearbyDto queryDto, Long currentUserId);
}
