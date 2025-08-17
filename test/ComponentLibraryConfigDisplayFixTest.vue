<template>
  <div>
    <!-- 测试组件库配置显示修复 -->
    <el-button type="primary">Element UI Button</el-button>
    <el-input placeholder="Element UI Input"></el-input>
    
    <!-- 这个组件应该只显示 element-ui 相关的组件 -->
    <!-- 不应该显示 element-plus 相关的组件 -->
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryConfigDisplayFixTest',
  data() {
    return {
      message: '测试组件库配置显示修复'
    }
  },
  mounted() {
    console.log('组件库配置显示修复测试组件已加载');
    console.log('此项目测试以下情况：');
    console.log('- 配置文件中只有 element-ui');
    console.log('- UI中应该只显示 element-ui 被勾选');
    console.log('- 不应该显示 element-plus 被勾选');
  }
}
</script>

<style scoped>
.test-container {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin: 10px 0;
}
</style> 