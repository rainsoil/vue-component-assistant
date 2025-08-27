<template>
    <div>
        <!-- B2测试：属性补全与值枚举 -->
        <!-- 在el-button标签内输入空格，应该能看到属性补全提示 -->
        <!-- 对于有枚举值的属性，应该显示可选值 -->

        <!-- 测试按钮组件属性 -->
        <el-button
                type="primary"
                size="large"
                disabled="false"
                loading="false">
            按钮
        </el-button>


        <el
        <!-- 测试输入框组件属性 -->
        <el-input
                v-model="inputValue"
                placeholder="请输入内容"
                type="text"
                disabled="false">
        </el-input>

        <!-- 测试表格组件属性 -->
        <el-table
                :data="tableData"
                border="true"
                stripe="true">
            <el-table-column prop="name" label="名称"></el-table-column>
        </el-table>

        <!-- 测试表单组件属性 -->
        <el-form
                :model="formData"
                label-width="100px"
                label-position="right">
            <el-form-item label="用户名">
                <el-input v-model="formData.username"></el-input>
            </el-form-item>
        </el-form>

        <!-- 测试对话框组件属性 -->
        <el-dialog
                v-model="dialogVisible"
                title="对话框"
                width="50%"
                center="true">
            <div>对话框内容</div>
        </el-dialog>

        <!-- 测试新组件属性补全 -->
        <!-- 在这里输入 <el-button 然后输入空格，应该看到属性补全提示 -->
    </div>
</template>

<script>
export default {
    name: 'B2AttributeCompletionTest',
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