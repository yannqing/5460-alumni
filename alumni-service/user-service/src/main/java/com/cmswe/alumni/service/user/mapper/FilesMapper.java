package com.cmswe.alumni.service.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cmswe.alumni.common.entity.Files;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author yanqing
 * @description 针对表【files(文件表)】的数据库操作Mapper
 * @Entity com.cmswe.alumni.common.entity.Files
 */
@Mapper
public interface FilesMapper extends BaseMapper<Files> {

    /**
     * 根据文件ID查询文件信息
     * @param fileId 文件ID
     * @return 文件信息
     */
    @Select("SELECT * FROM files WHERE file_id = #{fileId} AND is_deleted = 0")
    Files findByFileId(@Param("fileId") Long fileId);

    /**
     * 根据用户ID查询文件列表
     * @param wxId 用户ID
     * @return 文件列表
     */
    @Select("SELECT * FROM files WHERE wx_id = #{wxId} AND is_deleted = 0 ORDER BY created_time DESC")
    List<Files> findByWxId(@Param("wxId") Long wxId);

    /**
     * 根据会话ID查询文件列表
     * @param sessionId 会话ID
     * @return 文件列表
     */
    @Select("SELECT * FROM files WHERE session_id = #{sessionId} AND is_deleted = 0 ORDER BY created_time DESC")
    List<Files> findBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据文件MD5查询文件（用于去重）
     * @param fileMd5 文件MD5
     * @return 文件信息
     */
    @Select("SELECT * FROM files WHERE file_md5 = #{fileMd5} AND is_deleted = 0 LIMIT 1")
    Files findByFileMd5(@Param("fileMd5") String fileMd5);

    /**
     * 增加访问次数
     * @param fileId 文件ID
     * @return 影响行数
     */
    @Update("UPDATE files SET access_count = access_count + 1 WHERE file_id = #{fileId}")
    int increaseAccessCount(@Param("fileId") Long fileId);

    /**
     * 增加下载次数
     * @param fileId 文件ID
     * @return 影响行数
     */
    @Update("UPDATE files SET download_count = download_count + 1 WHERE file_id = #{fileId}")
    int increaseDownloadCount(@Param("fileId") Long fileId);

    /**
     * 增加预览次数
     * @param fileId 文件ID
     * @return 影响行数
     */
    @Update("UPDATE files SET preview_count = preview_count + 1 WHERE file_id = #{fileId}")
    int increasePreviewCount(@Param("fileId") Long fileId);

    /**
     * 增加分享次数
     * @param fileId 文件ID
     * @return 影响行数
     */
    @Update("UPDATE files SET share_count = share_count + 1 WHERE file_id = #{fileId}")
    int increaseShareCount(@Param("fileId") Long fileId);
}
