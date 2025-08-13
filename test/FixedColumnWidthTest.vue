<template>
  <div>
    <!-- 测试固定列宽和文字换行 -->
    <el-table :data="tableData" border style="width: 100%">
      <el-table-column prop="date" label="日期" width="180"></el-table-column>
      <el-table-column prop="name" label="姓名" width="180"></el-table-column>
      <el-table-column prop="address" label="地址" width="400"></el-table-column>
    </el-table>
  </div>
</template>

<script>
export default {
  name: 'FixedColumnWidthTest',
  data() {
    return {
      tableData: [
        { 
          date: '2023-05-03', 
          name: '张三', 
          address: '北京市朝阳区建国门外大街1号国贸大厦A座1001室，这是一个非常长的地址描述，用于测试文字换行效果' 
        },
        { 
          date: '2023-05-02', 
          name: '李四', 
          address: '上海市浦东新区陆家嘴环路1000号恒生银行大厦25楼，这也是一个很长的地址，用来测试表格列宽和文字换行的显示效果' 
        }
      ]
    }
  }
}
</script> 