<template>
  <div>
    <!-- 测试组件补全 -->
    <el-button>按钮</el-button>
    
    <!-- 测试属性补全 -->
    <el-input 
      v-model="inputValue"
      placeholder="请输入内容"
      clearable
      show-password
    />
    
    <!-- 测试事件补全 -->
    <el-button @click="handleClick" @mouseenter="handleMouseEnter">
      事件测试
    </el-button>
    
    <!-- 测试插槽 -->
    <el-card>
      <template #header>
        <span>卡片标题</span>
      </template>
      <div>卡片内容</div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'XMLExtensionPointsTest',
  data() {
    return {
      inputValue: ''
    }
  },
  methods: {
    handleClick() {
      console.log('按钮被点击')
    },
    handleMouseEnter() {
      console.log('鼠标进入')
    }
  }
}
</script>

<style scoped>
/* 测试样式 */
</style>
