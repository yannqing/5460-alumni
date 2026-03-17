-- ====================================================================
-- 校友列表查询性能优化 - 数据库索引脚本
-- 创建时间: 2026-03-17
-- 用途: 优化 queryAlumniList 接口性能
-- 注意: 如果索引已存在会报错，可以忽略错误继续执行
-- ====================================================================

-- ====================================================================
-- 1. wx_user_info 表索引优化
-- ====================================================================

-- 姓名索引（用于模糊查询）
-- 使用场景：WHERE name LIKE '%xxx%' 或 WHERE name = 'xxx'
CREATE INDEX idx_name ON wx_user_info(name);

-- 昵称索引（用于模糊查询）
-- 使用场景：WHERE nickname LIKE '%xxx%'
CREATE INDEX idx_nickname ON wx_user_info(nickname);

-- 手机号索引（用于精确/模糊查询）
-- 使用场景：WHERE phone LIKE '%xxx%' 或 WHERE phone = 'xxx'
CREATE INDEX idx_phone ON wx_user_info(phone);

-- 邮箱索引（用于模糊查询）
-- 使用场景：WHERE email LIKE '%xxx%'
CREATE INDEX idx_email ON wx_user_info(email);

-- 创建时间索引（用于排序）
-- 使用场景：ORDER BY created_time ASC/DESC
CREATE INDEX idx_created_time ON wx_user_info(created_time);

-- 性别索引（用于筛选）
-- 使用场景：WHERE gender = 1
CREATE INDEX idx_gender ON wx_user_info(gender);

-- 当前城市索引（用于地理位置筛选）
-- 使用场景：WHERE cur_city = 'xxx'
CREATE INDEX idx_cur_city ON wx_user_info(cur_city);

-- 当前省份索引
-- 使用场景：WHERE cur_province = 'xxx'
CREATE INDEX idx_cur_province ON wx_user_info(cur_province);

-- ====================================================================
-- 2. alumni_education 表索引优化
-- ====================================================================

-- 复合索引：用户ID + 教育经历类型（最重要的索引）
-- 使用场景：WHERE wx_id IN (...) AND type = 1
-- 用于批量查询用户的主要教育经历
CREATE INDEX idx_wxid_type ON alumni_education(wx_id, type);

-- 学校ID索引（用于学校维度查询）
-- 使用场景：WHERE school_id = xxx
CREATE INDEX idx_school_id ON alumni_education(school_id);

-- ====================================================================
-- 3. sys_tag_relation 表索引优化（标签关联）
-- ====================================================================

-- 复合索引：目标ID + 目标类型（最重要的索引）
-- 使用场景：WHERE target_id IN (...) AND target_type = 1
-- 用于批量查询多个用户的标签
CREATE INDEX idx_target_id_type ON sys_tag_relations(target_id, target_type);

-- 复合索引：标签ID + 目标类型
-- 使用场景：WHERE tag_id = xxx AND target_type = 1
CREATE INDEX idx_tag_id_type ON sys_tag_relations(tag_id, target_type);

-- ====================================================================
-- 4. user_privacy_setting 表索引优化
-- ====================================================================

-- 用户ID索引
-- 使用场景：WHERE wx_id = xxx 或 WHERE wx_id IN (...)
-- 用于批量查询用户的隐私设置
CREATE INDEX idx_wx_id ON user_privacy_setting(wx_id);

-- 复合索引：用户ID + 字段代码
-- 使用场景：WHERE wx_id = xxx AND field_code = 'xxx'
CREATE INDEX idx_wx_id_field_code ON user_privacy_setting(wx_id, field_code);

-- ====================================================================
-- 5. wx_users 表索引优化
-- ====================================================================

-- 认证标识索引
-- 使用场景：WHERE certification_flag = 1
CREATE INDEX idx_certification_flag ON wx_users(certification_flag);

-- ====================================================================
-- 6. user_follow 表索引优化（关注关系）
-- ====================================================================

-- 复合索引：用户ID + 目标类型 + 目标ID
-- 使用场景：WHERE wx_id = xxx AND target_type = 1 AND target_id = xxx
CREATE INDEX idx_wx_id_target ON user_follow(wx_id, target_type, target_id);

-- 复合索引：用户ID + 目标类型 + 关注状态
-- 使用场景：WHERE wx_id = xxx AND target_type = 1 AND follow_status IN (1,2,3)
CREATE INDEX idx_wx_id_type_status ON user_follow(wx_id, target_type, follow_status);

-- ====================================================================
-- 索引验证脚本
-- ====================================================================
-- 执行以下命令查看索引是否创建成功：

-- SHOW INDEX FROM wx_user_info;
-- SHOW INDEX FROM alumni_education;
-- SHOW INDEX FROM sys_tag_relation;
-- SHOW INDEX FROM user_privacy_setting;
-- SHOW INDEX FROM wx_users;
-- SHOW INDEX FROM user_follow;

-- ====================================================================
-- 使用 EXPLAIN 验证索引是否生效
-- ====================================================================
-- 示例：
-- EXPLAIN SELECT * FROM wx_user_info WHERE name LIKE '%张三%';
-- 检查 key 字段是否显示 idx_name

-- EXPLAIN SELECT * FROM alumni_education WHERE wx_id IN (1,2,3) AND type = 1;
-- 检查 key 字段是否显示 idx_wxid_type

-- ====================================================================
-- 使用说明
-- ====================================================================
-- 1. 执行前请先备份数据库
-- 2. 在测试环境执行并验证性能提升
-- 3. 如果索引已存在，会报错 "Duplicate key name"，可以忽略
-- 4. 执行后使用 SHOW INDEX 验证索引创建成功
-- 5. 使用 EXPLAIN 分析查询执行计划
-- 6. 定期分析表统计信息：ANALYZE TABLE wx_user_info;
-- ====================================================================

-- ====================================================================
-- 删除索引脚本（如需回滚）
-- ====================================================================
-- DROP INDEX idx_name ON wx_user_info;
-- DROP INDEX idx_nickname ON wx_user_info;
-- DROP INDEX idx_phone ON wx_user_info;
-- DROP INDEX idx_email ON wx_user_info;
-- DROP INDEX idx_created_time ON wx_user_info;
-- DROP INDEX idx_gender ON wx_user_info;
-- DROP INDEX idx_cur_city ON wx_user_info;
-- DROP INDEX idx_cur_province ON wx_user_info;
-- DROP INDEX idx_wxid_type ON alumni_education;
-- DROP INDEX idx_school_id ON alumni_education;
-- DROP INDEX idx_target_id_type ON sys_tag_relation;
-- DROP INDEX idx_tag_id_type ON sys_tag_relation;
-- DROP INDEX idx_wx_id ON user_privacy_setting;
-- DROP INDEX idx_wx_id_field_code ON user_privacy_setting;
-- DROP INDEX idx_certification_flag ON wx_users;
-- DROP INDEX idx_wx_id_target ON user_follow;
-- DROP INDEX idx_wx_id_type_status ON user_follow;
