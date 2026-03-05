-- 组织架构模板表
CREATE TABLE organize_archi_template
(
    template_id   BIGINT                             NOT NULL COMMENT
        '模板ID'
        PRIMARY KEY,
    template_name VARCHAR(100)                       NOT NULL COMMENT
        '模板名称',
    template_code VARCHAR(100)                       NOT NULL COMMENT
        '模板唯一代码',
    organize_type TINYINT                            NOT NULL COMMENT
        '适用组织类型（0校友会，1校处会，2商户）',
    template_json TEXT                               NOT NULL COMMENT
        '模板内容（JSON格式，包含树形结构）',
    description   VARCHAR(500)                       NULL COMMENT
        '模板描述',
    is_default    TINYINT  DEFAULT 0                 NULL COMMENT
        '是否默认模板：0-否，1-是',
    status        TINYINT  DEFAULT 1                 NULL COMMENT
        '状态：0-禁用 1-启用',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT
        '创建时间',
    update_time   DATETIME DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE
                CURRENT_TIMESTAMP COMMENT '更新时间',
    is_delete     TINYINT  DEFAULT 0                 NULL COMMENT
        '逻辑删除',
    CONSTRAINT organize_archi_template_template_code_uindex
        UNIQUE (template_code)
) COMMENT '组织架构模板表' ROW_FORMAT = DYNAMIC;


  -- 在校友会创建申请表中添加模板ID字段
ALTER TABLE `alumni_association_application`
    ADD COLUMN `template_id` BIGINT COMMENT '组织架构模板ID' AFTER
        `association_profile`;
