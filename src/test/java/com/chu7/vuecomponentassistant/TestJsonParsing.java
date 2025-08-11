package com.chu7.vuecomponentassistant;

import com.chu7.vuecomponentassistant.exceptions.VueKitException;
import com.chu7.vuecomponentassistant.remote.RemoteLibraryManager;
import com.chu7.vuecomponentassistant.remote.utils.HttpClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * 测试JSON解析功能
 */
public class TestJsonParsing {

    public static void main(String[] args) {
        // 测试JSON内容
        String testJson = """
                {
                  "id": "element-plus",
                  "name": "element-plus",
                  "displayName": "Element Plus",
                  "description": "Element Plus 组件库",
                  "version": "2.5.0",
                  "source": "OFFICIAL",
                  "sourceUrl": "",
                  "lastUpdated": "2025-08-11T10:00:09.914799",
                  "components": [
                    {
                      "name": "el-button",
                      "description": "常用的按钮组件，支持多种样式和事件。",
                      "version": "Element Plus >=1.0.0",
                      "example": "<el-button type=\\"primary\\">主要按钮</el-button>",
                      "docUrl": "https://element-plus.org/zh-CN/component/button.html",
                      "props": [
                        {
                          "name": "type",
                          "type": "string",
                          "description": "按钮类型",
                          "defaultValue": "default",
                          "required": false,
                          "options": [
                            "primary",
                            "success",
                            "warning",
                            "danger",
                            "info",
                            "default"
                          ]
                        }
                      ],
                      "events": [
                        {
                          "name": "click",
                          "description": "点击按钮时触发",
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
                }
                """;

        System.out.println("=== 测试JSON解析 ===");
        System.out.println("JSON长度: " + testJson.length());
        System.out.println("JSON前100字符: " + testJson.substring(0, Math.min(100, testJson.length())));

        // 测试解析
        try {
//            String json = HttpClient.downloadJson("https://gitee.com/rainsoil/vuekit-repo/raw/master/element-plus-libraries.json");
//            testJsonParsing(json);
            RemoteLibraryManager remoteLibraryManager = new RemoteLibraryManager();
            ComponentLibrary componentLibrary = remoteLibraryManager.downloadLibrary("https://gitee.com/rainsoil/vuekit-repo/raw/master/element-plus-libraries.json").get();
            System.out.println("组件数量: " + (componentLibrary.getComponents() != null ? componentLibrary.getComponents().size() : 0));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private static void testJsonParsing(String json) {
        try {
            Gson gson = new GsonBuilder()
                    .setLenient() // 使用宽松模式
                    .create();

            System.out.println("\n=== 尝试解析为单个对象 ===");
            try {
                ComponentLibrary library = gson.fromJson(json, ComponentLibrary.class);
                if (library != null && library.getName() != null) {
                    System.out.println("✅ 成功解析为单个对象: " + library.getName());
                    System.out.println("组件数量: " + (library.getComponents() != null ? library.getComponents().size() : 0));
                    return;
                } else {
                    System.out.println("❌ 解析成功但验证失败");
                }
            } catch (Exception e) {
                System.out.println("❌ 解析为单个对象失败: " + e.getMessage());
            }

            System.out.println("\n=== 尝试解析为数组 ===");
            try {
                Type listType = new TypeToken<List<ComponentLibrary>>() {
                }.getType();
                List<ComponentLibrary> libraries = gson.fromJson(json, listType);
                if (libraries != null && !libraries.isEmpty()) {
                    ComponentLibrary library = libraries.get(0);
                    System.out.println("✅ 成功从数组中解析: " + library.getName());
                    return;
                } else {
                    System.out.println("❌ 解析为数组成功但为空");
                }
            } catch (Exception e) {
                System.out.println("❌ 解析为数组失败: " + e.getMessage());
            }

            System.out.println("\n=== 分析JSON结构 ===");
            try {
                JsonElement element = gson.fromJson(json, JsonElement.class);
                if (element != null) {
                    System.out.println("JSON类型: " + element.getClass().getSimpleName());
                    if (element.isJsonObject()) {
                        System.out.println("对象键: " + element.getAsJsonObject().keySet());
                    } else if (element.isJsonArray()) {
                        System.out.println("数组大小: " + element.getAsJsonArray().size());
                    }
                }
            } catch (Exception e) {
                System.out.println("❌ 无法分析JSON结构: " + e.getMessage());
            }

        } catch (Exception e) {
            System.out.println("❌ 测试失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
