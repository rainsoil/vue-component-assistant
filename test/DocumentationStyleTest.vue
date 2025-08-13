<template>
  <div>
    <!-- 测试新的文档样式 -->
    <!-- 1. 将鼠标悬浮在下面的组件上，应该看到使用 IDEA 背景色的文档 -->
    <!-- 2. 右键点击组件选择"查看组件文档"，应该看到更紧凑的表格样式 -->
    
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
    
    <el-table
      :data="tableData"
      border
      style="width: 100%">
      <el-table-column
        prop="date"
        label="日期"
        width="180">
      </el-table-column>
      <el-table-column
        prop="name"
        label="姓名"
        width="180">
      </el-table-column>
      <el-table-column
        prop="address"
        label="地址">
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'DocumentationStyleTest',
  data() {
    return {
      value: '',
      options: [
        { value: 'option1', label: '选项1' },
        { value: 'option2', label: '选项2' },
        { value: 'option3', label: '选项3' }
      ],
      tableData: [
        {
          date: '2023-05-03',
          name: '张三',
          address: '北京市朝阳区'
        },
        {
          date: '2023-05-02',
          name: '李四',
          address: '上海市浦东新区'
        }
      ]
    }
  }
}
</script> 