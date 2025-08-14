<template>
  <div>
    <!-- 测试新的文档格式 -->
    <!-- 在下面的组件上悬浮鼠标或右键查看文档，应该看到新的格式 -->
    
    <!-- 测试组件 -->
    <el-upload
      action="https://jsonplaceholder.typicode.com/posts/"
      :on-preview="handlePreview"
      :on-remove="handleRemove"
      :before-remove="beforeRemove"
      multiple
      :limit="3"
      :on-exceed="handleExceed"
      :file-list="fileList">
      <el-button size="small" type="primary">点击上传</el-button>
      <template #tip>
        <div class="el-upload__tip">
          只能上传jpg/png文件，且不超过500kb
        </div>
      </template>
    </el-upload>
  </div>
</template>

<script>
export default {
  name: 'NewDocumentationFormatTest',
  data() {
    return {
      fileList: []
    }
  },
  methods: {
    handleRemove(file, fileList) {
      console.log(file, fileList);
    },
    handlePreview(file) {
      console.log(file);
    },
    handleExceed(files, fileList) {
      console.log(files, fileList);
    },
    beforeRemove(file, fileList) {
      return true;
    }
  }
}
</script> 