package com.chu7.vuecomponentassistant.utils;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.intellij.openapi.diagnostic.Logger;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * 组件库自动发现服务
 * 自动发现下载的组件库并配置相应的识别信息
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class LibraryAutoDiscoveryService {
    
    private static final Logger LOG = VueKitLogger.getLogger(LibraryAutoDiscoveryService.class);
    
    /** 组件库管理器 */
    private static final ComponentLibraryManager libraryManager = new ComponentLibraryManager();
    
    /** 动态配置管理器 */
    private static final DynamicLibraryConfigManager configManager = DynamicLibraryConfigManager.getInstance();
    
    /** 已处理的组件库ID集合 */
    private static final Set<String> processedLibraries = new HashSet<>();
    
    /**
     * 自动发现并配置所有下载的组件库
     * 在系统启动或组件库更新后调用
     */
    public static void autoDiscoverAllLibraries() {
        try {
            LOG.info("开始自动发现组件库...");
            
            List<ComponentLibrary> libraries = libraryManager.getAllLibraries();
            int discoveredCount = 0;
            
            for (ComponentLibrary library : libraries) {
                if (library != null && library.getId() != null) {
                    if (autoDiscoverLibrary(library)) {
                        discoveredCount++;
                    }
                }
            }
            
            LOG.info("组件库自动发现完成，处理了 " + discoveredCount + " 个组件库");
            
        } catch (Exception e) {
            LOG.error("自动发现组件库失败", e);
        }
    }
    
    /**
     * 自动发现并配置单个组件库
     * 
     * @param library 组件库信息
     * @return 是否成功发现并配置
     */
    public static boolean autoDiscoverLibrary(ComponentLibrary library) {
        if (library == null || library.getId() == null) {
            return false;
        }
        
        String libraryId = library.getId();
        
        // 如果已经处理过，跳过
        if (processedLibraries.contains(libraryId)) {
            return false;
        }
        
        try {
            // 检查是否已经在配置中
            if (configManager.isKnownLibrary(libraryId)) {
                LOG.debug("组件库已配置，跳过: " + libraryId);
                processedLibraries.add(libraryId);
                return true;
            }
            
            // 尝试自动推断配置
            if (DynamicLibraryInfoProvider.autoInferLibraryConfig(libraryId)) {
                LOG.info("成功自动发现并配置组件库: " + libraryId);
                processedLibraries.add(libraryId);
                return true;
            } else {
                LOG.warn("无法自动推断组件库配置: " + libraryId);
                return false;
            }
            
        } catch (Exception e) {
            LOG.error("自动发现组件库失败: " + libraryId, e);
            return false;
        }
    }
    
    /**
     * 处理新下载的组件库
     * 在用户下载新组件库后调用
     * 
     * @param libraryId 新下载的组件库ID
     * @return 是否成功处理
     */
    public static boolean handleNewlyDownloadedLibrary(String libraryId) {
        if (libraryId == null) {
            return false;
        }
        
        try {
            ComponentLibrary library = libraryManager.getLibraryById(libraryId);
            if (library != null) {
                return autoDiscoverLibrary(library);
            } else {
                LOG.warn("新下载的组件库不存在: " + libraryId);
                return false;
            }
        } catch (Exception e) {
            LOG.error("处理新下载的组件库失败: " + libraryId, e);
            return false;
        }
    }
    
    /**
     * 清理已处理的组件库记录
     * 在系统重启或配置重置后调用
     */
    public static void clearProcessedRecords() {
        processedLibraries.clear();
        LOG.debug("已清理组件库处理记录");
    }
    
    /**
     * 获取已处理的组件库数量
     * 
     * @return 已处理的组件库数量
     */
    public static int getProcessedLibraryCount() {
        return processedLibraries.size();
    }
    
    /**
     * 检查组件库是否已被处理
     * 
     * @param libraryId 组件库ID
     * @return 是否已被处理
     */
    public static boolean isLibraryProcessed(String libraryId) {
        return processedLibraries.contains(libraryId);
    }
    
    /**
     * 强制重新处理组件库
     * 用于配置更新后的重新发现
     * 
     * @param libraryId 组件库ID
     * @return 是否成功重新处理
     */
    public static boolean forceReprocessLibrary(String libraryId) {
        if (libraryId == null) {
            return false;
        }
        
        // 从已处理记录中移除
        processedLibraries.remove(libraryId);
        
        // 重新发现
        return handleNewlyDownloadedLibrary(libraryId);
    }
}
