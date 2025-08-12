<template>
  <div>
    <!-- 测试1: 空的事件补全 -->
    <el-table 
      data="[]" 
      stripe="false"
    ></el-table>
    
    <!-- 测试2: 已有属性的事件补全 -->
    <el-input 
      placeholder="请输入内容"
    />
    
    <!-- 测试3: 已有事件的事件补全 -->
    <el-select 
      @change="handleChange"
    >
      <el-option label="选项1" value="1" />
    </el-select>
    
    <!-- 测试4: 多个事件的事件补全 -->
    <el-table 
      @selection-change="handleSelectionChange"
      @sort-change="handleSortChange"
    >
      <el-table-column prop="name" label="姓名" />
    </el-table>
    
    <!-- 测试5: 表单事件补全 -->
    <el-form>
      <el-form-item label="用户名">
        <el-input />
      </el-form-item>
    </el-form>
    
    <!-- 测试6: 按钮事件补全 -->
    <el-button>按钮</el-button>
  </div>
</template>

<script>
export default {
  name: 'EventCompletionTest',
  data() {
    return {
      message: 'Hello Vue!'
    }
  },
  methods: {
    handleClick() {
      console.log('按钮被点击')
    },
    handleInput(value) {
      console.log('输入值:', value)
    },
    handleChange(value) {
      console.log('值改变:', value)
    },
    handleSubmit() {
      console.log('表单提交')
    },
    handleFocus() {
      console.log('获得焦点')
    },
    handleBlur() {
      console.log('失去焦点')
    },
    handleSelectionChange(selection) {
      console.log('选择改变:', selection)
    },
    handleSortChange(sort) {
      console.log('排序改变:', sort)
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
</style>
