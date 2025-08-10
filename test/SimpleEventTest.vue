<template>
    <div>


        <el-table data="[]" border="false" @sort-change="handleSort-change">
            <template #default>
                <!-- 自定义列内容 -->
            </template>
        </el-table>
    </div>
</template>

<script>
export default {
    name: 'SimpleEventTest'
}
</script>
