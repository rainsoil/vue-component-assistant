<template>
  <div>
    <!-- 测试 Element Plus 风格的文档格式 -->
    <!-- 在下面的组件上悬浮鼠标或右键查看文档，应该看到与官方文档一致的格式 -->
    
    <!-- 测试组件 -->
    <el-select
      v-model="value"
      placeholder="请选择"
      multiple
      :clearable="true"
      :collapse-tags="false"
      :multiple-limit="3"
      size="medium">
      <el-option
        v-for="item in options"
        :key="item.value"
        :label="item.label"
        :value="item.value">
      </el-option>
    </el-select>
  </div>
</template>

<script>
export default {
  name: 'ElementPlusStyleTest',
  data() {
    return {
      value: '',
      options: [
        { value: 'option1', label: '选项1' },
        { value: 'option2', label: '选项2' },
        { value: 'option3', label: '选项3' }
      ]
    }
  }
}
</script> 