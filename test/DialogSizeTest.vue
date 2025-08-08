<template>
  <div class="dialog-size-test">
    <h2>📏 弹框大小测试</h2>
    
    <div class="test-info">
      <h3>🎯 测试说明</h3>
      <ul>
        <li>新的弹框尺寸：1000 x 800 像素</li>
        <li>文本区域尺寸：980 x 680 像素</li>
        <li>字体大小：14px（内容）、20px（标题）、13px（按钮）</li>
        <li>支持中文显示和编码</li>
      </ul>
    </div>

    <div class="component-test">
      <h3>🧪 测试组件</h3>
      
      <!-- 按钮组件测试 -->
      <div class="test-section">
        <h4>按钮组件</h4>
        <el-button type="primary" @click="testButton">
          测试按钮 - 右键查看文档
        </el-button>
        <p>右键点击按钮查看文档弹框大小</p>
      </div>

      <!-- 输入框组件测试 -->
      <div class="test-section">
        <h4>输入框组件</h4>
        <el-input 
          v-model="testInput"
          placeholder="测试输入框 - 右键查看文档"
          @contextmenu="testInput"
        />
        <p>右键点击输入框查看文档弹框大小</p>
      </div>

      <!-- 表格组件测试 -->
      <div class="test-section">
        <h4>表格组件</h4>
        <el-table 
          :data="testTableData"
          style="width: 100%"
          @contextmenu="testTable"
        >
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="age" label="年龄" />
          <el-table-column prop="city" label="城市" />
        </el-table>
        <p>右键点击表格查看文档弹框大小</p>
      </div>

      <!-- 表单组件测试 -->
      <div class="test-section">
        <h4>表单组件</h4>
        <el-form 
          :model="testForm"
          @contextmenu="testForm"
        >
          <el-form-item label="用户名">
            <el-input v-model="testForm.username" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="testForm.email" />
          </el-form-item>
        </el-form>
        <p>右键点击表单查看文档弹框大小</p>
      </div>
    </div>

    <div class="size-comparison">
      <h3>📊 尺寸对比</h3>
      <div class="comparison-grid">
        <div class="comparison-item">
          <h4>旧尺寸</h4>
          <div class="size-box old-size">
            <span>800 x 600</span>
          </div>
        </div>
        <div class="comparison-item">
          <h4>新尺寸</h4>
          <div class="size-box new-size">
            <span>1000 x 800</span>
          </div>
        </div>
      </div>
      <p class="improvement">📈 尺寸增加了 25% 的宽度和 33% 的高度</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'DialogSizeTest',
  data() {
    return {
      testInput: '',
      testTableData: [
        { name: '张三', age: 25, city: '北京' },
        { name: '李四', age: 30, city: '上海' },
        { name: '王五', age: 28, city: '广州' }
      ],
      testForm: {
        username: '',
        email: ''
      }
    }
  },
  methods: {
    testButton() {
      console.log('按钮测试')
    },
    testInput() {
      console.log('输入框测试')
    },
    testTable() {
      console.log('表格测试')
    },
    testForm() {
      console.log('表单测试')
    }
  }
}
</script>

<style scoped>
.dialog-size-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.dialog-size-test h2 {
  text-align: center;
  color: #409EFF;
  margin-bottom: 30px;
  font-size: 28px;
}

.test-info {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.test-info h3 {
  margin-bottom: 15px;
  font-size: 20px;
}

.test-info ul {
  list-style: none;
  padding: 0;
}

.test-info li {
  margin-bottom: 8px;
  padding-left: 20px;
  position: relative;
}

.test-info li::before {
  content: '✨';
  position: absolute;
  left: 0;
}

.component-test {
  margin-bottom: 30px;
}

.component-test h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 22px;
}

.test-section {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.test-section:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0,0,0,0.15);
}

.test-section h4 {
  color: #409EFF;
  margin-bottom: 15px;
  font-size: 18px;
}

.test-section p {
  color: #606266;
  margin-top: 10px;
  font-size: 14px;
}

.size-comparison {
  background: linear-gradient(135deg, #f0f9ff, #e1f5fe);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.size-comparison h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 22px;
  text-align: center;
}

.comparison-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.comparison-item {
  text-align: center;
}

.comparison-item h4 {
  color: #606266;
  margin-bottom: 15px;
  font-size: 16px;
}

.size-box {
  padding: 20px;
  border-radius: 8px;
  font-weight: bold;
  font-size: 18px;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 80px;
}

.old-size {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
}

.new-size {
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
}

.improvement {
  text-align: center;
  color: #67C23A;
  font-weight: bold;
  font-size: 16px;
  margin: 0;
}
</style>
