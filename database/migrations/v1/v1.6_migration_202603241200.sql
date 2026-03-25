-- 将「城市」下「校友会认证」「信息维护」权限补授给系统超级管理员（v1.4 曾仅授给校促会管理员，导致超管缓存无此二码，管理入口不展示）
INSERT INTO cni_alumni.role_permissions (role_id, per_id, create_time, update_time, is_delete)
VALUES (2002944992284250113, 1995342452768153632, DEFAULT, DEFAULT, DEFAULT);
INSERT INTO cni_alumni.role_permissions (role_id, per_id, create_time, update_time, is_delete)
VALUES (2002944992284250113, 1995342452768153633, DEFAULT, DEFAULT, DEFAULT);
