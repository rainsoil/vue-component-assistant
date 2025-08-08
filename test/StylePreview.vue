<template>
  <!-- 新的悬浮提示样式预览 -->
  <div class="style-preview">
    <h2>🎨 新的悬浮提示样式预览</h2>
    
    <!-- 测试各种组件 -->
    <div class="component-grid">
      <!-- 按钮组件 -->
      <div class="component-card">
        <h3>按钮组件</h3>
        <el-button type="primary" @click="handleClick">
          主要按钮
        </el-button>
        <el-button type="success">
          成功按钮
        </el-button>
        <el-button type="warning">
          警告按钮
        </el-button>
        <el-button type="danger">
          危险按钮
        </el-button>
      </div>

      <!-- 输入框组件 -->
      <div class="component-card">
        <h3>输入框组件</h3>
        <el-input 
          v-model="inputValue"
          placeholder="请输入内容"
          clearable
          @input="handleInput"
          @change="handleChange"
        />
        <el-input 
          v-model="inputValue2"
          placeholder="带图标的输入框"
          prefix-icon="el-icon-search"
        />
      </div>

      <!-- 选择器组件 -->
      <div class="component-card">
        <h3>选择器组件</h3>
        <el-select 
          v-model="selectedValue"
          placeholder="请选择"
          @change="handleSelectChange"
        >
          <el-option label="选项1" value="1" />
          <el-option label="选项2" value="2" />
          <el-option label="选项3" value="3" />
        </el-select>
      </div>

      <!-- 表格组件 -->
      <div class="component-card">
        <h3>表格组件</h3>
        <el-table 
          :data="tableData"
          @selection-change="handleSelectionChange"
          @sort-change="handleSortChange"
          style="width: 100%"
        >
          <el-table-column prop="name" label="姓名" sortable />
          <el-table-column prop="age" label="年龄" />
          <el-table-column prop="address" label="地址" />
        </el-table>
      </div>

      <!-- 表单组件 -->
      <div class="component-card">
        <h3>表单组件</h3>
        <el-form 
          :model="formData"
          @submit="handleSubmit"
          @validate="handleValidate"
        >
          <el-form-item label="用户名" prop="username">
            <el-input v-model="formData.username" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="formData.email" />
          </el-form-item>
        </el-form>
      </div>

      <!-- 对话框组件 -->
      <div class="component-card">
        <h3>对话框组件</h3>
        <el-button @click="dialogVisible = true">
          打开对话框
        </el-button>
        <el-dialog 
          v-model="dialogVisible"
          title="对话框"
          @close="handleDialogClose"
          @open="handleDialogOpen"
        >
          <span>这是一个现代化的对话框内容</span>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="dialogVisible = false">取消</el-button>
              <el-button type="primary" @click="dialogVisible = false">确定</el-button>
            </span>
          </template>
        </el-dialog>
      </div>
    </div>

    <!-- 使用说明 -->
    <div class="instructions">
      <h3>📋 使用说明</h3>
      <ul>
        <li>将鼠标悬停在任何 Element Plus 组件上查看新的悬浮提示样式</li>
        <li>右键点击组件可以打开详细的文档对话框</li>
        <li>新的样式包含渐变背景、毛玻璃效果和动画</li>
        <li>支持响应式布局和现代化的视觉效果</li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'StylePreview',
  data() {
    return {
      inputValue: '',
      inputValue2: '',
      selectedValue: '',
      tableData: [
        { name: '张三', age: 25, address: '北京市朝阳区' },
        { name: '李四', age: 30, address: '上海市浦东新区' },
        { name: '王五', age: 28, address: '广州市天河区' }
      ],
      formData: {
        username: '',
        email: ''
      },
      dialogVisible: false
    }
  },
  methods: {
    handleClick() {
      console.log('按钮点击')
    },
    handleInput(value) {
      console.log('输入:', value)
    },
    handleChange(value) {
      console.log('变化:', value)
    },
    handleSelectChange(value) {
      console.log('选择变化:', value)
    },
    handleSelectionChange(selection) {
      console.log('选择变化:', selection)
    },
    handleSortChange(sort) {
      console.log('排序变化:', sort)
    },
    handleSubmit() {
      console.log('表单提交')
    },
    handleValidate(valid) {
      console.log('验证结果:', valid)
    },
    handleDialogClose() {
      console.log('对话框关闭')
    },
    handleDialogOpen() {
      console.log('对话框打开')
    }
  }
}
</script>

<style scoped>
.style-preview {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.style-preview h2 {
  text-align: center;
  color: #409EFF;
  margin-bottom: 30px;
  font-size: 28px;
}

.component-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.component-card {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.component-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.component-card h3 {
  color: #303133;
  margin-bottom: 15px;
  font-size: 18px;
  font-weight: 600;
}

.instructions {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.instructions h3 {
  margin-bottom: 15px;
  font-size: 20px;
}

.instructions ul {
  list-style: none;
  padding: 0;
}

.instructions li {
  margin-bottom: 8px;
  padding-left: 20px;
  position: relative;
}

.instructions li::before {
  content: '✨';
  position: absolute;
  left: 0;
}

.dialog-footer {
  text-align: right;
}
</style>
