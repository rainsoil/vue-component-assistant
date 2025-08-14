<template>
  <div>
    <!-- 测试简化的属性和事件匹配规则 -->
    
    <!-- 测试属性补全 -->
    <el-table da></el-table>
    <el-button ></el-button>
    <el-input dat></el-input>
    
    <!-- 测试事件补全 -->
    <el-button @cl></el-button>
    <el-table @row></el-table>
    
    <!-- 测试v-bind补全 -->
    <el-input :mod></el-input>
    <el-button :typ></el-button>
    
    <!-- 测试插槽补全 -->
    <el-table #def></el-table>
    <el-button #def></el-button>
    
  </div>
</template>

<script>
export default {
  name: 'SimplifiedCompletionTest',
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