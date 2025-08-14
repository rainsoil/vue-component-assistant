<template>
  <div>
    <!-- 测试策略模式事件补全 -->
    <el-table @curr></el-table>
    
    <!-- 测试事件补全 - 输入@后应该显示事件列表 -->
    <el-button @click="handleClick">按钮</el-button>
    
    <!-- 测试事件补全 - 输入@后应该显示事件列表 -->
    <el-input @input="handleInput" @change="handleChange" />
  </div>
</template>

<script>
export default {
  name: 'StrategyEventTest',
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
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
</style>
