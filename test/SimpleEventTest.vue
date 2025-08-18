<template>
    <div>


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
