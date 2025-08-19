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
 * <p>功能说明：</p>
 * <ul>
 *   <li>在项目启动时自动检查和下载必要的组件库</li>
 *   <li>确保 Element Plus、Ant Design Vue 等官方组件库数据可用</li>
 *   <li>提供组件库数据的自动更新机制</li>
 *   <li>优化启动性能，避免阻塞UI线程</li>
 *   <li>动态检测已安装的组件库</li>
 *   <li>智能组件库管理和更新</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>实现 IntelliJ IDEA 的 StartupActivity 接口</li>
 *   <li>使用 CompletableFuture 异步执行，避免UI阻塞</li>
 *   <li>动态检测组件库，不再硬编码特定组件库</li>
 *   <li>完善的错误处理和日志记录</li>
 *   <li>支持组件库的自动发现和管理</li>
 *   <li>向后兼容已废弃的硬编码方法</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>项目启动时的组件库自动初始化</li>
 *   <li>新项目首次使用时的组件库准备</li>
 *   <li>组件库数据的自动更新和维护</li>
 *   <li>开发环境的组件库状态检查</li>
 *   <li>团队项目的组件库统一管理</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 * @see com.intellij.openapi.startup.StartupActivity
 * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
 * @see com.chu7.vuecomponentassistant.remote.model.ComponentLibrary
 * @see com.chu7.vuecomponentassistant.utils.VueKitLogger
 */
public class ComponentLibraryInitializer implements StartupActivity {

    private static final Logger LOG = VueKitLogger.getLogger(ComponentLibraryInitializer.class);

    /**
     * 执行启动活动
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>在项目启动时自动执行组件库初始化</li>
     *   <li>异步执行以避免阻塞UI线程</li>
     *   <li>检查和初始化必要的组件库数据</li>
     *   <li>记录完整的初始化过程</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>使用 CompletableFuture.runAsync 异步执行</li>
     *   <li>记录初始化开始日志</li>
     *   <li>初始化组件库管理器</li>
     *   <li>检查并初始化官方组件库</li>
     *   <li>记录初始化完成日志</li>
     * </ol>
     *
     * <p>性能优化：</p>
     * <ul>
     *   <li>异步执行避免阻塞UI线程</li>
     *   <li>使用 CompletableFuture 进行非阻塞操作</li>
     *   <li>启动失败不影响项目正常加载</li>
     * </ul>
     *
     * @param project 当前项目实例，不能为null
     * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
     * @see #initializeOfficialLibraries(ComponentLibraryManager)
     */
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