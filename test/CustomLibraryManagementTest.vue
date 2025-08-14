<template>
  <!-- 测试自定义组件库管理功能 -->
  <div class="custom-library-test">
    <h2>🔧 自定义组件库管理功能测试</h2>
    
    <!-- 内置组件库测试 -->
    <div class="section">
      <h3>📚 内置组件库测试</h3>
      
      <!-- Element Plus 组件 -->
      <div class="component-group">
        <h4>Element Plus 组件</h4>
        <el-button type="primary" @click="handleClick">主要按钮</el-button>
        <el-input v-model="inputValue" placeholder="请输入内容" />
        <el-select v-model="selectedValue" placeholder="请选择">
          <el-option label="选项1" value="1" />
          <el-option label="选项2" value="2" />
        </el-select>
      </div>
      
      <!-- Element UI 组件（如果项目使用 Element UI） -->
      <div class="component-group">
        <h4>Element UI 组件</h4>
        <el-button type="success" @click="handleSuccess">成功按钮</el-button>
        <el-input v-model="inputValue2" placeholder="Element UI 输入框" />
      </div>
      
      <!-- Ant Design Vue 组件（如果项目使用 Ant Design Vue） -->
      <div class="component-group">
        <h4>Ant Design Vue 组件</h4>
        <a-button type="primary" @click="handleAntClick">Ant Design 按钮</a-button>
        <a-input v-model="antInputValue" placeholder="Ant Design 输入框" />
      </div>
    </div>
    
    <!-- 自定义组件库测试 -->
    <div class="section">
      <h3>🎨 自定义组件库测试</h3>
      
      <!-- 自定义组件（需要先通过管理功能上传） -->
      <div class="component-group">
        <h4>自定义组件</h4>
        <my-button type="primary" @click="handleCustomClick">自定义按钮</my-button>
        <my-input v-model="customInputValue" placeholder="自定义输入框" />
        <my-select v-model="customSelectedValue" placeholder="自定义选择器">
          <my-option label="自定义选项1" value="1" />
          <my-option label="自定义选项2" value="2" />
        </my-select>
      </div>
    </div>
    
    <!-- 功能说明 -->
    <div class="instructions">
      <h3>📋 功能说明</h3>
      <ul>
        <li><strong>组件展示</strong>：点击 "工具" → "自定义组件库管理" 首先会展示所有可用组件</li>
        <li><strong>组件库分组</strong>：组件按组件库分组显示，包括内置和自定义组件库</li>
        <li><strong>上传功能</strong>：可以上传自定义组件库的 JSON 文件</li>
        <li><strong>导出功能</strong>：可以导出任何组件库为 JSON 文件</li>
        <li><strong>删除功能</strong>：只能删除自定义组件库，内置组件库不可删除</li>
        <li><strong>模板导出</strong>：可以导出组件库模板，方便创建新的组件库</li>
      </ul>
      
      <h4>🔧 使用方法</h4>
      <ol>
        <li>在 IntelliJ IDEA 中打开 "工具" 菜单</li>
        <li>选择 "🔧 自定义组件库管理"</li>
        <li>首先会显示所有组件的概览</li>
        <li>然后可以选择执行管理操作</li>
      </ol>
      
      <h4>📦 自定义组件库 JSON 格式</h4>
      <pre><code>{
  "name": "my-component-library",
  "displayName": "我的组件库",
  "version": "1.0.0",
  "description": "自定义组件库描述",
  "componentPrefix": "my-",
  "components": [
    {
      "name": "my-button",
      "description": "自定义按钮组件",
      "version": "1.0.0",
      "example": "&lt;my-button type=\"primary\"&gt;按钮&lt;/my-button&gt;",
      "docUrl": "https://example.com/my-button",
      "props": [
        {
          "name": "type",
          "type": "string",
          "description": "按钮类型",
          "defaultValue": "default",
          "required": false,
          "options": ["primary", "success", "warning", "danger"]
        }
      ],
      "events": [
        {
          "name": "click",
          "description": "点击事件",
          "parameters": "event"
        }
      ],
      "slots": [
        {
          "name": "default",
          "description": "按钮内容"
        }
      ]
    }
  ]
}</code></pre>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CustomLibraryManagementTest',
  data() {
    return {
      inputValue: '',
      inputValue2: '',
      selectedValue: '',
      antInputValue: '',
      customInputValue: '',
      customSelectedValue: ''
    }
  },
  methods: {
    handleClick() {
      console.log('Element Plus 按钮点击')
    },
    handleSuccess() {
      console.log('Element UI 成功按钮点击')
    },
    handleAntClick() {
      console.log('Ant Design Vue 按钮点击')
    },
    handleCustomClick() {
      console.log('自定义按钮点击')
    }
  }
}
</script>

<style scoped>
.custom-library-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.custom-library-test h2 {
  text-align: center;
  color: #409EFF;
  margin-bottom: 30px;
  font-size: 28px;
}

.section {
  margin-bottom: 40px;
  padding: 20px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background-color: #fafafa;
}

.section h3 {
  color: #303133;
  margin-bottom: 20px;
  font-size: 20px;
}

.component-group {
  margin-bottom: 30px;
  padding: 15px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  background-color: white;
}

.component-group h4 {
  color: #606266;
  margin-bottom: 15px;
  font-size: 16px;
}

.component-group > * {
  margin-right: 10px;
  margin-bottom: 10px;
}

.instructions {
  margin-top: 40px;
  padding: 20px;
  border: 1px solid #e1f3d8;
  border-radius: 8px;
  background-color: #f0f9ff;
}

.instructions h3 {
  color: #67c23a;
  margin-bottom: 15px;
}

.instructions h4 {
  color: #409EFF;
  margin-top: 20px;
  margin-bottom: 10px;
}

.instructions ul, .instructions ol {
  margin-left: 20px;
}

.instructions li {
  margin-bottom: 8px;
  line-height: 1.6;
}

.instructions pre {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  margin-top: 10px;
}

.instructions code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 12px;
}
</style>
