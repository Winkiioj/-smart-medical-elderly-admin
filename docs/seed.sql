-- ============================================
-- 智慧医养管理系统 — 初始种子数据
-- 执行方式：mysql -u root -p123456 < seed.sql
-- ============================================

USE smart_elderly_care;

-- 1. 角色数据
INSERT INTO sys_role (role_code, role_name, description, status) VALUES
('ORG_ADMIN',    '机构管理员', '卫健委/平台最高管理者，查看全区报表', 1),
('COM_ADMIN',    '社区管理员', '管理本社区医生、分配老人、查看本社区报表', 1),
('DOCTOR',       '社区医生',   '管理签约老人、处理预警、随访、评估', 1),
('DEVICE_ADMIN', '设备管理员', '管理设备台账、监控设备状态、处理维修', 1);

-- 2. 菜单数据
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, perms, icon, menu_type, sort_order, status) VALUES
-- 机构管理员
(1,  0, '数据看板', '/dashboard',     'views/org/Dashboard.vue',     'dashboard:view',     'DataBoard',  1, 1, 1),
(2,  0, '报表统计', '/reports',       'views/org/Reports.vue',       'reports:view',       'Document',   1, 2, 1),
-- 社区管理员
(10, 0, '医生管理', '/doctors',       'views/com/DoctorManage.vue',  'doctor:manage',      'UserFilled', 1, 1, 1),
(11, 0, '老人分配', '/assign',        'views/com/ElderlyAssign.vue', 'assign:manage',      'Connection', 1, 2, 1),
(12, 0, '社区报表', '/com-reports',   'views/com/CommunityReports.vue', 'com-reports:view', 'DataLine',   1, 3, 1),
-- 社区医生
(20, 0, '工作台',   '/doctor-dashboard', 'views/doctor/Dashboard.vue',     'doctor:dashboard',  'HomeFilled', 1, 1, 1),
(21, 0, '老人档案', '/elderly',           'views/doctor/ElderlyList.vue',   'elderly:view',      'User',       1, 2, 1),
(22, 0, '健康管理', '/health',            NULL,                             'health:view',       'Monitor',    0, 3, 1),
(23, 22,'数据导入', '/health-import',     'views/doctor/HealthImport.vue',  'health:import',     'Upload',     1, 1, 1),
(24, 22,'趋势图',   '/health-trend',      'views/doctor/HealthTrend.vue',   'health:trend',      'TrendCharts',1, 2, 1),
(25, 0, '预警管理', '/warnings',          'views/doctor/WarningManage.vue', 'warning:manage',    'Warning',    1, 4, 1),
(26, 0, '随访管理', '/followup',          'views/doctor/FollowupManage.vue','followup:manage',   'Calendar',   1, 5, 1),
(27, 0, '评估报告', '/assessment',        'views/doctor/AssessmentManage.vue','assessment:manage','Document',   1, 6, 1),
-- 设备管理员
(30, 0, '设备台账', '/devices',           'views/device/DeviceManage.vue',  'device:manage',     'Cpu',        1, 1, 1),
(31, 0, '设备监控', '/device-monitor',    'views/device/DeviceMonitor.vue', 'device:monitor',    'VideoCamera',1, 2, 1),
(32, 0, '维修管理', '/device-repair',     'views/device/DeviceStatus.vue',  'device:repair',     'SetUp',      1, 3, 1);

-- 3. 角色-菜单关联
-- 机构管理员：全部菜单
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE status = 1;

-- 社区管理员：医生管理 + 老人分配 + 社区报表
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(2, 10), (2, 11), (2, 12);

-- 社区医生：工作台 + 老人档案 + 健康管理 + 预警管理 + 随访管理 + 评估报告
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(3, 20), (3, 21), (3, 22), (3, 23), (3, 24), (3, 25), (3, 26), (3, 27);

-- 设备管理员：设备台账 + 设备监控 + 维修管理
INSERT INTO sys_role_menu (role_id, menu_id) VALUES
(4, 30), (4, 31), (4, 32);

-- 4. 管理员账号（密码都是 123456，首次启动时由 InitDataRunner 自动加密为 BCrypt）
INSERT INTO sys_user (username, password, real_name, phone, community, status) VALUES
('admin',         '{noop}123456', '王凯',    '13800000000', NULL,          1),
('community_adm', '{noop}123456', '张社区',  '13800000001', '花园社区',     1),
('doctor_zhang',  '{noop}123456', '张医生',  '13800000002', '花园社区',     1),
('doctor_li',     '{noop}123456', '李医生',  '13800000003', '花园社区',     1),
('device_adm',    '{noop}123456', '刘设备',  '13800000004', NULL,          1);

-- 5. 用户-角色关联
-- admin = 机构管理员
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1);
-- community_adm = 社区管理员
INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2);
-- doctor_zhang, doctor_li = 社区医生
INSERT INTO sys_user_role (user_id, role_id) VALUES (3, 3);
INSERT INTO sys_user_role (user_id, role_id) VALUES (4, 3);
-- device_adm = 设备管理员
INSERT INTO sys_user_role (user_id, role_id) VALUES (5, 4);
