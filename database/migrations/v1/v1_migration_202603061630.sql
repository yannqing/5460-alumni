  -- 在 alumni_association_join_application 表中添加基本信息字段
  ALTER TABLE `alumni_association_join_application`
  ADD COLUMN `name` VARCHAR(100) COMMENT '真实姓名' AFTER `target_id`,
  ADD COLUMN `identify_code` VARCHAR(50) COMMENT '身份证号' AFTER `name`,
  ADD COLUMN `phone` VARCHAR(20) COMMENT '手机号' AFTER `identify_code`;

