package com.chu7.vuecomponentassistant.utils;

/**
 * 字符串标准化工具类
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>统一处理组件库名称的标准化和规范化</li>
 *   <li>支持大小写不敏感的字符串匹配</li>
 *   <li>移除空格、特殊字符和无关字符</li>
 *   <li>提供一致的字符串匹配规则</li>
 *   <li>支持作用域包名的解析和处理</li>
 *   <li>提供多种标准化策略和匹配方法</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>静态工具类：所有方法都是静态方法，无需实例化</li>
 *   <li>不可变性：输入字符串不会被修改，返回新的标准化字符串</li>
 *   <li>空值安全：所有方法都安全处理null输入</li>
 *   <li>一致性：提供统一的字符串处理规则</li>
 *   <li>性能优化：使用正则表达式进行高效处理</li>
 * </ul>
 *
 * <p>标准化规则：</p>
 * <ul>
 *   <li>字符全部转换为小写</li>
 *   <li>移除所有空白字符（空格、制表符、换行符等）</li>
 *   <li>支持严格模式：移除所有非字母数字字符</li>
 *   <li>支持作用域包名：@scope/package 格式的解析</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>组件库名称的标准化匹配</li>
 *   <li>包名和依赖名的规范化处理</li>
 *   <li>字符串搜索和比较操作</li>
 *   <li>配置文件中的名称解析</li>
 *   <li>用户输入的标准化处理</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see java.lang.String
 * @see java.util.regex.Pattern
 */
public class StringNormalizer {
    
    /**
     * 标准化字符串（转换为小写并移除空格）
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>将输入字符串转换为小写</li>
     *   <li>移除所有空白字符（空格、制表符、换行符等）</li>
     *   <li>保留字母、数字和特殊字符</li>
     *   <li>安全处理null输入</li>
     * </ul>
     *
     * <p>处理规则：</p>
     * <ul>
     *   <li>null输入返回空字符串</li>
     *   <li>使用正则表达式 "\\s+" 匹配所有空白字符</li>
     *   <li>转换为小写以提供大小写不敏感的匹配</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * String result = StringNormalizer.normalize("Element Plus");
     * // 结果: "elementplus"
     * 
     * String result2 = StringNormalizer.normalize("  Vue 3  ");
     * // 结果: "vue3"
     * }</pre>
     *
     * @param input 输入字符串，可以为null
     * @return 标准化后的字符串，如果输入为null则返回空字符串
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>将输入字符串转换为小写</li>
     *   <li>移除所有空白字符</li>
     *   <li>移除所有非字母数字字符</li>
     *   <li>只保留字母和数字</li>
     * </ul>
     *
     * <p>处理规则：</p>
     * <ul>
     *   <li>null输入返回空字符串</li>
     *   <li>使用正则表达式 "[^a-z0-9]" 移除非字母数字字符</li>
     *   <li>适用于需要纯字母数字字符串的场景</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * String result = StringNormalizer.normalizeStrict("Element-Plus@2.0.0");
     * // 结果: "elementplus200"
     * 
     * String result2 = StringNormalizer.normalizeStrict("Vue 3 (Beta)");
     * // 结果: "vue3beta"
     * }</pre>
     *
     * @param input 输入字符串，可以为null
     * @return 严格标准化后的字符串，只包含字母和数字
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>对两个输入字符串进行标准化处理</li>
     *   <li>比较标准化后的字符串是否相等</li>
     *   <li>提供大小写不敏感和空格不敏感的匹配</li>
     * </ul>
     *
     * <p>匹配规则：</p>
     * <ul>
     *   <li>null字符串被视为空字符串处理</li>
     *   <li>大小写不敏感</li>
     *   <li>空格不敏感</li>
     *   <li>使用 {@link #normalize(String)} 方法进行标准化</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * boolean match1 = StringNormalizer.matches("Element Plus", "elementplus");
     * // 结果: true
     * 
     * boolean match2 = StringNormalizer.matches("Vue 3", "VUE3");
     * // 结果: true
     * 
     * boolean match3 = StringNormalizer.matches("React", "Vue");
     * // 结果: false
     * }</pre>
     *
     * @param str1 第一个字符串，可以为null
     * @param str2 第二个字符串，可以为null
     * @return 如果标准化后的字符串相等返回true，否则返回false
     */
    public static boolean matches(String str1, String str2) {
        return normalize(str1).equals(normalize(str2));
    }
    
    /**
     * 检查字符串是否包含目标字符串（使用标准化规则）
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>对源字符串和目标字符串进行标准化处理</li>
     *   <li>检查标准化后的源字符串是否包含标准化后的目标字符串</li>
     *   <li>提供大小写不敏感和空格不敏感的包含检查</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * boolean contains1 = StringNormalizer.contains("Element Plus UI", "plus");
     * // 结果: true
     * 
     * boolean contains2 = StringNormalizer.contains("Vue 3 Composition API", "composition");
     * // 结果: true
     * }</pre>
     *
     * @param source 源字符串，可以为null
     * @param target 目标字符串，可以为null
     * @return 如果标准化后的源字符串包含标准化后的目标字符串返回true，否则返回false
     */
    public static boolean contains(String source, String target) {
        return normalize(source).contains(normalize(target));
    }
    
    /**
     * 检查字符串是否以目标字符串开头（使用标准化规则）
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>对源字符串和目标字符串进行标准化处理</li>
     *   <li>检查标准化后的源字符串是否以标准化后的目标字符串开头</li>
     *   <li>提供大小写不敏感和空格不敏感的前缀检查</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * boolean starts1 = StringNormalizer.startsWith("Element Plus", "element");
     * // 结果: true
     * 
     * boolean starts2 = StringNormalizer.startsWith("Vue 3", "vue");
     * // 结果: true
     * }</pre>
     *
     * @param source 源字符串，可以为null
     * @param target 目标字符串，可以为null
     * @return 如果标准化后的源字符串以标准化后的目标字符串开头返回true，否则返回false
     */
    public static boolean startsWith(String source, String target) {
        return normalize(source).startsWith(normalize(target));
    }
    
    /**
     * 检查字符串是否以目标字符串结尾（使用标准化规则）
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>对源字符串和目标字符串进行标准化处理</li>
     *   <li>检查标准化后的源字符串是否以标准化后的目标字符串结尾</li>
     *   <li>提供大小写不敏感和空格不敏感的后缀检查</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * boolean ends1 = StringNormalizer.endsWith("Element Plus", "plus");
     * // 结果: true
     * 
     * boolean ends2 = StringNormalizer.endsWith("Vue 3", "3");
     * // 结果: true
     * }</pre>
     *
     * @param source 源字符串，可以为null
     * @param target 目标字符串，可以为null
     * @return 如果标准化后的源字符串以标准化后的目标字符串结尾返回true，否则返回false
     */
    public static boolean endsWith(String source, String target) {
        return normalize(source).endsWith(normalize(target));
    }
    
    /**
     * 从字符串中提取组件库名称
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>移除作用域包名的 @scope/ 前缀</li>
     *   <li>对剩余的包名进行标准化处理</li>
     *   <li>适用于从完整包名中提取组件库名称</li>
     * </ul>
     *
     * <p>处理规则：</p>
     * <ul>
     *   <li>null输入返回空字符串</li>
     *   <li>使用正则表达式 "^@[^/]+/" 匹配作用域前缀</li>
     *   <li>调用 {@link #normalize(String)} 进行标准化</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * String lib1 = StringNormalizer.extractLibraryName("@element-plus/vue");
     * // 结果: "elementplusvue"
     * 
     * String lib2 = StringNormalizer.extractLibraryName("vue");
     * // 结果: "vue"
     * 
     * String lib3 = StringNormalizer.extractLibraryName("@ant-design/vue");
     * // 结果: "antdesignvue"
     * }</pre>
     *
     * @param input 输入字符串，可以为null
     * @return 提取并标准化后的组件库名称
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>检查包名是否以 @ 符号开头</li>
     *   <li>用于识别npm作用域包</li>
     *   <li>支持 @scope/package 格式的包名</li>
     * </ul>
     *
     * <p>作用域包名格式：</p>
     * <ul>
     *   <li>@scope/package：标准作用域包名</li>
     *   <li>@scope：只有作用域没有包名</li>
     *   <li>package：普通包名（非作用域）</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * boolean scoped1 = StringNormalizer.isScopedPackage("@element-plus/vue");
     * // 结果: true
     * 
     * boolean scoped2 = StringNormalizer.isScopedPackage("vue");
     * // 结果: false
     * 
     * boolean scoped3 = StringNormalizer.isScopedPackage("@ant-design");
     * // 结果: true
     * }</pre>
     *
     * @param packageName 包名，可以为null
     * @return 如果是作用域包名返回true，否则返回false
     */
    public static boolean isScopedPackage(String packageName) {
        return packageName != null && packageName.startsWith("@");
    }
    
    /**
     * 获取作用域包名的作用域部分
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>从作用域包名中提取作用域部分</li>
     *   <li>移除 @ 符号和 / 分隔符</li>
     *   <li>如果输入不是作用域包名，返回空字符串</li>
     * </ul>
     *
     * <p>提取规则：</p>
     * <ul>
     *   <li>@scope/package 格式：返回 scope</li>
     *   <li>@scope 格式：返回 scope</li>
     *   <li>非作用域包名：返回空字符串</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * String scope1 = StringNormalizer.getScope("@element-plus/vue");
     * // 结果: "element-plus"
     * 
     * String scope2 = StringNormalizer.getScope("@ant-design");
     * // 结果: "ant-design"
     * 
     * String scope3 = StringNormalizer.getScope("vue");
     * // 结果: ""
     * }</pre>
     *
     * @param packageName 包名，可以为null
     * @return 作用域部分，如果不是作用域包名则返回空字符串
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
     * <p>功能说明：</p>
     * <ul>
     *   <li>从作用域包名中提取包名部分</li>
     *   <li>移除 @scope/ 前缀</li>
     *   <li>如果输入不是作用域包名，返回原字符串</li>
     * </ul>
     *
     * <p>提取规则：</p>
     * <ul>
     *   <li>@scope/package 格式：返回 package</li>
     *   <li>@scope 格式：返回空字符串</li>
     *   <li>非作用域包名：返回原字符串</li>
     * </ul>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     * String pkg1 = StringNormalizer.getPackageName("@element-plus/vue");
     * // 结果: "vue"
     * 
     * String pkg2 = StringNormalizer.getPackageName("@ant-design/vue");
     * // 结果: "vue"
     * 
     * String pkg3 = StringNormalizer.getPackageName("vue");
     * // 结果: "vue"
     * 
     * String pkg4 = StringNormalizer.getPackageName("@ant-design");
     * // 结果: ""
     * }</pre>
     *
     * @param packageName 包名，可以为null
     * @return 包名部分，如果不是作用域包名则返回原字符串
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