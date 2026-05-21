/*
 Navicat Premium Data Transfer

 Source Server         : mysql5.7
 Source Server Type    : MySQL
 Source Server Version : 50738 (5.7.38)
 Source Host           : 127.0.0.1:3306
 Source Schema         : jeecg-boot

 Target Server Type    : MySQL
 Target Server Version : 50738 (5.7.38)
 File Encoding         : 65001

 Date: 15/05/2025 10:18:36
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for open_api
-- ----------------------------
DROP TABLE IF EXISTS `open_api`;
CREATE TABLE `open_api`  (
                             `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                             `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Interface name',
                             `request_method` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Request method',
                             `request_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'interface address',
                             `black_list` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP blacklist',
                             `body` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Request body content',
                             `origin_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'original address',
                             `status` int(10) NULL DEFAULT NULL COMMENT 'state',
                             `del_flag` int(10) NULL DEFAULT NULL COMMENT 'delete identifier',
                             `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Creator',
                             `create_time` datetime NULL DEFAULT NULL COMMENT 'creation time',
                             `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Modifier',
                             `update_time` datetime NULL DEFAULT NULL COMMENT 'modification time',
                             `headers_json` json NULL COMMENT 'Request headerjson',
                             `params_json` json NULL COMMENT 'Request parametersjson',
                             PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Interface table' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of open_api
-- ----------------------------
INSERT INTO `open_api` VALUES ('1922132683346649090', 'Query users by department', 'GET', 'TEwcXBlr', NULL, NULL, '/sys/user/queryUserByDepId', 1, 0, 'admin', '2025-05-13 11:31:58', 'admin', '2025-05-15 10:10:01', '[]', '[{\"id\": \"row_24\", \"note\": \"\", \"paramKey\": \"id\", \"required\": \"1\", \"defaultValue\": \"\"}]');

-- ----------------------------
-- Table structure for open_api_auth
-- ----------------------------
DROP TABLE IF EXISTS `open_api_auth`;
CREATE TABLE `open_api_auth`  (
                                  `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Authorization name',
                                  `ak` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'AK',
                                  `sk` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'SK',
                                  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Creator',
                                  `create_time` datetime NULL DEFAULT NULL COMMENT 'creation time',
                                  `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Modifier',
                                  `update_time` datetime NULL DEFAULT NULL COMMENT 'modification time',
                                  `system_user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Associated system user name',
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'permission table' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of open_api_auth
-- ----------------------------
INSERT INTO `open_api_auth` VALUES ('1922164194775056386', 'scott', 'ak-pFjyNHWRsJEFWlu6', '4hV5dBrZtmGAtPdbA5yseaeKRYNpzGsS', 'admin', '2025-05-13 13:37:11', NULL, NULL, 'e9ca23d68d884d4ebb19d07889727dae');

-- ----------------------------
-- Table structure for open_api_log
-- ----------------------------
DROP TABLE IF EXISTS `open_api_log`;
CREATE TABLE `open_api_log`  (
                                 `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                 `api_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'interfaceID',
                                 `call_auth_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'callID',
                                 `call_time` datetime NULL DEFAULT NULL COMMENT 'call时间',
                                 `used_time` bigint(20) NULL DEFAULT NULL COMMENT 'time consuming',
                                 `response_time` datetime NULL DEFAULT NULL COMMENT 'response time',
                                 PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'call记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of open_api_log
-- ----------------------------
INSERT INTO `open_api_log` VALUES ('1922175238557913090', '1922132683346649090', '1922164194775056386', '2025-05-13 14:21:04', 94, '2025-05-13 14:21:04');
INSERT INTO `open_api_log` VALUES ('1922175436256432130', '1922132683346649090', '1922164194775056386', '2025-05-13 14:21:51', 38, '2025-05-13 14:21:51');
INSERT INTO `open_api_log` VALUES ('1922175487921868802', '1922132683346649090', '1922164194775056386', '2025-05-13 14:22:03', 31, '2025-05-13 14:22:03');
INSERT INTO `open_api_log` VALUES ('1922176033789562883', '1922132683346649090', '1922164194775056386', '2025-05-13 14:24:13', 27, '2025-05-13 14:24:13');
INSERT INTO `open_api_log` VALUES ('1922176583943835650', '1922132683346649090', '1922164194775056386', '2025-05-13 14:26:25', 39, '2025-05-13 14:26:25');
INSERT INTO `open_api_log` VALUES ('1922177249969934337', '1922132683346649090', '1922164194775056386', '2025-05-13 14:28:08', 55250, '2025-05-13 14:29:03');
INSERT INTO `open_api_log` VALUES ('1922180212645941249', '1922132683346649090', '1922164194775056386', '2025-05-13 14:40:46', 4162, '2025-05-13 14:40:50');
INSERT INTO `open_api_log` VALUES ('1922180441692688385', '1922132683346649090', '1922164194775056386', '2025-05-13 14:41:11', 33346, '2025-05-13 14:41:44');
INSERT INTO `open_api_log` VALUES ('1922180521686454273', '1922132683346649090', '1922164194775056386', '2025-05-13 14:42:00', 3570, '2025-05-13 14:42:03');
INSERT INTO `open_api_log` VALUES ('1922180965825499138', '1922132683346649090', '1922164194775056386', '2025-05-13 14:42:10', 99211, '2025-05-13 14:43:49');
INSERT INTO `open_api_log` VALUES ('1922181034515615746', '1922132683346649090', '1922164194775056386', '2025-05-13 14:43:52', 14005, '2025-05-13 14:44:06');
INSERT INTO `open_api_log` VALUES ('1922183171307982850', '1922132683346649090', '1922164194775056386', '2025-05-13 14:52:15', 19834, '2025-05-13 14:52:35');
INSERT INTO `open_api_log` VALUES ('1922184177068523521', '1922132683346649090', '1922164194775056386', '2025-05-13 14:56:34', 748, '2025-05-13 14:56:35');
INSERT INTO `open_api_log` VALUES ('1922184729043107841', '1922132683346649090', '1922164194775056386', '2025-05-13 14:58:46', 1031, '2025-05-13 14:58:47');
INSERT INTO `open_api_log` VALUES ('1922184806453182465', '1922132683346649090', '1922164194775056386', '2025-05-13 14:59:05', 68, '2025-05-13 14:59:05');
INSERT INTO `open_api_log` VALUES ('1922184918382379009', '1922132683346649090', '1922164194775056386', '2025-05-13 14:59:10', 22155, '2025-05-13 14:59:32');
INSERT INTO `open_api_log` VALUES ('1922185292635844610', '1922132683346649090', '1922164194775056386', '2025-05-13 15:00:55', 6267, '2025-05-13 15:01:01');
INSERT INTO `open_api_log` VALUES ('1922186002672791554', '1922132683346649090', '1922164194775056386', '2025-05-13 15:03:23', 27554, '2025-05-13 15:03:50');
INSERT INTO `open_api_log` VALUES ('1922187506582425601', '1922132683346649090', '1922164194775056386', '2025-05-13 15:09:45', 3464, '2025-05-13 15:09:49');
INSERT INTO `open_api_log` VALUES ('1922187586597163011', '1922132683346649090', '1922164194775056386', '2025-05-13 15:10:08', 82, '2025-05-13 15:10:08');
INSERT INTO `open_api_log` VALUES ('1922187924741951490', '1922132683346649090', '1922164194775056386', '2025-05-13 15:10:49', 39590, '2025-05-13 15:11:28');
INSERT INTO `open_api_log` VALUES ('1922188138710261761', '1922132683346649090', '1922164194775056386', '2025-05-13 15:12:19', 758, '2025-05-13 15:12:19');
INSERT INTO `open_api_log` VALUES ('1922188290661507073', '1922132683346649090', '1922164194775056386', '2025-05-13 15:12:29', 26527, '2025-05-13 15:12:56');
INSERT INTO `open_api_log` VALUES ('1922189701755424769', '1922132683346649090', '1922164194775056386', '2025-05-13 15:18:28', 3619, '2025-05-13 15:18:32');
INSERT INTO `open_api_log` VALUES ('1922190076784803841', '1922132683346649090', '1922164194775056386', '2025-05-13 15:20:01', 741, '2025-05-13 15:20:02');
INSERT INTO `open_api_log` VALUES ('1922836671113101313', '1922132683346649090', '1922164194775056386', '2025-05-15 10:09:21', 186, '2025-05-15 10:09:22');
INSERT INTO `open_api_log` VALUES ('1922836856287428610', '1922132683346649090', '1922164194775056386', '2025-05-15 10:10:06', 145, '2025-05-15 10:10:06');

-- ----------------------------
-- Table structure for open_api_permission
-- ----------------------------
DROP TABLE IF EXISTS `open_api_permission`;
CREATE TABLE `open_api_permission`  (
                                        `id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                        `api_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'interfaceID',
                                        `api_auth_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'CertificationID',
                                        `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Creator',
                                        `create_time` datetime NULL DEFAULT NULL COMMENT 'creation time',
                                        `update_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'Updater',
                                        `update_time` datetime NULL DEFAULT NULL COMMENT 'Update time',
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'openapiAuthorize' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of open_api_permission
-- ----------------------------
INSERT INTO `open_api_permission` VALUES ('1922164225875820545', '1922132683346649090', '1922164194775056386', 'admin', '2025-05-13 13:37:18', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;


INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('1917957565728198657', '1922109301837606914', 'interface文档', '/openapi/SwaggerUI', 'openapi/SwaggerUI', 1, '', null, 1, null, '0', 1, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 23:01:32', 'admin', '2025-05-13 09:59:46', 0, 0, null, 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('1922109301837606914', '', 'OpenApimanage', '/openapi', 'layouts/RouteView', 1, '', null, 0, null, '0', 12.1, 0, 'ant-design:swap-outlined', 0, 0, 0, 0, null, 'admin', '2025-05-13 09:59:03', 'admin', '2025-05-13 10:02:43', 0, 0, null, 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050104193340030', '1922109301837606914', 'interfacemanage', '/openapi/openApiList', 'openapi/OpenApiList', 1, null, null, 1, null, '1', 0, 0, null, 0, 0, 0, 0, null, 'admin', '2025-05-01 16:19:03', 'admin', '2025-05-13 09:59:24', 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050104193350031', '2025050104193340030', '添加interfacemanage', null, null, 0, null, null, 2, 'openapi:open_api:add', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 16:19:03', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050104193350032', '2025050104193340030', '编辑interfacemanage', null, null, 0, null, null, 2, 'openapi:open_api:edit', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 16:19:03', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050104193350033', '2025050104193340030', '删除interfacemanage', null, null, 0, null, null, 2, 'openapi:open_api:delete', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 16:19:03', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050104193350034', '2025050104193340030', '批量删除interfacemanage', null, null, 0, null, null, 2, 'openapi:open_api:deleteBatch', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 16:19:03', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050104193350035', '2025050104193340030', 'Exportexcel_interfacemanage', null, null, 0, null, null, 2, 'openapi:open_api:exportXls', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 16:19:03', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050104193350036', '2025050104193340030', 'importexcel_interfacemanage', null, null, 0, null, null, 2, 'openapi:open_api:importExcel', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 16:19:03', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050105554940200', '1922109301837606914', 'Authorizemanage', '/openapi/openApiAuthList', 'openapi/OpenApiAuthList', 1, null, null, 1, null, '1', 0, 0, null, 0, 0, 0, 0, null, 'admin', '2025-05-01 17:55:20', 'admin', '2025-05-13 09:59:35', 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050105554940201', '2025050105554940200', '添加Authorizemanage', null, null, 0, null, null, 2, 'openapi:open_api_auth:add', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 17:55:20', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050105554940202', '2025050105554940200', '编辑Authorizemanage', null, null, 0, null, null, 2, 'openapi:open_api_auth:edit', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 17:55:20', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050105554940203', '2025050105554940200', '删除Authorizemanage', null, null, 0, null, null, 2, 'openapi:open_api_auth:delete', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 17:55:20', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050105554940204', '2025050105554940200', '批量删除Authorizemanage', null, null, 0, null, null, 2, 'openapi:open_api_auth:deleteBatch', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 17:55:20', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050105554940205', '2025050105554940200', 'Exportexcel_Authorizemanage', null, null, 0, null, null, 2, 'openapi:open_api_auth:exportXls', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 17:55:20', null, null, 0, 0, '1', 0);
INSERT INTO sys_permission (id, parent_id, name, url, component, is_route, component_name, redirect, menu_type, perms, perms_type, sort_no, always_show, icon, is_leaf, keep_alive, hidden, hide_tab, description, create_by, create_time, update_by, update_time, del_flag, rule_flag, status, internal_or_external) VALUES ('2025050105554940206', '2025050105554940200', 'importexcel_Authorizemanage', null, null, 0, null, null, 2, 'openapi:open_api_auth:importExcel', '1', null, 0, null, 1, 0, 0, 0, null, 'admin', '2025-05-01 17:55:20', null, null, 0, 0, '1', 0);

INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917957659860963330', 'f6817f48af4fb3af11b9e8bf182f618b', '1917957565728198657', null, '2025-05-01 23:01:55', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1922109760551858178', 'f6817f48af4fb3af11b9e8bf182f618b', '1922109301837606914', null, '2025-05-13 10:00:53', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917857071739539457', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050104193340030', null, '2025-05-01 16:22:13', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917857071806648321', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050104193350031', null, '2025-05-01 16:22:13', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917857071806648322', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050104193350032', null, '2025-05-01 16:22:13', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917857071806648323', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050104193350033', null, '2025-05-01 16:22:13', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917857071806648324', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050104193350034', null, '2025-05-01 16:22:13', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917857071806648325', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050104193350035', null, '2025-05-01 16:22:13', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917857071806648326', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050104193350036', null, '2025-05-01 16:22:13', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917881149426864129', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050105554940200', null, '2025-05-01 17:57:53', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917881149431058436', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050105554940203', null, '2025-05-01 17:57:53', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917881149431058437', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050105554940204', null, '2025-05-01 17:57:53', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917881149431058438', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050105554940205', null, '2025-05-01 17:57:53', '0:0:0:0:0:0:0:1');
INSERT INTO sys_role_permission (id, role_id, permission_id, data_rule_ids, operate_date, operate_ip) VALUES ('1917881149431058439', 'f6817f48af4fb3af11b9e8bf182f618b', '2025050105554940206', null, '2025-05-01 17:57:53', '0:0:0:0:0:0:0:1');