<template>
  <div>
    <!-- B1测试：组件标签补全 -->
      <el-button type=""></el-button>
    <!-- 在下面输入 <el- 应该能看到组件补全提示 -->
      <el-input placeholder="" type=""></el-input>
    <el-button>按钮</el-button>
    <el-input placeholder="输入框"></el-input>
    <el-table>
      <el-table-column></el-table-column>
    </el-table>
    <el-form>
      <el-form-item></el-form-item>
    </el-form>
    <el-dialog>
      <div>对话框内容</div>
    </el-dialog>
  </div>
</template>

<script>
export default {
  name: 'B1ComponentTagCompletionTest'
}
</script>

<style scoped>
/* 测试样式 */
</style> 