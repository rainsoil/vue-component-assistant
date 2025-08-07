<template>
  <div>
    <!-- 测试上传组件事件补全 -->
    <el-upload 
      <!-- 在这里输入 @suc 应该能看到 success 事件 -->
    ></el-upload>
    
    <!-- 测试输入框事件补全 -->
    <el-input 
      <!-- 在这里输入 @in 应该能看到 input 事件 -->
    />
  </div>
</template>

<script>
export default {
  name: 'EventDebugTest'
}
</script>
