<template>
  <div>
    <!-- B1修复测试：组件标签补全 -->
    <!-- 在下面输入 <el-in 应该能看到组件补全提示 -->
    <!-- 选中后应该显示正确的标签，如 <el-input></el-input> (不会多出<符号) -->
    <!-- 描述应该显示JSON中的真实描述 -->
    
    <!-- 测试各种组件 -->
    <el-button type="primary">按钮</el-button>
    <el-input v-model="inputValue" placeholder="输入框"></el-input>
    <el-table :data="tableData">
      <el-table-column prop="name" label="名称"></el-table-column>
    </el-table>
    <el-form :model="formData">
      <el-form-item label="用户名">
        <el-input v-model="formData.username"></el-input>
      </el-form-item>
    </el-form>
    <el-dialog v-model="dialogVisible" title="对话框">
      <div>对话框内容</div>
    </el-dialog>
    
    <!-- 测试新组件输入 -->
    <!-- 在这里输入 <el- 应该看到补全提示 -->
    <!-- 选中后应该显示正确的标签，不会多出<符号 -->
  </div>
</template>

<script>
export default {
  name: 'B1ComponentCompletionFixedTest',
  data() {
    return {
      inputValue: '',
      tableData: [
        { name: '测试数据1' },
        { name: '测试数据2' }
      ],
      formData: {
        username: ''
      },
      dialogVisible: false
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
</style> 