<template>
    <div>


        <el-input>
            <template #prepend>
                <!-- 输入框头部内容 -->
            </template>
            <template #append>
                <!-- 输入框尾部内容 -->
            </template>

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
