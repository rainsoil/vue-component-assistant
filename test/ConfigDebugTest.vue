<template>
  <div>
    <!-- 测试配置文件调试 -->
    <el-button type="primary">Element UI Button</el-button>
    <el-input placeholder="Element UI Input"></el-input>
    
    <!-- 这个组件用于测试配置文件调试日志 -->
  </div>
</template>

<script>
export default {
  name: 'ConfigDebugTest',
  data() {
    return {
      message: '测试配置文件调试日志'
    }
  },
  mounted() {
    console.log('配置文件调试测试组件已加载');
    console.log('此项目测试以下情况：');
    console.log('- 配置文件只有 element-ui');
    console.log('- 检查为什么UI中会同时勾选 element-plus 和 element-ui');
    console.log('- 查看详细的调试日志分析问题');
    console.log('- 验证修复效果');
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