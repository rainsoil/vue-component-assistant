<template>
  <div>
    <!-- Element Plus 组件测试 -->
    <el-button type="primary">Element Plus Button</el-button>
    <el-input placeholder="Element Plus Input"></el-input>
    
    <!-- Ant Design Vue 组件测试 -->
    <a-button type="primary">Ant Design Button</a-button>
    <a-input placeholder="Ant Design Input"></a-input>
    
    <!-- 其他组件库测试 -->
    <v-btn color="primary">Vuetify Button</v-btn>
    <q-btn color="primary">Quasar Button</q-btn>
  </div>
</template>

<script>
export default {
  name: 'PackageJsonAutoDetectTest',
  data() {
    return {
      message: '测试自动检测功能'
    }
  },
  mounted() {
    console.log('Package.json 自动检测测试组件已加载');
  }
}
</script>

<style scoped>
/* 测试样式 */
.test-container {
  padding: 20px;
  border: 1px solid #ccc;
  margin: 10px;
}
</style> 