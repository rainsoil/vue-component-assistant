<template>
  <div>
    <!-- 测试最新的文档样式修改 -->
    <!-- 1. 文档宽度调整为最宽 -->
    <!-- 2. 去除表格边框 -->
    <!-- 3. 简化标题，只保留英文标题 -->
    
    <!-- 测试组件 -->
    <el-button type="primary" size="large">
      主要按钮
    </el-button>
    
    <el-input
      v-model="inputValue"
      placeholder="请输入内容"
      clearable
      show-word-limit
      maxlength="50">
    </el-input>
    
    <el-form :model="form" label-width="120px">
      <el-form-item label="用户名">
        <el-input v-model="form.username"></el-input>
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" type="email"></el-input>
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" show-password></el-input>
      </el-form-item>
    </el-form>
    
    <el-card header="卡片标题" shadow="hover">
      <p>这是一个卡片内容，用于测试文档样式。</p>
      <el-button type="success">成功按钮</el-button>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'DocumentationStyleUpdateTest',
  data() {
    return {
      inputValue: '',
      form: {
        username: '',
        email: '',
        password: ''
      }
    }
  }
}
</script> 