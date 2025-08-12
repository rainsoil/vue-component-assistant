<template>
  <div>
    <!-- 测试1: 基础组件检测 -->
    <el-table @c>
      <!-- 这里应该能检测到 el-table 组件 -->
    </el-table>
    
    <!-- 测试2: 带属性的组件检测 -->
    <el-table data="[]" @cu>
      <!-- 这里应该也能检测到 el-table 组件 -->
    </el-table>
    
    <!-- 测试3: 多个属性后的组件检测 -->
    <el-table data="[]" border="false" height="" @change>
      <!-- 这里应该也能检测到 el-table 组件 -->
    </el-table>
    
    <!-- 测试4: 有问题的场景 -->
    <el-table data="[]" border="false" height="""" @cu>
      <!-- 这里应该也能检测到 el-table 组件 -->
    </el-table>
    
    <!-- 测试5: 其他组件 -->
    <el-button @click>
      <!-- 这里应该能检测到 el-button 组件 -->
    </el-button>
    
    <el-input @input>
      <!-- 这里应该能检测到 el-input 组件 -->
    </el-input>
  </div>
</template>

<script>
export default {
  name: 'ComponentDetectionTest',
  methods: {
    handleClick() {
      console.log('按钮被点击');
    },
    handleInput(value) {
      console.log('输入值:', value);
    },
    handleChange(value) {
      console.log('值改变:', value);
    }
  }
}
</script>

<style scoped>
.component-detection-test {
  padding: 20px;
  font-family: 'Microsoft YaHei', sans-serif;
}

.test-info {
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #409EFF;
}

.component-test {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.test-section {
  margin-bottom: 25px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.test-section h4 {
  color: #409EFF;
  margin-bottom: 10px;
}

.completion-test {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  border: 1px dashed #c0c4cc;
  min-height: 40px;
  margin-top: 10px;
}

.debug-info {
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #f59e0b;
}

.debug-info ul {
  margin: 10px 0;
  padding-left: 20px;
}

.debug-info li {
  margin: 5px 0;
  color: #666;
}
</style>
