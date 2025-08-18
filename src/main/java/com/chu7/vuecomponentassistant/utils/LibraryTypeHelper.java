package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;

/**
 * 组件库类型辅助工具类
 * 替代原来的LibraryType枚举，直接使用字符串包名处理组件库配置
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class LibraryTypeHelper {
    
    /** 日志记录器 */
    private static final Logger LOG = Logger.getInstance(LibraryTypeHelper.class);
    
    // 支持的组件库包名常量
    public static final String ELEMENT_UI = "element-ui";
    public static final String ELEMENT_PLUS = "element-plus";
    public static final String ANT_DESIGN_VUE = "ant-design-vue";
    public static final String VUETIFY = "vuetify";
    public static final String QUASAR = "quasar";
    public static final String UNKNOWN = "unknown";
    
    // 支持的组件库显示名称常量
    public static final String ELEMENT_UI_DISPLAY = "Element UI";
    public static final String ELEMENT_PLUS_DISPLAY = "Element Plus";
    public static final String ANT_DESIGN_VUE_DISPLAY = "Ant Design Vue";
    public static final String VUETIFY_DISPLAY = "Vuetify";
    public static final String QUASAR_DISPLAY = "Quasar";
    public static final String UNKNOWN_DISPLAY = "未知组件库";
    
    /**
     * 从组件库名称获取包名
     * 
     * @param libraryName 组件库名称
     * @return 包名
     */
    public static String getPackageName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        String cleanName = libraryName.trim().toLowerCase();
        
        switch (cleanName) {
            case "element-ui":
            case "element ui":
                return ELEMENT_UI;
            case "element-plus":
            case "element plus":
                return ELEMENT_PLUS;
            case "ant-design-vue":
            case "ant design vue":
            case "antd":
                return ANT_DESIGN_VUE;
            case "vuetify":
                return VUETIFY;
            case "quasar":
                return QUASAR;
            default:
                return UNKNOWN;
        }
    }
    
    /**
     * 从组件库名称获取显示名称
     * 
     * @param libraryName 组件库名称
     * @return 显示名称
     */
    public static String getDisplayName(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return UNKNOWN_DISPLAY;
        }
        
        String cleanName = libraryName.trim().toLowerCase();
        
        switch (cleanName) {
            case "element-ui":
            case "element ui":
                return ELEMENT_UI_DISPLAY;
            case "element-plus":
            case "element plus":
                return ELEMENT_PLUS_DISPLAY;
            case "ant-design-vue":
            case "ant design vue":
            case "antd":
                return ANT_DESIGN_VUE_DISPLAY;
            case "vuetify":
                return VUETIFY_DISPLAY;
            case "quasar":
                return QUASAR_DISPLAY;
            default:
                return UNKNOWN_DISPLAY;
        }
    }
    
    /**
     * 检查是否是已知的组件库
     * 
     * @param libraryName 组件库名称
     * @return 是否是已知组件库
     */
    public static boolean isKnownLibrary(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return false;
        }
        
        String cleanName = libraryName.trim().toLowerCase();
        return cleanName.equals(ELEMENT_UI) || 
               cleanName.equals(ELEMENT_PLUS) || 
               cleanName.equals(ANT_DESIGN_VUE) || 
               cleanName.equals(VUETIFY) || 
               cleanName.equals(QUASAR);
    }
    
    /**
     * 获取组件前缀
     * 
     * @param libraryName 组件库名称
     * @return 组件前缀
     */
    public static String getComponentPrefix(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return "";
        }
        
        String cleanName = libraryName.trim().toLowerCase();
        
        switch (cleanName) {
            case "element-ui":
            case "element plus":
                return "el-";
            case "ant-design-vue":
            case "antd":
                return "a-";
            case "vuetify":
                return "v-";
            case "quasar":
                return "q-";
            default:
                return "";
        }
    }
    
    /**
     * 获取文档URL模板
     * 
     * @param libraryName 组件库名称
     * @return 文档URL模板
     */
    public static String getDocumentationUrlTemplate(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return "";
        }
        
        String cleanName = libraryName.trim().toLowerCase();
        
        switch (cleanName) {
            case "element-ui":
                return "https://element.eleme.cn/#/zh-CN/component/";
            case "element-plus":
                return "https://element-plus.org/zh-CN/component/";
            case "ant-design-vue":
                return "https://antdv.com/components/";
            case "vuetify":
                return "https://vuetifyjs.com/en/components/";
            case "quasar":
                return "https://quasar.dev/vue-components/";
            default:
                return "";
        }
    }
    
    /**
     * 检查组件是否属于指定组件库
     * 
     * @param componentName 组件名称
     * @param libraryName 组件库名称
     * @return 是否属于指定组件库
     */
    public static boolean isComponentFromLibrary(String componentName, String libraryName) {
        if (componentName == null || libraryName == null) {
            return false;
        }
        
        String prefix = getComponentPrefix(libraryName);
        if (prefix.isEmpty()) {
            return false;
        }
        
        return componentName.startsWith(prefix);
    }
    
    /**
     * 获取组件数据路径
     * 
     * @param libraryName 组件库名称
     * @return 组件数据路径
     */
    public static String getComponentDataPath(String libraryName) {
        if (libraryName == null || libraryName.trim().isEmpty()) {
            return "";
        }
        
        String cleanName = libraryName.trim().toLowerCase();
        
        switch (cleanName) {
            case "element-ui":
                return "/data/element-ui-components.json";
            case "element-plus":
                return "/data/element-plus-components.json";
            case "ant-design-vue":
                return "/data/ant-design-vue-components.json";
            case "vuetify":
                return "/data/vuetify-components.json";
            case "quasar":
                return "/data/quasar-components.json";
            default:
                return "";
        }
    }
}
