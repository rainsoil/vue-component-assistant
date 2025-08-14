<template>
  <div>
    <!-- 测试线程问题修复 - 简化版 -->
    <el-button type="primary" @click="handleClick">
      测试按钮
    </el-button>
    
    <el-input v-model="inputValue" placeholder="测试输入框"></el-input>
  </div>
</template>

<script>
export default {
  name: 'ThreadingFixTest2',
  data() {
    return {
      inputValue: ''
    }
  },
  methods: {
    handleClick() {
      console.log('按钮被点击')
    }
  }
}
</script> 