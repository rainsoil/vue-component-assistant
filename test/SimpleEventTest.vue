<template>
    <div>
        <el-table : @current-change="handleCurrent-change" ></el-table>
        <el-input @clear="handleClear" @change></el-input>
    </div>
</template>

<script>
export default {
    name: 'SimpleEventTest'
}
</script>
