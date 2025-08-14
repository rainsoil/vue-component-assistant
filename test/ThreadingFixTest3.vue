<template>
  <div>
    <!-- 测试线程问题最终修复 -->
    <el-button type="primary" @click="handleClick">
      测试按钮
    </el-button>
  </div>
</template>

<script>
export default {
  name: 'ThreadingFixTest3',
  methods: {
    handleClick() {
      console.log('按钮被点击')
    }
  }
}
</script> 