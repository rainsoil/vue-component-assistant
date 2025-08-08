<template>
  <div class="component-detection-test">
    <h2>🔧 组件库检测测试</h2>
    
    <div class="test-info">
      <h3>🎯 测试说明</h3>
      <p>此文件用于测试插件是否正确检测到 Element UI 组件库。</p>
      <p>package.json 中配置的是 element-ui，所以应该显示 Element UI 的组件。</p>
    </div>
    
    <div class="component-test">
      <h3>🧪 组件测试</h3>
      
      <!-- 测试 Element UI 组件 -->
      <div class="test-section">
        <h4>Element UI 组件</h4>
        <el-button type="primary">Element UI 按钮 - 鼠标悬停查看文档</el-button>
        <el-input placeholder="Element UI 输入框"></el-input>
      </div>
      
      <!-- 测试补全功能 -->
      <div class="test-section">
        <h4>补全功能测试</h4>
        <p>在下面的位置输入 &lt;el- 应该提示 Element UI 组件：</p>
        <div class="completion-test">
          <!-- 在这里输入 <el- 测试组件补全 -->
        </div>
      </div>
    </div>
    
    <div class="debug-info">
      <h3>🐛 调试信息</h3>
      <p>如果插件仍然显示 Element Plus 组件，请检查：</p>
      <ul>
        <li>package.json 是否正确配置了 element-ui</li>
        <li>ComponentLibraryDetector 是否正确检测到组件库</li>
        <li>ComponentProvider 是否正确加载了 Element UI 数据</li>
        <li>ElementPlusTestCompletionProvider 是否正确使用了动态检测</li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ComponentDetectionTest',
  data() {
    return {
      testData: {
        message: 'Element UI 检测测试',
        components: ['el-button', 'el-input']
      }
    }
  },
  methods: {
    handleClick() {
      console.log('Element UI 按钮点击事件');
    }
  }
}
</script>

<style scoped>
.component-detection-test {
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
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.test-section {
  margin-bottom: 25px;
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
}

.test-section h4 {
  color: #409EFF;
  margin-bottom: 10px;
}

.completion-test {
  background: #f8f9fa;
  padding: 10px;
  border-radius: 4px;
  border: 1px dashed #c0c4cc;
  min-height: 40px;
  margin-top: 10px;
}

.debug-info {
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #f59e0b;
}

.debug-info ul {
  margin: 10px 0;
  padding-left: 20px;
}

.debug-info li {
  margin: 5px 0;
  color: #666;
}
</style>
