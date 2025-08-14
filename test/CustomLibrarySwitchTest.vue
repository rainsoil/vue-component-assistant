<template>
  <div>
    <!-- 测试从远程JSON切换到本地JSON后的组件提示 -->
    <!-- 输入组件名前缀，应该显示最新导入的组件 -->
    
    <!-- 测试组件1：应该显示最新导入的组件 -->
    <test-component-1></test-component-1>
    
    <!-- 测试组件2：应该显示最新导入的组件 -->
    <test-component-2></test-component-2>
    
    <!-- 测试组件3：应该显示最新导入的组件 -->
    <test-component-3></test-component-3>
    
    <!-- 测试组件属性 -->
    <test-component-1 
      prop1=""
      prop2=""
      @event1=""
      @event2=""
    ></test-component-1>
    
    <!-- 测试组件插槽 -->
    <test-component-2>
      <template #slot1>
        <!-- 插槽内容 -->
      </template>
    </test-component-2>
  </div>
</template>

<script>
export default {
  name: 'CustomLibrarySwitchTest',
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
/* 测试样式 */
</style> 