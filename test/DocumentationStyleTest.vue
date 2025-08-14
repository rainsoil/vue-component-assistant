<template>
  <div>
    <!-- 测试右键查看组件文档样式 -->
    <el-table :data="tableData" border>
      <el-table-column prop="name" label="姓名"></el-table-column>
      <el-table-column prop="age" label="年龄"></el-table-column>
    </el-table>
    
    <el-button type="primary" @click="handleClick">
      测试按钮
    </el-button>
  </div>
</template>

<script>
export default {
  name: 'DocumentationStyleTest',
  data() {
    return {
      tableData: [
        { name: '张三', age: 25 },
        { name: '李四', age: 30 }
      ]
    }
  },
  methods: {
    handleClick() {
      console.log('按钮被点击')
    }
  }
}
</script> 