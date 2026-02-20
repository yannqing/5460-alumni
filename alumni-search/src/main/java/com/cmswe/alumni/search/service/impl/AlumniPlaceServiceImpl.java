package com.cmswe.alumni.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.search.AlumniPlaceService;
import com.cmswe.alumni.common.dto.QueryNearbyDto;
import com.cmswe.alumni.common.dto.UpdateAlumniPlaceDto;
import com.cmswe.alumni.common.entity.AlumniPlace;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.AlumniPlaceListVo;
import com.cmswe.alumni.common.vo.NearbyPlaceVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.search.mapper.AlumniPlaceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 校友企业/场所服务实现
 *
 * @author CNI Alumni System
 * @since 2025-12-26
 */
@Slf4j
@Service("alumniPlaceService")
public class AlumniPlaceServiceImpl extends ServiceImpl<AlumniPlaceMapper, AlumniPlace> implements AlumniPlaceService {

        @Override
        public PageVo<NearbyPlaceVo> getNearbyPlaces(QueryNearbyDto queryDto) {
                // 1. 参数校验
                Optional.ofNullable(queryDto)
                                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL));

                if (queryDto.getLatitude() == null || queryDto.getLongitude() == null) {
                        throw new BusinessException("经纬度不能为空");
                }

                // 2. 获取查询参数
                int current = queryDto.getCurrent();
                int pageSize = queryDto.getPageSize();
                int offset = (current - 1) * pageSize;
                Integer radius = queryDto.getRadius();
                Integer placeType = queryDto.getPlaceType();
                String placeName = queryDto.getName();
                Integer isRecommended = queryDto.getIsRecommended();

                // 3. 执行分页查询（只查询正常运营且审核通过的企业/场所）
                List<NearbyPlaceVo> placeList = this.baseMapper.selectNearbyWithPage(
                                queryDto.getLatitude(),
                                queryDto.getLongitude(),
                                radius,
                                placeType,
                                placeName,
                                isRecommended,
                                offset,
                                pageSize);

                // 4. 查询总数
                Long total = this.baseMapper.countNearbyPlaces(
                                queryDto.getLatitude(),
                                queryDto.getLongitude(),
                                radius,
                                placeType,
                                placeName,
                                isRecommended);

                log.info("查询附近{}km校友企业/场所，位置：[{}, {}]，类型：{}，找到{}个结果",
                                radius, queryDto.getLatitude(), queryDto.getLongitude(),
                                placeType == null ? "全部" : (placeType == 1 ? "企业" : "场所"), total);

                // 5. 构建分页结果
                Page<NearbyPlaceVo> page = new Page<>(current, pageSize, total);
                page.setRecords(placeList);

                return PageVo.of(page);
        }

        @Override
        public AlumniPlaceListVo getPlaceDetail(Long placeId) {
                log.info("查询企业/场所详情，ID: {}", placeId);

                // 1. 参数校验
                Optional.ofNullable(placeId)
                                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "企业/场所ID不能为空"));

                // 2. 查询企业/场所信息
                AlumniPlace alumniPlace = this.getById(placeId);
                if (alumniPlace == null) {
                        throw new BusinessException("企业/场所不存在");
                }

                // 3. 转换为 VO
                AlumniPlaceListVo vo = AlumniPlaceListVo.objToVo(alumniPlace);

                log.info("查询企业/场所详情成功，名称: {}", alumniPlace.getPlaceName());

                return vo;
        }

        @Override
        @Transactional(rollbackFor = Exception.class)
        public boolean updatePlaceInfo(UpdateAlumniPlaceDto updateDto) {
                log.info("管理员更新企业/场所基本信息，企业/场所 ID: {}", updateDto.getPlaceId());

                // 1. 参数校验
                Optional.ofNullable(updateDto)
                                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "更新参数不能为空"));
                Optional.ofNullable(updateDto.getPlaceId())
                                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "企业/场所ID不能为空"));

                // 2. 查询企业/场所是否存在
                AlumniPlace alumniPlace = this.getById(updateDto.getPlaceId());
                if (alumniPlace == null) {
                        throw new BusinessException("企业/场所不存在");
                }

                // 3. 更新字段（只更新非空字段）
                if (updateDto.getPlaceName() != null) {
                        alumniPlace.setPlaceName(updateDto.getPlaceName());
                }
                if (updateDto.getPlaceType() != null) {
                        alumniPlace.setPlaceType(updateDto.getPlaceType());
                }
                if (updateDto.getAlumniAssociationId() != null) {
                        alumniPlace.setAlumniAssociationId(updateDto.getAlumniAssociationId());
                }
                if (updateDto.getProvince() != null) {
                        alumniPlace.setProvince(updateDto.getProvince());
                }
                if (updateDto.getCity() != null) {
                        alumniPlace.setCity(updateDto.getCity());
                }
                if (updateDto.getDistrict() != null) {
                        alumniPlace.setDistrict(updateDto.getDistrict());
                }
                if (updateDto.getAddress() != null) {
                        alumniPlace.setAddress(updateDto.getAddress());
                }
                if (updateDto.getLatitude() != null) {
                        alumniPlace.setLatitude(updateDto.getLatitude());
                }
                if (updateDto.getLongitude() != null) {
                        alumniPlace.setLongitude(updateDto.getLongitude());
                }
                if (updateDto.getContactPhone() != null) {
                        alumniPlace.setContactPhone(updateDto.getContactPhone());
                }
                if (updateDto.getContactEmail() != null) {
                        alumniPlace.setContactEmail(updateDto.getContactEmail());
                }
                if (updateDto.getBusinessHours() != null) {
                        alumniPlace.setBusinessHours(updateDto.getBusinessHours());
                }
                if (updateDto.getImages() != null) {
                        alumniPlace.setImages(updateDto.getImages());
                }
                if (updateDto.getLogo() != null) {
                        alumniPlace.setLogo(updateDto.getLogo());
                }
                if (updateDto.getDescription() != null) {
                        alumniPlace.setDescription(updateDto.getDescription());
                }
                if (updateDto.getEstablishedTime() != null) {
                        alumniPlace.setEstablishedTime(updateDto.getEstablishedTime());
                }
                if (updateDto.getStatus() != null) {
                        alumniPlace.setStatus(updateDto.getStatus());
                }
                if (updateDto.getIsRecommended() != null) {
                        alumniPlace.setIsRecommended(updateDto.getIsRecommended());
                }

                alumniPlace.setUpdateTime(LocalDateTime.now());

                // 4. 执行更新
                boolean updateResult = this.updateById(alumniPlace);

                if (updateResult) {
                        log.info("管理员更新企业/场所基本信息成功，企业/场所 ID: {}, 名称: {}",
                                        updateDto.getPlaceId(), alumniPlace.getPlaceName());
                } else {
                        log.error("管理员更新企业/场所基本信息失败，企业/场所 ID: {}", updateDto.getPlaceId());
                        throw new BusinessException("更新企业/场所信息失败，请重试");
                }

                return updateResult;
        }

        @Override
        public List<AlumniPlaceListVo> getMyPlaceList(Long wxId) {
                log.info("查询用户个人的企业/场所列表，用户 ID: {}", wxId);

                // 1. 参数校验
                Optional.ofNullable(wxId)
                                .orElseThrow(() -> new BusinessException(ErrorType.ARGS_NOT_NULL, "用户ID不能为空"));

                // 2. 查询用户创建的企业/场所（只查询审核通过的）
                LambdaQueryWrapper<AlumniPlace> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper
                                .eq(AlumniPlace::getAlumniId, wxId)
                                .eq(AlumniPlace::getReviewStatus, 1) // 审核状态：1-审核通过
                                .orderByDesc(AlumniPlace::getCreateTime);

                List<AlumniPlace> placeList = this.list(queryWrapper);

                // 3. 转换为 VO
                List<AlumniPlaceListVo> voList = placeList.stream()
                                .map(AlumniPlaceListVo::objToVo)
                                .collect(Collectors.toList());

                log.info("查询用户个人的企业/场所列表成功，用户 ID: {}, 找到 {} 个企业/场所", wxId, voList.size());

                return voList;
        }
}
