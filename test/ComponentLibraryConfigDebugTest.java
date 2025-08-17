package com.chu7.vuecomponentassistant.test;

import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
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
            Set<ComponentLibraryDetector.LibraryType> enabledLibraries = configManager.getEnabledLibraries(project);
            
            VueKitLogger.info(LOG, "当前启用的组件库数量: " + enabledLibraries.size());
            for (ComponentLibraryDetector.LibraryType libraryType : enabledLibraries) {
                VueKitLogger.info(LOG, "- " + libraryType.name() + " (" + libraryType.getDisplayName() + ")");
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
     * 测试 fromLibraryName 方法
     */
    private static void testFromLibraryName(String libraryName) {
        try {
            ComponentLibraryDetector.LibraryType type = ComponentLibraryDetector.LibraryType.fromLibraryName(libraryName);
            VueKitLogger.info(LOG, "fromLibraryName('" + libraryName + "') -> " + type.name() + " (" + type.getDisplayName() + ")");
        } catch (Exception e) {
            VueKitLogger.error(LOG, "测试 fromLibraryName('" + libraryName + "') 失败", e);
        }
    }
    
    /**
     * 测试 inferLibraryType 方法（通过反射调用私有方法）
     */
    private static void testInferLibraryType(String libraryName) {
        try {
            // 使用反射调用私有方法
            java.lang.reflect.Method method = ComponentLibraryDetector.LibraryType.class.getDeclaredMethod("inferLibraryType", String.class);
            method.setAccessible(true);
            ComponentLibraryDetector.LibraryType type = (ComponentLibraryDetector.LibraryType) method.invoke(null, libraryName);
            VueKitLogger.info(LOG, "inferLibraryType('" + libraryName + "') -> " + type.name() + " (" + type.getDisplayName() + ")");
        } catch (Exception e) {
            VueKitLogger.error(LOG, "测试 inferLibraryType('" + libraryName + "') 失败", e);
        }
    }
} 