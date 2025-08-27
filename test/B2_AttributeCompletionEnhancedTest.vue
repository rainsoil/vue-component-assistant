<template>
  <div>
    <!-- B2增强测试：属性补全与值枚举 -->
    <!-- 测试属性名称补全：在el-button标签内输入空格，然后输入ty，应该看到type属性提示 -->
    <!-- 测试属性值枚举：在type="后输入，应该看到可选值 -->
    <!-- 测试属性描述显示：悬停在属性上应该显示描述 -->
    
    <!-- 测试按钮组件属性补全 -->
    <el-button 
      <!-- 在这里输入空格，然后输入ty，应该看到type属性提示 -->
      <!-- 在type="后输入，应该看到primary, success, warning等选项 -->
      type="primary" 
      size="large" 
      disabled="false" 
      loading="false">
      按钮
    </el-button>
    
    <!-- 测试输入框组件属性补全 -->
    <el-input 
      <!-- 在这里输入空格，然后输入ty，应该看到type属性提示 -->
      <!-- 在type="后输入，应该看到text, textarea, password等选项 -->
      v-model="inputValue" 
      placeholder="请输入内容" 
      type="text" 
      disabled="false">
    </el-input>
    
    <!-- 测试表格组件属性补全 -->
    <el-table 
      <!-- 在这里输入空格，应该看到data, border, stripe等属性提示 -->
      :data="tableData" 
      border="true" 
      stripe="true">
      <el-table-column prop="name" label="名称"></el-table-column>
    </el-table>
    
    <!-- 测试表单组件属性补全 -->
    <el-form 
      <!-- 在这里输入空格，应该看到model, label-width, label-position等属性提示 -->
      :model="formData" 
      label-width="100px" 
      label-position="right">
      <el-form-item label="用户名">
        <el-input v-model="formData.username"></el-input>
      </el-form-item>
    </el-form>
    
    <!-- 测试对话框组件属性补全 -->
    <el-dialog 
      <!-- 在这里输入空格，应该看到v-model, title, width, center等属性提示 -->
      v-model="dialogVisible" 
      title="对话框" 
      width="50%" 
      center="true">
      <div>对话框内容</div>
    </el-dialog>
    
    <!-- 测试新组件属性补全 -->
    <!-- 在这里输入 <el-button 然后输入空格，应该看到属性补全提示 -->
    <!-- 输入ty应该看到type属性，输入si应该看到size属性 -->
  </div>
</template>

<script>
export default {
  name: 'B2AttributeCompletionEnhancedTest',
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
