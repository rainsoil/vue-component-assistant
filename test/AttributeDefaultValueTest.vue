<template>
  <div>
    <!-- 测试不同类型的属性默认值 -->
    
    <!-- 字符串类型：应该加引号 -->
    <el-input placeholder="请输入内容"></el-input>
    
    <!-- 布尔类型：不应该加引号 -->
    <el-input :disabled="false"></el-input>
    <el-input :readonly="true"></el-input>
    
    <!-- 数字类型：不应该加引号 -->
    <el-input :maxlength="100"></el-input>
    <el-input :minlength="1"></el-input>
    
    <!-- 混合类型测试 -->
    <el-button type="primary" :disabled="false" :loading="true" size="large">按钮</el-button>
    
    <!-- 数组类型：应该加引号 -->
    <el-select v-model="value" :options="['选项1', '选项2', '选项3']"></el-select>
    
    <!-- 对象类型：应该加引号 -->
    <el-form :model="{ name: '', age: 0 }"></el-form>
  </div>
</template>

<script>
export default {
  name: 'AttributeDefaultValueTest',
  data() {
    return {
      value: ''
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
</style> 