<template>
  <div>
    <!-- 测试调试模式功能验证 -->
    <el-button type="primary">Element UI Button</el-button>
    <el-input placeholder="Element UI Input"></el-input>
    
    <!-- 这个组件用于验证调试模式功能是否正常工作 -->
  </div>
</template>

<script>
export default {
  name: 'DebugModeVerificationTest',
  data() {
    return {
      message: '测试调试模式功能验证'
    }
  },
  mounted() {
    console.log('调试模式功能验证测试组件已加载');
    console.log('此项目测试以下情况：');
    console.log('- 验证调试模式UI是否正确显示');
    console.log('- 检查调试模式复选框是否可见');
    console.log('- 测试调试模式状态保存功能');
    console.log('- 验证调试日志输出功能');
    console.log('- 检查配置文件中的enableDebugMode字段');
    console.log('');
    console.log('验证步骤：');
    console.log('1. 打开 Settings/Tools/Vue Kit 项目组件库配置');
    console.log('2. 查找"启用调试模式"复选框（应该在组件库列表下方）');
    console.log('3. 如果看不到复选框，检查IDEA的日志输出');
    console.log('4. 如果看到复选框，勾选它并检查配置文件');
    console.log('5. 查看IDEA的Event Log窗口是否有调试日志输出');
  }
}
</script>

<style scoped>
.test-container {
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin: 10px 0;
  background-color: #f9f9f9;
}

.verification-steps {
  margin-top: 15px;
  padding: 10px;
  background-color: #e8f4fd;
  border-left: 4px solid #2196f3;
}

.verification-steps h4 {
  margin: 0 0 10px 0;
  color: #1976d2;
}

.verification-steps ol {
  margin: 0;
  padding-left: 20px;
}

.verification-steps li {
  margin-bottom: 5px;
  color: #333;
}
</style> 