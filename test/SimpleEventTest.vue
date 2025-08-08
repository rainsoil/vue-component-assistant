<template>
    <div>


        <my-button></my-button>
        <my-input></my-input>


        <el-table border="false" @sort-change="handleSort-change">
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
