"""
生成 SMECMS 测试用例文档
从模板 7.测试用例.docx 复制，替换内容为智慧医养项目测试用例
"""
import sys, io
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
import copy
import shutil
import datetime

TEMPLATE = r'D:\ProgramData\work\-smart-medical-elderly-admin\docs\7.测试用例.docx'
OUTPUT   = r'D:\ProgramData\work\-smart-medical-elderly-admin\docs\SMECMS_测试用例.docx'

# ===== 测试用例数据 =====
TEST_CASES = [
    # ========== 1. 登录认证 ==========
    {
        "module": "1. 用户登录",
        "cases": [
            {"id": "TC_LOGIN_01", "pre": "用户拥有有效账号（医生/管理员/设备管理员）", "steps": "1. 打开登录页面\n2. 输入正确的用户名和密码\n3. 点击\"登录\"按钮", "expect": "登录成功，根据角色跳转至对应页面（医生→医生工作台，设备管理员→设备Dashboard，机构管理员→工作台）"},
            {"id": "TC_LOGIN_02", "pre": "用户拥有有效账号", "steps": "1. 打开登录页面\n2. 输入正确的用户名，错误的密码\n3. 点击\"登录\"按钮", "expect": "登录失败，页面提示\"用户名或密码错误\""},
            {"id": "TC_LOGIN_03", "pre": "用户拥有有效账号", "steps": "1. 打开登录页面\n2. 不输入用户名和密码\n3. 点击\"登录\"按钮", "expect": "登录失败，页面提示\"请输入用户名和密码\""},
            {"id": "TC_LOGIN_04", "pre": "用户拥有有效账号", "steps": "1. 打开登录页面\n2. 输入用户名，不输入密码\n3. 点击\"登录\"按钮", "expect": "登录失败，页面提示\"请输入密码\""},
            {"id": "TC_LOGIN_05", "pre": "用户未登录", "steps": "1. 浏览器地址栏直接输入内部页面URL（如 /devices）\n2. 回车访问", "expect": "页面重定向至登录页，提示\"请先登录\""},
            {"id": "TC_LOGIN_06", "pre": "用户已登录", "steps": "1. 点击右上角退出登录\n2. 再次访问系统页面", "expect": "退出成功，Token清除，需重新登录才能访问"},
        ]
    },
    # ========== 2. 老人档案管理 ==========
    {
        "module": "2. 老人档案管理",
        "cases": [
            {"id": "TC_ELDERLY_01", "pre": "① 医生已登录\n② 进入\"老人档案\"页面", "steps": "1. 点击\"新增老人\"按钮\n2. 填写姓名、身份证号、社区、联系方式等必填项\n3. 点击\"保存\"", "expect": "新增成功，老人列表中显示该记录，身份证号自动解析性别和出生日期"},
            {"id": "TC_ELDERLY_02", "pre": "① 医生已登录\n② 老人列表中存在多条记录", "steps": "1. 在搜索栏输入姓名关键字\n2. 选择社区下拉筛选\n3. 设置年龄段筛选", "expect": "列表按筛选条件过滤，显示匹配的老人记录，分页正常"},
            {"id": "TC_ELDERLY_03", "pre": "① 医生已登录\n② 老人列表中存在\"张大爷\"", "steps": "1. 点击\"张大爷\"行的\"编辑\"按钮\n2. 修改联系电话\n3. 点击\"保存\"", "expect": "编辑成功，老人详情中联系电话已更新"},
            {"id": "TC_ELDERLY_04", "pre": "① 医生已登录\n② 已打开某老人的详情页", "steps": "1. 在\"家属信息\"区域点击\"新增家属\"\n2. 填写家属姓名、关系、联系电话\n3. 点击\"保存\"", "expect": "家属信息添加成功，详情页显示新家属记录"},
            {"id": "TC_ELDERLY_05", "pre": "① 医生已登录\n② 老人详情页已有家属\"张三\"", "steps": "1. 点击家属\"张三\"行的\"删除\"按钮\n2. 确认删除", "expect": "家属信息删除成功，详情页不再显示该家属"},
        ]
    },
    # ========== 3. 健康数据管理 ==========
    {
        "module": "3. 健康数据导入与管理",
        "cases": [
            {"id": "TC_HEALTH_01", "pre": "① 医生已登录\n② 进入\"健康导入\"页面", "steps": "1. 下载导入模板\n2. 按模板格式填写健康数据（血压、血糖、心率等）\n3. 上传JSON文件\n4. 点击\"导入\"", "expect": "导入成功，提示导入条数及异常条数"},
            {"id": "TC_HEALTH_02", "pre": "① 医生已登录\n② 进入\"健康导入\"页面", "steps": "1. 上传格式错误的JSON文件\n2. 点击\"导入\"", "expect": "导入失败，提示文件格式错误"},
            {"id": "TC_HEALTH_03", "pre": "① 某老人已有多次健康数据\n② 进入该老人的健康趋势页面", "steps": "1. 选择指标类型（血压）\n2. 选择时间范围（近30天）\n3. 查看趋势图", "expect": "趋势图正确显示血压数据点和预警阈值参照线，单位标注正确"},
            {"id": "TC_HEALTH_04", "pre": "① 医生已登录\n② 进入某老人健康数据列表", "steps": "1. 查看列表中的健康数据记录\n2. 检查每条记录包含测量日期、收缩压、舒张压、血糖、心率等", "expect": "列表正常显示所有健康指标数据，按测量日期倒序排列"},
            {"id": "TC_HEALTH_05", "pre": "① 某老人刚导入了异常血压数据（收缩压>160）", "steps": "1. 等待系统自动扫描\n2. 进入预警记录页面查看", "expect": "系统自动生成一条预警记录，状态为\"待处理\""},
        ]
    },
    # ========== 4. 医生工作台 ==========
    {
        "module": "4. 医生Dashboard工作台",
        "cases": [
            {"id": "TC_DASHBOARD_01", "pre": "① 医生已登录\n② 该医生负责若干老人", "steps": "1. 进入医生工作台页面\n2. 查看顶部数字卡片", "expect": "正确显示\"负责老人总数\"\"本月新增\"\"设备在线率\"\"待处理预警\"\"未读消息\"五个统计卡片"},
            {"id": "TC_DASHBOARD_02", "pre": "① 医生已登录\n② 近30天有新增老人", "steps": "1. 查看近30天新增老人趋势图\n2. 对比各时间点数据", "expect": "折线图正确展示新增老人数量变化趋势"},
            {"id": "TC_DASHBOARD_03", "pre": "① 医生已登录\n② 该医生负责多位不同年龄的老人", "steps": "1. 查看老人年龄分布饼图\n2. 检查各年龄段占比", "expect": "饼图正确展示年龄段分布，图例和数据标签一致"},
        ]
    },
    # ========== 5. 预警处理 (C) ==========
    {
        "module": "5. 预警处理",
        "cases": [
            {"id": "TC_WARN_01", "pre": "① 医生已登录\n② 预警列表中存在待处理预警", "steps": "1. 进入\"预警记录\"页面\n2. 查看列表", "expect": "列表按\"待处理→处理中→已完成→已关闭\"排序，显示预警级别标签（轻/中/重）、触发值、触发时间"},
            {"id": "TC_WARN_02", "pre": "① 有一条待处理预警（状态=0）", "steps": "1. 点击预警行进入详情\n2. 点击\"接单处理\"按钮", "expect": "预警状态变为\"处理中\"，页面显示\"已自动生成随访计划\"提示和\"前往随访管理\"按钮"},
            {"id": "TC_WARN_03", "pre": "① 有一条处理中预警（状态=1）", "steps": "1. 点击详情页\"前往随访管理\"\n2. 执行关联的【预警生成】随访计划\n3. 返回预警详情页\n4. 填写\"处理意见\"和\"处理结果\"\n5. 点击\"完成处理\"", "expect": "预警状态变为\"已完成\"，处理意见和处理结果已保存"},
            {"id": "TC_WARN_04", "pre": "① 有一条待处理预警", "steps": "1. 进入预警详情\n2. 点击\"关闭预警\"\n3. 选择关闭原因\"误报-设备故障\"\n4. 确认关闭", "expect": "预警状态变为\"已关闭\"，关闭原因已保存"},
            {"id": "TC_WARN_05", "pre": "① 有一条处理中预警（已接单）", "steps": "1. 进入随访管理页面\n2. 查看计划列表", "expect": "列表中有一条标注为\"【预警生成】\"的随访计划，与预警标题对应"},
        ]
    },
    # ========== 6. 随访管理 (C) ==========
    {
        "module": "6. 随访管理",
        "cases": [
            {"id": "TC_FUP_01", "pre": "① 医生已登录\n② 进入\"随访管理\"页面", "steps": "1. 点击\"新建随访计划\"\n2. 选择老人\n3. 选择随访类型（电话）\n4. 设置计划日期\n5. 填写随访内容\n6. 点击\"创建\"", "expect": "创建成功，列表中出现新计划，状态为\"待执行\""},
            {"id": "TC_FUP_02", "pre": "① 某老人已有一条\"待执行\"的电话随访", "steps": "1. 新建随访计划\n2. 选择同一位老人、同类型（电话）\n3. 点击\"创建\"", "expect": "创建失败，提示\"该老人已有未完成的同类型随访计划\""},
            {"id": "TC_FUP_03", "pre": "① 有一条待执行计划", "steps": "1. 点击计划行的\"开始\"按钮\n2. 进入执行页面\n3. 查看左侧老人信息和近期健康数据\n4. 填写随访日期、老人状态、干预措施、随访结果\n5. 设置下次随访日期\n6. 点击\"完成随访\"", "expect": "随访完成，计划状态变为\"已完成\"，自动生成一条新的随访计划（下次随访），随访记录已保存"},
            {"id": "TC_FUP_04", "pre": "① 有一条已完成的随访计划", "steps": "1. 点击\"查看\"按钮\n2. 查看随访记录详情", "expect": "页面以只读模式展示该随访的所有填写内容（日期、状态、措施、结果、下次计划日期）"},
            {"id": "TC_FUP_05", "pre": "① 计划日期已过期的待执行计划", "steps": "1. 查看随访计划列表\n2. 找到过期计划", "expect": "过期计划显示\"已逾期\"红色标签，剩余天数列显示\"已逾期 X 天\""},
        ]
    },
    # ========== 7. 评估报告 (C) ==========
    {
        "module": "7. 评估报告管理",
        "cases": [
            {"id": "TC_ASSESS_01", "pre": "① 医生已登录\n② 进入\"评估管理\"页面", "steps": "1. 点击\"新建评估报告\"\n2. 从下拉框选择老人\n3. 从下拉框选择评估模板\n4. 点击\"创建\"", "expect": "创建成功，跳转至评估填写页面"},
            {"id": "TC_ASSESS_02", "pre": "① 已创建评估报告（草稿）\n② 选择的是\"老年综合健康评估\"模板（5维度）", "steps": "1. 逐维度为每个评估项打分\n2. 查看维度小计是否自动汇总\n3. 填写评估结论（≥20字）\n4. 点击\"完成评估\"", "expect": "评估完成，系统自动计算加权总分和等级（优秀/良好/一般/较差），结论长度验证通过"},
            {"id": "TC_ASSESS_03", "pre": "① 已创建评估报告（草稿）\n② 结论字数不足20字", "steps": "1. 填写少于20字的结论\n2. 尝试点击\"完成评估\"", "expect": "\"完成评估\"按钮为禁用状态，无法提交"},
            {"id": "TC_ASSESS_04", "pre": "① 有一份已完成的评估报告", "steps": "1. 在评估管理列表点击\"查看\"\n2. 查看报告详情", "expect": "以只读模式展示各维度评分、总分/满分、等级、结论"},
        ]
    },
    # ========== 8. 设备管理 (C) ==========
    {
        "module": "8. 设备台账管理",
        "cases": [
            {"id": "TC_DEV_01", "pre": "① 设备管理员已登录\n② 进入\"设备台账\"页面", "steps": "1. 点击\"录入新设备\"\n2. 填写设备名称、类型、社区、购置日期、保修截止日\n3. 设备编号留空（自动生成）\n4. 点击\"保存\"", "expect": "录入成功，设备编号自动生成为 DEV-yyyyMMdd-xxx 格式，初始状态为\"离线\""},
            {"id": "TC_DEV_02", "pre": "① 设备列表中存在一台已录入设备", "steps": "1. 点击设备的\"编辑\"按钮\n2. 修改设备名称和备注\n3. 点击\"保存\"", "expect": "编辑成功，列表和设备详情中信息已更新"},
            {"id": "TC_DEV_03", "pre": "① 有一台设备状态为\"在线\"", "steps": "1. 在状态下拉框中选择\"已报废\"\n2. 在确认弹窗中点击\"确认报废\"", "expect": "设备状态变为\"已报废\"，状态不可再变更"},
            {"id": "TC_DEV_04", "pre": "① 有一台设备状态为\"离线\"", "steps": "1. 在状态下拉框中选择\"在线\"\n2. 确认变更", "expect": "设备状态变为\"在线\"，标签颜色变为绿色"},
            {"id": "TC_DEV_05", "pre": "① 医生已登录\n② 进入\"设备报修\"页面", "steps": "1. 从下拉框选择报修设备\n2. 选择故障类型\"设备无法开机\"\n3. 填写故障描述\n4. 点击\"提交报修\"", "expect": "报修成功，设备状态变为\"维修中\""},
        ]
    },
    # ========== 9. 设备Dashboard (C) ==========
    {
        "module": "9. 设备工作台",
        "cases": [
            {"id": "TC_DEVDASH_01", "pre": "① 设备管理员已登录\n② 系统中存在各状态的设备", "steps": "1. 进入设备工作台页面\n2. 查看统计卡片", "expect": "正确显示在线/离线/维修中/已报废的设备数量"},
            {"id": "TC_DEVDASH_02", "pre": "① 有设备处于\"维修中\"状态", "steps": "1. 查看维修中设备列表\n2. 查看质保到期设备列表", "expect": "维修中设备列表最多显示5条；质保到期列表显示30天内即将到期的设备"},
        ]
    },
    # ========== 10. 标签管理 (C) ==========
    {
        "module": "10. 老人标签管理",
        "cases": [
            {"id": "TC_TAG_01", "pre": "① 医生已登录\n② 进入\"标签管理\"页面", "steps": "1. 点击\"新增标签\"\n2. 输入标签名\"独居\"\n3. 选择标签颜色\n4. 点击\"保存\"", "expect": "标签新增成功，左侧标签列表中出现\"独居\"标签"},
            {"id": "TC_TAG_02", "pre": "① 标签\"独居\"已存在", "steps": "1. 点击\"独居\"的\"编辑\"按钮\n2. 修改颜色\n3. 点击\"保存\"", "expect": "标签颜色更新成功"},
            {"id": "TC_TAG_03", "pre": "① 标签\"独居\"已存在\n② 系统中已有老人\"张大爷\"", "steps": "1. 在右侧\"为老人分配标签\"选择\"张大爷\"\n2. 勾选\"独居\"\n3. 点击\"保存\"", "expect": "保存成功，张大爷的标签中显示\"独居\""},
            {"id": "TC_TAG_04", "pre": "① 张大爷已有标签\"独居\"", "steps": "1. 选择张大爷\n2. 取消勾选\"独居\"\n3. 点击\"保存\"", "expect": "保存成功，张大爷不再有\"独居\"标签"},
        ]
    },
    # ========== 11. 角色权限 ==========
    {
        "module": "11. 角色权限与菜单",
        "cases": [
            {"id": "TC_ROLE_01", "pre": "① 设备管理员已登录", "steps": "1. 查看左侧菜单栏\n2. 查看可访问的页面", "expect": "菜单栏仅显示\"工作台\"和\"设备台账\"，无医生相关菜单（预警、随访、评估等）"},
            {"id": "TC_ROLE_02", "pre": "① 医生已登录", "steps": "1. 查看左侧菜单栏\n2. 查看可访问的页面", "expect": "菜单栏显示医生工作台、老人档案、健康导入、健康趋势、设备报修、预警记录、随访管理、评估报告、标签管理，无机构管理菜单"},
            {"id": "TC_ROLE_03", "pre": "① 机构管理员已登录", "steps": "1. 查看左侧菜单栏\n2. 查看可访问的页面", "expect": "菜单栏显示工作台、社区管理、报表统计等管理功能，无医生业务菜单"},
        ]
    },
    # ========== 12. 报表统计 ==========
    {
        "module": "12. 报表统计",
        "cases": [
            {"id": "TC_REPORT_01", "pre": "① 机构管理员已登录\n② 系统中有多个社区的运营数据", "steps": "1. 进入\"报表统计\"页面\n2. 查看\"全局总览\"标签页\n3. 查看柱状图和数据表", "expect": "正确展示各社区老人数柱状图、设备分布柱状图、社区对比表格"},
            {"id": "TC_REPORT_02", "pre": "① 机构管理员已登录", "steps": "1. 切换至\"社区详情\"标签\n2. 选择社区\n3. 查看饼图和趋势图", "expect": "正确展示年龄分布饼图、30天新增趋势折线图、近7天预警趋势图"},
            {"id": "TC_REPORT_03", "pre": "① 机构管理员已登录\n② 报表页面已有数据", "steps": "1. 点击\"导出Excel\"按钮\n2. 下载文件并打开", "expect": "Excel文件下载成功，数据与页面上显示一致，格式规范"},
        ]
    },
]

TOTAL_CASES = sum(len(m["cases"]) for m in TEST_CASES)

# ===== 执行生成 =====
print(f"复制模板: {TEMPLATE}")
shutil.copy2(TEMPLATE, OUTPUT)

doc = Document(OUTPUT)

# ===== 1. 替换封面信息 =====
cover_replacements = {
    "001": "HD2026SMECMS01",
    "九寨沟游客服务系统": "智慧医养大数据公共服务平台",
    "Version: 1.0.2": "Version: 1.0",
    "项 目 承 担 部 门：项目管理部": "项 目 承 担 部 门：项目七组",
    "撰  写  人（签名）： 付杰": "撰  写  人（签名）： 项目七开发团队",
    "完   成   日": "完   成   日",
    "1.0.": "1.0",
}

# Replace text in all paragraphs
for p in doc.paragraphs:
    for old, new in cover_replacements.items():
        if old in p.text:
            for run in p.runs:
                if old in run.text:
                    run.text = run.text.replace(old, new)

# Replace text in all table cells
for t in doc.tables:
    for r in t.rows:
        for c in r.cells:
            for old, new in cover_replacements.items():
                if old in c.text:
                    for p in c.paragraphs:
                        for run in p.runs:
                            if old in run.text:
                                run.text = run.text.replace(old, new)

# ===== 2. 更新文档信息表 (Table 2) =====
meta_table = doc.tables[1]  # second table
meta_updates = {
    "标题: 九寨沟游客服务系统-测试用例": "标题: 智慧医养大数据公共服务平台-测试用例",
    "作者: 付杰": "作者: 项目七开发团队",
    "创建日期: 2025年8月25日": "创建日期: 2026年7月19日",
    "版本: V1.0.2": "版本: V1.0",
    "部门名称: 1组": "部门名称: 项目七组",
}
for r in meta_table.rows:
    cell_text = r.cells[0].text.strip()
    if cell_text in meta_updates:
        r.cells[0].paragraphs[0].clear()
        r.cells[0].paragraphs[0].add_run(meta_updates[cell_text])

meta_table.rows[3].cells[0].paragraphs[0].clear()
meta_table.rows[3].cells[0].paragraphs[0].add_run("上次更新日期: 2026年7月19日")

# ===== 3. 更新修订历史表 (Table 3) =====
hist_table = doc.tables[2]
# Clear old rows (keep header)
for row in hist_table.rows[1:]:
    for c in row.cells:
        for p in c.paragraphs:
            p.clear()

hist_table.rows[1].cells[0].paragraphs[0].add_run("2026/07/19")
hist_table.rows[1].cells[1].paragraphs[0].add_run("1.0")
hist_table.rows[1].cells[2].paragraphs[0].add_run("正式发布（基于测试计划V1.0）")
hist_table.rows[1].cells[3].paragraphs[0].add_run("项目七开发团队")

# ===== 4. 更新测试环境 =====
env_replace = {
    "一、测试环境": "一、测试环境",
}
for p in doc.paragraphs:
    if "操作系统" in p.text:
        p.clear()
        p.add_run("操作系统：Windows 10/11")
    if "浏览器" in p.text:
        p.clear()
        p.add_run("浏览器：Chrome、Edge、Firefox 最新版本")
    if "一、测试环境" in p.text and p.style.name.startswith("Title"):
        pass  # keep title

# ===== 5. 替换测试用例标题 =====
for p in doc.paragraphs:
    if "二、测试用例" in p.text and p.style.name.startswith("Title"):
        p.clear()
        p.add_run(f"二、测试用例（共{TOTAL_CASES}条）")

# ===== 6. 删除旧测试用例表格（从第4个表开始） =====
# Table 0 = cover, Table 1 = meta, Table 2 = history, Table 3 = TOC
# Tables 4+ = old test cases
tables_to_remove = doc.tables[3:]  # table 3 is TOC, keep it
# But we need to keep TOC (table 3). Let's remove from table 4 onwards.
# python-docx doesn't have a clean table delete API.
# We'll clear and rebuild old tables instead.

# Remove old test case paragraphs too
old_module_titles = ["1.用户注册", "2.验证码登录", "3.账户密码登录", "4.酒店管理",
                     "5. 旅游景区管理", "6. 平台人员管理", "7. 投诉管理"]
for p in doc.paragraphs:
    if p.text.strip() in old_module_titles:
        # Remove the run text, later paragraphs will overwrite this
        p.clear()

# ===== 7. 清空旧表（从Table4开始，Table3是目录下的那个TOC格，跳过） =====
# Actually let me find the exact structure. The template has:
# Table 0,1,2,3 = cover meta history toc
# Tables 4,5,6,... = test case tables (Table 4 is the first actual test case)
# But wait, Table 3 in my earlier output was the TOC table (4行x1列 = 目录)
# Actually looking at output: Table 3 was (21行x4列) = revision history,
# Table 4 was (6行x4列) = first test case TC_YZMDT_01

# Hmm, indexes might differ. Let me be safe: find the index of first TC_ table.

first_tc_idx = None
for i, t in enumerate(doc.tables):
    cell_text = t.cell(0, 0).text if t.rows and t.columns else ""
    if cell_text.strip().startswith("测试用例序号"):
        first_tc_idx = i
        break

if first_tc_idx is None:
    first_tc_idx = 4  # fallback

old_tc_tables = doc.tables[first_tc_idx:]
print(f"找到 {len(old_tc_tables)} 个旧测试用例表（从索引{first_tc_idx}开始）")

# ===== 8. 写入新测试用例 =====
# Strategy: reuse existing tables + add new ones if needed
# Each test case = 1 table of 7 rows x 4 cols

def set_cell(table, row, col, text, bold_left=True):
    """Set cell text, left column bold"""
    cell = table.cell(row, col)
    cell.paragraphs[0].clear()
    run = cell.paragraphs[0].add_run(text)
    if bold_left and col == 0:
        run.bold = True
    run.font.size = Pt(9)
    run.font.name = '微软雅黑'

def create_test_table(doc, tc, tc_idx):
    """Create a new 6-row test case table"""
    table = doc.add_table(rows=5, cols=4, style='Table Grid')
    table.autofit = True

    set_cell(table, 0, 0, "测试用例序号")
    set_cell(table, 0, 1, tc["id"])
    set_cell(table, 0, 2, "版本号")
    set_cell(table, 0, 3, "1.0")

    set_cell(table, 1, 0, "测试环境")
    env = "Win10/11, Chrome/Edge"
    set_cell(table, 1, 1, env, False)
    set_cell(table, 1, 2, env, False)
    set_cell(table, 1, 3, env, False)

    set_cell(table, 2, 0, "前提条件")
    set_cell(table, 2, 1, tc["pre"], False)
    set_cell(table, 2, 2, tc["pre"], False)
    set_cell(table, 2, 3, tc["pre"], False)

    set_cell(table, 3, 0, "测试步骤")
    set_cell(table, 3, 1, tc["steps"], False)
    set_cell(table, 3, 2, tc["steps"], False)
    set_cell(table, 3, 3, tc["steps"], False)

    set_cell(table, 4, 0, "预期输出")
    set_cell(table, 4, 1, tc["expect"], False)
    set_cell(table, 4, 2, tc["expect"], False)
    set_cell(table, 4, 3, tc["expect"], False)

    # Set column widths
    for row in table.rows:
        row.cells[0].width = Cm(2.5)
        row.cells[1].width = Cm(5)
        row.cells[2].width = Cm(2.5)
        row.cells[3].width = Cm(5)

    return table

# Remove old test case tables by clearing their XML
# python-docx approach: clear all old test case tables, then add new ones
for t in doc.tables[first_tc_idx:]:
    tbl = t._tbl
    tbl.getparent().remove(tbl)

# Remove old module title paragraphs (the numbered test case titles like "1.用户注册")
# Find and remove old test content paragraphs
paras_to_clear = []
for p in doc.paragraphs:
    text = p.text.strip()
    if text in old_module_titles:
        p.clear()
    # Also clear very short paragraphs that were old test case labels
    if text in ["账户密码登录", "酒店管理", "旅游景区管理", "平台人员管理", "投诉管理"]:
        p.clear()

# Now add module titles and test case tables
# We need to find where "二、测试用例" ends and insert after it
# Simplest: find the "二、测试用例" paragraph and add everything after it
# Actually, let me add tables at the end of the body

# First, add module titles after the test environment paragraphs
# We need to work with the document body element directly
body = doc.element.body
last_para = None
insert_point = None

# Find the last paragraph that says "二、测试用例"
for p in doc.paragraphs:
    if "二、测试用例" in p.text:
        insert_point = p._element
        break

if insert_point is None:
    insert_point = doc.paragraphs[-1]._element

# Insert module titles and tables after the insert point
insert_after = insert_point

# Clear all the remaining content after "二、测试用例" that belongs to old project
# Actually, the old tables are already removed. The old paragraphs are cleared.

# Now add new content
first_module = True
current_element = insert_after

for module_data in TEST_CASES:
    # Add a blank paragraph separator before each module
    if first_module:
        first_module = False
    else:
        p_sep = doc.add_paragraph()
        current_element.addnext(p_sep._element)
        current_element = p_sep._element

    # Add module title
    p_title = doc.add_paragraph()
    run_title = p_title.add_run(module_data["module"])
    run_title.bold = True
    run_title.font.size = Pt(12)
    run_title.font.name = '微软雅黑'
    current_element.addnext(p_title._element)
    current_element = p_title._element

    # Add test case tables
    for tc in module_data["cases"]:
        # Add a blank line before table
        p_space = doc.add_paragraph()
        current_element.addnext(p_space._element)
        current_element = p_space._element

        # Create table and insert it
        table = doc.add_table(rows=5, cols=4, style='Table Grid')
        current_element.addnext(table._tbl)
        current_element = table._tbl

        # Fill table content
        set_cell(table, 0, 0, "测试用例序号")
        set_cell(table, 0, 1, tc["id"])
        set_cell(table, 0, 2, "版本号")
        set_cell(table, 0, 3, "1.0")

        set_cell(table, 1, 0, "测试环境")
        env = "Win10/11, Chrome/Edge"
        set_cell(table, 1, 1, env, False)
        set_cell(table, 1, 2, env, False)
        set_cell(table, 1, 3, env, False)

        set_cell(table, 2, 0, "前提条件")
        set_cell(table, 2, 1, tc["pre"], False)
        set_cell(table, 2, 2, tc["pre"], False)
        set_cell(table, 2, 3, tc["pre"], False)

        set_cell(table, 3, 0, "测试步骤")
        set_cell(table, 3, 1, tc["steps"], False)
        set_cell(table, 3, 2, tc["steps"], False)
        set_cell(table, 3, 3, tc["steps"], False)

        set_cell(table, 4, 0, "预期输出")
        set_cell(table, 4, 1, tc["expect"], False)
        set_cell(table, 4, 2, tc["expect"], False)
        set_cell(table, 4, 3, tc["expect"], False)

        # Set column widths
        for row in table.rows:
            row.cells[0].width = Cm(2.5)
            row.cells[1].width = Cm(5)
            row.cells[2].width = Cm(2.5)
            row.cells[3].width = Cm(5)

# Remove the old TOC table (table 3) since its content was for old project
# Table 0=cover, 1=meta, 2=history, 3=toc
if len(doc.tables) > 3:
    toc_table = doc.tables[3]
    if toc_table.rows and toc_table.rows[0].cells and toc_table.rows[0].cells[0].text.strip() == "目录":
        toc_table._tbl.getparent().remove(toc_table._tbl)

# Save
doc.save(OUTPUT)
print(f"\n生成完成: {OUTPUT}")
print(f"共 {len(TEST_CASES)} 个模块，{TOTAL_CASES} 条测试用例")
print("截图位置已保留'预期输出'行，请手动截图后插入")
