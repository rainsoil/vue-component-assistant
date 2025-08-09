<template>
    <div>


        <el-table data="[]" @sort-change="handleSort-change">
            <template #default>
                <!-- 自定义列内容 -->
            </template>
        </el-table>
        <my-button type="primary">
            <template #default>
                <!-- 按钮内容 -->
            </template>
        </my-button>
    </div>
</template>

<script>
export default {
    name: 'SimpleEventTest'
}
</script>
