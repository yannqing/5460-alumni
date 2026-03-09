  ALTER TABLE alumni_association_application
  ADD COLUMN former_school_name VARCHAR(255) NULL COMMENT '母校曾用名' AFTER
  school_id;
