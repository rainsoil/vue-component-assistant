<template>
  <!-- 测试悬浮提示和右键菜单文档功能 -->
  <div>
    <!-- 测试按钮组件 -->
    <el-button type="primary" @click="handleClick">
      主要按钮
    </el-button>
    
    <!-- 测试输入框组件 -->
    <el-input 
      v-model="inputValue"
      placeholder="请输入内容"
      clearable
      @input="handleInput"
      @change="handleChange"
    />
    
    <!-- 测试选择器组件 -->
    <el-select 
      v-model="selectedValue"
      placeholder="请选择"
      @change="handleSelectChange"
    >
      <el-option label="选项1" value="1" />
      <el-option label="选项2" value="2" />
    </el-select>
    
    <!-- 测试表格组件 -->
    <el-table 
      :data="tableData"
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column prop="name" label="姓名" sortable />
      <el-table-column prop="age" label="年龄" />
    </el-table>
    
    <!-- 测试表单组件 -->
    <el-form 
      :model="formData"
      @submit="handleSubmit"
      @validate="handleValidate"
    >
      <el-form-item label="用户名" prop="username">
        <el-input v-model="formData.username" />
      </el-form-item>
    </el-form>
    
    <!-- 测试对话框组件 -->
    <el-dialog 
      v-model="dialogVisible"
      title="对话框"
      @close="handleDialogClose"
      @open="handleDialogOpen"
    >
      <span>对话框内容</span>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'TestDocumentation',
  data() {
    return {
      inputValue: '',
      selectedValue: '',
      tableData: [
        { name: '张三', age: 25 },
        { name: '李四', age: 30 }
      ],
      formData: {
        username: ''
      },
      dialogVisible: false
    }
  },
  methods: {
    handleClick() {
      console.log('按钮点击')
    },
    handleInput(value) {
      console.log('输入:', value)
    },
    handleChange(value) {
      console.log('变化:', value)
    },
    handleSelectChange(value) {
      console.log('选择变化:', value)
    },
    handleSelectionChange(selection) {
      console.log('选择变化:', selection)
    },
    handleSortChange(sort) {
      console.log('排序变化:', sort)
    },
    handleSubmit() {
      console.log('表单提交')
    },
    handleValidate(valid) {
      console.log('验证结果:', valid)
    },
    handleDialogClose() {
      console.log('对话框关闭')
    },
    handleDialogOpen() {
      console.log('对话框打开')
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
</style>
