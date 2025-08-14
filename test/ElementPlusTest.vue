<template>
  <div class="test-container">
    <!-- 测试组件补全 -->
    <!-- 在这里输入 < 应该能看到组件列表 -->
    <!-- 输入 el-bu 应该能看到 el-button -->
    
    <!-- 测试属性补全 -->
    <el-button 
      <!-- 在这里输入属性名，应该只显示 el-button 的属性 -->
    >按钮</el-button>
    
    <!-- 测试事件补全 -->
    <el-input 
      <!-- 在这里输入 @ 应该能看到 el-input 的事件 -->
    />
    
    <!-- 测试不同组件的属性隔离 -->
    <el-select 
      <!-- 这里应该只显示 el-select 的属性，不显示 el-button 的属性 -->
    >
      <el-option label="选项1" value="1" />
    </el-select>
    
    <!-- 测试表单组件 -->
    <el-form>
      <el-form-item label="用户名">
        <el-input 
          <!-- 这里应该只显示 el-input 的属性 -->
        />
      </el-form-item>
    </el-form>
    
    <!-- 测试表格组件 -->
    <el-table 
      <!-- 这里应该只显示 el-table 的属性 -->
    >
      <el-table-column 
        <!-- 这里应该只显示 el-table-column 的属性 -->
      />
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'ElementPlusTest',
  data() {
    return {
      // 测试数据
    }
  },
  methods: {
    // 测试方法
  }
}
</script>

<style scoped>
.test-container {
  padding: 20px;
}
</style>
