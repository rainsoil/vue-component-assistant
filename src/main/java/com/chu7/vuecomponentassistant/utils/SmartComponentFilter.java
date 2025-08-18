package com.chu7.vuecomponentassistant.utils;

import com.chu7.vuecomponentassistant.completion2.ElementPlusComponent;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.diagnostic.Logger;
import com.chu7.vuecomponentassistant.utils.VueKitLogger;
import com.chu7.vuecomponentassistant.utils.ComponentLibraryDetector;
import com.chu7.vuecomponentassistant.utils.LibraryTypeHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 智能组件库过滤器
 * 
 * 功能说明：
 * - 根据项目配置动态过滤组件库
 * - 智能排序：当前项目使用的组件库优先
 * - 自动检测项目依赖的组件库
 * - 支持用户配置的组件库启用/禁用
 * 
 * 特性：
 * - 自动化程度高：大部分情况下无需用户干预
 * - 用户可控：支持手动配置组件库启用状态
 * - 性能优化：避免加载不必要的组件库
 * - 智能匹配：根据 package.json 自动识别项目组件库
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class SmartComponentFilter {
    
    private static final Logger LOG = VueKitLogger.getLogger(SmartComponentFilter.class);
    
    // 组件库名称集合 - 现在动态从远程组件库管理器获取
    private static final Set<String> AVAILABLE_LIBRARIES = new HashSet<>();
    
    static {
        // 初始化时动态加载组件库
        initializeLibraryMapping();
    }
    
    /**
     * 初始化组件库映射
     */
    private static void initializeLibraryMapping() {
        try {
            // 动态获取已安装的组件库
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                String packageName = library.getName();
                AVAILABLE_LIBRARIES.add(packageName);
                
                // 动态生成相关包名，避免硬编码
                addRelatedPackages(packageName);
            }
            
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "动态初始化组件库映射失败: " + e.getMessage());
        }
    }
    
    /**
     * 动态添加相关包名
     * 
     * @param packageName 主包名
     */
    private static void addRelatedPackages(String packageName) {
        // 根据包名动态生成相关包名
        String normalizedPackageName = StringNormalizer.normalize(packageName);
        
        // 这里可以根据需要添加相关包名的生成逻辑
        // 例如：如果主包名是 element-plus，可以添加 @element-plus/icons-vue 等
        // 目前暂时不添加，避免硬编码
    }
    
    /**
     * 根据项目配置智能过滤组件
     * 
     * @param allComponents 所有可用组件
     * @param project 项目对象
     * @return 过滤后的组件列表，按优先级排序
     */
    public List<ElementPlusComponent> filterComponentsByProject(
            List<ElementPlusComponent> allComponents, 
            Project project) {
        
        try {
            VueKitLogger.debug(LOG, "开始智能过滤组件，项目: " + project.getName());
            
            // 1. 检测项目使用的组件库
            Set<String> projectLibraries = detectProjectLibraries(project);
            VueKitLogger.info(LOG, "检测到项目使用的组件库: " + String.join(", ", projectLibraries));
            
            // 2. 获取用户配置的启用组件库
            Set<String> enabledLibraries = getUserEnabledLibraries(project);
            VueKitLogger.debug(LOG, "用户启用的组件库: " + String.join(", ", enabledLibraries));
            
            // 3. 合并项目检测和用户配置
            Set<String> finalLibraries = new HashSet<>();
            finalLibraries.addAll(projectLibraries);
            finalLibraries.addAll(enabledLibraries);
            
            VueKitLogger.info(LOG, "最终启用的组件库: " + String.join(", ", finalLibraries));
            
            // 4. 过滤组件
            List<ElementPlusComponent> filteredComponents = allComponents.stream()
                .filter(component -> isFromEnabledLibrary(component, finalLibraries))
                .collect(Collectors.toList());
            
            VueKitLogger.debug(LOG, "过滤前组件数量: " + allComponents.size());
            VueKitLogger.debug(LOG, "过滤后组件数量: " + filteredComponents.size());
            
            // 5. 智能排序：项目组件库优先
            List<ElementPlusComponent> sortedComponents = sortComponentsByPriority(
                filteredComponents, projectLibraries);
            
            VueKitLogger.info(LOG, "智能过滤完成，返回 " + sortedComponents.size() + " 个组件");
            return sortedComponents;
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "智能过滤组件失败", e);
            // 出错时返回原始组件列表，确保功能可用
            return allComponents;
        }
    }
    
    /**
     * 检测项目使用的组件库
     * 
     * @param project 项目对象
     * @return 项目使用的组件库类型集合
     */
    public Set<String> detectProjectLibraries(Project project) {
        Set<String> libraries = new HashSet<>();
        
        try {
            // 1. 查找 package.json 文件
            VirtualFile packageJson = findPackageJson(project);
            if (packageJson == null) {
                VueKitLogger.warn(LOG, "项目根目录未找到 package.json 文件");
                return libraries;
            }
            
            // 2. 解析 package.json
            String packageJsonContent = new String(packageJson.contentsToByteArray(), StandardCharsets.UTF_8);
            Map<String, Object> packageData = parsePackageJson(packageJsonContent);
            
            if (packageData == null) {
                VueKitLogger.warn(LOG, "解析 package.json 失败");
                return libraries;
            }
            
            // 3. 检查 dependencies 和 devDependencies
            checkDependencies(packageData, "dependencies", libraries);
            checkDependencies(packageData, "devDependencies", libraries);
            
            VueKitLogger.info(LOG, "从 package.json 检测到组件库: " + String.join(", ", libraries));
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "检测项目组件库失败", e);
        }
        
        return libraries;
    }
    
    /**
     * 查找项目的 package.json 文件
     * 
     * @param project 项目对象
     * @return package.json 文件，如果未找到则返回 null
     */
    private VirtualFile findPackageJson(Project project) {
        try {
            VirtualFile projectDir = com.chu7.vuecomponentassistant.utils.ProjectPathHelper.getProjectRoot(project);
            VirtualFile packageJson = projectDir.findChild("package.json");
            
            if (packageJson != null && packageJson.exists()) {
                VueKitLogger.debug(LOG, "找到 package.json: " + packageJson.getPath());
                return packageJson;
            }
            
            // 尝试在子目录中查找
            for (VirtualFile child : projectDir.getChildren()) {
                if (child.isDirectory()) {
                    VirtualFile childPackageJson = child.findChild("package.json");
                    if (childPackageJson != null && childPackageJson.exists()) {
                        VueKitLogger.debug(LOG, "在子目录找到 package.json: " + childPackageJson.getPath());
                        return childPackageJson;
                    }
                }
            }
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "查找 package.json 失败", e);
        }
        
        return null;
    }
    
    /**
     * 解析 package.json 内容
     * 
     * @param content package.json 文件内容
     * @return 解析后的数据，如果解析失败则返回 null
     */
    private Map<String, Object> parsePackageJson(String content) {
        try {
            // 使用简单的 JSON 解析，避免引入额外的依赖
            // 这里可以后续替换为 Gson 或其他 JSON 解析器
            return parseSimpleJson(content);
        } catch (Exception e) {
            VueKitLogger.error(LOG, "解析 package.json 失败", e);
            return null;
        }
    }
    
    /**
     * 简单的 JSON 解析（临时实现）
     * 
     * @param content JSON 字符串
     * @return 解析后的数据
     */
    private Map<String, Object> parseSimpleJson(String content) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 提取 dependencies 和 devDependencies 部分
            extractDependenciesSection(content, "dependencies", result);
            extractDependenciesSection(content, "devDependencies", result);
            
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "简单 JSON 解析失败，使用备用方法", e);
        }
        
        return result;
    }
    
    /**
     * 提取依赖部分
     * 
     * @param content JSON 内容
     * @param sectionName 部分名称（dependencies 或 devDependencies）
     * @param result 结果映射
     */
    private void extractDependenciesSection(String content, String sectionName, Map<String, Object> result) {
        try {
            String pattern = "\"" + sectionName + "\"\\s*:\\s*\\{";
            int startIndex = content.indexOf(pattern);
            if (startIndex == -1) return;
            
            startIndex = content.indexOf("{", startIndex);
            if (startIndex == -1) return;
            
            int braceCount = 0;
            int endIndex = startIndex;
            
            for (int i = startIndex; i < content.length(); i++) {
                char c = content.charAt(i);
                if (c == '{') braceCount++;
                else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        endIndex = i + 1;
                        break;
                    }
                }
            }
            
            if (endIndex > startIndex) {
                String depsSection = content.substring(startIndex, endIndex);
                Map<String, String> deps = parseDependenciesMap(depsSection);
                result.put(sectionName, deps);
            }
            
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "提取 " + sectionName + " 部分失败", e);
        }
    }
    
    /**
     * 解析依赖映射
     * 
     * @param depsSection 依赖部分字符串
     * @return 依赖映射
     */
    private Map<String, String> parseDependenciesMap(String depsSection) {
        Map<String, String> deps = new HashMap<>();
        
        try {
            // 简单的正则匹配，提取包名和版本
            String[] lines = depsSection.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("\"") && line.contains(":")) {
                    int colonIndex = line.indexOf(":");
                    if (colonIndex > 0) {
                        String packageName = line.substring(1, colonIndex).trim();
                        String version = line.substring(colonIndex + 1).trim();
                        
                        // 清理版本字符串
                        version = version.replaceAll("[\",]", "").trim();
                        
                        if (!packageName.isEmpty() && !version.isEmpty()) {
                            deps.put(packageName, version);
                        }
                    }
                }
            }
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "解析依赖映射失败", e);
        }
        
        return deps;
    }
    
    /**
     * 检查依赖中的组件库
     * 
     * @param packageData package.json 数据
     * @param sectionName 依赖部分名称
     * @param libraries 检测到的组件库集合
     */
    private void checkDependencies(Map<String, Object> packageData, String sectionName, 
                                 Set<String> libraries) {
        
        Object depsObj = packageData.get(sectionName);
        if (depsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> deps = (Map<String, String>) depsObj;
            
            for (String packageName : deps.keySet()) {
                String libraryType = LibraryTypeHelper.getPackageName(packageName);
                if (LibraryTypeHelper.isKnownLibrary(libraryType)) {
                    libraries.add(libraryType);
                    VueKitLogger.debug(LOG, "在 " + sectionName + " 中发现组件库: " + 
                        packageName + " -> " + LibraryTypeHelper.getDisplayName(libraryType));
                }
            }
        }
    }
    
    /**
     * 获取用户启用的组件库
     * 
     * @param project 项目对象
     * @return 用户启用的组件库集合
     */
    private Set<String> getUserEnabledLibraries(Project project) {
        try {
            // 集成用户配置管理器
            ComponentLibraryConfigManager configManager = ComponentLibraryConfigManager.getInstance(project);
            Set<String> enabledLibraries = configManager.getEnabledLibraryNames(project);
            
            VueKitLogger.debug(LOG, "从用户配置获取启用的组件库: " + 
                enabledLibraries.stream()
                    .map(LibraryTypeHelper::getDisplayName)
                    .collect(Collectors.joining(", ")));
            
            return enabledLibraries;
            
        } catch (Exception e) {
            VueKitLogger.error(LOG, "获取用户启用的组件库失败", e);
            return new HashSet<>();
        }
    }
    
    /**
     * 检查组件是否来自启用的组件库
     * 
     * @param component 组件对象
     * @param enabledLibraries 启用的组件库集合
     * @return 如果组件来自启用的组件库则返回 true
     */
    private boolean isFromEnabledLibrary(ElementPlusComponent component, 
                                       Set<String> enabledLibraries) {
        
        // 如果没有启用任何组件库，则不显示任何组件
        if (enabledLibraries.isEmpty()) {
            VueKitLogger.debug(LOG, "没有启用任何组件库，不显示任何组件");
            return false;
        }
        
        // 根据组件名称前缀判断来源
        String componentName = component.getName();
        if (componentName == null) {
            return false;
        }
        
        // 检查组件是否来自启用的组件库
        for (String libraryType : enabledLibraries) {
            if (isComponentFromLibrary(componentName, libraryType)) {
                VueKitLogger.debug(LOG, "组件 " + componentName + " 来自启用的组件库: " + LibraryTypeHelper.getDisplayName(libraryType));
                return true;
            }
        }
        
        VueKitLogger.debug(LOG, "组件 " + componentName + " 不属于任何启用的组件库，将被过滤");
        return false;
    }
    
    /**
     * 检查组件是否来自指定的组件库
     * 
     * @param componentName 组件名称
     * @param libraryType 组件库类型
     * @return 如果组件来自指定组件库则返回 true
     */
    private boolean isComponentFromLibrary(String componentName, String libraryType) {
        return LibraryTypeHelper.isComponentFromLibrary(componentName, libraryType);
    }
    
    /**
     * 根据优先级排序组件
     * 
     * @param components 组件列表
     * @param projectLibraries 项目使用的组件库
     * @return 排序后的组件列表
     */
    private List<ElementPlusComponent> sortComponentsByPriority(
            List<ElementPlusComponent> components,
            Set<String> projectLibraries) {
        
        return components.stream()
            .sorted((c1, c2) -> {
                boolean c1IsProjectLibrary = isFromProjectLibrary(c1, projectLibraries);
                boolean c2IsProjectLibrary = isFromProjectLibrary(c2, projectLibraries);
                
                if (c1IsProjectLibrary && !c2IsProjectLibrary) return -1;
                if (!c1IsProjectLibrary && c2IsProjectLibrary) return 1;
                
                // 如果优先级相同，按名称排序
                return c1.getName().compareTo(c2.getName());
            })
            .collect(Collectors.toList());
    }
    
    /**
     * 检查组件是否来自项目使用的组件库
     * 
     * @param component 组件对象
     * @param projectLibraries 项目使用的组件库
     * @return 如果组件来自项目使用的组件库则返回 true
     */
    private boolean isFromProjectLibrary(ElementPlusComponent component,
                                       Set<String> projectLibraries) {
        
        String componentName = component.getName();
        if (componentName == null) {
            return false;
        }
        
        // 根据组件名称前缀判断是否来自项目使用的组件库
        for (String libraryType : projectLibraries) {
            if (isComponentFromLibrary(componentName, libraryType)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 获取组件库的包名映射
     * 
     * @return 包名到组件库类型的映射
     */
    public static Map<String, String> getPackageToLibraryMap() {
        Map<String, String> packageToLibrary = new HashMap<>();
        
        try {
            // 动态获取已安装的组件库
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            if (installedLibraries != null && !installedLibraries.isEmpty()) {
                for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                    String packageName = library.getName();
                    if (packageName != null && !packageName.trim().isEmpty()) {
                        packageToLibrary.put(packageName, packageName);
                        VueKitLogger.debug(LOG, "动态添加包名映射: " + packageName + " -> " + packageName);
                    }
                }
            } else {
                VueKitLogger.debug(LOG, "没有找到已安装的组件库，返回空映射");
            }
        } catch (Exception e) {
            VueKitLogger.warn(LOG, "动态获取组件库映射失败，返回空映射: " + e.getMessage());
        }
        
        return packageToLibrary;
    }
}
