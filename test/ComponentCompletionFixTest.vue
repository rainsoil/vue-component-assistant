<template>
  <div>
    <!-- 测试场景1: 输入 <el-ta 选择 el-table -->
    <!-- 期望结果: <el-table></el-table> -->
    <!-- 不应该出现: <<el-table></el-table> -->
    
    <!-- 测试场景2: 直接输入 el-table -->
    <!-- 期望结果: <el-table></el-table> -->
    
    <!-- 测试场景3: 输入 <el-bu 选择 el-button -->
    <!-- 期望结果: <el-button></el-button> -->
    
    <!-- 测试场景4: 输入 <a-bu 选择 a-button -->
    <!-- 期望结果: <a-button></a-button> -->
    
  </div>
</template>

<script>
export default {
  name: 'ComponentCompletionFixTest'
}
</script>

<style scoped>
/* 测试样式 */
</style> 