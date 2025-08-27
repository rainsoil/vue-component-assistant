<template>
  <div>
    <!-- B2修复测试：属性补全与值枚举 -->
    <!-- 测试1：属性名称补全 -->
    <!-- 在el-button标签内输入空格，然后输入ty，应该看到type属性提示 -->
    <!-- 测试2：属性值枚举 -->
    <!-- 在type="后输入p，应该看到primary, success, warning等选项 -->

    <!-- 测试按钮组件属性补全 -->
    <el-button 
      <!-- 在这里输入空格，然后输入ty，应该看到type属性提示 -->
      <!-- 在type="后输入p，应该看到primary等选项 -->
      type="primary" 
      size="large" 
      disabled="false" 
      loading="false">
      按钮
    </el-button>

    <!-- 测试输入框组件属性补全 -->
    <el-input 
      <!-- 在这里输入空格，然后输入ty，应该看到type属性提示 -->
      <!-- 在type="后输入t，应该看到text, textarea等选项 -->
      v-model="inputValue" 
      placeholder="请输入内容" 
      type="text" 
      disabled="false">
    </el-input>
    
    <!-- 测试新组件属性补全 -->
    <!-- 在这里输入 <el-button 然后输入空格，应该看到属性补全提示 -->
    <!-- 输入ty应该看到type属性，输入si应该看到size属性 -->
  </div>
</template>

<script>
export default {
  name: 'B2AttributeCompletionFixedTest',
  data() {
    return {
      inputValue: ''
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
</style>
