<template>
  <div>
    <!-- 测试组件属性的真实默认值功能 -->
    <!-- 选中属性后会插入: 属性="组件库中配置的真实默认值" -->
    
    <!-- 测试el-button组件的属性补全 -->
    <el-button ty></el-button>
    <el-button si></el-button>
    <el-button dis></el-button>
    
    <!-- 测试el-input组件的属性补全 -->
    <el-input mod></el-input>
    <el-input pla></el-input>
    <el-input dis></el-input>
    
    <!-- 测试el-table组件的属性补全 -->
    <el-table da></el-table>
    <el-table he></el-table>
    <el-table bo></el-table>
    
  </div>
</template>

<script>
export default {
  name: 'ComponentDefaultValueTest',
  data() {
    return {
      text: '',
      tableData: [],
      loading: false
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
</style> 