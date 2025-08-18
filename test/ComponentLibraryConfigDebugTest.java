package com.chu7.vuecomponentassistant.test;

import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.utils.LibraryTypeHelper;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;

import java.util.Set;

/**
 * 组件库配置调试测试类
 * 
 * 用于诊断组件库配置显示问题
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentLibraryConfigDebugTest {
    
    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryConfigDebugTest.class);
    
    /**
     * 调试组件库配置
     * 
     * @param project 项目对象
     */
    public static void debugComponentLibraryConfig(Project project) {
        try {
            VueKitLogger.info(LOG, "=== 开始调试组件库配置 ===");
            
            // 1. 获取组件库配置管理器
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            
            // 2. 获取当前启用的组件库
            Set<String> enabledLibraries = configManager.getEnabledLibraryNames(project);
            
            VueKitLogger.info(LOG, "当前启用的组件库数量: " + enabledLibraries.size());
            for (String libraryType : enabledLibraries) {
                VueKitLogger.info(LOG, "- " + libraryType + " (" + LibraryTypeHelper.getDisplayName(libraryType) + ")");
            }
            
            // 3. 测试 fromLibraryName 方法
            VueKitLogger.info(LOG, "=== 测试 fromLibraryName 方法 ===");
            testFromLibraryName("element-ui");
            testFromLibraryName("element-plus");
            testFromLibraryName("ant-design-vue");
            testFromLibraryName("vuetify");
            testFromLibraryName("quasar");
            
            // 4. 测试 inferLibraryType 方法
            VueKitLogger.info(LOG, "=== 测试 inferLibraryType 方法 ===");
            testInferLibraryType("element-ui");
            testInferLibraryType("element-plus");
            testInferLibraryType("ant-design-vue");
            testInferLibraryType("vuetify");
            testInferLibraryType("quasar");
            
            VueKitLogger.info(LOG, "=== 调试完成 ===");
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "调试组件库配置时发生错误", e);
        }
    }
    
    /**
     * 测试 getPackageName 方法
     */
    private static void testFromLibraryName(String libraryName) {
        try {
            String type = LibraryTypeHelper.getPackageName(libraryName);
            VueKitLogger.info(LOG, "getPackageName('" + libraryName + "') -> " + type + " (" + LibraryTypeHelper.getDisplayName(type) + ")");
        } catch (Exception e) {
            VueKitLogger.error(LOG, "测试 getPackageName('" + libraryName + "') 失败", e);
        }
    }
    
    /**
     * 测试 getDisplayName 方法
     */
    private static void testInferLibraryType(String libraryName) {
        try {
            String type = LibraryTypeHelper.getPackageName(libraryName);
            String displayName = LibraryTypeHelper.getDisplayName(type);
            VueKitLogger.info(LOG, "getDisplayName('" + libraryName + "') -> " + type + " (" + displayName + ")");
        } catch (Exception e) {
            VueKitLogger.error(LOG, "测试 getDisplayName('" + libraryName + "') 失败", e);
        }
    }
} 