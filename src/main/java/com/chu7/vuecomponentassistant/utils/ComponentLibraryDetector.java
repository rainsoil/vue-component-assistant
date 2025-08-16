package com.chu7.vuecomponentassistant.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.diagnostic.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 组件库检测器
 * 
 * 功能说明：
 * - 读取项目根目录下的 package.json 文件
 * - 检测项目使用的组件库类型
 * - 支持 Element UI、Element Plus、Ant Design Vue
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class ComponentLibraryDetector {
    
    /** 日志记录器 */
    private static final Logger LOG = Logger.getInstance(ComponentLibraryDetector.class);

    /**
     * 组件库类型枚举
     * 注意：这个枚举现在主要用于向后兼容
     * 新的组件库信息应该从远程组件库管理器中动态获取
     */
    public enum LibraryType {
        ELEMENT_UI("element-ui", "Element UI"),
        ELEMENT_PLUS("element-plus", "Element Plus"),
        ANT_DESIGN_VUE("ant-design-vue", "Ant Design Vue"),
        VUETIFY("vuetify", "Vuetify"),
        QUASAR("quasar", "Quasar"),
        UNKNOWN("unknown", "未知组件库");

        private final String packageName;
        private final String displayName;

        LibraryType(String packageName, String displayName) {
            this.packageName = packageName;
            this.displayName = displayName;
        }

        public String getPackageName() {
            return packageName;
        }

        public String getDisplayName() {
            return displayName;
        }
        
        /**
         * 从组件库名称动态创建 LibraryType
         * 优先从远程组件库管理器获取信息
         */
        public static LibraryType fromLibraryName(String libraryName) {
            if (libraryName == null || libraryName.trim().isEmpty()) {
                return UNKNOWN;
            }
            
            try {
                // 尝试从远程组件库管理器获取信息
                com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                    new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
                java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                    libraryManager.getAllLibraries();
                
                for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                    if (libraryName.equalsIgnoreCase(library.getName())) {
                        // 根据组件库名称推断类型
                        return inferLibraryType(library.getName());
                    }
                }
            } catch (Exception e) {
                VueKitLogger.debug(LOG, "从远程获取组件库信息失败，使用本地推断: " + e.getMessage());
            }
            
            // 后备方案：使用本地推断
            return inferLibraryType(libraryName);
        }
        
        /**
         * 根据组件库名称推断类型
         */
        private static LibraryType inferLibraryType(String libraryName) {
            String lowerName = libraryName.toLowerCase();
            
            if (lowerName.contains("element-plus")) {
                return ELEMENT_PLUS;
            } else if (lowerName.contains("element-ui")) {
                return ELEMENT_UI;
            } else if (lowerName.contains("ant-design-vue")) {
                return ANT_DESIGN_VUE;
            } else if (lowerName.contains("vuetify")) {
                return VUETIFY;
            } else if (lowerName.contains("quasar")) {
                return QUASAR;
            }
            
            return UNKNOWN;
        }
    }

    /**
     * 检测项目使用的组件库
     * 
     * 检测逻辑：
     * 1. 验证项目对象是否有效
     * 2. 获取项目根目录
     * 3. 查找并读取 package.json 文件
     * 4. 解析 JSON 内容，检查 dependencies 和 devDependencies
     * 5. 根据检测到的包名返回对应的组件库类型
     * 
     * @param project 当前项目对象，不能为 null
     * @return 检测到的组件库类型，如果未检测到则返回 UNKNOWN
     * @throws IllegalArgumentException 如果项目对象为 null
     */
    public static LibraryType detectComponentLibrary(Project project) {
        // 参数验证
        if (project == null) {
            throw new IllegalArgumentException("项目对象不能为 null");
        }
        
        VueKitLogger.debug(LOG, "=== 开始检测组件库 ===");
        
        // 获取项目根目录
        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) {
            VueKitLogger.warn(LOG, "无法获取项目根目录，返回 UNKNOWN");
            return LibraryType.UNKNOWN;
        }

        VueKitLogger.debug(LOG, "项目目录: " + projectDir.getPath());

        // 查找 package.json 文件
        VirtualFile packageJsonFile = projectDir.findChild("package.json");
        if (packageJsonFile == null) {
            VueKitLogger.warn(LOG, "未找到 package.json 文件，返回 UNKNOWN");
            return LibraryType.UNKNOWN;
        }

        VueKitLogger.debug(LOG, "找到 package.json 文件: " + packageJsonFile.getPath());

        try {
            // 读取 package.json 内容
            String packageJsonContent = readFileContent(packageJsonFile);
            if (packageJsonContent == null || packageJsonContent.trim().isEmpty()) {
                VueKitLogger.warn(LOG, "package.json 内容为空，返回 UNKNOWN");
                return LibraryType.UNKNOWN;
            }

            VueKitLogger.debug(LOG, "package.json 内容长度: " + packageJsonContent.length());

            // 解析 JSON
            JsonObject packageJson = JsonParser.parseString(packageJsonContent).getAsJsonObject();
            
            // 优先检查 dependencies，然后检查 devDependencies
            VueKitLogger.debug(LOG, "检查 dependencies...");
            LibraryType result = checkDependencies(packageJson, "dependencies");
            if (result != LibraryType.UNKNOWN) {
                VueKitLogger.info(LOG, "在 dependencies 中检测到: " + result.getDisplayName());
                return result;
            }

            // 检查 devDependencies
            VueKitLogger.debug(LOG, "检查 devDependencies...");
            result = checkDependencies(packageJson, "devDependencies");
            if (result != LibraryType.UNKNOWN) {
                VueKitLogger.info(LOG, "在 devDependencies 中检测到: " + result.getDisplayName());
                return result;
            }

            // 检查 peerDependencies
            VueKitLogger.debug(LOG, "检查 peerDependencies...");
            result = checkDependencies(packageJson, "peerDependencies");
            if (result != LibraryType.UNKNOWN) {
                VueKitLogger.info(LOG, "在 peerDependencies 中检测到: " + result.getDisplayName());
                return result;
            }

            VueKitLogger.info(LOG, "未检测到任何支持的组件库，返回 UNKNOWN");
            return LibraryType.UNKNOWN;

        } catch (Exception e) {
            VueKitLogger.error(LOG, "检测组件库时出错: " + e.getMessage(), e);
            return LibraryType.UNKNOWN;
        }
    }

    /**
     * 检查依赖项中是否包含组件库
     * 
     * 检测逻辑：
     * 1. 验证指定的依赖类型是否存在
     * 2. 获取依赖对象并遍历检查
     * 3. 动态检查已安装的组件库
     * 4. 记录检测到的组件库版本信息
     * 
     * @param packageJson package.json 的 JSON 对象，不能为 null
     * @param dependencyType 依赖类型（dependencies、devDependencies、peerDependencies）
     * @return 检测到的组件库类型，如果未检测到则返回 UNKNOWN
     */
    private static LibraryType checkDependencies(JsonObject packageJson, String dependencyType) {
        if (!packageJson.has(dependencyType)) {
            VueKitLogger.debug(LOG, "  " + dependencyType + " 不存在");
            return LibraryType.UNKNOWN;
        }

        JsonObject dependencies = packageJson.getAsJsonObject(dependencyType);
        VueKitLogger.debug(LOG, "  检查 " + dependencyType + " 中的依赖项...");
        
        try {
            // 动态获取已安装的组件库列表
            com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager = 
                new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
            java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries = 
                libraryManager.getAllLibraries();
            
            // 检查每个已安装的组件库是否在依赖中
            for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
                String packageName = library.getName();
                if (dependencies.has(packageName)) {
                    String version = dependencies.get(packageName).getAsString();
                    VueKitLogger.info(LOG, "    找到 " + packageName + ": " + version);
                    return LibraryType.fromLibraryName(packageName);
                }
            }
            
            // 检查一些常见的组件库包名变体
            String[] commonPackages = {
                "element-plus", "element-ui", "ant-design-vue", "vuetify", "quasar",
                "@element-plus/icons-vue", "@ant-design/icons-vue", "@quasar/extras"
            };
            
            for (String packageName : commonPackages) {
                if (dependencies.has(packageName)) {
                    String version = dependencies.get(packageName).getAsString();
                    VueKitLogger.info(LOG, "    找到 " + packageName + ": " + version);
                    return LibraryType.fromLibraryName(packageName);
                }
            }
            
        } catch (Exception e) {
            VueKitLogger.debug(LOG, "动态检查组件库失败，使用静态检查: " + e.getMessage());
            
            // 后备方案：静态检查
            if (dependencies.has("element-plus")) {
                String version = dependencies.get("element-plus").getAsString();
                VueKitLogger.info(LOG, "    找到 element-plus: " + version);
                return LibraryType.ELEMENT_PLUS;
            }
            if (dependencies.has("element-ui")) {
                String version = dependencies.get("element-ui").getAsString();
                VueKitLogger.info(LOG, "    找到 element-ui: " + version);
                return LibraryType.ELEMENT_UI;
            }
            if (dependencies.has("ant-design-vue")) {
                String version = dependencies.get("ant-design-vue").getAsString();
                VueKitLogger.info(LOG, "    找到 ant-design-vue: " + version);
                return LibraryType.ANT_DESIGN_VUE;
            }
            if (dependencies.has("vuetify")) {
                String version = dependencies.get("vuetify").getAsString();
                VueKitLogger.info(LOG, "    找到 vuetify: " + version);
                return LibraryType.VUETIFY;
            }
            if (dependencies.has("quasar")) {
                String version = dependencies.get("quasar").getAsString();
                VueKitLogger.info(LOG, "    找到 quasar: " + version);
                return LibraryType.QUASAR;
            }
        }

        VueKitLogger.debug(LOG, "  在 " + dependencyType + " 中未找到支持的组件库");
        return LibraryType.UNKNOWN;
    }

    /**
     * 读取文件内容
     * 
     * 读取逻辑：
     * 1. 获取文件的输入流
     * 2. 读取所有字节数据
     * 3. 使用UTF-8编码转换为字符串
     * 4. 自动关闭输入流（使用try-with-resources）
     * 
     * @param file 要读取的虚拟文件，不能为 null
     * @return 文件内容字符串，如果读取失败则返回 null
     * @throws IllegalArgumentException 如果文件对象为 null
     */
    private static String readFileContent(VirtualFile file) {
        if (file == null) {
            throw new IllegalArgumentException("文件对象不能为 null");
        }
        
        try (InputStream inputStream = file.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            String content = new String(bytes, StandardCharsets.UTF_8);
            VueKitLogger.debug(LOG, "成功读取文件: " + file.getPath() + ", 内容长度: " + content.length());
            return content;
        } catch (IOException e) {
            VueKitLogger.error(LOG, "读取文件失败: " + file.getPath() + ", 错误: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * 获取组件库的组件前缀
     * 
     * 组件前缀说明：
     * - Element UI/Plus: 使用 "el-" 前缀（如 el-button, el-input）
     * - Ant Design Vue: 使用 "a-" 前缀（如 a-button, a-input）
     * - 未知类型: 返回空字符串
     * 
     * @param libraryType 组件库类型，不能为 null
     * @return 对应的组件前缀字符串
     * @throws IllegalArgumentException 如果组件库类型为 null
     */
    public static String getComponentPrefix(LibraryType libraryType) {
        if (libraryType == null) {
            throw new IllegalArgumentException("组件库类型不能为 null");
        }
        
        switch (libraryType) {
            case ELEMENT_UI:
            case ELEMENT_PLUS:
                return "el-";
            case ANT_DESIGN_VUE:
                return "a-";
            case VUETIFY:
                return "v-";
            case QUASAR:
                return "q-";
            default:
                VueKitLogger.debug(LOG, "未知组件库类型: " + libraryType + ", 返回空前缀");
                return "";
        }
    }

    /**
     * 获取组件库的文档 URL 模板
     * 
     * 文档URL模板说明：
     * - Element UI: 官方中文文档，支持组件名占位符 %s
     * - Element Plus: 官方中文文档，支持组件名占位符 %s
     * - Ant Design Vue: 官方中文文档，支持组件名占位符 %s
     * - 未知类型: 返回空字符串
     * 
     * @param libraryType 组件库类型，不能为 null
     * @return 对应的文档URL模板字符串
     * @throws IllegalArgumentException 如果组件库类型为 null
     */
    public static String getDocumentationUrlTemplate(LibraryType libraryType) {
        if (libraryType == null) {
            throw new IllegalArgumentException("组件库类型不能为 null");
        }
        
        switch (libraryType) {
            case ELEMENT_UI:
                return "https://element.eleme.cn/#/zh-CN/component/%s";
            case ELEMENT_PLUS:
                return "https://element-plus.org/zh-CN/component/%s.html";
            case ANT_DESIGN_VUE:
                return "https://antdv.com/components/%s-cn";
            case VUETIFY:
                return "https://vuetifyjs.com/en/components/%s/";
            case QUASAR:
                return "https://quasar.dev/vue-components/%s";
            default:
                VueKitLogger.debug(LOG, "未知组件库类型: " + libraryType + ", 返回空文档模板");
                return "";
        }
    }

    /**
     * 检查组件是否属于指定的组件库
     * 
     * 检查逻辑：
     * 1. 验证组件名称不为空
     * 2. 获取组件库的组件前缀
     * 3. 检查组件名称是否以该前缀开头
     * 
     * @param componentName 要检查的组件名称，不能为 null 或空字符串
     * @param libraryType 组件库类型，不能为 null
     * @return 如果组件属于该组件库则返回 true，否则返回 false
     * @throws IllegalArgumentException 如果组件名称或组件库类型为 null
     */
    public static boolean isComponentFromLibrary(String componentName, LibraryType libraryType) {
        if (componentName == null || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException("组件名称不能为 null 或空字符串");
        }
        
        if (libraryType == null) {
            throw new IllegalArgumentException("组件库类型不能为 null");
        }

        String prefix = getComponentPrefix(libraryType);
        boolean belongsToLibrary = componentName.startsWith(prefix);
        
        VueKitLogger.debug(LOG, "检查组件 '" + componentName + "' 是否属于 '" + 
            libraryType.getDisplayName() + "' 组件库: " + belongsToLibrary);
        
        return belongsToLibrary;
    }

    /**
     * 获取组件库的配置文件路径
     * 
     * 注意：此方法已废弃，不再使用内置资源文件
     * 组件库数据现在通过远程组件库管理器动态加载
     * 
     * @param libraryType 组件库类型，不能为 null
     * @return 空字符串（不再使用内置资源文件）
     * @throws IllegalArgumentException 如果组件库类型为 null
     * @deprecated 使用远程组件库管理器替代
     */
    @Deprecated
    public static String getComponentDataPath(LibraryType libraryType) {
        if (libraryType == null) {
            throw new IllegalArgumentException("组件库类型不能为 null");
        }
        
        VueKitLogger.warn(LOG, "getComponentDataPath 方法已废弃，组件库数据现在通过远程组件库管理器动态加载");
        return ""; // 不再使用内置资源文件
    }

    /**
     * 打印检测信息（用于调试）
     * 
     * 调试信息包括：
     * - 项目基本信息（路径、名称等）
     * - 检测到的组件库类型
     * - 组件前缀
     * - 文档URL模板
     * - 数据文件路径
     * 
     * 注意：此方法主要用于开发和调试阶段，生产环境建议使用日志记录
     * 
     * @param project 当前项目，可以为 null
     */
    public static void printDetectionInfo(Project project) {
        VueKitLogger.info(LOG, "=== 组件库检测信息 ===");
        
        if (project == null) {
            VueKitLogger.warn(LOG, "项目对象为 null，无法获取项目信息");
            return;
        }
        
        VueKitLogger.info(LOG, "项目路径: " + project.getBasePath());
        VueKitLogger.info(LOG, "项目名称: " + project.getName());
        
        try {
            LibraryType detectedType = detectComponentLibrary(project);
            VueKitLogger.info(LOG, "检测到的组件库: " + detectedType.getDisplayName());
            VueKitLogger.info(LOG, "组件前缀: " + getComponentPrefix(detectedType));
            VueKitLogger.info(LOG, "文档模板: " + getDocumentationUrlTemplate(detectedType));
            VueKitLogger.info(LOG, "数据来源: 远程组件库管理器");
        } catch (Exception e) {
            VueKitLogger.error(LOG, "检测组件库时发生错误", e);
        }
        
        VueKitLogger.info(LOG, "=====================");
    }
}
