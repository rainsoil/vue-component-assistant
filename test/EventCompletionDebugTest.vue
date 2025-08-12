<template>
  <div>
    <!-- 测试1: 基础事件补全 -->
    <el-table @c>
      <!-- 这里应该能触发事件补全，显示以c开头的事件 -->
    </el-table>
    
    <!-- 测试2: 带属性的事件补全 -->
    <el-table data="[]" @cu>
      <!-- 这里应该也能触发事件补全，显示以cu开头的事件 -->
    </el-table>
    
    <!-- 测试3: 多个属性后的事件补全 -->
    <el-table data="[]" border="false" height="" @change>
      <!-- 这里应该也能触发事件补全 -->
    </el-table>
    
    <!-- 测试4: 有问题的场景 -->
    <el-table data="[]" border="false" height="""" @cu>
      <!-- 这里应该也能触发事件补全 -->
    </el-table>
    
    <!-- 测试5: 不同长度的事件前缀 -->
    <el-button @cl>
      <!-- 这里应该显示以cl开头的事件 -->
    </el-button>
    
    <el-input @in>
      <!-- 这里应该显示以in开头的事件 -->
    </el-input>
    
    <!-- 测试6: 空事件前缀 -->
    <el-select @>
      <!-- 这里应该显示所有事件 -->
    </el-select>
  </div>
</template>

<script>
export default {
  name: 'EventCompletionDebugTest',
  methods: {
    handleClick() {
      console.log('按钮被点击');
    },
    handleInput(value) {
      console.log('输入值:', value);
    },
    handleChange(value) {
      console.log('值改变:', value);
    },
    handleCurrentChange(value) {
      console.log('当前行改变:', value);
    }
  }
}
</script>
