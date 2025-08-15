<template>
  <div class="component-library-display-test">
    <h2>🔧 组件库显示测试</h2>
    
    <div class="test-info">
      <h3>🎯 测试说明</h3>
      <p>此文件用于测试插件是否正确显示检测到的组件库类型。</p>
      <p>根据您的 package.json 配置，应该显示正确的组件库名称。</p>
    </div>
    
    <div class="component-test">
      <h3>🧪 组件测试</h3>
      
      <!-- 测试组件补全 -->
      <div class="test-section">
        <h4>组件补全测试</h4>
        <p>在下面的位置输入 &lt;el- 应该提示正确的组件库名称：</p>
        <div class="completion-test">
          <!-- 在这里输入 <el- 测试组件补全 -->
          <!-- 应该显示检测到的组件库名称（Element UI 或 Element Plus） -->
        </div>
      </div>
      
      <!-- 测试属性补全 -->
      <div class="test-section">
        <h4>属性补全测试</h4>
        <p>在下面的组件中输入空格应该提示属性：</p>
        <div class="attribute-test">
          <!-- 在这里输入 <el-button 然后输入空格测试属性补全 -->
        </div>
      </div>
      
      <!-- 测试事件补全 -->
      <div class="test-section">
        <h4>事件补全测试</h4>
        <p>在下面的组件中输入 @ 应该提示事件：</p>
        <div class="event-test">
          <!-- 在这里输入 <el-button @ 测试事件补全 -->
        </div>
      </div>
    </div>
    
    <div class="expected-behavior">
      <h3>✅ 期望行为</h3>
      <ul>
        <li><strong>组件补全</strong>：输入 &lt;el- 时，类型列应该显示检测到的组件库名称（如 "Element UI" 或 "Element Plus"）</li>
        <li><strong>组件描述</strong>：描述列应该显示 "[Element UI] 组件描述" 或 "[Element Plus] 组件描述"</li>
        <li><strong>属性补全</strong>：在组件标签内输入空格时，应该提示该组件的属性</li>
        <li><strong>事件补全</strong>：输入 @ 时，应该提示该组件的事件</li>
      </ul>
    </div>
    
    <div class="debug-info">
      <h3>🐛 调试信息</h3>
      <p>如果显示不正确，请检查：</p>
      <ul>
        <li>package.json 中是否正确配置了组件库依赖</li>
        <li>ComponentLibraryDetector 是否正确检测到组件库</li>
        <li>ComponentProvider 是否正确设置了组件描述</li>
        <li>ElementPlusTestCompletionProvider 是否正确显示组件库名称</li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentLibraryDisplayTest',
  data() {
    return {
      message: '测试组件库显示功能'
    }
  },
  methods: {
    handleClick() {
      console.log('按钮被点击');
    },
    handleInput(value) {
      console.log('输入值:', value);
    }
  }
}
</script>

<style scoped>
.component-library-display-test {
  padding: 20px;
  font-family: 'Microsoft YaHei', sans-serif;
}

.test-info {
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #409EFF;
}

.component-test {
  margin-bottom: 20px;
}

.test-section {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 15px;
  border-left: 4px solid #28a745;
}

.test-section h4 {
  margin-top: 0;
  color: #28a745;
}

.completion-test,
.attribute-test,
.event-test {
  background: #fff;
  border: 2px dashed #dee2e6;
  border-radius: 4px;
  padding: 20px;
  margin-top: 10px;
  min-height: 60px;
  color: #6c757d;
  font-style: italic;
}

.expected-behavior {
  background: linear-gradient(135deg, #fff3cd, #ffeaa7);
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border-left: 4px solid #ffc107;
}

.expected-behavior h3 {
  margin-top: 0;
  color: #856404;
}

.debug-info {
  background: linear-gradient(135deg, #f8d7da, #f5c6cb);
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #dc3545;
}

.debug-info h3 {
  margin-top: 0;
  color: #721c24;
}

.debug-info ul {
  margin-bottom: 0;
}
</style>
