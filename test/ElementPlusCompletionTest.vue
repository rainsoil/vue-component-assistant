<template>


    <el-button type="default" ></el-button>
    <el-input placeholder=""></el-input>
    <el-upload headers="{}" action="" accept="" @succ></el-upload>


</template>

<script>
export default {
    name: 'ElementPlusDemo',
    data() {
        return {}
    },
    methods: {}
}
</script>

<style scoped>
.element-plus-demo {
    padding: 20px;
}

.dialog-footer {
    text-align: right;
}

.el-button {
    margin-right: 10px;
    margin-bottom: 10px;
}

.el-input,
.el-select {
    margin-bottom: 10px;
    width: 200px;
}

.el-form {
    margin-bottom: 20px;
}

.el-table {
    margin-bottom: 20px;
}

.el-card {
    margin-bottom: 20px;
}

.el-tabs {
    margin-bottom: 20px;
}

.el-progress {
    margin-bottom: 20px;
}
</style>
