<template>
  <div>
    <!-- 测试属性补全 -->
    <el-table data="[]" border="false">
        
    </el-table>
    
    <!-- 测试事件补全 -->
    <el-button @click></el-button>
    
    <!-- 测试更多属性 -->
    <el-input placeholder></el-input>
    
    <!-- 测试卡槽补全 - sl前缀显示所有卡槽 -->
    <el-table>
      <template #sl></template>
    </el-table>
    
    <!-- 测试卡槽补全 - 直接输入#显示所有卡槽 -->
    <el-card>
      <template #></template>
    </el-card>
    
    <!-- 测试卡槽补全 - 输入特定前缀 -->
    <el-dialog>
      <template #header></template>
    </el-dialog>
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
