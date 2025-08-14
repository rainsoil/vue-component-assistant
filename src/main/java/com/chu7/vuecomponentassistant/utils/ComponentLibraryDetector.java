package com.chu7.vuecomponentassistant.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectUtil;
import com.intellij.openapi.vfs.VirtualFile;

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

    /**
     * 组件库类型枚举
     */
    public enum LibraryType {
        ELEMENT_UI("element-ui", "Element UI"),
        ELEMENT_PLUS("element-plus", "Element Plus"),
        ANT_DESIGN_VUE("ant-design-vue", "Ant Design Vue"),
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
    }

    /**
     * 检测项目使用的组件库
     * 
     * @param project 当前项目
     * @return 检测到的组件库类型
     */
    public static LibraryType detectComponentLibrary(Project project) {
        System.out.println("=== 开始检测组件库 ===");
        
        if (project == null) {
            System.out.println("项目为空，返回 UNKNOWN");
            return LibraryType.UNKNOWN;
        }

        VirtualFile projectDir = ProjectUtil.guessProjectDir(project);
        if (projectDir == null) {
            System.out.println("项目目录为空，返回 UNKNOWN");
            return LibraryType.UNKNOWN;
        }

        System.out.println("项目目录: " + projectDir.getPath());

        // 查找 package.json 文件
        VirtualFile packageJsonFile = projectDir.findChild("package.json");
        if (packageJsonFile == null) {
            System.out.println("未找到 package.json 文件，返回 UNKNOWN");
            return LibraryType.UNKNOWN;
        }

        System.out.println("找到 package.json 文件: " + packageJsonFile.getPath());

        try {
            // 读取 package.json 内容
            String packageJsonContent = readFileContent(packageJsonFile);
            if (packageJsonContent == null || packageJsonContent.trim().isEmpty()) {
                System.out.println("package.json 内容为空，返回 UNKNOWN");
                return LibraryType.UNKNOWN;
            }

            System.out.println("package.json 内容长度: " + packageJsonContent.length());

            // 解析 JSON
            JsonObject packageJson = JsonParser.parseString(packageJsonContent).getAsJsonObject();
            
            // 检查 dependencies
            System.out.println("检查 dependencies...");
            LibraryType result = checkDependencies(packageJson, "dependencies");
            if (result != LibraryType.UNKNOWN) {
                System.out.println("在 dependencies 中检测到: " + result.getDisplayName());
                return result;
            }

            // 检查 devDependencies
            System.out.println("检查 devDependencies...");
            result = checkDependencies(packageJson, "devDependencies");
            if (result != LibraryType.UNKNOWN) {
                System.out.println("在 devDependencies 中检测到: " + result.getDisplayName());
                return result;
            }

            // 检查 peerDependencies
            System.out.println("检查 peerDependencies...");
            result = checkDependencies(packageJson, "peerDependencies");
            if (result != LibraryType.UNKNOWN) {
                System.out.println("在 peerDependencies 中检测到: " + result.getDisplayName());
                return result;
            }

            System.out.println("未检测到任何支持的组件库，返回 UNKNOWN");
            return LibraryType.UNKNOWN;

        } catch (Exception e) {
            System.err.println("检测组件库时出错: " + e.getMessage());
            e.printStackTrace();
            return LibraryType.UNKNOWN;
        }
    }

    /**
     * 检查依赖项中是否包含组件库
     * 
     * @param packageJson package.json 的 JSON 对象
     * @param dependencyType 依赖类型（dependencies、devDependencies、peerDependencies）
     * @return 检测到的组件库类型
     */
    private static LibraryType checkDependencies(JsonObject packageJson, String dependencyType) {
        if (!packageJson.has(dependencyType)) {
            System.out.println("  " + dependencyType + " 不存在");
            return LibraryType.UNKNOWN;
        }

        JsonObject dependencies = packageJson.getAsJsonObject(dependencyType);
        System.out.println("  检查 " + dependencyType + " 中的依赖项...");
        
        // 检查 Element Plus
        if (dependencies.has("element-plus")) {
            String version = dependencies.get("element-plus").getAsString();
            System.out.println("    找到 element-plus: " + version);
            return LibraryType.ELEMENT_PLUS;
        }

        // 检查 Element UI
        if (dependencies.has("element-ui")) {
            String version = dependencies.get("element-ui").getAsString();
            System.out.println("    找到 element-ui: " + version);
            return LibraryType.ELEMENT_UI;
        }

        // 检查 Ant Design Vue
        if (dependencies.has("ant-design-vue")) {
            String version = dependencies.get("ant-design-vue").getAsString();
            System.out.println("    找到 ant-design-vue: " + version);
            return LibraryType.ANT_DESIGN_VUE;
        }

        System.out.println("  在 " + dependencyType + " 中未找到支持的组件库");
        return LibraryType.UNKNOWN;
    }

    /**
     * 读取文件内容
     * 
     * @param file 虚拟文件
     * @return 文件内容
     */
    private static String readFileContent(VirtualFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("读取文件失败: " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取组件库的组件前缀
     * 
     * @param libraryType 组件库类型
     * @return 组件前缀
     */
    public static String getComponentPrefix(LibraryType libraryType) {
        switch (libraryType) {
            case ELEMENT_UI:
                return "el-";
            case ELEMENT_PLUS:
                return "el-";
            case ANT_DESIGN_VUE:
                return "a-";
            default:
                return "";
        }
    }

    /**
     * 获取组件库的文档 URL 模板
     * 
     * @param libraryType 组件库类型
     * @return 文档 URL 模板
     */
    public static String getDocumentationUrlTemplate(LibraryType libraryType) {
        switch (libraryType) {
            case ELEMENT_UI:
                return "https://element.eleme.cn/#/zh-CN/component/%s";
            case ELEMENT_PLUS:
                return "https://element-plus.org/zh-CN/component/%s.html";
            case ANT_DESIGN_VUE:
                return "https://antdv.com/components/%s-cn";
            default:
                return "";
        }
    }

    /**
     * 检查组件是否属于指定的组件库
     * 
     * @param componentName 组件名称
     * @param libraryType 组件库类型
     * @return 是否属于该组件库
     */
    public static boolean isComponentFromLibrary(String componentName, LibraryType libraryType) {
        if (componentName == null) {
            return false;
        }

        String prefix = getComponentPrefix(libraryType);
        return componentName.startsWith(prefix);
    }

    /**
     * 获取组件库的配置文件路径
     * 
     * @param libraryType 组件库类型
     * @return 配置文件路径
     */
    public static String getComponentDataPath(LibraryType libraryType) {
        switch (libraryType) {
            case ELEMENT_UI:
                return "/data/element-ui-components.json";
            case ELEMENT_PLUS:
                return "/data/element-plus-components.json";
            case ANT_DESIGN_VUE:
                return "/data/ant-design-vue-components.json";
            default:
                return "/data/element-plus-components.json"; // 默认使用 Element Plus
        }
    }

    /**
     * 打印检测信息（用于调试）
     * 
     * @param project 当前项目
     */
    public static void printDetectionInfo(Project project) {
        System.out.println("=== 组件库检测信息 ===");
        System.out.println("项目路径: " + (project != null ? project.getBasePath() : "null"));
        
        LibraryType detectedType = detectComponentLibrary(project);
        System.out.println("检测到的组件库: " + detectedType.getDisplayName());
        System.out.println("组件前缀: " + getComponentPrefix(detectedType));
        System.out.println("文档模板: " + getDocumentationUrlTemplate(detectedType));
        System.out.println("数据文件: " + getComponentDataPath(detectedType));
        System.out.println("=====================");
    }
}
