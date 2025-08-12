<template>
    <div>

        <el-table data="[]" border="false" @sort-change="handleSort-change" @current-change="handleCurrent-change">
            <template #default>
                <!-- 自定义列内容 -->
            </template>
        </el-table>
        <el-input placeholder="" disabled="false">
            <template #prepend>
                <!-- 输入框头部内容 -->
            </template>
            <template #append>
                <!-- 输入框尾部内容 -->
            </template>
        </el-input>
    </div>
</template>

<script>
export default {
    name: 'SimpleEventTest'
}
</script>

<style>
/* 测试样式 */
</style>
