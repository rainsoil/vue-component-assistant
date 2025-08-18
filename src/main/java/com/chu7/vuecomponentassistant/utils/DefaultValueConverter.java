package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;

/**
 * 默认值类型转换工具类
 * 处理组件属性默认值的类型转换，支持 String、Boolean、Number 等类型
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class DefaultValueConverter {
    
    private static final Logger LOG = VueKitLogger.getLogger(DefaultValueConverter.class);
    
    /**
     * 将任意类型的默认值转换为字符串
     * 
     * @param defaultValue 默认值
     * @return 字符串形式的默认值
     */
    public static String toString(Object defaultValue) {
        if (defaultValue == null) {
            return "";
        }
        return defaultValue.toString();
    }
    
    /**
     * 将任意类型的默认值转换为布尔值
     * 
     * @param defaultValue 默认值
     * @return 布尔值，如果无法转换则返回 null
     */
    public static Boolean toBoolean(Object defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        
        if (defaultValue instanceof Boolean) {
            return (Boolean) defaultValue;
        }
        
        if (defaultValue instanceof String) {
            String str = ((String) defaultValue).toLowerCase().trim();
            if ("true".equals(str) || "1".equals(str) || "yes".equals(str)) {
                return true;
            }
            if ("false".equals(str) || "0".equals(str) || "no".equals(str)) {
                return false;
            }
        }
        
        if (defaultValue instanceof Number) {
            Number num = (Number) defaultValue;
            if (num.intValue() == 1) {
                return true;
            }
            if (num.intValue() == 0) {
                return false;
            }
        }
        
        return null;
    }
    
    /**
     * 将任意类型的默认值转换为数字
     * 
     * @param defaultValue 默认值
     * @return 数字，如果无法转换则返回 null
     */
    public static Number toNumber(Object defaultValue) {
        if (defaultValue == null) {
            return null;
        }
        
        if (defaultValue instanceof Number) {
            return (Number) defaultValue;
        }
        
        if (defaultValue instanceof String) {
            try {
                String str = ((String) defaultValue).trim();
                if (str.contains(".")) {
                    return Double.parseDouble(str);
                } else {
                    return Long.parseLong(str);
                }
            } catch (NumberFormatException e) {
                LOG.debug("无法将字符串转换为数字: " + defaultValue, e);
                return null;
            }
        }
        
        if (defaultValue instanceof Boolean) {
            return ((Boolean) defaultValue) ? 1 : 0;
        }
        
        return null;
    }
    
    /**
     * 获取默认值的类型描述
     * 
     * @param defaultValue 默认值
     * @return 类型描述
     */
    public static String getTypeDescription(Object defaultValue) {
        if (defaultValue == null) {
            return "null";
        }
        
        if (defaultValue instanceof String) {
            return "string";
        } else if (defaultValue instanceof Boolean) {
            return "boolean";
        } else if (defaultValue instanceof Integer) {
            return "integer";
        } else if (defaultValue instanceof Long) {
            return "long";
        } else if (defaultValue instanceof Double) {
            return "double";
        } else if (defaultValue instanceof Float) {
            return "float";
        } else {
            return defaultValue.getClass().getSimpleName().toLowerCase();
        }
    }
    
    /**
     * 格式化默认值用于显示
     * 
     * @param defaultValue 默认值
     * @return 格式化后的字符串
     */
    public static String formatForDisplay(Object defaultValue) {
        if (defaultValue == null) {
            return "无";
        }
        
        if (defaultValue instanceof String) {
            String str = (String) defaultValue;
            if (str.isEmpty()) {
                return "";
            }
            return str; // 字符串类型不加引号，由调用方决定
        }
        
        if (defaultValue instanceof Boolean) {
            return defaultValue.toString();
        }
        
        if (defaultValue instanceof Number) {
            return defaultValue.toString();
        }
        
        return defaultValue.toString();
    }
    
    /**
     * 检查两个默认值是否相等（考虑类型转换）
     * 
     * @param value1 值1
     * @param value2 值2
     * @return 是否相等
     */
    public static boolean equals(Object value1, Object value2) {
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }
        
        // 直接比较
        if (value1.equals(value2)) {
            return true;
        }
        
        // 尝试类型转换后比较
        try {
            // 尝试转换为数字比较
            Number num1 = toNumber(value1);
            Number num2 = toNumber(value2);
            if (num1 != null && num2 != null) {
                return num1.doubleValue() == num2.doubleValue();
            }
            
            // 尝试转换为布尔值比较
            Boolean bool1 = toBoolean(value1);
            Boolean bool2 = toBoolean(value2);
            if (bool1 != null && bool2 != null) {
                return bool1.equals(bool2);
            }
            
        } catch (Exception e) {
            LOG.debug("比较默认值时发生异常", e);
        }
        
        return false;
    }
}
