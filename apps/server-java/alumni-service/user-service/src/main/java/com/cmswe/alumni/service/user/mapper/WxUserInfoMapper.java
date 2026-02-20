package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.vo.NearbyAlumniVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yanqing
 * @description 针对表【wx_user_info(用户基本信息表)】的数据库操作Mapper
 * @Entity com.cmswe.alumni.common.entity.WxUserInfo
 */
@Mapper
public interface WxUserInfoMapper extends BaseMapper<WxUserInfo> {

    /**
     * 根据 wxId 查询用户信息
     * @param wxId 用户ID
     * @return 用户信息
     */
    @Select("SELECT * FROM wx_user_info WHERE wx_id = #{wxId} AND is_delete = 0")
    WxUserInfo findByWxId(@Param("wxId") Long wxId);

    /**
     * 根据手机号查询用户信息
     * @param phone 手机号
     * @return 用户信息
     */
    @Select("SELECT * FROM wx_user_info WHERE phone = #{phone} AND is_delete = 0")
    WxUserInfo findByPhone(@Param("phone") String phone);

    /**
     * 根据证件号查询用户信息
     * @param identifyCode 证件号
     * @return 用户信息
     */
    @Select("SELECT * FROM wx_user_info WHERE identify_code = #{identifyCode} AND is_delete = 0")
    WxUserInfo findByIdentifyCode(@Param("identifyCode") String identifyCode);

    /**
     * 根据地理位置分页查询附近校友（带距离计算、关注状态和条件筛选）
     *
     * @param latitude     纬度
     * @param longitude    经度
     * @param radius       半径（公里）
     * @param name         校友名称（可选）
     * @param currentUserId 当前用户ID（用于查询关注状态）
     * @param offset       分页偏移量
     * @param pageSize     每页数量
     * @return 校友列表（包含距离和关注状态）
     */
    List<NearbyAlumniVo> selectNearbyWithPage(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") Integer radius,
            @Param("name") String name,
            @Param("currentUserId") Long currentUserId,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    /**
     * 统计附近校友总数（用于分页）
     *
     * @param latitude     纬度
     * @param longitude    经度
     * @param radius       半径（公里）
     * @param name         校友名称（可选）
     * @param currentUserId 当前用户ID（排除自己）
     * @return 总数
     */
    Long countNearbyAlumni(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") Integer radius,
            @Param("name") String name,
            @Param("currentUserId") Long currentUserId
    );
}
