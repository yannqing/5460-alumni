package com.cmswe.alumni.api.search;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.QueryNearbyDto;
import com.cmswe.alumni.common.dto.UpdateAlumniPlaceDto;
import com.cmswe.alumni.common.entity.AlumniPlace;
import com.cmswe.alumni.common.vo.AlumniPlaceListVo;
import com.cmswe.alumni.common.vo.NearbyPlaceVo;
import com.cmswe.alumni.common.vo.PageVo;

import java.util.List;

/**
 * 校友企业/场所服务接口
 *
 * @author CNI Alumni System
 * @since 2025-12-26
 */
public interface AlumniPlaceService extends IService<AlumniPlace> {

    /**
     * 根据地理位置查询附近企业/场所（分页）
     *
     * @param queryDto 查询参数（包含经纬度、半径、分页等）
     * @return 附近企业/场所分页列表
     */
    PageVo<NearbyPlaceVo> getNearbyPlaces(QueryNearbyDto queryDto);

    /**
     * 根据ID查询企业/场所详情
     *
     * @param placeId 企业/场所ID
     * @return 企业/场所详情
     */
    AlumniPlaceListVo getPlaceDetail(Long placeId);

    /**
     * 管理员更新企业/场所基本信息
     *
     * @param updateDto 更新参数
     * @return 是否更新成功
     */
    boolean updatePlaceInfo(UpdateAlumniPlaceDto updateDto);

    /**
     * 获取用户个人的企业/场所列表
     *
     * @param wxId 用户ID
     * @return 用户的企业/场所列表
     */
    List<AlumniPlaceListVo> getMyPlaceList(Long wxId);
}
