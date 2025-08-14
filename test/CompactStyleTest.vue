<template>
  <div class="compact-style-test">
    <h2>📏 紧凑样式测试</h2>
    
    <div class="test-info">
      <h3>🎯 紧凑化优化说明</h3>
      <ul>
        <li>标题字体：从 24px 减小到 18px</li>
        <li>描述字体：从 15px 减小到 12px</li>
        <li>内容字体：从 14px 减小到 11px</li>
        <li>代码字体：从 13px 减小到 10px</li>
        <li>内边距：整体减少 30-50%</li>
        <li>网格最小宽度：从 400px 减小到 300px</li>
      </ul>
    </div>

    <div class="component-test">
      <h3>🧪 测试组件</h3>
      
      <!-- 按钮组件测试 -->
      <div class="test-section">
        <h4>按钮组件</h4>
        <el-button type="primary" @click="testButton">
          测试按钮 - 悬停查看紧凑文档
        </el-button>
        <p>将鼠标悬停在按钮上查看新的紧凑样式</p>
      </div>

      <!-- 输入框组件测试 -->
      <div class="test-section">
        <h4>输入框组件</h4>
        <el-input 
          v-model="testInput"
          placeholder="测试输入框 - 悬停查看紧凑文档"
        />
        <p>将鼠标悬停在输入框上查看新的紧凑样式</p>
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
        <p>将鼠标悬停在表格上查看新的紧凑样式</p>
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
        <p>将鼠标悬停在表单上查看新的紧凑样式</p>
      </div>
    </div>

    <div class="size-comparison">
      <h3>📊 字体大小对比</h3>
      <div class="comparison-grid">
        <div class="comparison-item">
          <h4>旧字体大小</h4>
          <div class="font-size-box old-font">
            <div class="font-item">
              <span class="label">标题:</span>
              <span class="size">24px</span>
            </div>
            <div class="font-item">
              <span class="label">描述:</span>
              <span class="size">15px</span>
            </div>
            <div class="font-item">
              <span class="label">内容:</span>
              <span class="size">14px</span>
            </div>
            <div class="font-item">
              <span class="label">代码:</span>
              <span class="size">13px</span>
            </div>
          </div>
        </div>
        <div class="comparison-item">
          <h4>新字体大小</h4>
          <div class="font-size-box new-font">
            <div class="font-item">
              <span class="label">标题:</span>
              <span class="size">18px</span>
            </div>
            <div class="font-item">
              <span class="label">描述:</span>
              <span class="size">12px</span>
            </div>
            <div class="font-item">
              <span class="label">内容:</span>
              <span class="size">11px</span>
            </div>
            <div class="font-item">
              <span class="label">代码:</span>
              <span class="size">10px</span>
            </div>
          </div>
        </div>
      </div>
      <p class="improvement">📉 字体大小减少了 20-30%，显示更紧凑</p>
    </div>

    <div class="layout-features">
      <h3>✨ 紧凑化特性</h3>
      <div class="features-grid">
        <div class="feature-item">
          <h4>📏 减少间距</h4>
          <p>内边距和外边距减少 30-50%，充分利用空间</p>
        </div>
        <div class="feature-item">
          <h4>🎨 优化网格</h4>
          <p>最小列宽从 400px 减小到 300px，适应窄屏</p>
        </div>
        <div class="feature-item">
          <h4>💻 紧凑代码</h4>
          <p>代码块字体和间距优化，显示更多内容</p>
        </div>
        <div class="feature-item">
          <h4>🏷️ 小徽章</h4>
          <p>标签徽章尺寸减小，不占用过多空间</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CompactStyleTest',
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
.compact-style-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.compact-style-test h2 {
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
  margin-bottom: 30px;
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

.font-size-box {
  padding: 20px;
  border-radius: 8px;
  font-weight: bold;
  color: white;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 120px;
}

.old-font {
  background: linear-gradient(135deg, #ff6b6b, #ee5a52);
}

.new-font {
  background: linear-gradient(135deg, #4ecdc4, #44a08d);
}

.font-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.font-item .label {
  font-size: 14px;
}

.font-item .size {
  font-size: 16px;
  font-weight: bold;
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
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
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
