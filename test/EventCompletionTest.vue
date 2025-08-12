<template>
  <div>
    <!-- 测试场景1: 正常的事件补全 -->
    <el-table data="[]" border="false" height="" @c="">
      <!-- 这里应该能触发事件补全 -->
    </el-table>
    
    <!-- 测试场景2: 有问题的场景 -->
    <el-table data="[]" border="false" height="""" @cu="">
      <!-- 这里应该也能触发事件补全 -->
    </el-table>
    
    <!-- 测试场景3: 多个引号的情况 -->
    <el-table data="[]" border="false" height=""""" @cur="">
      <!-- 这里应该也能触发事件补全 -->
    </el-table>
    
    <!-- 测试场景4: 混合属性 -->
    <el-table data="[]" border="false" height="""" width="100" @change="">
      <!-- 这里应该也能触发事件补全 -->
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'EventCompletionTest',
  methods: {
    handleCurrentChange() {
      console.log('current change');
    },
    handleSortChange() {
      console.log('sort change');
    }
  }
}
</script>
