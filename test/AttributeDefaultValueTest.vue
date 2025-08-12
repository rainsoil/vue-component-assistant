<template>
  <div>
    <!-- 测试属性补全defaultValue功能 -->
    <!-- 选中属性后会插入: 属性="defaultValue" -->
    
    <!-- 测试组件属性补全 -->
    <el-table da></el-table>
    <el-button ></el-button>
    <el-input dat></el-input>
    
    <!-- 测试通用Vue属性补全 -->
    <div cla></div>
    <span sty></span>
    <button v-if></button>
    
    <!-- 测试v-bind属性补全 -->
    <el-input :mod></el-input>
    <el-button :typ></el-button>
    
  </div>
</template>

<script>
export default {
  name: 'AttributeDefaultValueTest',
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