<template>
    <div>

        <my-button type="primary">
            <template #default>
                <!-- 按钮内容 -->
            </template>
        </my-button>

        <my-button>
            <template #default>
                <!-- 按钮内容 -->
            </template>
        </my-button>
        <my-input>

        </my-input>


        <el-table border="false" data="[]" @sort-change="handleSort-change">
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
