<template>
  <div>
    <!-- 测试表格列宽设置 -->
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="date" label="日期" width="180"></el-table-column>
      <el-table-column prop="name" label="姓名" width="180"></el-table-column>
      <el-table-column prop="address" label="地址"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'TableColumnWidthTest',
  data() {
    return {
      tableData: [
        { date: '2023-05-03', name: '张三', address: '北京市朝阳区' },
        { date: '2023-05-02', name: '李四', address: '上海市浦东新区' }
      ]
    }
  }
}
</script> 