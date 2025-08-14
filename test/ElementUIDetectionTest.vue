<template>
  <div class="element-ui-detection-test">
    <h2>🔧 Element UI 组件库检测测试</h2>
    
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
        <el-select placeholder="Element UI 选择器">
          <el-option label="选项1" value="1"></el-option>
          <el-option label="选项2" value="2"></el-option>
        </el-select>
      </div>
      
      <!-- 测试补全功能 -->
      <div class="test-section">
        <h4>补全功能测试</h4>
        <p>在下面的位置输入 &lt;el- 应该提示 Element UI 组件：</p>
        <div class="completion-test">
          <!-- 在这里输入 <el- 测试组件补全 -->
        </div>
      </div>
      
      <!-- 测试属性补全 -->
      <div class="test-section">
        <h4>属性补全测试</h4>
        <p>在下面的组件中输入空格应该提示 Element UI 的属性：</p>
        <div class="attribute-test">
          <!-- 在这里输入 <el-button 然后输入空格测试属性补全 -->
        </div>
      </div>
      
      <!-- 测试事件补全 -->
      <div class="test-section">
        <h4>事件补全测试</h4>
        <p>在下面的组件中输入 @ 应该提示 Element UI 的事件：</p>
        <div class="event-test">
          <!-- 在这里输入 <el-button @ 测试事件补全 -->
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
  name: 'ElementUIDetectionTest',
  data() {
    return {
      testData: {
        message: 'Element UI 检测测试',
        components: ['el-button', 'el-input', 'el-select', 'el-option']
      }
    }
  },
  methods: {
    handleClick() {
      console.log('Element UI 按钮点击事件');
    },
    handleInput(value) {
      console.log('Element UI 输入事件:', value);
    },
    handleChange(value) {
      console.log('Element UI 选择事件:', value);
    }
  }
}
</script>

<style scoped>
.element-ui-detection-test {
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

.completion-test,
.attribute-test,
.event-test {
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
