<template>
  <div>
    <!-- 测试配置文件路径和功能开关 -->
    <el-button type="primary">Element UI Button</el-button>
    <el-input placeholder="Element UI Input"></el-input>
    
    <!-- 这个组件用于测试配置文件路径和功能开关的保存 -->
  </div>
</template>

<script>
export default {
  name: 'ConfigFileTest',
  data() {
    return {
      message: '测试配置文件路径和功能开关'
    }
  },
  mounted() {
    console.log('配置文件路径和功能开关测试组件已加载');
    console.log('此项目测试以下情况：');
    console.log('- 配置文件路径：项目根目录/.idea/vuekit-project-config.json');
    console.log('- 功能开关应该能够正确保存到配置文件');
    console.log('- 包括：组件补全、属性补全、事件补全、插槽补全');
    console.log('- 包括：悬停文档、右键文档、缓存、调试模式');
    console.log('- 配置修改后应该立即保存到文件');
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