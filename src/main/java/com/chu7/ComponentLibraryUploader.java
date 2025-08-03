package com.chu7;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 组件库上传器
 * 负责处理组件库 JSON 文件的导入和解析功能
 */
public class ComponentLibraryUploader {
    
    /**
     * 上传组件库文件
     * 读取 JSON 文件并解析为组件库对象，然后添加到项目中
     * 
     * @param project 项目实例
     * @param file 要上传的 JSON 文件
     * @return 是否上传成功
     */
    public static boolean uploadComponentLibrary(Project project, VirtualFile file) {
        try {
            // 读取文件内容
            String content = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);
            
            // 解析 JSON 内容为组件库列表
            List<ComponentLibrary> libraries = parseComponentLibraries(content);
            
            if (libraries != null && !libraries.isEmpty()) {
                // 获取组件库管理器并添加组件库
                ComponentLibraryManager manager = ComponentLibraryManager.getInstance(project);
                for (ComponentLibrary library : libraries) {
                    manager.addLibrary(library);
                }
                
                // 显示成功消息
                Messages.showInfoMessage(project, 
                    String.format("成功导入 %d 个组件库", libraries.size()), 
                    "组件库导入成功");
                return true;
            } else {
                // 显示错误消息
                Messages.showErrorDialog(project, 
                    "JSON 文件格式不正确或没有找到有效的组件库数据", 
                    "导入失败");
                return false;
            }
        } catch (IOException e) {
            // 显示文件读取错误
            Messages.showErrorDialog(project, 
                "读取文件失败: " + e.getMessage(), 
                "导入失败");
            return false;
        }
    }
    
    /**
     * 解析组件库 JSON 内容
     * 将 JSON 字符串解析为组件库对象列表
     * 
     * @param jsonContent JSON 字符串内容
     * @return 组件库列表，解析失败时返回 null
     */
    private static List<ComponentLibrary> parseComponentLibraries(String jsonContent) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(jsonContent, new TypeToken<List<ComponentLibrary>>(){}.getType());
        } catch (Exception e) {
            // 解析失败时返回 null
            return null;
        }
    }
    
    /**
     * 获取组件库模板
     * 提供一个完整的组件库 JSON 模板，包含属性、事件和卡槽的示例
     * @return JSON 格式的组件库模板
     */
    public static String getComponentLibraryTemplate() {
        return """
            [
              {
                "name": "My Custom Library",
                "description": "自定义组件库示例",
                "version": "1.0.0",
                "author": "Your Name",
                "website": "https://your-website.com",
                "docUrl": "https://your-docs.com",
                "components": [
                  {
                    "name": "my-button",
                    "description": "自定义按钮组件",
                    "version": "1.0.0",
                    "example": "<my-button type=\"primary\">按钮</my-button>",
                    "docUrl": "https://your-docs.com/my-button",
                    "props": [
                      {
                        "name": "type",
                        "type": "string",
                        "description": "按钮类型",
                        "defaultValue": "default",
                        "required": false,
                        "options": ["primary", "success", "warning", "danger", "default"]
                      },
                      {
                        "name": "size",
                        "type": "string",
                        "description": "按钮尺寸",
                        "defaultValue": "default",
                        "required": false,
                        "options": ["large", "default", "small"]
                      },
                      {
                        "name": "disabled",
                        "type": "boolean",
                        "description": "是否禁用",
                        "defaultValue": "false",
                        "required": false
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
                      },
                      {
                        "name": "icon",
                        "description": "按钮图标"
                      }
                    ]
                  },
                  {
                    "name": "my-input",
                    "description": "自定义输入框组件",
                    "version": "1.0.0",
                    "example": "<my-input v-model=\"value\" />",
                    "docUrl": "https://your-docs.com/my-input",
                    "props": [
                      {
                        "name": "modelValue",
                        "type": "string",
                        "description": "绑定值",
                        "defaultValue": "",
                        "required": false
                      },
                      {
                        "name": "placeholder",
                        "type": "string",
                        "description": "占位符",
                        "defaultValue": "",
                        "required": false
                      }
                    ],
                    "events": [
                      {
                        "name": "input",
                        "description": "输入事件",
                        "parameters": "value"
                      },
                      {
                        "name": "change",
                        "description": "值改变事件",
                        "parameters": "value"
                      }
                    ],
                    "slots": [
                      {
                        "name": "prefix",
                        "description": "输入框头部内容"
                      },
                      {
                        "name": "suffix",
                        "description": "输入框尾部内容"
                      }
                    ]
                  }
                ]
              }
            ]
            """;
    }
} 