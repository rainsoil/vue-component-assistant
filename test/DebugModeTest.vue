<template>
  <div>
    <!-- 测试调试模式功能 -->
    <el-button type="primary">Element UI Button</el-button>
    <el-input placeholder="Element UI Input"></el-input>
    
    <!-- 这个组件用于测试调试模式功能 -->
  </div>
</template>

<script>
export default {
  name: 'DebugModeTest',
  data() {
    return {
      message: '测试调试模式功能'
    }
  },
  mounted() {
    console.log('调试模式测试组件已加载');
    console.log('此项目测试以下情况：');
    console.log('- 在项目组件库配置中启用调试模式');
    console.log('- 查看是否能看到详细的调试日志');
    console.log('- 验证项目级调试模式是否生效');
    console.log('- 测试日志输出功能');
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