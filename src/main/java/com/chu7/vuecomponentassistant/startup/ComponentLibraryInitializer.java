package com.chu7.vuecomponentassistant.startup;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.ImportResult;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.startup.StartupManager;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 组件库初始化器
 * 
 * 功能说明：
 * - 在项目启动时自动检查和下载必要的组件库
 * - 确保 Element Plus、Ant Design Vue 等官方组件库数据可用
 * - 提供组件库数据的自动更新机制
 * - 优化启动性能，避免阻塞UI线程
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentLibraryInitializer implements StartupActivity {

    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryInitializer.class);

    @Override
    public void runActivity(@NotNull Project project) {
        // 在后台线程中执行组件库初始化，避免阻塞UI
        CompletableFuture.runAsync(() -> {
            try {
                VueKitLogger.info(LOG, "=== 开始组件库初始化 ===");
                
                // 初始化组件库管理器
                ComponentLibraryManager manager = new ComponentLibraryManager();
                
                // 检查并下载官方组件库
                initializeOfficialLibraries(manager);
                
                VueKitLogger.info(LOG, "=== 组件库初始化完成 ===");
                
            } catch (Exception e) {
                VueKitLogger.error(LOG, "组件库初始化失败", e);
            }
        });
    }

    /**
     * 初始化官方组件库
     * 
     * @param manager 组件库管理器
     */
    private void initializeOfficialLibraries(ComponentLibraryManager manager) {
        try {
            VueKitLogger.info(LOG, "检查官方组件库...");
            
            // 获取所有已安装的组件库
            List<ComponentLibrary> existingLibraries = manager.getAllLibraries();
            VueKitLogger.info(LOG, "已安装的组件库数量: " + existingLibraries.size());
            
            // 动态检查已安装的组件库，不再硬编码特定组件库名称
            if (existingLibraries.isEmpty()) {
                VueKitLogger.info(LOG, "没有发现已安装的组件库，建议从官方市场下载");
            } else {
                VueKitLogger.info(LOG, "已发现以下组件库:");
                for (ComponentLibrary library : existingLibraries) {
                    VueKitLogger.info(LOG, "  - " + library.getName() + " (" + library.getDisplayName() + ")");
                }
            }
            
            // 不再硬编码检查特定组件库，而是动态处理
            VueKitLogger.info(LOG, "组件库初始化检查完成");
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "初始化官方组件库失败", e);
        }
    }

    /**
     * 检查是否已安装指定组件库
     * 
     * @param libraries 已安装的组件库列表
     * @param libraryId 要检查的组件库ID
     * @return 如果已安装返回true，否则返回false
     */
    private boolean hasLibrary(List<ComponentLibrary> libraries, String libraryId) {
        return libraries.stream()
                .anyMatch(library -> libraryId.equals(library.getId()));
    }

    /**
     * 下载 Element Plus 组件库
     * 
     * @param manager 组件库管理器
     */
    private void downloadElementPlusLibrary(ComponentLibraryManager manager) {
        try {
            // 创建 Element Plus 组件库
            ComponentLibrary elementPlusLibrary = createElementPlusLibrary();
            
            // 导入组件库
            ImportResult result = manager.importLibrary(elementPlusLibrary);
            
            if (result.isSuccess()) {
                VueKitLogger.info(LOG, "✅ Element Plus 组件库下载成功");
            } else {
                VueKitLogger.warn(LOG, "⚠️ Element Plus 组件库下载失败: " + result.getMessage());
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "下载 Element Plus 组件库失败", e);
        }
    }

    /**
     * 下载 Ant Design Vue 组件库
     * 
     * @param manager 组件库管理器
     */
    private void downloadAntDesignVueLibrary(ComponentLibraryManager manager) {
        try {
            // 创建 Ant Design Vue 组件库
            ComponentLibrary antDesignVueLibrary = createAntDesignVueLibrary();
            
            // 导入组件库
            ImportResult result = manager.importLibrary(antDesignVueLibrary);
            
            if (result.isSuccess()) {
                VueKitLogger.info(LOG, "✅ Ant Design Vue 组件库下载成功");
            } else {
                VueKitLogger.warn(LOG, "⚠️ Ant Design Vue 组件库下载失败: " + result.getMessage());
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "下载 Ant Design Vue 组件库失败", e);
        }
    }

    /**
     * 下载 Element UI 组件库
     * 
     * @param manager 组件库管理器
     */
    private void downloadElementUILibrary(ComponentLibraryManager manager) {
        try {
            // 动态获取已安装的组件库，不再硬编码
            java.util.List<ComponentLibrary> installedLibraries = manager.getAllLibraries();
            
            if (installedLibraries != null && !installedLibraries.isEmpty()) {
                VueKitLogger.info(LOG, "✅ 发现已安装的组件库，共 " + installedLibraries.size() + " 个");
                
                // 可以选择性地导入一些组件库
                for (ComponentLibrary library : installedLibraries) {
                    if (library.getName().toLowerCase().contains("element")) {
                        VueKitLogger.info(LOG, "✅ 发现 Element 相关组件库: " + library.getName());
                    }
                }
            } else {
                VueKitLogger.info(LOG, "ℹ️ 没有发现已安装的组件库");
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "检查已安装组件库失败", e);
        }
    }

    /**
     * 创建 Element Plus 组件库（已废弃）
     * 
     * @return Element Plus 组件库对象
     * @deprecated 使用动态获取替代硬编码创建
     */
    @Deprecated
    private ComponentLibrary createElementPlusLibrary() {
        VueKitLogger.warn(LOG, "createElementPlusLibrary 方法已废弃，请使用动态获取");
        return null;
    }

    /**
     * 创建 Ant Design Vue 组件库
     * 
     * @return Ant Design Vue 组件库对象
     */
    /**
     * 创建 Ant Design Vue 组件库（已废弃）
     * 
     * @return Ant Design Vue 组件库对象
     * @deprecated 使用动态获取替代硬编码创建
     */
    @Deprecated
    private ComponentLibrary createAntDesignVueLibrary() {
        VueKitLogger.warn(LOG, "createAntDesignVueLibrary 方法已废弃，请使用动态获取");
        return null;
    }

    /**
     * 创建 Element UI 组件库（已废弃）
     * 
     * @return Element UI 组件库对象
     * @deprecated 使用动态获取替代硬编码创建
     */
    @Deprecated
    private ComponentLibrary createElementUILibrary() {
        VueKitLogger.warn(LOG, "createElementUILibrary 方法已废弃，请使用动态获取");
        return null;
    }

    /**
     * 创建基本的 Element Plus 组件列表
     * 
     * @return 组件列表
     */
    private java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentInfo> createBasicElementPlusComponents() {
        java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentInfo> components = new java.util.ArrayList<>();
        
        // 添加一些基本的 Element Plus 组件
        components.add(createComponentInfo("button", "按钮", "常用的操作按钮"));
        components.add(createComponentInfo("input", "输入框", "通过鼠标或键盘输入字符"));
        components.add(createComponentInfo("select", "选择器", "当选项过多时，使用下拉菜单展示并选择内容"));
        components.add(createComponentInfo("table", "表格", "用于展示多条结构类似的数据"));
        components.add(createComponentInfo("form", "表单", "由输入框、选择器、单选框、多选框等控件组成"));
        
        return components;
    }

    /**
     * 创建基本的 Ant Design Vue 组件列表
     * 
     * @return 组件列表
     */
    private java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentInfo> createBasicAntDesignVueComponents() {
        java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentInfo> components = new java.util.ArrayList<>();
        
        // 添加一些基本的 Ant Design Vue 组件
        components.add(createComponentInfo("button", "按钮", "按钮用于开始一个即时操作"));
        components.add(createComponentInfo("input", "输入框", "通过鼠标或键盘输入字符"));
        components.add(createComponentInfo("select", "选择器", "下拉选择器"));
        components.add(createComponentInfo("table", "表格", "展示行列数据"));
        components.add(createComponentInfo("form", "表单", "具有数据收集、验证和提交功能的表单"));
        
        return components;
    }

    /**
     * 创建基本的 Element UI 组件列表
     * 
     * @return 组件列表
     */
    private java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentInfo> createBasicElementUIComponents() {
        java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentInfo> components = new java.util.ArrayList<>();
        
        // 添加一些基本的 Element UI 组件
        components.add(createComponentInfo("button", "按钮", "常用的操作按钮"));
        components.add(createComponentInfo("input", "输入框", "通过鼠标或键盘输入字符"));
        components.add(createComponentInfo("select", "选择器", "当选项过多时，使用下拉菜单展示并选择内容"));
        components.add(createComponentInfo("table", "表格", "用于展示多条结构类似的数据"));
        components.add(createComponentInfo("form", "表单", "由输入框、选择器、单选框、多选框等控件组成"));
        
        return components;
    }

    /**
     * 创建组件信息
     * 
     * @param name 组件名称
     * @param displayName 显示名称
     * @param description 描述
     * @return 组件信息对象
     */
    private com.chu7.vuecomponentassistant.remote.model.ComponentInfo createComponentInfo(String name, String displayName, String description) {
        com.chu7.vuecomponentassistant.remote.model.ComponentInfo component = new com.chu7.vuecomponentassistant.remote.model.ComponentInfo(
            name, displayName, description, name
        );
        return component;
    }
} 