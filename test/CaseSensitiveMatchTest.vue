<template>
  <div>
    <!-- 测试不同大小写和空格的组件库 -->
    <el-button type="primary">Element Plus Button</el-button>
    <el-input placeholder="Element Plus Input"></el-input>
    
    <a-button type="primary">Ant Design Button</a-button>
    <a-input placeholder="Ant Design Input"></a-input>
    
    <v-btn color="primary">Vuetify Button</v-btn>
    <q-btn color="primary">Quasar Button</q-btn>
  </div>
</template>

<script>
export default {
  name: 'CaseSensitiveMatchTest',
  data() {
    return {
      message: '测试大小写和空格匹配功能'
    }
  },
  mounted() {
    console.log('大小写和空格匹配测试组件已加载');
    console.log('此项目测试以下情况：');
    console.log('- Element-Plus (大写开头) -> 应该匹配 element-plus');
    console.log('- ANT-DESIGN-VUE (全大写) -> 应该匹配 ant-design-vue');
    console.log('- Vuetify (末尾空格) -> 应该匹配 vuetify');
    console.log('- quasar (开头空格) -> 应该匹配 quasar');
  }
}
</script>

<style scoped>
/* 测试样式 */
.case-sensitive-test {
  padding: 20px;
  border: 2px solid #4CAF50;
  margin: 10px;
  background-color: #f0f8f0;
}
</style> 