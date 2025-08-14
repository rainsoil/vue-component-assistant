<template>
  <div class="custom-library-test">
    <h2>🔧 自定义组件库功能测试</h2>
    
    <div class="test-info">
      <h3>🎯 功能说明</h3>
      <p>此文件用于测试自定义组件库功能。</p>
      <p>在输入 <code>&lt;my-</code> 时应该提示自定义组件库的组件。</p>
    </div>
    
    <div class="component-test">
      <h3>🧪 自定义组件测试</h3>
      
      <!-- 测试自定义组件 -->
      <div class="test-section">
        <h4>自定义组件库组件</h4>
        <my-button type="primary">自定义按钮 - 鼠标悬停查看文档</my-button>
        <my-input placeholder="自定义输入框"></my-input>
        <my-select placeholder="自定义选择器">
          <my-option label="选项1" value="1"></my-option>
          <my-option label="选项2" value="2"></my-option>
        </my-select>
        <my-table :data="tableData" :columns="tableColumns"></my-table>
      </div>
      
      <!-- 测试补全功能 -->
      <div class="test-section">
        <h4>补全功能测试</h4>
        <p>在下面的位置输入 &lt;my- 应该提示自定义组件库的组件：</p>
        <div class="completion-test">
          <!-- 在这里输入 <my- 测试组件补全 -->
        </div>
      </div>
    </div>
    
    <div class="usage-guide">
      <h3>📖 使用指南</h3>
      <div class="guide-section">
        <h4>1. 上传自定义组件库</h4>
        <ul>
          <li>在 IntelliJ IDEA 中，选择 Tools → 自定义组件库管理</li>
          <li>点击"上传新组件库"</li>
          <li>选择 JSON 配置文件或直接输入配置内容</li>
          <li>点击"验证配置"检查格式是否正确</li>
          <li>点击"加载组件库"完成上传</li>
        </ul>
      </div>
      
      <div class="guide-section">
        <h4>2. JSON 配置格式</h4>
        <pre><code>{
  "name": "组件库名称",
  "displayName": "显示名称",
  "version": "版本号",
  "description": "描述信息",
  "componentPrefix": "组件前缀",
  "documentationUrlTemplate": "文档URL模板",
  "components": [
    {
      "name": "组件名称",
      "description": "组件描述",
      "props": [...],
      "events": [...],
      "slots": [...]
    }
  ]
}</code></pre>
      </div>
      
      <div class="guide-section">
        <h4>3. 管理已加载的组件库</h4>
        <ul>
          <li>查看已加载组件库：Tools → 自定义组件库管理 → 查看已加载组件库</li>
          <li>删除组件库：Tools → 自定义组件库管理 → 删除组件库</li>
          <li>清空所有组件库：Tools → 自定义组件库管理 → 清空所有组件库</li>
        </ul>
      </div>
    </div>
    
    <div class="features">
      <h3>✨ 功能特性</h3>
      <ul>
        <li>✅ 支持上传自定义组件库 JSON 配置文件</li>
        <li>✅ 支持直接输入 JSON 配置内容</li>
        <li>✅ 提供配置验证功能</li>
        <li>✅ 支持多个自定义组件库同时加载</li>
        <li>✅ 智能组件前缀识别</li>
        <li>✅ 完整的组件属性、事件、插槽支持</li>
        <li>✅ 自定义文档链接生成</li>
        <li>✅ 组件库管理功能（查看、删除、清空）</li>
        <li>✅ 与内置组件库无缝集成</li>
      </ul>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomLibraryTest',
  data() {
    return {
      tableData: [
        { id: 1, name: '张三', age: 25, email: 'zhangsan@example.com' },
        { id: 2, name: '李四', age: 30, email: 'lisi@example.com' },
        { id: 3, name: '王五', age: 28, email: 'wangwu@example.com' }
      ],
      tableColumns: [
        { prop: 'id', label: 'ID', width: 80 },
        { prop: 'name', label: '姓名', width: 120 },
        { prop: 'age', label: '年龄', width: 80 },
        { prop: 'email', label: '邮箱', width: 200 }
      ]
    }
  },
  methods: {
    handleButtonClick() {
      console.log('自定义按钮点击事件');
    },
    handleInputChange(value) {
      console.log('输入框值改变:', value);
    },
    handleSelectChange(value) {
      console.log('选择器值改变:', value);
    }
  }
}
</script>

<style scoped>
.custom-library-test {
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

.usage-guide {
  background: linear-gradient(135deg, #f0f9ff, #e0f2fe);
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.guide-section {
  margin-bottom: 20px;
}

.guide-section h4 {
  color: #409EFF;
  margin-bottom: 10px;
}

.guide-section ul {
  margin: 10px 0;
  padding-left: 20px;
}

.guide-section li {
  margin: 5px 0;
  color: #666;
}

.guide-section pre {
  background: #f8f9fa;
  padding: 15px;
  border-radius: 4px;
  border: 1px solid #e4e7ed;
  overflow-x: auto;
  font-family: 'Consolas', monospace;
  font-size: 12px;
}

.features {
  background: linear-gradient(135deg, #fff7ed, #ffedd5);
  padding: 20px;
  border-radius: 8px;
  border-left: 4px solid #f59e0b;
}

.features ul {
  margin: 10px 0;
  padding-left: 20px;
}

.features li {
  margin: 8px 0;
  color: #666;
}

code {
  background: #f1f3f4;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Consolas', monospace;
  font-size: 12px;
}
</style>
