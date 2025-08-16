<template>
  <div class="component-library-filtering-test">
    <h2>🔧 组件库过滤功能测试</h2>
    
    <div class="test-info">
      <h3>🎯 测试说明</h3>
      <p>此文件用于测试组件库过滤功能是否正确工作。</p>
      <p>如果你禁用了 Element Plus 组件库，下面的 Element Plus 组件应该不会在代码补全中显示。</p>
    </div>
    
    <div class="component-test">
      <h3>🧪 组件测试</h3>
      
      <!-- Element Plus 组件测试 -->
      <div class="test-section">
        <h4>Element Plus 组件</h4>
        <p>如果 Element Plus 被禁用，这些组件不应该在代码补全中显示：</p>
        <el-button type="primary">Element Plus 按钮</el-button>
        <el-input placeholder="Element Plus 输入框"></el-input>
        <el-select placeholder="Element Plus 选择器">
          <el-option label="选项1" value="1"></el-option>
          <el-option label="选项2" value="2"></el-option>
        </el-select>
      </div>
      
      <!-- Element UI 组件测试 -->
      <div class="test-section">
        <h4>Element UI 组件</h4>
        <p>如果 Element UI 被禁用，这些组件不应该在代码补全中显示：</p>
        <el-button type="success">Element UI 按钮</el-button>
        <el-input placeholder="Element UI 输入框"></el-input>
      </div>
      
      <!-- Ant Design Vue 组件测试 -->
      <div class="test-section">
        <h4>Ant Design Vue 组件</h4>
        <p>如果 Ant Design Vue 被禁用，这些组件不应该在代码补全中显示：</p>
        <a-button type="primary">Ant Design 按钮</a-button>
        <a-input placeholder="Ant Design 输入框" />
      </div>
      
      <!-- 补全功能测试 -->
      <div class="test-section">
        <h4>补全功能测试</h4>
        <p>在下面的位置输入组件前缀，测试是否只显示启用的组件库的组件：</p>
        <div class="completion-test">
          <!-- 在这里输入 <el- 测试 Element Plus/UI 组件补全 -->
          <!-- 在这里输入 <a- 测试 Ant Design Vue 组件补全 -->
          <!-- 在这里输入 <v- 测试 Vuetify 组件补全 -->
          <!-- 在这里输入 <q- 测试 Quasar 组件补全 -->
        </div>
      </div>
    </div>
    
    <div class="debug-info">
      <h3>🐛 调试信息</h3>
      <p>如果组件库过滤功能不正常工作，请检查：</p>
      <ul>
        <li>是否在设置中正确配置了组件库启用状态</li>
        <li>SmartComponentFilter 是否正确过滤了组件</li>
        <li>ComponentProvider 是否正确应用了过滤逻辑</li>
        <li>ComponentLibraryConfigManager 是否正确保存了配置</li>
        <li>是否需要重启 IDE 或重新加载项目</li>
      </ul>
      
      <h4>配置步骤：</h4>
      <ol>
        <li>打开设置页面 (File → Settings → Tools → VueKit)</li>
        <li>点击"🔧 组件库启用管理"按钮</li>
        <li>取消勾选不需要的组件库</li>
        <li>点击"应用"保存配置</li>
        <li>重启 IDE 或重新加载项目</li>
        <li>测试代码补全功能</li>
      </ol>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryFilteringTest',
  data() {
    return {
      message: '组件库过滤功能测试'
    }
  },
  methods: {
    testComponentCompletion() {
      console.log('测试组件补全功能');
    }
  }
}
</script>

<style scoped>
.component-library-filtering-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.test-info {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.test-section {
  border: 1px solid #ddd;
  padding: 15px;
  margin-bottom: 15px;
  border-radius: 6px;
}

.test-section h4 {
  color: #409EFF;
  margin-top: 0;
}

.completion-test {
  background: #f9f9f9;
  padding: 10px;
  border: 1px dashed #ccc;
  border-radius: 4px;
  min-height: 60px;
}

.debug-info {
  background: #fff3cd;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #ffc107;
}

.debug-info ul, .debug-info ol {
  margin: 10px 0;
  padding-left: 20px;
}

.debug-info li {
  margin: 5px 0;
}
</style> 