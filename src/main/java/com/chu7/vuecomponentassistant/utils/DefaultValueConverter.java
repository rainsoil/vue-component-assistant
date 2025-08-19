package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;

/**
 * 默认值类型转换工具类
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>处理组件属性默认值的类型转换和格式化</li>
 *   <li>支持多种数据类型的转换（String、Boolean、Number等）</li>
 *   <li>提供智能的类型识别和转换逻辑</li>
 *   <li>支持默认值的显示格式化和比较操作</li>
 *   <li>处理null值和异常情况的优雅降级</li>
 *   <li>集成到VueKit日志系统进行调试</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>静态工具类：所有方法都是静态方法，无需实例化</li>
 *   <li>类型安全：提供安全的类型转换，避免运行时异常</li>
 *   <li>智能转换：支持多种输入格式的自动识别和转换</li>
 *   <li>空值安全：所有方法都安全处理null输入</li>
 *   <li>异常处理：完善的异常捕获和日志记录</li>
 *   <li>向后兼容：支持多种数据格式的输入</li>
 * </ul>
 *
 * <p>支持的数据类型：</p>
 * <ul>
 *   <li>字符串（String）：支持各种文本格式</li>
 *   <li>布尔值（Boolean）：支持true/false、1/0、yes/no等格式</li>
 *   <li>数字（Number）：支持整数、浮点数、科学计数法等</li>
 *   <li>null值：安全处理空值情况</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件属性默认值的类型转换</li>
 *   <li>配置文件中的值类型处理</li>
 *   <li>用户输入数据的类型验证</li>
 *   <li>组件库数据的类型标准化</li>
 *   <li>UI显示中的值格式化</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see java.lang.String
 * @see java.lang.Boolean
 * @see java.lang.Number
 * @see com.chu7.vuecomponentassistant.utils.VueKitLogger
 */
public class DefaultValueConverter {
    
    /**
     * 日志记录器，用于记录类型转换过程中的调试信息和异常
     */
    private static final Logger LOG = VueKitLogger.getLogger(DefaultValueConverter.class);
    
    /**
     * 将任意类型的默认值转换为字符串
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>将任意对象转换为字符串表示</li>
     *   <li>安全处理null值，返回空字符串</li>
     *   <li>调用对象的toString()方法进行转换</li>
     *   <li>适用于需要字符串输出的场景</li>
     * </ul>
     *
     * <p>转换规则：</p>
     * <ul>
     *   <li>null输入返回空字符串</li>
     *   <li>非null对象调用toString()方法</li>
     *   <li>支持所有实现了toString()的对象</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * String result1 = DefaultValueConverter.toString("Hello");
     * // 结果: "Hello"
     * 
     * String result2 = DefaultValueConverter.toString(123);
     * // 结果: "123"
     * 
     * String result3 = DefaultValueConverter.toString(true);
     * // 结果: "true"
     * 
     * String result4 = DefaultValueConverter.toString(null);
     * // 结果: ""
     * }</pre>
     *
     * @param defaultValue 要转换的默认值，可以为null
     * @return 字符串形式的默认值，如果输入为null则返回空字符串
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>智能识别和转换各种格式的布尔值</li>
     *   <li>支持字符串、数字、布尔值等多种输入类型</li>
     *   <li>提供安全的类型转换，无法转换时返回null</li>
     *   <li>适用于配置文件和用户输入的布尔值处理</li>
     * </ul>
     *
     * <p>转换规则：</p>
     * <ul>
     *   <li>null输入返回null</li>
     *   <li>Boolean类型直接返回</li>
     *   <li>字符串类型：支持"true"/"false"、"1"/"0"、"yes"/"no"等</li>
     *   <li>数字类型：1转换为true，0转换为false</li>
     *   <li>其他类型返回null</li>
     * </ul>
     *
     * <p>支持的字符串格式：</p>
     * <ul>
     *   <li>true: "true", "1", "yes", "TRUE", "True"等</li>
     *   <li>false: "false", "0", "no", "FALSE", "False"等</li>
     *   <li>大小写不敏感，自动去除首尾空格</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * Boolean result1 = DefaultValueConverter.toBoolean("true");
     * // 结果: true
     * 
     * Boolean result2 = DefaultValueConverter.toBoolean("1");
     * // 结果: true
     * 
     * Boolean result3 = DefaultValueConverter.toBoolean("no");
     * // 结果: false
     * 
     * Boolean result4 = DefaultValueConverter.toBoolean("invalid");
     * // 结果: null
     * }</pre>
     *
     * @param defaultValue 要转换的默认值，可以为null
     * @return 转换后的布尔值，如果无法转换则返回null
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>智能识别和转换各种格式的数字值</li>
     *   <li>支持字符串、数字、布尔值等多种输入类型</li>
     *   <li>自动识别整数和浮点数格式</li>
     *   <li>提供安全的类型转换，无法转换时返回null</li>
     * </ul>
     *
     * <p>转换规则：</p>
     * <ul>
     *   <li>null输入返回null</li>
     *   <li>Number类型直接返回</li>
     *   <li>字符串类型：自动识别整数和浮点数</li>
     *   <li>布尔值：true转换为1，false转换为0</li>
     *   <li>其他类型返回null</li>
     * </ul>
     *
     * <p>数字格式识别：</p>
     * <ul>
     *   <li>包含小数点：解析为Double类型</li>
     *   <li>不包含小数点：解析为Long类型</li>
     *   <li>支持科学计数法（如1.23e-4）</li>
     *   <li>自动去除首尾空格</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * Number result1 = DefaultValueConverter.toNumber("123");
     * // 结果: 123L (Long类型)
     * 
     * Number result2 = DefaultValueConverter.toNumber("3.14");
     * // 结果: 3.14 (Double类型)
     * 
     * Number result3 = DefaultValueConverter.toNumber(true);
     * // 结果: 1
     * 
     * Number result4 = DefaultValueConverter.toNumber("invalid");
     * // 结果: null
     * }</pre>
     *
     * @param defaultValue 要转换的默认值，可以为null
     * @return 转换后的数字，如果无法转换则返回null
     * @throws NumberFormatException 当字符串无法解析为数字时抛出（内部捕获）
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>识别输入对象的实际数据类型</li>
     *   <li>返回人类可读的类型描述字符串</li>
     *   <li>支持常见的基本数据类型和自定义类型</li>
     *   <li>适用于调试和类型信息显示</li>
     * </ul>
     *
     * <p>类型识别规则：</p>
     * <ul>
     *   <li>null值：返回"null"</li>
     *   <li>基本包装类型：返回对应的类型名称</li>
     *   <li>自定义类型：返回类名的简单形式（小写）</li>
     *   <li>数组类型：返回元素类型的描述</li>
     * </ul>
     *
     * <p>支持的类型：</p>
     * <ul>
     *   <li>null → "null"</li>
     *   <li>String → "string"</li>
     *   <li>Boolean → "boolean"</li>
     *   <li>Integer → "integer"</li>
     *   <li>Long → "long"</li>
     *   <li>Double → "double"</li>
     *   <li>Float → "float"</li>
     *   <li>其他类型 → 类名的小写形式</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * String type1 = DefaultValueConverter.getTypeDescription("Hello");
     * // 结果: "string"
     * 
     * String type2 = DefaultValueConverter.getTypeDescription(123);
     * // 结果: "integer"
     * 
     * String type3 = DefaultValueConverter.getTypeDescription(true);
     * // 结果: "boolean"
     * 
     * String type4 = DefaultValueConverter.getTypeDescription(null);
     * // 结果: "null"
     * }</pre>
     *
     * @param defaultValue 要检查类型的默认值，可以为null
     * @return 类型描述字符串，null值返回"null"
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>将默认值格式化为适合UI显示的字符串</li>
     *   <li>处理不同类型的显示格式</li>
     *   <li>提供用户友好的显示效果</li>
     *   <li>适用于配置界面和文档显示</li>
     * </ul>
     *
     * <p>格式化规则：</p>
     * <ul>
     *   <li>null值：显示为"无"</li>
     *   <li>空字符串：显示为空字符串</li>
     *   <li>字符串：直接显示，不添加引号</li>
     *   <li>布尔值：显示为"true"或"false"</li>
     *   <li>数字：显示为数字字符串</li>
     *   <li>其他类型：调用toString()方法</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * String display1 = DefaultValueConverter.formatForDisplay("Hello");
     * // 结果: "Hello"
     * 
     * String display2 = DefaultValueConverter.formatForDisplay(123);
     * // 结果: "123"
     * 
     * String display3 = DefaultValueConverter.formatForDisplay(true);
     * // 结果: "true"
     * 
     * String display4 = DefaultValueConverter.formatForDisplay(null);
     * // 结果: "无"
     * 
     * String display5 = DefaultValueConverter.formatForDisplay("");
     * // 结果: ""
     * }</pre>
     *
     * @param defaultValue 要格式化的默认值，可以为null
     * @return 格式化后的显示字符串
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>智能比较两个不同类型的值是否相等</li>
     *   <li>支持直接比较和类型转换后的比较</li>
     *   <li>处理null值的特殊情况</li>
     *   <li>提供安全的比较操作，避免异常</li>
     * </ul>
     *
     * <p>比较策略：</p>
     * <ol>
     *   <li>null值比较：两个null值相等，一个null一个非null不相等</li>
     *   <li>直接比较：调用equals()方法进行直接比较</li>
     *   <li>数字比较：尝试转换为数字后比较数值</li>
     *   <li>布尔比较：尝试转换为布尔值后比较</li>
     *   <li>其他情况：返回false</li>
     * </ol>
     *
     * <p>类型转换比较：</p>
     * <ul>
     *   <li>数字比较：支持不同数字类型间的比较</li>
     *   <li>布尔比较：支持字符串、数字到布尔值的转换比较</li>
     *   <li>异常处理：转换失败时记录调试日志</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * boolean equal1 = DefaultValueConverter.equals("123", 123);
     * // 结果: true (数字比较)
     * 
     * boolean equal2 = DefaultValueConverter.equals("true", 1);
     * // 结果: true (布尔比较)
     * 
     * boolean equal3 = DefaultValueConverter.equals("hello", "world");
     * // 结果: false (字符串比较)
     * 
     * boolean equal4 = DefaultValueConverter.equals(null, null);
     * // 结果: true (null比较)
     * 
     * boolean equal5 = DefaultValueConverter.equals("1", "2");
     * // 结果: false (数字比较)
     * }</pre>
     *
     * @param value1 第一个要比较的值，可以为null
     * @param value2 第二个要比较的值，可以为null
     * @return 如果两个值相等返回true，否则返回false
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
