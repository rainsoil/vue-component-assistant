<template>
  <div>
    <!-- 测试上传组件事件补全 -->
    <el-upload 
      <!-- 在这里输入 @su 应该能看到 submit 事件 -->
    ></el-upload>
    
    <!-- 测试其他事件 -->
    <el-upload 
      <!-- 在这里输入 @ 应该能看到所有上传事件 -->
    ></el-upload>
  </div>
</template>

<script>
export default {
  name: 'UploadEventTest',
  methods: {
    handleSubmit() {
      console.log('文件提交');
    },
    handleSuccess() {
      console.log('上传成功');
    },
    handleError() {
      console.log('上传失败');
    }
  }
}
</script>
