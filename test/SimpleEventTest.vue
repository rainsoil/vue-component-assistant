<template>
    <div>



      <el-table   data="" ></el-table>
        <el-table  data="[]" select="当用户手动勾选数据行的 Checkbox 时触发的事件" >

        </el-table>

        <el-table data="[]" border="false"  >

        </el-table>

        <el-input placeholder="" @clear="">

        </el-input>
        <el-table data=""></el-table>
        <el-button disabled="false" @click="handleClick">
            <template #default>
                <!-- 按钮内容 -->
            </template>
        </el-button>

        <el-button disabled="false" @click="handleClick">
            <template #default>
                <!-- 按钮内容 -->
            </template>
        </el-button>

        <el-button disabled="false"></el-button>
        <el-button disabled="false"></el-button>
        <el-button>

        </el-button>


        <el-input disabled="false" @change="handleChange">
            <template #append>
                <!-- 输入框后置内容 -->
            </template>
        </el-input>

        <el-input>
            <template #append>
                <!-- 输入框后置内容 -->
            </template>
        </el-input>
        <el-input disabled="false" @clear="handleClear"></el-input>

        <el-input disabled="false" placeholder=无>
        </el-input>

        <el-input disabled="false" placeholder=无>

        </el-input>
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
import ElementUIDetectionTest from "./ElementUIDetectionTest.vue";

export default {
    name: 'SimpleEventTest',
    components: {ElementUIDetectionTest}
}
</script>

<style>
/* 测试样式 */
</style>
