<template>
  <div class="event-completion-test">
    <!-- 测试1: 空的事件补全 -->
    <el-button 
      <!-- 在这里输入 @ 应该能看到 el-button 的事件 -->
    >按钮</el-button>
    
    <!-- 测试2: 已有属性的事件补全 -->
    <el-input 
      placeholder="请输入内容"
      <!-- 在这里输入 @ 应该能看到 el-input 的事件 -->
    />
    
    <!-- 测试3: 已有事件的事件补全 -->
    <el-select 
      @change="handleChange"
      <!-- 在这里输入 @ 应该能看到 el-select 的其他事件 -->
    >
      <el-option label="选项1" value="1" />
    </el-select>
    
    <!-- 测试4: 多个事件的事件补全 -->
    <el-table 
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
      <!-- 在这里输入 @ 应该能看到 el-table 的其他事件 -->
    >
      <el-table-column prop="name" label="姓名" />
    </el-table>
    
    <!-- 测试5: 表单事件补全 -->
    <el-form 
      <!-- 在这里输入 @ 应该能看到 el-form 的事件 -->
    >
      <el-form-item label="用户名">
        <el-input />
      </el-form-item>
    </el-form>
    
    <!-- 测试6: 对话框事件补全 -->
    <el-dialog 
      <!-- 在这里输入 @ 应该能看到 el-dialog 的事件 -->
    >
      <span>对话框内容</span>
    </el-dialog>
    
    <!-- 测试7: 进度条事件补全 -->
    <el-progress 
      <!-- 在这里输入 @ 应该能看到 el-progress 的事件 -->
    />
    
    <!-- 测试8: 滑块事件补全 -->
    <el-slider 
      <!-- 在这里输入 @ 应该能看到 el-slider 的事件 -->
    />
  </div>
</template>

<script>
export default {
  name: 'EventCompletionTest',
  data() {
    return {
      // 测试数据
      inputValue: '',
      selectedValue: '',
      tableData: [],
      formData: {},
      dialogVisible: false,
      progressValue: 0,
      sliderValue: 0
    }
  },
  methods: {
    // 按钮事件
    handleClick() {
      console.log('按钮点击');
    },
    handleDblclick() {
      console.log('按钮双击');
    },
    
    // 输入框事件
    handleInput(value) {
      console.log('输入值:', value);
    },
    handleChange(value) {
      console.log('值变化:', value);
    },
    handleFocus() {
      console.log('获得焦点');
    },
    handleBlur() {
      console.log('失去焦点');
    },
    
    // 选择器事件
    handleSelectChange(value) {
      console.log('选择变化:', value);
    },
    handleVisibleChange(visible) {
      console.log('下拉框显示状态:', visible);
    },
    handleRemoveTag(tag) {
      console.log('移除标签:', tag);
    },
    
    // 表格事件
    handleSelectionChange(selection) {
      console.log('选择变化:', selection);
    },
    handleSortChange(sort) {
      console.log('排序变化:', sort);
    },
    handleRowClick(row) {
      console.log('行点击:', row);
    },
    
    // 表单事件
    handleSubmit() {
      console.log('表单提交');
    },
    handleValidate(valid) {
      console.log('表单验证:', valid);
    },
    
    // 对话框事件
    handleDialogClose() {
      console.log('对话框关闭');
    },
    handleDialogOpen() {
      console.log('对话框打开');
    },
    
    // 进度条事件
    handleProgressChange(value) {
      console.log('进度变化:', value);
    },
    
    // 滑块事件
    handleSliderChange(value) {
      console.log('滑块值:', value);
    }
  }
}
</script>

<style scoped>
.event-completion-test {
  padding: 20px;
}
</style>
