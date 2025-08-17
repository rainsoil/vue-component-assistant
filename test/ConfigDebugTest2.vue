<template>
  <div>
    <!-- 测试配置文件调试和修复 -->
    <el-button type="primary">Element UI Button</el-button>
    <el-input placeholder="Element UI Input"></el-input>
    
    <!-- 这个组件用于测试配置文件调试和修复效果 -->
  </div>
</template>

<script>
export default {
  name: 'ConfigDebugTest2',
  data() {
    return {
      message: '测试配置文件调试和修复效果'
    }
  },
  mounted() {
    console.log('配置文件调试和修复测试组件已加载');
    console.log('此项目测试以下情况：');
    console.log('- 修复了复选框默认选中问题');
    console.log('- 添加了详细的调试日志');
    console.log('- 配置文件只有 element-ui');
    console.log('- UI中应该只显示 element-ui 被勾选');
    console.log('- 不应该显示 element-plus 被勾选');
    console.log('- 查看详细的调试日志验证修复效果');
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