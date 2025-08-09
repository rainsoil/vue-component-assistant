<template>
    <div>


        <el-table stripe="false" @sort-change="handleSort-change" data="[]">
            <template #default>
                <!-- 自定义列内容 -->
            </template>
        </el-table>
        <my-input>

        </my-input>
        <my-button type="primary">
            <template #default>
                <!-- 按钮内容 -->
            </template>
        </my-button>

        <my-button></my-button>
    </div>
</template>

<script>
export default {
    name: 'SimpleEventTest'
}
</script>
