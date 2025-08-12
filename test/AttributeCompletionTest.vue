<template>
  <div>
    <!-- 测试属性补全 -->
    <!-- 在 el-table 后面输入空格，应该出现属性提示 -->
    <el-table></el-table>
    
    <!-- 测试属性补全 - 输入部分属性名 -->
    <el-button></el-button>
    
    <!-- 测试 v-bind 属性补全 -->
    <el-input></el-input>
    
    <!-- 测试事件补全 -->
    <el-button></el-button>
    
  </div>
</template>

<script>
export default {
  name: 'AttributeCompletionTest',
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