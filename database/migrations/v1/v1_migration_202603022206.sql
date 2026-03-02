 ALTER TABLE `alumni_association`
  ADD COLUMN `association_profile` TEXT
  COMMENT '校友会简介' AFTER `logo`;



  -- 添加主要负责人相关字段
  ALTER TABLE `alumni_association`
  ADD COLUMN `charge_wx_id` BIGINT COMMENT
  '主要负责人微信用户ID' AFTER
  `association_profile`,
  ADD COLUMN `charge_name` VARCHAR(100)
  COMMENT '主要负责人姓名' AFTER
  `charge_wx_id`,
  ADD COLUMN `charge_role` VARCHAR(100)
  COMMENT '主要负责人架构角色' AFTER
  `charge_name`,
  ADD COLUMN `charge_social_affiliation`
  VARCHAR(255) COMMENT '主要负责人社会职务'
  AFTER `charge_role`;

  -- 添加驻会代表相关字段
  ALTER TABLE `alumni_association`
  ADD COLUMN `zh_wx_id` BIGINT COMMENT
  '驻会代表微信用户ID' AFTER
  `charge_social_affiliation`,
  ADD COLUMN `zh_name` VARCHAR(100) COMMENT
  '驻会代表姓名' AFTER `zh_wx_id`,
  ADD COLUMN `zh_phone` VARCHAR(20) COMMENT
  '驻会代表联系电话' AFTER `zh_name`,
  ADD COLUMN `zh_social_affiliation`
  VARCHAR(255) COMMENT '驻会代表社会职务'
  AFTER `zh_phone`;
