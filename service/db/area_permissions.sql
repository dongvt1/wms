-- 权限数据插入脚本 - 区域管理模块
-- 创建区域管理相关权限

-- 1. 区域管理菜单权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000001', '3', '区域管理', '/warehouse/area', 'warehouse/AreaList', 'AreaList', NULL, 1, NULL, '1', 1, 1, 'appstore-o', 1, 0, 1, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 2. 区域列表权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000002', '1290000000000000001', '区域列表', '/warehouse/area/WarehouseAreaList', 'warehouse/WarehouseAreaList', 'WarehouseAreaList', NULL, 1, 'warehouse:area:list', '1', 1, 1, 'database', 1, 0, 1, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 3. 区域添加权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000003', '1290000000000000002', '区域添加', NULL, NULL, NULL, NULL, 2, 'warehouse:area:add', '1', 1, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 4. 区域编辑权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000004', '1290000000000000002', '区域编辑', NULL, NULL, NULL, NULL, 2, 'warehouse:area:edit', '1', 2, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 5. 区域删除权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000005', '1290000000000000002', '区域删除', NULL, NULL, NULL, NULL, 2, 'warehouse:area:delete', '1', 3, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 6. 区域详情权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000006', '1290000000000000002', '区域详情', NULL, NULL, NULL, NULL, 2, 'warehouse:area:detail', '1', 4, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 7. 货架管理权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000007', '1290000000000000001', '货架管理', '/warehouse/shelf', 'warehouse/WarehouseShelfList', 'WarehouseShelfList', NULL, 1, NULL, '1', 2, 1, 'hdd', 1, 0, 1, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 8. 货架列表权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000008', '1290000000000000007', '货架列表', '/warehouse/shelf/WarehouseShelfList', 'warehouse/WarehouseShelfList', 'WarehouseShelfList', NULL, 1, 'warehouse:shelf:list', '1', 1, 1, 'database', 1, 0, 1, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 9. 货架添加权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000009', '1290000000000000008', '货架添加', NULL, NULL, NULL, NULL, 2, 'warehouse:shelf:add', '1', 1, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 10. 货架编辑权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000010', '1290000000000000008', '货架编辑', NULL, NULL, NULL, NULL, 2, 'warehouse:shelf:edit', '1', 2, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 11. 货架删除权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000011', '1290000000000000008', '货架删除', NULL, NULL, NULL, NULL, 2, 'warehouse:shelf:delete', '1', 3, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 12. 货架详情权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000012', '1290000000000000008', '货架详情', NULL, NULL, NULL, NULL, 2, 'warehouse:shelf:detail', '1', 4, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 13. 货位管理权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000013', '1290000000000000001', '货位管理', '/warehouse/slot', 'warehouse/WarehouseSlotList', 'WarehouseSlotList', NULL, 1, NULL, '1', 3, 1, 'inbox', 1, 0, 1, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 14. 货位列表权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000014', '1290000000000000013', '货位列表', '/warehouse/slot/WarehouseSlotList', 'warehouse/WarehouseSlotList', 'WarehouseSlotList', NULL, 1, 'warehouse:slot:list', '1', 1, 1, 'database', 1, 0, 1, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 15. 货位添加权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000015', '1290000000000000014', '货位添加', NULL, NULL, NULL, NULL, 2, 'warehouse:slot:add', '1', 1, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 16. 货位编辑权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000016', '1290000000000000014', '货位编辑', NULL, NULL, NULL, NULL, 2, 'warehouse:slot:edit', '1', 2, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 17. 货位删除权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000017', '1290000000000000014', '货位删除', NULL, NULL, NULL, NULL, 2, 'warehouse:slot:delete', '1', 3, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 18. 货位详情权限
INSERT INTO sys_permission (id, parent_id, name, url, component, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_route, is_leaf, keep_alive, hidden, create_by, create_time, update_by, update_time, del_flag) 
VALUES ('1290000000000000018', '1290000000000000014', '货位详情', NULL, NULL, NULL, NULL, 2, 'warehouse:slot:detail', '1', 4, 0, NULL, 0, 1, 0, 0, 'admin', NOW(), 'admin', NOW(), '0');

-- 为管理员角色分配权限
INSERT INTO sys_role_permission (id, role_id, permission_id, create_by, create_time, update_by, update_time) 
VALUES 
('rp1290000000000000001', '1', '1290000000000000001', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000002', '1', '1290000000000000002', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000003', '1', '1290000000000000003', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000004', '1', '1290000000000000004', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000005', '1', '1290000000000000005', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000006', '1', '1290000000000000006', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000007', '1', '1290000000000000007', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000008', '1', '1290000000000000008', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000009', '1', '1290000000000000009', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000010', '1', '1290000000000000010', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000011', '1', '1290000000000000011', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000012', '1', '1290000000000000012', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000013', '1', '1290000000000000013', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000014', '1', '1290000000000000014', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000015', '1', '1290000000000000015', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000016', '1', '1290000000000000016', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000017', '1', '1290000000000000017', 'admin', NOW(), 'admin', NOW()),
('rp1290000000000000018', '1', '1290000000000000018', 'admin', NOW(), 'admin', NOW());