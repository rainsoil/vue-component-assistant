<template>
    <div>


        <el-input placeholder="" @clear="handleClear">




        </el-input>
        <el-table>
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
