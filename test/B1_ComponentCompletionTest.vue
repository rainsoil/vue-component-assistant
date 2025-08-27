<template>
    <div>
        <!-- B1测试：组件标签补全 -->
        <!-- 在下面输入 <el- 应该能看到组件补全提示 -->

        <el-table data=""></el-table>
        <el-input disabled="" modelValue=""></el-input>
        <el-table data="" @select="" @selection-change="">
            <template #append>

            </template>
            <template #empty>

            </template>
            <template #default>

            </template>
            <template #reference="scope">

            </template>
        </el-table>

        <el-input placeholder="" disabled="" @clear="" @input="" @blur=""></el-input>
        <el-button type="primary"></el-button>
        <el-input type="password" disabled="false"></el-input>
        <!-- 测试各种组件 -->
        <el-button type="warning">按钮</el-button>
        <el-input v-model="inputValue" placeholder="输入框"></el-input>
        <el-table :data="tableData">
            <el-table-column prop="name" label="名称"></el-table-column>
        </el-table>
        <el-form :model="formData">
            <el-form-item label="用户名">
                <el-input v-model="formData.username"></el-input>
            </el-form-item>
        </el-form>
        <el-dialog v-model="dialogVisible" title="对话框">
            <div>对话框内容</div>
        </el-dialog>

        <!-- 测试新组件输入 -->
        <!-- 在这里输入 <el- 应该看到补全提示 -->
    </div>
</template>

<script>
export default {
    name: 'B1ComponentCompletionTest',
    data() {
        return {
            inputValue: '',
            tableData: [
                {name: '测试数据1'},
                {name: '测试数据2'}
            ],
            formData: {
                username: ''
            },
            dialogVisible: false
        }
    }
}
</script>

<style scoped>
/* 测试样式 */
</style> 