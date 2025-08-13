<template>
  <div>
    <!-- 测试删除组件库后的补全提示 -->
    <!-- 1. 先下载一个官方组件库 -->
    <!-- 2. 在下面输入组件前缀，应该能看到下载的组件 -->
    <!-- 3. 删除该组件库 -->
    <!-- 4. 再次输入组件前缀，应该看不到已删除的组件 -->
    
    <!-- 测试区域：在这里输入组件前缀 -->
    <test-component></test-component>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryDeleteTest'
}
</script> 