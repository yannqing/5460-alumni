package com.cmswe.alumni.api.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cmswe.alumni.common.dto.QueryAlumniListDto;
import com.cmswe.alumni.common.dto.UpdateUserInfoDto;
import com.cmswe.alumni.common.dto.UpdateUserPrivacySettingsRequest;
import com.cmswe.alumni.common.dto.UpdateUserTagsDto;
import com.cmswe.alumni.common.entity.WxUser;
import com.cmswe.alumni.common.vo.UserDetailVo;
import com.cmswe.alumni.common.vo.UserListResponse;
import com.cmswe.alumni.common.vo.UserPrivacySettingListVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户服务接口
 * @author yanqing
 * @description 用户相关业务操作接口定义，可用于 Feign 调用
 */
public interface UserService extends IService<WxUser> {
    
    /**
     * 更新用户个人信息
     * @param wxId 用户ID
     * @param updateDto 更新信息DTO
     * @return 是否更新成功
     */

    boolean updateUserInfo(Long wxId, UpdateUserInfoDto updateDto) throws JsonProcessingException;

    /**
     * 根据用户token获取用户信息
     * @param wxId 用户ID
     * @return 用户信息
     */
    UserDetailVo getUserById(Long wxId) throws JsonProcessingException;

    /**
     * 生成测试用的7天有效期token
     * @param userId 用户ID
     * @return token信息Map
     */
    java.util.Map<String, Object> generateTestToken(Long userId);

    /**
     * 根据用户token获取用户隐私设置
     * @param wxId 用户ID
     * @return 用户隐私设置
     */
    List<UserPrivacySettingListVo> getUserPrivacy(Long wxId) throws JsonProcessingException;

    /**
     * 根据 id 获取校友信息
     *
     * @param id 校友用户ID
     * @return
     */
    UserDetailVo getAlumniInfo(Long id);

    /**
     * 根据 id 获取校友信息（包含关注状态和校友认证状态）
     *
     * @param id 校友用户ID
     * @param currentUserId 当前登录用户ID（可为null，为null时不查询关注状态）
     * @return 校友详情VO
     */
    com.cmswe.alumni.common.vo.AlumniDetailVo getAlumniInfoWithStatus(Long id, Long currentUserId);

    /**
     * 获取校友列表
     *
     * @param queryAlumniListDto 查询条件
     */
    Page<UserListResponse> queryAlumniList(QueryAlumniListDto queryAlumniListDto);

    /**
     * 更新用户隐私设置
     *
     * @param wxId                             用户id
     * @param updateUserPrivacySettingsRequest 更新请求
     * @return 返回更新结果
     */
    boolean updateUserPrivacy(Long wxId, UpdateUserPrivacySettingsRequest updateUserPrivacySettingsRequest);

    /**
     * 更新用户个人标签
     *
     * @param wxId            用户id
     * @param updateUserTagsDto 更新标签请求
     * @return 返回更新结果
     */
    boolean updateUserTags(Long wxId, UpdateUserTagsDto updateUserTagsDto);

    /**
     * 用户上线
     * @param token
     * @return
     */
    String onlineByToken(String token) throws JsonProcessingException;

    /**
     * 用户下线
     * @param userId
     */
    void offline(String userId);
}