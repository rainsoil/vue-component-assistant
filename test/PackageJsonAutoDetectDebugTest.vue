<template>
  <div>
    <!-- 只使用 Element UI 组件进行测试 -->
    <el-button type="primary">Element UI Button</el-button>
    <el-input placeholder="Element UI Input"></el-input>
    <el-table :data="tableData">
      <el-table-column prop="name" label="Name"></el-table-column>
      <el-table-column prop="age" label="Age"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'PackageJsonAutoDetectDebugTest',
  data() {
    return {
      tableData: [
        { name: 'John', age: 25 },
        { name: 'Jane', age: 30 }
      ]
    }
  },
  mounted() {
    console.log('Package.json 自动检测调试测试组件已加载');
    console.log('此项目应该只检测到 element-ui，不应该检测到 element-plus');
  }
}
</script>

<style scoped>
/* 测试样式 */
.debug-container {
  padding: 20px;
  border: 2px solid #ff6b6b;
  margin: 10px;
  background-color: #fff5f5;
}
</style> 