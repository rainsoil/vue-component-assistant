<template>
  <div>
    <!-- 测试导入组件库时的加载状态 -->
    <!-- 这个文件用于测试导入组件库时是否正确显示加载状态 -->
    
    <!-- 测试组件 -->
    <el-button type="primary" @click="handleClick">
      测试按钮
    </el-button>
    
    <el-input
      v-model="inputValue"
      placeholder="测试输入框"
      clearable
      show-word-limit
      maxlength="50">
    </el-input>
    
    <el-select v-model="selectValue" placeholder="测试选择器">
      <el-option label="选项1" value="1"></el-option>
      <el-option label="选项2" value="2"></el-option>
      <el-option label="选项3" value="3"></el-option>
    </el-select>
    
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="date" label="日期" width="180"></el-table-column>
      <el-table-column prop="name" label="姓名" width="180"></el-table-column>
      <el-table-column prop="address" label="地址"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'LoadingStateTest',
  data() {
    return {
      inputValue: '',
      selectValue: '',
      tableData: [
        {
          date: '2023-05-03',
          name: '张三',
          address: '北京市朝阳区'
        },
        {
          date: '2023-05-02',
          name: '李四',
          address: '上海市浦东新区'
        }
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