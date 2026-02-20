package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.MessageDeadLetter;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 消息死信队列 Mapper
 *
 * @author CMSWE
 * @since 2025-12-09
 */
@Mapper
public interface MessageDeadLetterMapper extends BaseMapper<MessageDeadLetter> {

    /**
     * 查询未处理的死信消息
     *
     * @param limit 限制数量
     * @return 死信消息列表
     */
    List<MessageDeadLetter> selectUnprocessed(@Param("limit") Integer limit);

    /**
     * 查询指定消息类别的死信消息
     *
     * @param messageCategory 消息类别
     * @param processStatus   处理状态
     * @return 死信消息列表
     */
    List<MessageDeadLetter> selectByCategory(@Param("messageCategory") String messageCategory,
                                              @Param("processStatus") Integer processStatus);

    /**
     * 更新死信消息处理状态
     *
     * @param id            死信消息ID
     * @param processStatus 处理状态
     * @param processResult 处理结果
     * @return 影响行数
     */
    int updateProcessStatus(@Param("id") Long id,
                            @Param("processStatus") Integer processStatus,
                            @Param("processResult") String processResult);

    /**
     * 增加重试次数
     *
     * @param id 死信消息ID
     * @return 影响行数
     */
    int incrementRetryCount(@Param("id") Long id);

    /**
     * 按失败原因分组统计
     *
     * @param processStatus 处理状态（null表示所有状态）
     * @return 统计结果列表
     */
    List<MessageDeadLetter> countByFailureReason(@Param("processStatus") Integer processStatus);
}
