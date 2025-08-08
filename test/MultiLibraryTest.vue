<template>
  <div class="multi-library-test">
    <h2>🔧 多组件库支持测试</h2>
    
    <div class="test-info">
      <h3>🎯 多组件库支持说明</h3>
      <ul>
        <li>自动检测项目使用的组件库（Element UI、Element Plus、Ant Design Vue）</li>
        <li>通过读取 package.json 文件进行检测</li>
        <li>支持不同组件库的文档链接生成</li>
        <li>统一的 HTML 表格文档显示</li>
        <li>智能组件前缀识别（el-、a-）</li>
      </ul>
    </div>

    <div class="library-detection">
      <h3>🔍 组件库检测</h3>
      <div class="detection-info">
        <p><strong>检测方式：</strong>读取项目根目录下的 package.json 文件</p>
        <p><strong>检测范围：</strong>dependencies、devDependencies、peerDependencies</p>
        <p><strong>支持库：</strong>Element UI、Element Plus、Ant Design Vue</p>
      </div>
    </div>

    <div class="component-test">
      <h3>🧪 组件测试</h3>
      
      <!-- Element Plus 组件测试 -->
      <div class="test-section element-plus">
        <h4>Element Plus 组件</h4>
        <el-button type="primary" @click="testElementPlus">
          Element Plus 按钮 - 右键查看文档
        </el-button>
        <el-input 
          v-model="elementPlusInput"
          placeholder="Element Plus 输入框 - 右键查看文档"
        />
        <p>右键点击组件选择"查看文档"测试 Element Plus 支持</p>
      </div>

      <!-- Ant Design Vue 组件测试 -->
      <div class="test-section ant-design">
        <h4>Ant Design Vue 组件</h4>
        <a-button type="primary" @click="testAntDesign">
          Ant Design 按钮 - 右键查看文档
        </a-button>
        <a-input 
          v-model="antDesignInput"
          placeholder="Ant Design 输入框 - 右键查看文档"
        />
        <p>右键点击组件选择"查看文档"测试 Ant Design Vue 支持</p>
      </div>

      <!-- Element UI 组件测试 -->
      <div class="test-section element-ui">
        <h4>Element UI 组件</h4>
        <el-button type="primary" @click="testElementUI">
          Element UI 按钮 - 右键查看文档
        </el-button>
        <el-input 
          v-model="elementUIInput"
          placeholder="Element UI 输入框 - 右键查看文档"
        />
        <p>右键点击组件选择"查看文档"测试 Element UI 支持</p>
      </div>
    </div>

    <div class="library-comparison">
      <h3>📊 组件库对比</h3>
      <div class="comparison-table">
        <table class="library-table">
          <thead>
            <tr>
              <th>组件库</th>
              <th>包名</th>
              <th>组件前缀</th>
              <th>文档链接</th>
              <th>数据文件</th>
            </tr>
          </thead>
          <tbody>
            <tr>
              <td><span class="library-name">Element UI</span></td>
              <td>element-ui</td>
              <td><code>el-</code></td>
              <td>element.eleme.cn</td>
              <td>element-ui-components.json</td>
            </tr>
            <tr>
              <td><span class="library-name">Element Plus</span></td>
              <td>element-plus</td>
              <td><code>el-</code></td>
              <td>element-plus.org</td>
              <td>element-plus-components.json</td>
            </tr>
            <tr>
              <td><span class="library-name">Ant Design Vue</span></td>
              <td>ant-design-vue</td>
              <td><code>a-</code></td>
              <td>antdv.com</td>
              <td>ant-design-vue-components.json</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="detection-process">
      <h3>🔧 检测流程</h3>
      <div class="process-steps">
        <div class="step">
          <div class="step-number">1</div>
          <div class="step-content">
            <h4>读取 package.json</h4>
            <p>从项目根目录读取 package.json 文件</p>
          </div>
        </div>
        <div class="step">
          <div class="step-number">2</div>
          <div class="step-content">
            <h4>解析依赖项</h4>
            <p>检查 dependencies、devDependencies、peerDependencies</p>
          </div>
        </div>
        <div class="step">
          <div class="step-number">3</div>
          <div class="step-content">
            <h4>匹配组件库</h4>
            <p>根据包名匹配对应的组件库类型</p>
          </div>
        </div>
        <div class="step">
          <div class="step-number">4</div>
          <div class="step-content">
            <h4>加载配置</h4>
            <p>加载对应组件库的数据文件和配置</p>
          </div>
        </div>
        <div class="step">
          <div class="step-number">5</div>
          <div class="step-content">
            <h4>生成文档</h4>
            <p>根据组件库类型生成相应的文档链接</p>
          </div>
        </div>
      </div>
    </div>

    <div class="url-generation">
      <h3>🔗 文档链接生成</h3>
      <div class="url-examples">
        <div class="url-example">
          <h4>Element Plus</h4>
          <div class="url-pattern">
            <code>https://element-plus.org/zh-CN/component/{component}.html</code>
          </div>
          <div class="url-example-item">
            <span>el-button</span> → <span>https://element-plus.org/zh-CN/component/button.html</span>
          </div>
          <div class="url-example-item">
            <span>el-input</span> → <span>https://element-plus.org/zh-CN/component/input.html</span>
          </div>
        </div>
        <div class="url-example">
          <h4>Element UI</h4>
          <div class="url-pattern">
            <code>https://element.eleme.cn/#/zh-CN/component/{component}</code>
          </div>
          <div class="url-example-item">
            <span>el-button</span> → <span>https://element.eleme.cn/#/zh-CN/component/button</span>
          </div>
          <div class="url-example-item">
            <span>el-input</span> → <span>https://element.eleme.cn/#/zh-CN/component/input</span>
          </div>
        </div>
        <div class="url-example">
          <h4>Ant Design Vue</h4>
          <div class="url-pattern">
            <code>https://antdv.com/components/{component}-cn</code>
          </div>
          <div class="url-example-item">
            <span>a-button</span> → <span>https://antdv.com/components/button-cn</span>
          </div>
          <div class="url-example-item">
            <span>a-input</span> → <span>https://antdv.com/components/input-cn</span>
          </div>
        </div>
      </div>
    </div>

    <div class="features">
      <h3>✨ 功能特性</h3>
      <div class="features-grid">
        <div class="feature-item">
          <h4>🔍 智能检测</h4>
          <p>自动检测项目使用的组件库，无需手动配置</p>
        </div>
        <div class="feature-item">
          <h4>📚 统一文档</h4>
          <p>所有组件库使用统一的 HTML 表格文档格式</p>
        </div>
        <div class="feature-item">
          <h4>🔗 正确链接</h4>
          <p>根据组件库类型生成正确的官方文档链接</p>
        </div>
        <div class="feature-item">
          <h4>🎨 一致样式</h4>
          <p>不同组件库的文档显示保持一致的视觉效果</p>
        </div>
        <div class="feature-item">
          <h4>⚡ 动态加载</h4>
          <p>根据项目配置动态加载对应的组件数据</p>
        </div>
        <div class="feature-item">
          <h4>🔄 实时更新</h4>
          <p>支持项目依赖变化时的实时检测和更新</p>
        </div>
      </div>
    </div>

    <div class="usage-guide">
      <h3>📖 使用指南</h3>
      <div class="guide-content">
        <div class="guide-step">
          <h4>1. 项目配置</h4>
          <p>确保项目根目录下有 package.json 文件，并包含对应的组件库依赖</p>
        </div>
        <div class="guide-step">
          <h4>2. 自动检测</h4>
          <p>插件会自动读取 package.json 并检测使用的组件库</p>
        </div>
        <div class="guide-step">
          <h4>3. 右键查看</h4>
          <p>在任意组件上右键点击，选择"查看文档"</p>
        </div>
        <div class="guide-step">
          <h4>4. 文档显示</h4>
          <p>查看格式化的 HTML 表格文档，包含属性、事件、插槽信息</p>
        </div>
        <div class="guide-step">
          <h4>5. 官方链接</h4>
          <p>点击"打开官方文档"按钮跳转到对应的官方文档页面</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'MultiLibraryTest',
  data() {
    return {
      elementPlusInput: '',
      antDesignInput: '',
      elementUIInput: ''
    }
  },
  methods: {
    testElementPlus() {
      console.log('Element Plus 按钮测试')
    },
    testAntDesign() {
      console.log('Ant Design Vue 按钮测试')
    },
    testElementUI() {
      console.log('Element UI 按钮测试')
    }
  }
}
</script>

<style scoped>
.multi-library-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.multi-library-test h2 {
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

.library-detection {
  background: linear-gradient(135deg, #f0f9ff, #e1f5fe);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.library-detection h3 {
  color: #303133;
  margin-bottom: 15px;
  font-size: 20px;
}

.detection-info p {
  margin-bottom: 8px;
  color: #606266;
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

.element-plus {
  border-left: 4px solid #409EFF;
}

.ant-design {
  border-left: 4px solid #1890ff;
}

.element-ui {
  border-left: 4px solid #67C23A;
}

.library-comparison {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.library-comparison h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 22px;
  text-align: center;
}

.library-table {
  width: 100%;
  border-collapse: collapse;
  margin: 8px 0;
  background: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  font-size: 14px;
}

.library-table th {
  background: linear-gradient(135deg, #409EFF, #67C23A);
  color: white;
  font-weight: 600;
  padding: 12px 15px;
  text-align: left;
  border: none;
}

.library-table td {
  padding: 12px 15px;
  border-bottom: 1px solid #e4e7ed;
  line-height: 1.4;
}

.library-table tr:last-child td {
  border-bottom: none;
}

.library-table tr:hover {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
}

.library-name {
  color: #409EFF;
  font-weight: 600;
  font-size: 14px;
}

.library-table code {
  background: rgba(64, 158, 255, 0.1);
  color: #409EFF;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
}

.detection-process {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.detection-process h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 22px;
  text-align: center;
}

.process-steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.step {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  display: flex;
  align-items: flex-start;
  gap: 15px;
  transition: transform 0.3s ease;
}

.step:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0,0,0,0.15);
}

.step-number {
  background: linear-gradient(135deg, #409EFF, #67C23A);
  color: white;
  width: 30px;
  height: 30px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  flex-shrink: 0;
}

.step-content h4 {
  color: #409EFF;
  margin-bottom: 8px;
  font-size: 16px;
}

.step-content p {
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
  margin: 0;
}

.url-generation {
  background: white;
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.url-generation h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 22px;
  text-align: center;
}

.url-examples {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.url-example {
  background: #f8f9fa;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  padding: 15px;
}

.url-example h4 {
  color: #409EFF;
  margin-bottom: 10px;
  font-size: 16px;
}

.url-pattern {
  background: #2d3748;
  color: #e2e8f0;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 10px;
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
  overflow-x: auto;
}

.url-example-item {
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

.url-example-item span:first-child {
  color: #409EFF;
  font-weight: 600;
  font-family: 'JetBrains Mono', monospace;
}

.url-example-item span:last-child {
  color: #67C23A;
  font-family: 'JetBrains Mono', monospace;
  font-size: 12px;
}

.features {
  background: linear-gradient(135deg, #f0f9ff, #e1f5fe);
  padding: 20px;
  border-radius: 12px;
  margin-bottom: 30px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.features h3 {
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

.usage-guide {
  background: linear-gradient(135deg, #f8f9fa, #e9ecef);
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
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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
