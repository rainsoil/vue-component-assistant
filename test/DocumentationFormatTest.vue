<template>
  <div>
    <!-- 测试新的文档格式 -->
    <!-- 在下面的组件上悬浮鼠标或右键查看文档，应该看到新的表格格式 -->
    
    <!-- 测试组件 -->
    <el-carousel 
      height="400px" 
      :initial-index="0"
      @change="handleChange">
      <el-carousel-item>
        <h3>1</h3>
      </el-carousel-item>
      <el-carousel-item>
        <h3>2</h3>
      </el-carousel-item>
    </el-carousel>
  </div>
</template>

<script>
export default {
  name: 'DocumentationFormatTest',
  methods: {
    handleChange(index, oldIndex) {
      console.log('Carousel changed:', index, oldIndex);
    }
  }
}
</script> 