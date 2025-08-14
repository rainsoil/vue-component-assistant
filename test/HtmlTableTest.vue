<template>
  <div class="html-table-test">
    <h2>📋 HTML 表格文档测试</h2>
    
    <div class="test-info">
      <h3>🎯 HTML 表格化优化说明</h3>
      <ul>
        <li>右键文档现在使用 HTML 格式显示</li>
        <li>表格样式与悬浮弹框完全一致</li>
        <li>支持渐变背景、悬停效果、圆角边框</li>
        <li>使用 JBEditorPane 显示 HTML 内容</li>
        <li>支持中文字体和编码</li>
      </ul>
    </div>

    <div class="component-test">
      <h3>🧪 测试组件</h3>
      
      <!-- 按钮组件测试 -->
      <div class="test-section">
        <h4>按钮组件</h4>
        <el-button type="primary" @click="testButton">
          测试按钮 - 右键查看 HTML 表格文档
        </el-button>
        <p>右键点击按钮选择"查看文档"查看新的 HTML 表格格式</p>
      </div>

      <!-- 输入框组件测试 -->
      <div class="test-section">
        <h4>输入框组件</h4>
        <el-input 
          v-model="testInput"
          placeholder="测试输入框 - 右键查看 HTML 表格文档"
        />
        <p>右键点击输入框选择"查看文档"查看新的 HTML 表格格式</p>
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
        <p>右键点击表格选择"查看文档"查看新的 HTML 表格格式</p>
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
        <p>右键点击表单选择"查看文档"查看新的 HTML 表格格式</p>
      </div>
    </div>

    <div class="format-comparison">
      <h3>📊 格式对比</h3>
      <div class="comparison-grid">
        <div class="comparison-item">
          <h4>旧格式（纯文本）</h4>
          <div class="format-preview old-format">
            <pre>🔧 属性 (Attributes)
--------------------------------------------------------------------------------
参数              说明                      类型         可选值               默认值
--------------------------------------------------------------------------------
type             按钮类型                   string       primary / success... —
size             按钮尺寸                   string       large / default...   default
disabled         是否禁用状态               boolean      —                    false</pre>
          </div>
        </div>
        <div class="comparison-item">
          <h4>新格式（HTML 表格）</h4>
          <div class="format-preview new-format">
            <div class="html-table-preview">
              <table class="preview-table">
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
                    <td><span class="preview-name">type</span></td>
                    <td>按钮类型</td>
                    <td>string</td>
                    <td>primary / success / warning / danger / info</td>
                    <td>—</td>
                  </tr>
                  <tr>
                    <td><span class="preview-name">size</span></td>
                    <td>按钮尺寸</td>
                    <td>string</td>
                    <td>large / default / small</td>
                    <td>default</td>
                  </tr>
                  <tr>
                    <td><span class="preview-name">disabled</span></td>
                    <td>是否禁用状态</td>
                    <td>boolean</td>
                    <td>—</td>
                    <td>false</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
      <p class="improvement">📈 HTML 表格格式更美观，视觉效果更佳</p>
    </div>

    <div class="html-features">
      <h3>✨ HTML 特性</h3>
      <div class="features-grid">
        <div class="feature-item">
          <h4>🎨 美观样式</h4>
          <p>渐变背景、圆角边框、阴影效果，视觉效果更佳</p>
        </div>
        <div class="feature-item">
          <h4>🖱️ 交互效果</h4>
          <p>行悬停高亮、按钮悬停效果，提供良好的交互体验</p>
        </div>
        <div class="feature-item">
          <h4>📱 响应式布局</h4>
          <p>表格自适应宽度，在不同屏幕尺寸下都能良好显示</p>
        </div>
        <div class="feature-item">
          <h4>🔤 字体支持</h4>
          <p>支持中文字体，确保中文内容正确显示</p>
        </div>
      </div>
    </div>

    <div class="html-examples">
      <h3>📋 HTML 表格示例</h3>
      
      <div class="example-section">
        <h4>属性表格示例</h4>
        <div class="example-table">
          <table class="demo-table">
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
                <td><span class="demo-name">type</span></td>
                <td>按钮类型</td>
                <td>string</td>
                <td>primary / success / warning / danger / info</td>
                <td>—</td>
              </tr>
              <tr>
                <td><span class="demo-name">size</span></td>
                <td>按钮尺寸</td>
                <td>string</td>
                <td>large / default / small</td>
                <td>default</td>
              </tr>
              <tr>
                <td><span class="demo-name">disabled</span></td>
                <td>是否禁用状态</td>
                <td>boolean</td>
                <td>—</td>
                <td>false</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="example-section">
        <h4>事件表格示例</h4>
        <div class="example-table">
          <table class="demo-table">
            <thead>
              <tr>
                <th>事件名称</th>
                <th>说明</th>
                <th>回调参数</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><span class="demo-name">@click</span></td>
                <td>点击按钮时触发</td>
                <td>(event: Event)</td>
              </tr>
              <tr>
                <td><span class="demo-name">@change</span></td>
                <td>值改变时触发</td>
                <td>(value: string | number)</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="example-section">
        <h4>插槽表格示例</h4>
        <div class="example-table">
          <table class="demo-table">
            <thead>
              <tr>
                <th>插槽名</th>
                <th>说明</th>
                <th>作用域</th>
              </tr>
            </thead>
            <tbody>
              <tr>
                <td><span class="demo-name">#default</span></td>
                <td>按钮内容</td>
                <td>—</td>
              </tr>
              <tr>
                <td><span class="demo-name">#icon</span></td>
                <td>按钮图标</td>
                <td>—</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="usage-guide">
      <h3>📖 使用指南</h3>
      <div class="guide-content">
        <div class="guide-step">
          <h4>1. 右键操作</h4>
          <p>在任意 Element Plus 组件上右键点击，选择"查看文档"菜单项</p>
        </div>
        <div class="guide-step">
          <h4>2. 查看 HTML 表格</h4>
          <p>在弹出的对话框中查看格式化的 HTML 表格文档，样式美观</p>
        </div>
        <div class="guide-step">
          <h4>3. 交互体验</h4>
          <p>鼠标悬停在表格行上可以看到高亮效果</p>
        </div>
        <div class="guide-step">
          <h4>4. 复制文档</h4>
          <p>点击"复制文档"按钮可以复制完整的 HTML 格式文档</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HtmlTableTest',
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
.html-table-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.html-table-test h2 {
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

.format-comparison {
  background: linear-gradient(135deg, #f0f9ff, #e1f5fe);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.format-comparison h3 {
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

.format-preview {
  padding: 15px;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  text-align: left;
  overflow-x: auto;
}

.old-format pre {
  margin: 0;
  font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace;
  font-size: 11px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-wrap: break-word;
  color: #606266;
}

.new-format {
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

.preview-name {
  color: #409EFF;
  font-weight: 600;
  font-family: 'JetBrains Mono', monospace;
  font-size: 10px;
  background: rgba(64, 158, 255, 0.1);
  padding: 2px 4px;
  border-radius: 3px;
}

.improvement {
  text-align: center;
  color: #67C23A;
  font-weight: bold;
  font-size: 16px;
  margin: 0;
}

.html-features {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.html-features h3 {
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

.html-examples {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.html-examples h3 {
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
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  overflow-x: auto;
}

.demo-table {
  width: 100%;
  border-collapse: collapse;
  margin: 8px 0;
  background: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  font-size: 12px;
}

.demo-table th {
  background: linear-gradient(135deg, #409EFF, #67C23A);
  color: white;
  font-weight: 600;
  padding: 10px 12px;
  text-align: left;
  border: none;
}

.demo-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #e4e7ed;
  line-height: 1.4;
}

.demo-table tr:last-child td {
  border-bottom: none;
}

.demo-table tr:hover {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
}

.demo-name {
  color: #409EFF;
  font-weight: 600;
  font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace;
  font-size: 11px;
  background: rgba(64, 158, 255, 0.1);
  padding: 3px 6px;
  border-radius: 3px;
  display: inline-block;
}

.usage-guide {
  background: linear-gradient(135deg, #f0f9ff, #e1f5fe);
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.usage-guide h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 22px;
  text-align: center;
}

.guide-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.guide-step {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  transition: transform 0.3s ease;
}

.guide-step:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.guide-step h4 {
  color: #409EFF;
  margin-bottom: 10px;
  font-size: 16px;
}

.guide-step p {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}
</style>
