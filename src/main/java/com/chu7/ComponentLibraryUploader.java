package com.chu7;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ComponentLibraryUploader {
    
    public static boolean uploadComponentLibrary(Project project, VirtualFile file) {
        try {
            String content = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);
            List<ComponentLibrary> libraries = parseComponentLibraries(content);
            
            if (libraries != null && !libraries.isEmpty()) {
                ComponentLibraryManager manager = ComponentLibraryManager.getInstance(project);
                for (ComponentLibrary library : libraries) {
                    manager.addLibrary(library);
                }
                Messages.showInfoMessage(project, 
                    String.format("成功导入 %d 个组件库", libraries.size()), 
                    "组件库导入成功");
                return true;
            } else {
                Messages.showErrorDialog(project, 
                    "JSON 文件格式不正确或没有找到有效的组件库数据", 
                    "导入失败");
                return false;
            }
        } catch (IOException e) {
            Messages.showErrorDialog(project, 
                "读取文件失败: " + e.getMessage(), 
                "导入失败");
            return false;
        }
    }
    
    private static List<ComponentLibrary> parseComponentLibraries(String jsonContent) {
        try {
            Gson gson = new Gson();
            return gson.fromJson(jsonContent, new TypeToken<List<ComponentLibrary>>(){}.getType());
        } catch (Exception e) {
            return null;
        }
    }
    
    public static String generateComponentLibraryTemplate() {
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
                    "docUrl": "https://your-docs.com/my-button"
                  },
                  {
                    "name": "my-input",
                    "description": "自定义输入框组件",
                    "version": "1.0.0",
                    "example": "<my-input v-model=\"value\" />",
                    "docUrl": "https://your-docs.com/my-input"
                  },
                  {
                    "name": "my-card",
                    "description": "自定义卡片组件",
                    "version": "1.0.0",
                    "example": "<my-card title=\"标题\">内容</my-card>",
                    "docUrl": "https://your-docs.com/my-card"
                  },
                  {
                    "name": "my-table",
                    "description": "自定义表格组件",
                    "version": "1.0.0",
                    "example": "<my-table :data=\"tableData\" />",
                    "docUrl": "https://your-docs.com/my-table"
                  }
                ]
              },
              {
                "name": "Business Components",
                "description": "业务组件库",
                "version": "2.0.0",
                "author": "Business Team",
                "website": "https://business-components.com",
                "docUrl": "https://business-components.com/docs",
                "components": [
                  {
                    "name": "business-form",
                    "description": "业务表单组件",
                    "version": "2.0.0",
                    "example": "<business-form :model=\"formData\" />",
                    "docUrl": "https://business-components.com/docs/form"
                  },
                  {
                    "name": "business-chart",
                    "description": "业务图表组件",
                    "version": "2.0.0",
                    "example": "<business-chart :data=\"chartData\" />",
                    "docUrl": "https://business-components.com/docs/chart"
                  }
                ]
              }
            ]
            """;
    }
} 