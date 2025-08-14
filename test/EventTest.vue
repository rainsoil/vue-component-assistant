<template>
  <div>
    <!-- 测试上传组件事件补全 -->
    <el-upload 
      <!-- 在这里输入 @su 应该能看到 success 事件 -->
    ></el-upload>
    
    <!-- 测试按钮事件补全 -->
    <el-button 
      <!-- 在这里输入 @cl 应该能看到 click 事件 -->
    >按钮</el-button>
    
    <!-- 测试输入框事件补全 -->
    <el-input 
      <!-- 在这里输入 @in 应该能看到 input 事件 -->
    />
    
    <!-- 测试表单事件补全 -->
    <el-form 
      <!-- 在这里输入 @va 应该能看到 validate 事件 -->
    >
      <el-form-item label="用户名">
        <el-input />
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
export default {
  name: 'EventTest',
  methods: {
    // 上传事件
    handleSuccess(response, file, fileList) {
      console.log('上传成功');
    },
    handleError(error, file, fileList) {
      console.log('上传失败');
    },
    handleChange(file, fileList) {
      console.log('文件变化');
    },
    
    // 按钮事件
    handleClick() {
      console.log('按钮点击');
    },
    
    // 输入框事件
    handleInput(value) {
      console.log('输入值:', value);
    },
    
    // 表单事件
    handleValidate(valid, fields) {
      console.log('表单验证:', valid);
    }
  }
}
</script>

<style scoped>
.event-test {
  padding: 20px;
}
</style>
