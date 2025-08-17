package com.chu7.vuecomponentassistant.utils;

/**
 * 字符串标准化工具类
 * 
 * 功能说明：
 * - 统一处理组件库名称的标准化
 * - 支持大小写不敏感的匹配
 * - 移除空格和特殊字符
 * - 提供一致的匹配规则
 * 
 * 设计原则：
 * - 字符全部转小写
 * - 替换掉所有空格
 * - 移除特殊字符（可选）
 * - 提供多种匹配策略
 * 
 * @author VueKit Team
 * @version 1.0.0
 */
public class StringNormalizer {
    
    /**
     * 标准化字符串（转换为小写并移除空格）
     * 
     * @param input 输入字符串
     * @return 标准化后的字符串
     */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase().replaceAll("\\s+", "");
    }
    
    /**
     * 标准化字符串（转换为小写、移除空格和特殊字符）
     * 
     * @param input 输入字符串
     * @return 标准化后的字符串
     */
    public static String normalizeStrict(String input) {
        if (input == null) {
            return "";
        }
        return input.toLowerCase()
                   .replaceAll("\\s+", "")
                   .replaceAll("[^a-z0-9]", "");
    }
    
    /**
     * 检查两个字符串是否匹配（使用标准化规则）
     * 
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 是否匹配
     */
    public static boolean matches(String str1, String str2) {
        return normalize(str1).equals(normalize(str2));
    }
    
    /**
     * 检查字符串是否包含目标字符串（使用标准化规则）
     * 
     * @param source 源字符串
     * @param target 目标字符串
     * @return 是否包含
     */
    public static boolean contains(String source, String target) {
        return normalize(source).contains(normalize(target));
    }
    
    /**
     * 检查字符串是否以目标字符串开头（使用标准化规则）
     * 
     * @param source 源字符串
     * @param target 目标字符串
     * @return 是否以目标字符串开头
     */
    public static boolean startsWith(String source, String target) {
        return normalize(source).startsWith(normalize(target));
    }
    
    /**
     * 检查字符串是否以目标字符串结尾（使用标准化规则）
     * 
     * @param source 源字符串
     * @param target 目标字符串
     * @return 是否以目标字符串结尾
     */
    public static boolean endsWith(String source, String target) {
        return normalize(source).endsWith(normalize(target));
    }
    
    /**
     * 从字符串中提取组件库名称
     * 
     * @param input 输入字符串
     * @return 提取的组件库名称
     */
    public static String extractLibraryName(String input) {
        if (input == null) {
            return "";
        }
        
        // 移除 @scope/ 前缀
        String cleaned = input.replaceAll("^@[^/]+/", "");
        
        // 标准化
        return normalize(cleaned);
    }
    
    /**
     * 检查是否为作用域包名
     * 
     * @param packageName 包名
     * @return 是否为作用域包名
     */
    public static boolean isScopedPackage(String packageName) {
        return packageName != null && packageName.startsWith("@");
    }
    
    /**
     * 获取作用域包名的作用域部分
     * 
     * @param packageName 包名
     * @return 作用域部分
     */
    public static String getScope(String packageName) {
        if (!isScopedPackage(packageName)) {
            return "";
        }
        
        int slashIndex = packageName.indexOf('/');
        if (slashIndex > 0) {
            return packageName.substring(1, slashIndex);
        }
        
        return packageName.substring(1);
    }
    
    /**
     * 获取作用域包名的包名部分
     * 
     * @param packageName 包名
     * @return 包名部分
     */
    public static String getPackageName(String packageName) {
        if (!isScopedPackage(packageName)) {
            return packageName;
        }
        
        int slashIndex = packageName.indexOf('/');
        if (slashIndex > 0) {
            return packageName.substring(slashIndex + 1);
        }
        
        return packageName;
    }
} 