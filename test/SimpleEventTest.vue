<template>
  <div>
    <!-- 测试1: 基础事件补全 -->
    <el-table data="[]" height="""" @sort-change="handleSort-change($event)">
      <!-- 这里应该能触发事件补全 -->
    </el-table>
    
    <!-- 测试2: 带属性的事件补全 -->
    <el-table data="[]" @cu>
      <!-- 这里应该也能触发事件补全 -->
    </el-table>
    
    <!-- 测试3: 多个属性后的事件补全 -->
    <el-table data="[]" border="false" @change>
      <!-- 这里应该也能触发事件补全 -->
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'SimpleEventTest'
}
</script>

<style>
/* 测试样式 */
</style>
