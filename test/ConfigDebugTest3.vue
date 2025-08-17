<template>
  <div>
    <!-- 测试缓存内容调试 -->
    <el-button type="primary">Element UI Button</el-button>
    <el-input placeholder="Element UI Input"></el-input>
    
    <!-- 这个组件用于测试缓存内容调试 -->
  </div>
</template>

<script>
export default {
  name: 'ConfigDebugTest3',
  data() {
    return {
      message: '测试缓存内容调试'
    }
  },
  mounted() {
    console.log('缓存内容调试测试组件已加载');
    console.log('此项目测试以下情况：');
    console.log('- 检查本地缓存中的组件库数据');
    console.log('- 查看为什么返回了 element-plus 和 element-ui');
    console.log('- 分析复选框创建和勾选逻辑');
    console.log('- 查看详细的调试日志');
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