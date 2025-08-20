<template>
  <div>
    <!-- 测试 VueKit XML 扩展点的组件补全功能 -->
    <!-- 输入 < 后应该能看到 VueKit 组件的补全建议 -->
    
    <!-- Element Plus 组件测试 -->
    <el-button>按钮</el-button>
    <el-table>
      <el-table-column></el-table-column>
    </el-table>
    
    <!-- Ant Design Vue 组件测试 -->
    <a-button>按钮</a-button>
    <a-table>
      <a-table-column></a-table-column>
    </a-table>
    
    <!-- 自定义组件测试 -->
    <custom-component></custom-component>
    
    <!-- 测试属性补全 -->
    <el-button type="primary" size="large" disabled>主要按钮</el-button>
    
    <!-- 测试事件补全 -->
    <el-button @click="handleClick" @change="handleChange">事件按钮</el-button>
    
    <!-- 测试插槽补全 -->
    <el-card>
      <template #header>卡片标题</template>
      <template #default>卡片内容</template>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'VueKitXmlExtensionTest',
  methods: {
    handleClick() {
      console.log('按钮被点击');
    },
    handleChange() {
      console.log('按钮状态改变');
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
.test-container {
  padding: 20px;
}
</style> 