package com.chu7.vuecomponentassistant.startup;

import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.chu7.vuecomponentassistant.utils.PackageJsonAutoDetector;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * 组件库配置启动活动
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>在项目启动时自动加载组件库配置</li>
 *   <li>自动检测项目中的 package.json 文件并匹配组件库</li>
 *   <li>在首次使用时自动启用匹配到的组件库</li>
 *   <li>使用 StartupActivity 接口（兼容性支持）</li>
 *   <li>确保组件库配置在项目完全加载后生效</li>
 *   <li>智能配置文件生成和验证</li>
 *   <li>完整的启动日志记录</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>实现 IntelliJ IDEA 的 StartupActivity 接口</li>
 *   <li>在项目完全加载后执行，避免依赖问题</li>
 *   <li>智能检测项目配置文件状态</li>
 *   <li>自动生成缺失的配置文件</li>
 *   <li>完善的错误处理和日志记录</li>
 *   <li>启动失败不影响项目正常加载</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>项目首次打开时的自动配置</li>
 *   <li>项目重新加载后的配置恢复</li>
 *   <li>新项目依赖检测和组件库启用</li>
 *   <li>团队项目配置的统一初始化</li>
 *   <li>开发环境迁移时的配置自动恢复</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 3.0.0
 * @since 1.0.0
 * @see com.intellij.openapi.startup.StartupActivity
 * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager
 * @see com.chu7.vuecomponentassistant.utils.PackageJsonAutoDetector
 * @see com.chu7.vuecomponentassistant.utils.VueKitLogger
 */
public class ComponentLibraryStartupActivity implements StartupActivity {
    
    private static final Logger LOG = Logger.getInstance(ComponentLibraryStartupActivity.class);
    
    /**
     * 执行启动活动
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>在项目启动时自动执行组件库配置加载</li>
     *   <li>自动检测项目依赖并启用匹配的组件库</li>
     *   <li>确保项目配置文件存在且有效</li>
     *   <li>记录完整的启动过程和结果</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>记录启动开始日志</li>
     *   <li>获取组件库配置管理器</li>
     *   <li>触发项目启动时的配置加载</li>
     *   <li>检查并确保配置文件存在</li>
     *   <li>自动检测并启用项目中的组件库</li>
     *   <li>记录启动完成日志</li>
     * </ol>
     *
     * <p>错误处理：</p>
     * <ul>
     *   <li>启动失败时记录详细错误日志</li>
     *   <li>不会阻止项目正常加载</li>
     *   <li>使用 try-catch 确保异常不会传播</li>
     * </ul>
     *
     * @param project 当前项目实例，不能为null
     * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager#onProjectStarted(Project)
     * @see com.chu7.vuecomponentassistant.utils.PackageJsonAutoDetector#autoDetectAndEnableLibraries(Project)
     */
    @Override
    public void runActivity(@NotNull Project project) {
        try {
            VueKitLogger.info(LOG, "=== 启动组件库配置加载 ===");
            VueKitLogger.info(LOG, "项目名称: " + project.getName());
            VueKitLogger.info(LOG, "项目路径: " + project.getBasePath());
            
            // 获取组件库配置管理器
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            
            // 触发项目启动时的配置加载
            configManager.onProjectStarted(project);
            
            // 检查并确保配置文件存在
            ensureProjectConfigFileExists(project);
            
            // 自动检测并启用项目中的组件库
            boolean autoDetected = PackageJsonAutoDetector.autoDetectAndEnableLibraries(project);
            if (autoDetected) {
                VueKitLogger.info(LOG, "✅ 自动检测并启用组件库成功");
            } else {
                VueKitLogger.info(LOG, "ℹ️ 跳过自动检测（配置文件已存在或无匹配的组件库）");
            }
            
            VueKitLogger.info(LOG, "✅ 组件库配置加载完成");
            
        } catch (Exception e) {
            String errorMsg = "启动时加载组件库配置失败";
            VueKitLogger.error(LOG, errorMsg, e);
            // 启动失败不应该阻止项目正常加载，所以只记录日志
        }
    }
    
    /**
     * 确保项目配置文件存在
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>检查项目配置文件是否存在且有效</li>
     *   <li>如果配置文件不存在，尝试自动生成</li>
     *   <li>使用 PackageJsonAutoDetector 进行智能检测</li>
     *   <li>确保后续操作有可用的配置文件</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>检查配置文件是否存在且有效</li>
     *   <li>如果无效，尝试自动检测并生成配置</li>
     *   <li>自动检测失败时创建空配置文件</li>
     *   <li>记录配置文件的生成结果</li>
     * </ol>
     *
     * <p>错误处理：</p>
     * <ul>
     *   <li>配置文件检查失败时记录错误日志</li>
     *   <li>自动检测失败时创建空配置作为备用</li>
     *   <li>使用 try-catch 确保异常不会传播</li>
     * </ul>
     *
     * @param project 当前项目实例，不能为null
     * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager#hasValidProjectConfig(Project)
     * @see com.chu7.vuecomponentassistant.utils.PackageJsonAutoDetector#autoDetectAndEnableLibraries(Project)
     * @see #createEmptyProjectConfig(Project)
     */
    private void ensureProjectConfigFileExists(Project project) {
        try {
            VueKitLogger.info(LOG, "检查项目配置文件是否存在...");
            
            // 检查配置文件是否存在且有效
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            if (!configManager.hasValidProjectConfig(project)) {
                VueKitLogger.info(LOG, "项目配置文件不存在或无效，尝试生成...");
                
                // 尝试自动检测并生成配置文件
                boolean success = PackageJsonAutoDetector.autoDetectAndEnableLibraries(project);
                if (success) {
                    VueKitLogger.info(LOG, "✅ 项目配置文件生成成功");
                } else {
                    VueKitLogger.info(LOG, "⚠️ 项目配置文件生成失败，将使用默认配置");
                    // 创建空的配置文件，确保后续操作正常进行
                    createEmptyProjectConfig(project);
                }
            } else {
                VueKitLogger.info(LOG, "✅ 项目配置文件已存在且有效");
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "确保项目配置文件存在时发生错误", e);
        }
    }
    
    /**
     * 创建空的项目配置文件
     * 
     * @param project 项目对象
     */
    private void createEmptyProjectConfig(Project project) {
        try {
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            // 创建空的配置
            Set<String> emptyConfig = new HashSet<>();
            configManager.setProjectEnabledLibraryNames(project, emptyConfig);
            VueKitLogger.info(LOG, "已创建空的项目配置文件");
        } catch (Exception e) {
            VueKitLogger.error(LOG, "创建空项目配置文件失败", e);
        }
    }
}
