<template>
  <div>
    <!-- 测试递归更新问题修复 -->
    <!-- 这个文件用于测试文档悬浮提示是否正常工作，不再出现递归更新错误 -->
    
    <!-- 测试组件 -->
    <el-button type="primary">
      测试按钮
    </el-button>
    
    <el-input
      v-model="inputValue"
      placeholder="测试输入框">
    </el-input>
    
    <el-select v-model="selectValue" placeholder="测试选择器">
      <el-option label="选项1" value="1"></el-option>
      <el-option label="选项2" value="2"></el-option>
    </el-select>
    
    <el-table :data="tableData">
      <el-table-column prop="name" label="姓名"></el-table-column>
      <el-table-column prop="age" label="年龄"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'RecursiveUpdateFixTest',
  data() {
    return {
      inputValue: '',
      selectValue: '',
      tableData: [
        { name: '张三', age: 25 },
        { name: '李四', age: 30 }
      ]
    }
  }
}
</script> 