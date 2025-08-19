<template>
    <div>

        <el-collapse-item></el-collapse-item>
        <el-input></el-input>
        <el-input placeholder="">
            <template #append>
                <!-- 输入框后置内容 -->
            </template>
        </el-input>

        <el-input disabled="false">
            <template #append>
                <!-- 输入框后置内容 -->
            </template>
        </el-input>

        <my-button type="default"></my-button>
        <my-input placeholder="" @clear="handleClear">
            <template #append>
                <!-- 输入框后置内容 -->
            </template>
        </my-input>

        <el-input disabled="false" @clear="handleClear">
            <template #append>
                <!-- 输入框后置内容 -->
            </template>
        </el-input>

        <el-input disabled="false">
            <template #suffix>
                <!-- 输入框尾部内容 -->
            </template>
            <template #append>
                <!-- 输入框后置内容 -->
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
