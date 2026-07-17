<template>
  <div class="dashboard">
    <!-- 统计卡片行 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #e6f7ff">
              <el-icon size="28" color="#409eff"><User /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-number">1,280</div>
              <div class="stat-label">老人总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #f6ffed">
              <el-icon size="28" color="#67c23a"><Plus /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-number">36</div>
              <div class="stat-label">本月新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #fff7e6">
              <el-icon size="28" color="#e6a23c"><WarningFilled /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-number">12</div>
              <div class="stat-label">待处理预警</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon" style="background: #fef0f0">
              <el-icon size="28" color="#f56c6c"><Monitor /></el-icon>
            </div>
            <div class="stat-text">
              <div class="stat-number">96<span class="unit">%</span></div>
              <div class="stat-label">设备在线率</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { User, Plus, WarningFilled, Monitor } from '@element-plus/icons-vue'
</script>

<style scoped>
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-number .unit {
  font-size: 16px;
  font-weight: normal;
  color: #909399;
}
.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
</style>
