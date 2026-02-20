package com.cmswe.alumni.service.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cmswe.alumni.api.user.WxUserInfoService;
import com.cmswe.alumni.common.dto.QueryNearbyDto;
import com.cmswe.alumni.common.entity.WxUserInfo;
import com.cmswe.alumni.common.enums.ErrorType;
import com.cmswe.alumni.common.exception.BusinessException;
import com.cmswe.alumni.common.vo.NearbyAlumniVo;
import com.cmswe.alumni.common.vo.PageVo;
import com.cmswe.alumni.service.user.mapper.WxUserInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class WxUserInfoServiceImpl extends ServiceImpl<WxUserInfoMapper, WxUserInfo> implements WxUserInfoService {

    @Override
    @Deprecated
    public PageVo<NearbyAlumniVo> getNearbyAlumni(QueryNearbyDto queryDto) {
        // 调用新方法，不传当前用户ID（不显示关注状态）
        return getNearbyAlumni(queryDto, null);
    }

    @Override
    public PageVo<NearbyAlumniVo> getNearbyAlumni(QueryNearbyDto queryDto, Long currentUserId) {
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
        String name = queryDto.getName();

        // 3. 执行分页查询（包含关注状态）
        List<NearbyAlumniVo> alumniList = this.baseMapper.selectNearbyWithPage(
                queryDto.getLatitude(),
                queryDto.getLongitude(),
                radius,
                name,
                currentUserId,
                offset,
                pageSize
        );

        // 4. 查询总数
        Long total = this.baseMapper.countNearbyAlumni(
                queryDto.getLatitude(),
                queryDto.getLongitude(),
                radius,
                name,
                currentUserId
        );

        log.info("查询附近{}km校友，位置：[{}, {}]，当前用户：{}，找到{}个结果",
                radius, queryDto.getLatitude(), queryDto.getLongitude(), currentUserId, total);

        // 5. 构建分页结果
        Page<NearbyAlumniVo> page = new Page<>(current, pageSize, total);
        page.setRecords(alumniList);

        return PageVo.of(page);
    }
}
