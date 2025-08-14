<template>
  <!-- 组件库管理功能测试 -->
  <div>
    <h2>🔧 组件库管理功能测试</h2>
    
    <!-- Element Plus 组件测试 -->
    <el-button type="primary" @click="handleClick">Element Plus 按钮</el-button>
    <el-input v-model="inputValue" placeholder="Element Plus 输入框" />
    
    <!-- Element UI 组件测试（如果项目使用 Element UI） -->
    <el-button type="success" @click="handleSuccess">Element UI 按钮</el-button>
    
    <!-- Ant Design Vue 组件测试（如果项目使用 Ant Design Vue） -->
    <a-button type="primary" @click="handleAntClick">Ant Design 按钮</a-button>
    
    <!-- 自定义组件测试（需要先上传自定义组件库） -->
    <my-button type="primary" @click="handleCustomClick">自定义按钮</my-button>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryManagementTest',
  data() {
    return {
      inputValue: ''
    }
  },
  methods: {
    handleClick() {
      console.log('Element Plus 按钮点击')
    },
    handleSuccess() {
      console.log('Element UI 按钮点击')
    },
    handleAntClick() {
      console.log('Ant Design 按钮点击')
    },
    handleCustomClick() {
      console.log('自定义按钮点击')
    }
  }
}
</script>

<style scoped>
h2 {
  color: #409EFF;
  margin-bottom: 20px;
}

* {
  margin-right: 10px;
  margin-bottom: 10px;
}
</style>
