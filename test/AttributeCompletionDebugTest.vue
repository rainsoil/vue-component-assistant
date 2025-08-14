<template>
  <div>
    <!-- 测试属性补全调试 -->
    <!-- 在 el-table 后面输入 da，应该出现 data 属性提示 -->
    <el-table da></el-table>
    
    <!-- 测试空格后的属性补全 -->
    <el-button ></el-button>
    
    <!-- 测试部分属性名 -->
    <el-input dat></el-input>
    
  </div>
</template>

<script>
export default {
  name: 'AttributeCompletionDebugTest',
  data() {
    return {
      tableData: [],
      loading: false
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
</style> 