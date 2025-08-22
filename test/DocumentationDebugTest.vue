<template>
  <div class="test-container">
    <h1>测试悬停文档</h1>
    <el-button type="primary">Primary Button</el-button>
    <el-input v-model="inputValue" placeholder="请输入内容"></el-input>
    <el-table :data="tableData" height="250">
      <el-table-column prop="name" label="姓名" width="180"></el-table-column>
      <el-table-column prop="age" label="年龄" width="180"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'DocumentationDebugTest',
  data() {
    return {
      inputValue: '',
      tableData: [
        { name: '张三', age: 20 },
        { name: '李四', age: 25 }
      ]
    }
  }
}
</script>

<style scoped>
.test-container {
  padding: 20px;
}
</style>
