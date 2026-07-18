-- ===== 评估模板种子数据 =====
-- 先清空旧数据
DELETE FROM assessment_score WHERE 1=1;
DELETE FROM assessment_report WHERE 1=1;
DELETE FROM assessment_dimension WHERE 1=1;
DELETE FROM assessment_template WHERE 1=1;
ALTER TABLE assessment_template AUTO_INCREMENT = 1;
ALTER TABLE assessment_dimension AUTO_INCREMENT = 1;

-- 模板1：老年综合健康评估
INSERT INTO assessment_template (id, template_name, description, dimension_count, full_score, status, create_time) VALUES
(1, '老年综合健康评估', '适用于社区老人的综合健康评估，覆盖日常生活能力、认知功能、营养状况、跌倒风险、疼痛评估五个维度', 5, 79, 1, NOW());

INSERT INTO assessment_dimension (template_id, dimension_name, max_score, weight, scoring_guide, sort_order, create_time) VALUES
(1, '日常生活能力(ADL)', 14, 1.0, '{"desc":"评估老人日常生活自理能力，共7题，每题0-2分：2=独立完成，1=需部分帮助，0=完全依赖","items":["进食","洗澡","穿衣","如厕","床椅转移","平地行走","上下楼梯"]}', 1, NOW()),
(1, '认知功能(MMSE简化版)', 19, 0.8, '{"desc":"评估老人认知功能，共19题，每题0-1分","items":["定向力","记忆力","注意力和计算力","回忆力","语言能力"]}', 2, NOW()),
(1, '营养状况(MNA-SF)', 14, 0.6, '{"desc":"评估老人营养状况，共6题","items":["近3月食物摄入量变化","近3月体重变化","活动能力","心理创伤或急性疾病","神经心理问题","BMI"]}', 3, NOW()),
(1, '跌倒风险评估(Morse)', 20, 0.5, '{"desc":"评估老人跌倒风险，共6项","items":["跌倒史","次要诊断","行走辅助","静脉输液","步态","心理状态"]}', 4, NOW()),
(1, '疼痛评估(NRS)', 12, 0.3, '{"desc":"评估老人疼痛状况，数字评分0-10，0=无痛，10=最剧烈疼痛","items":["当前疼痛程度","过去一周平均疼痛","疼痛对日常生活的影响"]}', 5, NOW());

-- 模板2：慢病管理评估
INSERT INTO assessment_template (id, template_name, description, dimension_count, full_score, status, create_time) VALUES
(2, '慢病管理评估', '适用于患有高血压、糖尿病、冠心病等慢性病老人的专项评估', 4, 65, 1, NOW());

INSERT INTO assessment_dimension (template_id, dimension_name, max_score, weight, scoring_guide, sort_order, create_time) VALUES
(2, '血压管理', 15, 1.0, '{"desc":"评估血压控制情况","items":["近1月血压达标率","用药依从性","生活方式管理","定期监测频率"]}', 1, NOW()),
(2, '血糖管理', 15, 1.0, '{"desc":"评估血糖控制情况","items":["空腹血糖达标率","餐后血糖控制","低血糖事件","糖化血红蛋白趋势"]}', 2, NOW()),
(2, '用药依从性', 20, 0.7, '{"desc":"评估慢病用药管理","items":["按时服药情况","药物副作用认知","多药联用管理","定期复诊情况","药物储存"]}', 3, NOW()),
(2, '生活方式', 15, 0.5, '{"desc":"评估健康生活方式","items":["饮食管理(低盐/低糖)","运动习惯","戒烟限酒","体重管理","睡眠质量"]}', 4, NOW());

-- 模板3：年度健康体检评估
INSERT INTO assessment_template (id, template_name, description, dimension_count, full_score, status, create_time) VALUES
(3, '年度健康体检评估', '适用于社区老人年度体检后的综合健康评估', 4, 55, 1, NOW());

INSERT INTO assessment_dimension (template_id, dimension_name, max_score, weight, scoring_guide, sort_order, create_time) VALUES
(3, '体格检查', 15, 1.0, '{"desc":"评估身高体重BMI、视力、听力等基本指标","items":["BMI指数","视力变化","听力变化","血压","心率"]}', 1, NOW()),
(3, '实验室检查', 15, 0.8, '{"desc":"评估血常规、尿常规、生化全项等指标","items":["血常规","尿常规","肝功能","肾功能","血脂"]}', 2, NOW()),
(3, '慢病控制', 15, 0.6, '{"desc":"评估慢病管理效果","items":["血压控制","血糖控制","冠心病管理","慢阻肺管理"]}', 3, NOW()),
(3, '心理健康', 10, 0.3, '{"desc":"评估心理健康状况","items":["抑郁筛查(GDS)","焦虑评估","社交参与度","生活满意度"]}', 4, NOW());
