  -- 在 wx_user_work 表中添加工作地址字段
  ALTER TABLE `wx_user_work`
  ADD COLUMN `work_address` VARCHAR(200) COMMENT '工作地址' AFTER `industry`;

