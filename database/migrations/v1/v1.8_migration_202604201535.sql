DROP INDEX idx_alumni_association_id ON merchant;


ALTER TABLE merchant
MODIFY alumni_association_id JSON NULL COMMENT '关联校友会ID';