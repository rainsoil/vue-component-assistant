<template>
  <div class="table-style-test">
    <h2>📊 表格样式测试</h2>
    
    <div class="test-info">
      <h3>🎯 表格化优化说明</h3>
      <ul>
        <li>属性表格：参数、说明、类型、可选值、默认值</li>
        <li>事件表格：事件名称、说明、回调参数</li>
        <li>插槽表格：插槽名、说明、作用域</li>
        <li>表头样式：渐变背景，白色文字</li>
        <li>行悬停：淡色背景高亮</li>
        <li>紧凑布局：适合悬浮弹框显示</li>
      </ul>
    </div>

    <div class="component-test">
      <h3>🧪 测试组件</h3>
      
      <!-- 按钮组件测试 -->
      <div class="test-section">
        <h4>按钮组件</h4>
        <el-button type="primary" @click="testButton">
          测试按钮 - 悬停查看表格文档
        </el-button>
        <p>将鼠标悬停在按钮上查看新的表格样式</p>
      </div>

      <!-- 输入框组件测试 -->
      <div class="test-section">
        <h4>输入框组件</h4>
        <el-input 
          v-model="testInput"
          placeholder="测试输入框 - 悬停查看表格文档"
        />
        <p>将鼠标悬停在输入框上查看新的表格样式</p>
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
        <p>将鼠标悬停在表格上查看新的表格样式</p>
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
        <p>将鼠标悬停在表单上查看新的表格样式</p>
      </div>
    </div>

    <div class="style-comparison">
      <h3>📊 样式对比</h3>
      <div class="comparison-grid">
        <div class="comparison-item">
          <h4>旧样式（网格布局）</h4>
          <div class="style-preview old-style">
            <div class="preview-item">
              <div class="preview-header">
                <span class="preview-name">type</span>
                <span class="preview-badge">属性</span>
              </div>
              <div class="preview-desc">按钮类型</div>
              <div class="preview-default">默认值: primary</div>
            </div>
            <div class="preview-item">
              <div class="preview-header">
                <span class="preview-name">size</span>
                <span class="preview-badge">属性</span>
              </div>
              <div class="preview-desc">按钮尺寸</div>
              <div class="preview-default">默认值: default</div>
            </div>
          </div>
        </div>
        <div class="comparison-item">
          <h4>新样式（表格布局）</h4>
          <div class="style-preview new-style">
            <table class="preview-table">
              <thead>
                <tr>
                  <th>参数</th>
                  <th>说明</th>
                  <th>类型</th>
                  <th>默认值</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td><span class="preview-name">type</span></td>
                  <td>按钮类型</td>
                  <td>string</td>
                  <td>primary</td>
                </tr>
                <tr>
                  <td><span class="preview-name">size</span></td>
                  <td>按钮尺寸</td>
                  <td>string</td>
                  <td>default</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <p class="improvement">📈 表格布局更清晰，信息更易读</p>
    </div>

    <div class="table-features">
      <h3>✨ 表格特性</h3>
      <div class="features-grid">
        <div class="feature-item">
          <h4>📋 结构化信息</h4>
          <p>表格形式展示，信息层次清晰，便于快速查找</p>
        </div>
        <div class="feature-item">
          <h4>🎨 美观表头</h4>
          <p>渐变背景表头，白色文字，视觉效果突出</p>
        </div>
        <div class="feature-item">
          <h4>🖱️ 交互反馈</h4>
          <p>行悬停高亮，提供良好的交互体验</p>
        </div>
        <div class="feature-item">
          <h4>📱 紧凑布局</h4>
          <p>适合悬浮弹框显示，充分利用有限空间</p>
        </div>
      </div>
    </div>

    <div class="table-examples">
      <h3>📋 表格示例</h3>
      
      <div class="example-section">
        <h4>属性表格示例</h4>
        <table class="example-table">
          <thead>
            <tr>
              <th>参数</th>
              <th>说明</th>
              <th>类型</th>
              <th>可选值</th>
              <th>默认值</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><span class="example-name">type</span></td>
              <td>按钮类型</td>
              <td>string</td>
              <td>primary / success / warning / danger / info</td>
              <td>—</td>
            </tr>
            <tr>
              <td><span class="example-name">size</span></td>
              <td>按钮尺寸</td>
              <td>string</td>
              <td>large / default / small</td>
              <td>default</td>
            </tr>
            <tr>
              <td><span class="example-name">disabled</span></td>
              <td>是否禁用状态</td>
              <td>boolean</td>
              <td>—</td>
              <td>false</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="example-section">
        <h4>事件表格示例</h4>
        <table class="example-table">
          <thead>
            <tr>
              <th>事件名称</th>
              <th>说明</th>
              <th>回调参数</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><span class="example-name">@click</span></td>
              <td>点击按钮时触发</td>
              <td>(event: Event)</td>
            </tr>
            <tr>
              <td><span class="example-name">@change</span></td>
              <td>值改变时触发</td>
              <td>(value: string | number)</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="example-section">
        <h4>插槽表格示例</h4>
        <table class="example-table">
          <thead>
            <tr>
              <th>插槽名</th>
              <th>说明</th>
              <th>作用域</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><span class="example-name">#default</span></td>
              <td>按钮内容</td>
              <td>—</td>
            </tr>
            <tr>
              <td><span class="example-name">#icon</span></td>
              <td>按钮图标</td>
              <td>—</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'TableStyleTest',
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
.table-style-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.table-style-test h2 {
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

.style-comparison {
  background: linear-gradient(135deg, #f0f9ff, #e1f5fe);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.style-comparison h3 {
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

.style-preview {
  padding: 15px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.old-style {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.preview-item {
  padding: 10px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  background: #f8f9fa;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.preview-name {
  color: #409EFF;
  font-weight: 600;
  font-family: 'JetBrains Mono', monospace;
  font-size: 11px;
  background: rgba(64, 158, 255, 0.1);
  padding: 3px 6px;
  border-radius: 3px;
}

.preview-badge {
  background: linear-gradient(135deg, #ecf5ff, #e1f5fe);
  color: #409EFF;
  padding: 2px 6px;
  border-radius: 8px;
  font-size: 9px;
  font-weight: 600;
}

.preview-desc {
  color: #606266;
  font-size: 11px;
  margin-bottom: 6px;
}

.preview-default {
  color: #909399;
  font-size: 10px;
  background: #f5f7fa;
  padding: 3px 6px;
  border-radius: 4px;
  display: inline-block;
}

.new-style {
  overflow: hidden;
}

.preview-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 11px;
}

.preview-table th {
  background: linear-gradient(135deg, #409EFF, #67C23A);
  color: white;
  font-weight: 600;
  padding: 8px 12px;
  text-align: left;
  border: none;
}

.preview-table td {
  padding: 8px 12px;
  border-bottom: 1px solid #e4e7ed;
  line-height: 1.4;
}

.preview-table tr:last-child td {
  border-bottom: none;
}

.preview-table tr:hover {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
}

.improvement {
  text-align: center;
  color: #67C23A;
  font-weight: bold;
  font-size: 16px;
  margin: 0;
}

.table-features {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.table-features h3 {
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

.table-examples {
  background: white;
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.table-examples h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 22px;
  text-align: center;
}

.example-section {
  margin-bottom: 30px;
}

.example-section h4 {
  color: #409EFF;
  margin-bottom: 15px;
  font-size: 18px;
}

.example-table {
  width: 100%;
  border-collapse: collapse;
  margin: 8px 0;
  background: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  font-size: 12px;
}

.example-table th {
  background: linear-gradient(135deg, #409EFF, #67C23A);
  color: white;
  font-weight: 600;
  padding: 10px 12px;
  text-align: left;
  border: none;
}

.example-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #e4e7ed;
  line-height: 1.4;
}

.example-table tr:last-child td {
  border-bottom: none;
}

.example-table tr:hover {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
}

.example-name {
  color: #409EFF;
  font-weight: 600;
  font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace;
  font-size: 11px;
  background: rgba(64, 158, 255, 0.1);
  padding: 3px 6px;
  border-radius: 3px;
  display: inline-block;
}
</style>
