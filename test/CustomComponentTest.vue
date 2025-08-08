<template>
  <div>
    <!-- 测试自定义组件补全 -->
    <my-button type="primary">按钮</my-button>
    <my-input v-model="inputValue" placeholder="请输入内容" />
    <my-card title="卡片标题">卡片内容</my-card>
    
    <!-- 测试输入 my- 时的补全 -->
    <!-- 在这里输入 <my- 应该会提示自定义组件 -->
  </div>
</template>

<script>
export default {
  name: 'CustomComponentTest',
  data() {
    return {
      inputValue: ''
    }
  }
}
</script>
