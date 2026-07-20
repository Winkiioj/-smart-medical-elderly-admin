<template>
  <div class="forbidden">
    <div class="code">403</div>
    <div class="text">抱歉，您没有权限访问此页面</div>
    <el-button type="primary" @click="$router.push('/dashboard')">返回工作台</el-button>
  </div>
</template>

<style scoped>
.forbidden {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  height: 100vh; gap: 20px; background: #f5f7fa;
}
.code { font-size: 96px; font-weight: bold; color: #dcdfe6; }
.text { font-size: 16px; color: #909399; }
</style>
