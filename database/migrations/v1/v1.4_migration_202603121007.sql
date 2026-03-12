-- 新增「审核校友总会」权限
INSERT INTO cni_alumni.permission (per_id, per_uuid, pid, name, code, type, create_time, update_time, is_delete, status, sort_order) VALUES (1995342452768153631, null, 1995342452768153603, '审核校友总会', 'SYSTEM_GENERAL_ALUMNI_ASSOCIATION_AUDIT', 1, DEFAULT, DEFAULT, DEFAULT, DEFAULT, DEFAULT);

-- 将「审核校友总会」权限分配给系统管理员
INSERT INTO cni_alumni.role_permissions (role_id, per_id, create_time, update_time, is_delete) VALUES (2002944992284250113, 1995342452768153631, DEFAULT, DEFAULT, DEFAULT);