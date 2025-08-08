<template>
  <div class="right-click-table-test">
    <h2>📋 右键表格文档测试</h2>
    
    <div class="test-info">
      <h3>🎯 右键文档表格化优化说明</h3>
      <ul>
        <li>属性表格：参数、说明、类型、可选值、默认值</li>
        <li>事件表格：事件名称、说明、回调参数</li>
        <li>插槽表格：插槽名、说明、作用域</li>
        <li>文本对齐：使用 String.format 实现表格对齐</li>
        <li>内容截断：过长的描述自动截断并添加省略号</li>
        <li>统一格式：与悬浮弹框保持一致的表格结构</li>
      </ul>
    </div>

    <div class="component-test">
      <h3>🧪 测试组件</h3>
      
      <!-- 按钮组件测试 -->
      <div class="test-section">
        <h4>按钮组件</h4>
        <el-button type="primary" @click="testButton">
          测试按钮 - 右键查看表格文档
        </el-button>
        <p>右键点击按钮选择"查看文档"查看新的表格格式</p>
      </div>

      <!-- 输入框组件测试 -->
      <div class="test-section">
        <h4>输入框组件</h4>
        <el-input 
          v-model="testInput"
          placeholder="测试输入框 - 右键查看表格文档"
        />
        <p>右键点击输入框选择"查看文档"查看新的表格格式</p>
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
        <p>右键点击表格选择"查看文档"查看新的表格格式</p>
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
        <p>右键点击表单选择"查看文档"查看新的表格格式</p>
      </div>
    </div>

    <div class="format-comparison">
      <h3>📊 格式对比</h3>
      <div class="comparison-grid">
        <div class="comparison-item">
          <h4>旧格式（列表样式）</h4>
          <div class="format-preview old-format">
            <pre>🔧 属性列表:
----------------------------------------
• type - 按钮类型 (默认值: primary)
• size - 按钮尺寸 (默认值: default)
• disabled - 是否禁用状态 (默认值: false)

🎯 事件列表:
----------------------------------------
• @click - 点击按钮时触发
• @change - 值改变时触发

🔌 插槽列表:
----------------------------------------
• #default - 按钮内容
• #icon - 按钮图标</pre>
          </div>
        </div>
        <div class="comparison-item">
          <h4>新格式（表格样式）</h4>
          <div class="format-preview new-format">
            <pre>🔧 属性 (Attributes)
--------------------------------------------------------------------------------
参数              说明                      类型         可选值               默认值
--------------------------------------------------------------------------------
type             按钮类型                   string       primary / success... —
size             按钮尺寸                   string       large / default...   default
disabled         是否禁用状态               boolean      —                    false

🎯 事件 (Events)
--------------------------------------------------------------------------------
事件名称              说明                              回调参数
--------------------------------------------------------------------------------
@click              点击按钮时触发                       (event: Event)
@change             值改变时触发                         (value: string | number)

🔌 插槽 (Slots)
--------------------------------------------------------------------------------
插槽名                说明                                    作用域
--------------------------------------------------------------------------------
#default             按钮内容                                 —
#icon                按钮图标                                 —</pre>
          </div>
        </div>
      </div>
      <p class="improvement">📈 表格格式更清晰，信息更易读</p>
    </div>

    <div class="table-features">
      <h3>✨ 表格特性</h3>
      <div class="features-grid">
        <div class="feature-item">
          <h4>📋 结构化信息</h4>
          <p>表格形式展示，信息层次清晰，便于快速查找</p>
        </div>
        <div class="feature-item">
          <h4>🎯 文本对齐</h4>
          <p>使用 String.format 实现完美的列对齐效果</p>
        </div>
        <div class="feature-item">
          <h4>✂️ 智能截断</h4>
          <p>过长的描述自动截断并添加省略号，保持表格整齐</p>
        </div>
        <div class="feature-item">
          <h4>🔄 格式统一</h4>
          <p>与悬浮弹框保持一致的表格结构和信息展示</p>
        </div>
      </div>
    </div>

    <div class="table-examples">
      <h3>📋 表格示例</h3>
      
      <div class="example-section">
        <h4>属性表格示例</h4>
        <div class="example-table">
          <pre>🔧 属性 (Attributes)
--------------------------------------------------------------------------------
参数              说明                      类型         可选值               默认值
--------------------------------------------------------------------------------
type             按钮类型                   string       primary / success... —
size             按钮尺寸                   string       large / default...   default
disabled         是否禁用状态               boolean      —                    false
loading          是否加载中状态             boolean      —                    false
icon             图标类名                   string       —                    —
round            是否圆角按钮               boolean      —                    false
circle           是否圆形按钮               boolean      —                    false</pre>
        </div>
      </div>

      <div class="example-section">
        <h4>事件表格示例</h4>
        <div class="example-table">
          <pre>🎯 事件 (Events)
--------------------------------------------------------------------------------
事件名称              说明                              回调参数
--------------------------------------------------------------------------------
@click              点击按钮时触发                       (event: Event)
@change             值改变时触发                         (value: string | number)
@focus              获得焦点时触发                       (event: FocusEvent)
@blur               失去焦点时触发                       (event: FocusEvent)
@keydown            按下键盘时触发                       (event: KeyboardEvent)</pre>
        </div>
      </div>

      <div class="example-section">
        <h4>插槽表格示例</h4>
        <div class="example-table">
          <pre>🔌 插槽 (Slots)
--------------------------------------------------------------------------------
插槽名                说明                                    作用域
--------------------------------------------------------------------------------
#default             按钮内容                                 —
#icon                按钮图标                                 —
#loading             加载状态图标                             —
#prefix              按钮前缀内容                             —
#suffix              按钮后缀内容                             —</pre>
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
          <h4>2. 查看表格</h4>
          <p>在弹出的对话框中查看格式化的表格文档，信息更清晰易读</p>
        </div>
        <div class="guide-step">
          <h4>3. 复制文档</h4>
          <p>点击"复制文档"按钮可以复制完整的表格格式文档到剪贴板</p>
        </div>
        <div class="guide-step">
          <h4>4. 打开官方文档</h4>
          <p>点击"打开官方文档"按钮可以在浏览器中打开对应的官方文档页面</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'RightClickTableTest',
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
.right-click-table-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.right-click-table-test h2 {
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

.format-preview pre {
  margin: 0;
  font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace;
  font-size: 11px;
  line-height: 1.4;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.old-format pre {
  color: #606266;
}

.new-format pre {
  color: #303133;
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
  margin-bottom: 30px;
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
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
  overflow-x: auto;
}

.example-table pre {
  margin: 0;
  font-family: 'JetBrains Mono', 'Fira Code', 'Monaco', 'Menlo', monospace;
  font-size: 11px;
  line-height: 1.4;
  color: #303133;
  white-space: pre-wrap;
  word-wrap: break-word;
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
