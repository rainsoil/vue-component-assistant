<template>
  <div class="test-filtering">
    <!-- 测试组件库过滤功能 -->
    <!-- 当没有启用任何组件库时，不应该显示任何组件 -->
    
    <!-- 测试 Element Plus 组件（应该被过滤掉） -->
    <!-- <el-button>测试按钮</el-button> -->
    <!-- <el-input placeholder="测试输入框"></el-input> -->
    
    <!-- 测试 Ant Design Vue 组件（应该被过滤掉） -->
    <!-- <a-button>Ant Design 按钮</a-button> -->
    <!-- <a-input placeholder="Ant Design 输入框" /> -->
    
    <!-- 测试 Vuetify 组件（应该被过滤掉） -->
    <!-- <v-btn>Vuetify 按钮</v-btn> -->
    <!-- <v-text-field label="Vuetify 输入框"></v-text-field> -->
    
    <!-- 测试 Quasar 组件（应该被过滤掉） -->
    <!-- <q-btn>Quasar 按钮</q-btn> -->
    <!-- <q-input label="Quasar 输入框" /> -->
    
    <!-- 在这里输入 <el- 或 <a- 来测试组件补全是否被正确过滤 -->
    <!-- 预期结果：当没有启用任何组件库时，不应该显示任何组件建议 -->
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryFilteringTest3',
  data() {
    return {
      message: '测试组件库过滤功能'
    }
  },
  methods: {
    testFiltering() {
      console.log('测试组件库过滤功能');
    }
  }
}
</script>

<style scoped>
.test-filtering {
  padding: 20px;
  border: 2px dashed #ccc;
  margin: 20px;
  background-color: #f9f9f9;
}
</style> 