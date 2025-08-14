<template>
  <div class="html-width-test">
    <h2>📏 HTML 文档宽度测试</h2>
    
    <div class="test-info">
      <h3>🎯 宽度优化说明</h3>
      <ul>
        <li>新的文档宽度：1300px（原来是 650px）</li>
        <li>宽度增加：100%（提高 1 倍）</li>
        <li>网格布局：自适应列数，最小列宽 400px</li>
        <li>内容间距：增加内边距和外边距</li>
        <li>响应式设计：支持不同屏幕尺寸</li>
      </ul>
    </div>

    <div class="component-test">
      <h3>🧪 测试组件</h3>
      
      <!-- 按钮组件测试 -->
      <div class="test-section">
        <h4>按钮组件</h4>
        <el-button type="primary" @click="testButton">
          测试按钮 - 悬停查看文档
        </el-button>
        <p>将鼠标悬停在按钮上查看新的文档宽度</p>
      </div>

      <!-- 输入框组件测试 -->
      <div class="test-section">
        <h4>输入框组件</h4>
        <el-input 
          v-model="testInput"
          placeholder="测试输入框 - 悬停查看文档"
        />
        <p>将鼠标悬停在输入框上查看新的文档宽度</p>
      </div>

      <!-- 表格组件测试 -->
      <div class="test-section">
        <h4>表格组件</h4>
        <el-table 
          :data="testTableData"
          style="width: 100%"
        >
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="age" label="年龄" />
          <el-table-column prop="city" label="城市" />
          <el-table-column prop="job" label="职业" />
        </el-table>
        <p>将鼠标悬停在表格上查看新的文档宽度</p>
      </div>

      <!-- 表单组件测试 -->
      <div class="test-section">
        <h4>表单组件</h4>
        <el-form :model="testForm">
          <el-form-item label="用户名">
            <el-input v-model="testForm.username" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="testForm.email" />
          </el-form-item>
          <el-form-item label="电话">
            <el-input v-model="testForm.phone" />
          </el-form-item>
        </el-form>
        <p>将鼠标悬停在表单上查看新的文档宽度</p>
      </div>
    </div>

    <div class="width-comparison">
      <h3>📊 宽度对比</h3>
      <div class="comparison-grid">
        <div class="comparison-item">
          <h4>旧宽度</h4>
          <div class="width-box old-width">
            <span>650px</span>
            <div class="width-visual old-visual"></div>
          </div>
        </div>
        <div class="comparison-item">
          <h4>新宽度</h4>
          <div class="width-box new-width">
            <span>1300px</span>
            <div class="width-visual new-visual"></div>
          </div>
        </div>
      </div>
      <p class="improvement">📈 宽度增加了 100%，显示效果更佳</p>
    </div>

    <div class="layout-features">
      <h3>✨ 布局特性</h3>
      <div class="features-grid">
        <div class="feature-item">
          <h4>🎨 网格布局</h4>
          <p>自适应列数，最小列宽 400px，充分利用宽屏空间</p>
        </div>
        <div class="feature-item">
          <h4>📱 响应式设计</h4>
          <p>支持不同屏幕尺寸，自动调整布局</p>
        </div>
        <div class="feature-item">
          <h4>📏 优化间距</h4>
          <p>增加内边距和外边距，提升视觉体验</p>
        </div>
        <div class="feature-item">
          <h4>💻 代码显示</h4>
          <p>更宽的代码块，显示更多代码内容</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HtmlWidthTest',
  data() {
    return {
      testInput: '',
      testTableData: [
        { name: '张三', age: 25, city: '北京', job: '前端工程师' },
        { name: '李四', age: 30, city: '上海', job: 'UI设计师' },
        { name: '王五', age: 28, city: '广州', job: '产品经理' },
        { name: '赵六', age: 32, city: '深圳', job: '后端工程师' }
      ],
      testForm: {
        username: '',
        email: '',
        phone: ''
      }
    }
  },
  methods: {
    testButton() {
      console.log('按钮测试')
    }
  }
}
</script>

<style scoped>
.html-width-test {
  padding: 20px;
  max-width: 1400px;
  margin: 0 auto;
}

.html-width-test h2 {
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

.width-comparison {
  background: linear-gradient(135deg, #f0f9ff, #e1f5fe);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.width-comparison h3 {
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

.width-box {
  padding: 20px;
  border-radius: 8px;
  font-weight: bold;
  font-size: 18px;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  min-height: 100px;
}

.old-width {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
}

.new-width {
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
}

.width-visual {
  height: 20px;
  border-radius: 10px;
  background: rgba(255,255,255,0.3);
}

.old-visual {
  width: 65px;
}

.new-visual {
  width: 130px;
}

.improvement {
  text-align: center;
  color: #67C23A;
  font-weight: bold;
  font-size: 16px;
  margin: 0;
}

.layout-features {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.layout-features h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 22px;
  text-align: center;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.feature-item {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.feature-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.feature-item h4 {
  color: #409EFF;
  margin-bottom: 10px;
  font-size: 16px;
}

.feature-item p {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}
</style>
