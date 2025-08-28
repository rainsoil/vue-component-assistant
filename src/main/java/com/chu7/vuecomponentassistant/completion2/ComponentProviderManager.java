package com.chu7.vuecomponentassistant.completion2;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.diagnostic.Logger;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ComponentProvider 管理器
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>管理项目中所有的 ComponentProvider 实例</li>
 *   <li>当组件库发生变化时，通知所有 ComponentProvider 重新加载数据</li>
 *   <li>确保组件库的增删改操作能够及时反映到补全提示中</li>
 *   <li>提供项目级别的组件提供者隔离</li>
 *   <li>支持动态组件库更新和同步</li>
 * </ul>
 * 
 * <p>设计特点：</p>
 * <ul>
 *   <li>使用 ConcurrentHashMap 确保线程安全</li>
 *   <li>支持多项目环境，每个项目独立管理</li>
 *   <li>延迟初始化，按需创建 ComponentProvider</li>
 *   <li>提供统一的通知机制</li>
 *   <li>完善的错误处理和日志记录</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>多项目开发环境</li>
 *   <li>组件库动态更新</li>
 *   <li>补全功能的数据同步</li>
 *   <li>项目级别的组件管理</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 1.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.completion2.ComponentProvider
 * @see com.chu7.vuecomponentassistant.utils.VueKitLogger
 */
public class ComponentProviderManager {
    
    /**
     * 日志记录器
     * 用于记录组件提供者管理过程中的关键信息和错误
     */
    private static final Logger LOG = VueKitLogger.getLogger(ComponentProviderManager.class);
    
    /**
     * 项目到组件提供者的映射表
     * 使用 ConcurrentHashMap 确保线程安全，支持多项目环境
     */
    private static final Map<Project, ComponentProvider> providers = new ConcurrentHashMap<>();
    
    /**
     * 获取或创建项目的 ComponentProvider
     * 
     * <p>该方法会执行以下操作：</p>
     * <ol>
     *   <li>检查项目是否已有注册的 ComponentProvider</li>
     *   <li>如果没有，创建新的 ComponentProvider 实例</li>
     *   <li>返回可用的 ComponentProvider</li>
     * </ol>
     * 
     * <p>注意事项：</p>
     * <ul>
     *   <li>ComponentProvider 构造函数中会自动调用 registerProvider</li>
     *   <li>避免使用 computeIfAbsent 防止递归调用</li>
     *   <li>每个项目都有独立的组件提供者实例</li>
     * </ul>
     * 
     * @param project 项目对象，不能为null
     * @return 项目的 ComponentProvider 实例
     * @throws IllegalArgumentException 如果project为null
     * 
     * @see com.chu7.vuecomponentassistant.completion2.ComponentProvider#ComponentProvider(Project)
     * @see #registerProvider(Project, ComponentProvider)
     */
    public static ComponentProvider getProvider(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        
        ComponentProvider provider = providers.get(project);
        if (provider == null) {
            // 直接创建新的 ComponentProvider 实例，不通过 computeIfAbsent 避免递归
            provider = new ComponentProvider(project);
            // 注意：ComponentProvider 构造函数中会调用 registerProvider，所以这里不需要再次注册
        }
        return provider;
    }
    
    /**
     * 检查项目是否已注册 ComponentProvider
     * 
     * <p>该方法用于检查指定项目是否已经有注册的组件提供者实例。</p>
     * 
     * @param project 项目对象，不能为null
     * @return 如果项目已注册 ComponentProvider 则返回true，否则返回false
     * @throws IllegalArgumentException 如果project为null
     * 
     * @see #registerProvider(Project, ComponentProvider)
     * @see #removeProvider(Project)
     */
    public static boolean isProviderRegistered(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        return providers.containsKey(project);
    }
    
    /**
     * 注册项目的 ComponentProvider
     * 
     * <p>该方法用于将项目的组件提供者注册到管理器中，
     * 通常由 ComponentProvider 构造函数自动调用。</p>
     * 
     * <p>注册流程：</p>
     * <ol>
     *   <li>将项目与组件提供者关联</li>
     *   <li>记录注册操作的日志信息</li>
     *   <li>支持后续的管理和通知操作</li>
     * </ol>
     * 
     * @param project 项目对象，不能为null
     * @param provider 组件提供者实例，不能为null
     * @throws IllegalArgumentException 如果project或provider为null
     * 
     * @see #removeProvider(Project)
     * @see #isProviderRegistered(Project)
     */
    public static void registerProvider(Project project, ComponentProvider provider) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        if (provider == null) {
            throw new IllegalArgumentException("组件提供者不能为null");
        }
        
        providers.put(project, provider);
        VueKitLogger.debug(LOG, "注册项目的 ComponentProvider: " + project.getName());
    }
    
    /**
     * 移除项目的 ComponentProvider
     * 
     * <p>该方法用于从管理器中移除指定项目的组件提供者，
     * 通常在项目关闭或清理时调用。</p>
     * 
     * <p>移除操作：</p>
     * <ul>
     *   <li>从映射表中移除项目关联</li>
     *   <li>记录移除操作的日志信息</li>
     *   <li>释放相关资源</li>
     * </ul>
     * 
     * @param project 项目对象，不能为null
     * @throws IllegalArgumentException 如果project为null
     * 
     * @see #registerProvider(Project, ComponentProvider)
     * @see #isProviderRegistered(Project)
     */
    public static void removeProvider(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        
        providers.remove(project);
        VueKitLogger.debug(LOG, "移除项目的 ComponentProvider: " + project.getName());
    }
    
    /**
     * 通知所有 ComponentProvider 重新加载组件数据
     * 
     * <p>当组件库发生变化时调用此方法，确保所有项目的补全功能
     * 都能获取到最新的组件信息。</p>
     * 
     * <p>通知流程：</p>
     * <ol>
     *   <li>遍历所有已注册的 ComponentProvider</li>
     *   <li>调用每个提供者的 reloadComponents 方法</li>
     *   <li>记录重新加载的结果和统计信息</li>
     *   <li>处理可能的异常情况</li>
     * </ol>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>远程组件库更新后</li>
     *   <li>本地组件库配置变更后</li>
     *   <li>组件库版本升级后</li>
     *   <li>手动触发数据刷新</li>
     * </ul>
     * 
     * @see #notifyProviderReload(Project)
     * @see com.chu7.vuecomponentassistant.completion2.ComponentProvider#reloadComponents()
     */
    public static void notifyAllProvidersReload() {
        VueKitLogger.debug(LOG, "通知所有 ComponentProvider 重新加载数据");
        
        for (ComponentProvider provider : providers.values()) {
            try {
                provider.reloadComponents();
            } catch (Exception e) {
                VueKitLogger.error(LOG, "重新加载 ComponentProvider 失败: " + e.getMessage(), e);
            }
        }
        
        VueKitLogger.debug(LOG, "所有 ComponentProvider 重新加载完成，共 " + providers.size() + " 个实例");
    }
    
    /**
     * 通知指定项目的 ComponentProvider 重新加载组件数据
     * 
     * <p>该方法用于通知特定项目的组件提供者重新加载数据，
     * 比全局通知更精确，性能更好。</p>
     * 
     * <p>通知流程：</p>
     * <ol>
     *   <li>查找指定项目的 ComponentProvider</li>
     *   <li>如果找到，调用其 reloadComponents 方法</li>
     *   <li>记录操作日志和结果</li>
     *   <li>处理可能的异常情况</li>
     * </ol>
     * 
     * @param project 项目对象，不能为null
     * @throws IllegalArgumentException 如果project为null
     * 
     * @see #notifyAllProvidersReload()
     * @see com.chu7.vuecomponentassistant.completion2.ComponentProvider#reloadComponents()
     */
    public static void notifyProviderReload(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为null");
        }
        
        ComponentProvider provider = providers.get(project);
        if (provider != null) {
            try {
                VueKitLogger.debug(LOG, "通知项目 " + project.getName() + " 的 ComponentProvider 重新加载数据");
                provider.reloadComponents();
            } catch (Exception e) {
                VueKitLogger.error(LOG, "重新加载项目 " + project.getName() + " 的 ComponentProvider 失败: " + e.getMessage(), e);
            }
        }
    }
    
    /**
     * 获取当前管理的 ComponentProvider 数量
     * 
     * <p>该方法用于获取当前管理器中注册的项目数量，
     * 可用于监控和调试目的。</p>
     * 
     * @return 当前管理的 ComponentProvider 数量
     * 
     * @see #isProviderRegistered(Project)
     * @see #clearAllProviders()
     */
    public static int getProviderCount() {
        return providers.size();
    }
    
    /**
     * 清空所有 ComponentProvider
     * 
     * <p>该方法用于清空管理器中所有的组件提供者，
     * 通常在插件关闭或系统清理时调用。</p>
     * 
     * <p>清空操作：</p>
     * <ul>
     *   <li>移除所有项目关联</li>
     *   <li>记录清空操作的日志信息</li>
     *   <li>释放相关资源</li>
     * </ul>
     * 
     * @see #getProviderCount()
     * @see #removeProvider(Project)
     */
    public static void clearAllProviders() {
        providers.clear();
        VueKitLogger.debug(LOG, "清空所有 ComponentProvider");
    }
} 