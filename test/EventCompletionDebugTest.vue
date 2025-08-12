<template>
  <div>
    <!-- 测试事件补全调试 -->
    
    <!-- 测试场景1: 基础事件补全 -->
    <el-table @></el-table>
    
    <!-- 测试场景2: 带前缀的事件补全 -->
    <el-table @cu></el-table>
    
    <!-- 测试场景3: 带属性的事件补全 -->
    <el-table data="[]" @></el-table>
    
    <!-- 测试场景4: 复杂属性后的事件补全 -->
    <el-table data="[]" border="false" height="defaultValue" @></el-table>
    
    <!-- 测试场景5: 带事件前缀的补全 -->
    <el-table data="[]" @sort></el-table>
    
  </div>
</template>

<script>
export default {
  name: 'EventCompletionDebugTest',
  data() {
    return {
      tableData: []
    }
  },
  methods: {
    handleSortChange(column, prop, order) {
      console.log('排序改变:', column, prop, order);
    },
    handleCurrentChange(currentRow, oldCurrentRow) {
      console.log('当前行改变:', currentRow, oldCurrentRow);
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
</style>
