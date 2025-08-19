package com.chu7.vuecomponentassistant.completion2;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Element Plus 上下文分析器
 * 
 * <p>该类用于分析当前编辑位置的组件上下文，提供智能的组件识别和上下文分析功能，
 * 支持Vue模板中的组件标签、属性、事件等元素的识别。</p>
 * 
 * <p>功能特点：</p>
 * <ul>
 *   <li>组件标签识别：自动识别Vue模板中的组件标签</li>
 *   <li>上下文分析：分析当前编辑位置的上下文环境</li>
 *   <li>动态检测：支持多种Vue组件库的动态检测</li>
 *   <li>位置判断：判断是否在组件标签内、事件绑定位置等</li>
 * </ul>
 * 
 * <p>支持的组件库：</p>
 * <ul>
 *   <li>Element Plus (el-)</li>
 *   <li>Ant Design Vue (a-)</li>
 *   <li>Vuetify (v-)</li>
 *   <li>Quasar (q-)</li>
 *   <li>Naive UI (n-)</li>
 *   <li>PrimeVue (p-)</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>代码补全功能中的上下文分析</li>
 *   <li>智能提示和文档显示</li>
 *   <li>组件库项目识别</li>
 *   <li>Vue开发辅助功能</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.intellij.psi.PsiElement
 * @see com.intellij.psi.PsiFile
 */
public class ElementPlusContextAnalyzer {
    
    private static final Logger LOG = Logger.getInstance(ElementPlusContextAnalyzer.class);
    
    // 匹配Vue模板中的组件标签
    private static final Pattern COMPONENT_PATTERN = Pattern.compile("<([a-zA-Z][a-zA-Z0-9-]*)\\b");
    // 匹配Element Plus组件前缀
    private static final Pattern ELEMENT_PLUS_PATTERN = Pattern.compile("el-[a-zA-Z-]+");
    
    /**
     * 获取当前组件名称
     * 
     * <p>该方法分析当前编辑位置，向前查找最近的组件标签，
     * 并判断是否是Element Plus组件，返回组件名称。</p>
     * 
     * <p>分析流程：</p>
     * <ol>
     *   <li>获取当前文件内容</li>
     *   <li>确定当前编辑位置</li>
     *   <li>向前查找最近的组件标签</li>
     *   <li>验证是否是Element Plus组件</li>
     *   <li>返回组件名称</li>
     * </ol>
     * 
     * <p>组件识别规则：</p>
     * <ul>
     *   <li>使用正则表达式匹配组件标签</li>
     *   <li>支持字母、数字、连字符的组件名</li>
     *   <li>验证Element Plus组件前缀 (el-)</li>
     *   <li>返回最后一个匹配的组件名称</li>
     * </ul>
     * 
     * <p>错误处理：</p>
     * <ul>
     *   <li>捕获所有异常并记录警告日志</li>
     *   <li>在出错时返回null</li>
     *   <li>不会中断分析流程</li>
     * </ul>
     * 
     * @param element 当前编辑位置的PSI元素，不能为null
     * @return 当前组件名称，如果不是Element Plus组件或分析失败则返回null
     * 
     * @see #isInVueTemplate(PsiElement)
     * @see #isInsideComponentTag(PsiElement)
     */
    public String getCurrentComponent(PsiElement element) {
        if (element == null) {
            return null;
        }
        
        try {
            // 获取当前文件内容
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return null;
            }
            
            String fileText = file.getText();
            int offset = element.getTextOffset();
            
            // 向前查找最近的组件标签
            String beforeText = fileText.substring(0, offset);
            Matcher matcher = COMPONENT_PATTERN.matcher(beforeText);
            
            String lastComponent = null;
            while (matcher.find()) {
                lastComponent = matcher.group(1);
            }
            
            // 检查是否是Element Plus组件
            if (lastComponent != null && ELEMENT_PLUS_PATTERN.matcher(lastComponent).matches()) {
                return lastComponent;
            }
            
        } catch (Exception e) {
            LOG.warn("Error analyzing component context", e);
        }
        
        return null;
    }
    
    /**
     * 检查当前是否在Vue模板中
     * 
     * <p>该方法检查当前编辑位置是否在Vue模板文件中，
     * 通过文件扩展名判断文件类型。</p>
     * 
     * <p>检查逻辑：</p>
     * <ul>
     *   <li>获取当前文件对象</li>
     *   <li>提取文件名并转换为小写</li>
     *   <li>检查文件扩展名是否为Vue相关</li>
     * </ul>
     * 
     * <p>支持的文件类型：</p>
     * <ul>
     *   <li>.vue 文件：Vue单文件组件</li>
     *   <li>.html 文件：可能包含Vue模板的HTML文件</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>确定是否启用Vue相关功能</li>
     *   <li>组件补全功能的开关控制</li>
     *   <li>上下文分析的预处理</li>
     *   <li>功能适配和优化</li>
     * </ul>
     * 
     * @param element 当前编辑位置的PSI元素，不能为null
     * @return 如果当前在Vue模板中则返回true，否则返回false
     * 
     * @see #getCurrentComponent(PsiElement)
     * @see #isInsideComponentTag(PsiElement)
     */
    public boolean isInVueTemplate(PsiElement element) {
        if (element == null) {
            return false;
        }
        
        PsiFile file = element.getContainingFile();
        if (file == null) {
            return false;
        }
        
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".vue") || fileName.endsWith(".html");
    }
    
    /**
     * 检查当前是否在组件标签内
     * 
     * <p>该方法检查当前编辑位置是否在Vue组件标签的内部，
     * 通过分析标签结构判断当前位置。</p>
     * 
     * <p>检查逻辑：</p>
     * <ol>
     *   <li>获取当前文件内容</li>
     *   <li>确定当前编辑位置</li>
     *   <li>向前查找最近的开始标签</li>
     *   <li>向后查找最近的结束标签</li>
     *   <li>分析标签内容结构</li>
     * </ol>
     * 
     * <p>标签内判断规则：</p>
     * <ul>
     *   <li>当前位置在开始标签和结束标签之间</li>
     *   <li>标签内容包含空格（表示有属性）</li>
     *   <li>不包含结束标签标记 (</)</li>
     *   <li>支持自闭合标签的判断</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>属性补全功能的启用判断</li>
     *   <li>组件标签内编辑的识别</li>
     *   <li>智能提示的上下文分析</li>
     *   <li>Vue开发辅助功能</li>
     * </ul>
     * 
     * @param element 当前编辑位置的PSI元素，不能为null
     * @return 如果当前在组件标签内则返回true，否则返回false
     * 
     * @see #getCurrentComponent(PsiElement)
     * @see #isInVueTemplate(PsiElement)
     */
    public boolean isInsideComponentTag(PsiElement element) {
        if (element == null) {
            return false;
        }
        
        try {
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return false;
            }
            
            String fileText = file.getText();
            int offset = element.getTextOffset();
            
            // 检查当前位置是否在标签内
            String beforeText = fileText.substring(0, offset);
            String afterText = fileText.substring(offset);
            
            // 查找最近的开始标签和结束标签
            int lastOpenTag = beforeText.lastIndexOf('<');
            int nextCloseTag = afterText.indexOf('>');
            
            if (lastOpenTag >= 0 && nextCloseTag >= 0) {
                String tagContent = beforeText.substring(lastOpenTag) + afterText.substring(0, nextCloseTag + 1);
                return tagContent.contains(" ") && !tagContent.contains("</");
            }
            
        } catch (Exception e) {
            LOG.warn("Error checking if inside component tag", e);
        }
        
        return false;
    }
    
    /**
     * 检查当前是否在事件绑定位置
     * 
     * <p>该方法检查当前编辑位置是否在Vue事件绑定的位置，
     * 通过查找@符号判断是否在事件绑定上下文中。</p>
     * 
     * <p>检查逻辑：</p>
     * <ol>
     *   <li>获取当前文件内容</li>
     *   <li>确定当前编辑位置</li>
     *   <li>向前查找@符号</li>
     *   <li>判断是否在事件绑定位置</li>
     * </ol>
     * 
     * <p>事件绑定识别规则：</p>
     * <ul>
     *   <li>在当前位置前10个字符范围内查找@符号</li>
     *   <li>@符号表示Vue事件绑定语法</li>
     *   <li>支持@click、@change等事件绑定</li>
     *   <li>考虑光标位置的前后关系</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>事件补全功能的启用判断</li>
     *   <li>事件处理函数的智能提示</li>
     *   <li>Vue事件绑定的上下文分析</li>
     *   <li>开发辅助功能</li>
     * </ul>
     * 
     * @param element 当前编辑位置的PSI元素，不能为null
     * @return 如果当前在事件绑定位置则返回true，否则返回false
     * 
     * @see #getCurrentComponent(PsiElement)
     * @see #isInsideComponentTag(PsiElement)
     */
    public boolean isInEventBinding(PsiElement element) {
        if (element == null) {
            return false;
        }
        
        try {
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return false;
            }
            
            String fileText = file.getText();
            int offset = element.getTextOffset();
            
            // 检查当前位置前后是否有@符号
            String beforeText = fileText.substring(Math.max(0, offset - 10), offset);
            return beforeText.contains("@");
            
        } catch (Exception e) {
            LOG.warn("Error checking if in event binding", e);
        }
        
        return false;
    }
    
    /**
     * 获取当前属性名称
     * 
     * <p>该方法分析当前编辑位置，向前查找最近的属性名称，
     * 用于识别当前正在编辑的属性。</p>
     * 
     * <p>分析流程：</p>
     * <ol>
     *   <li>获取当前文件内容</li>
     *   <li>确定当前编辑位置</li>
     *   <li>向前查找最近的属性模式</li>
     *   <li>提取属性名称</li>
     *   <li>返回最后一个匹配的属性</li>
     * </ol>
     * 
     * <p>属性识别规则：</p>
     * <ul>
     *   <li>使用正则表达式匹配属性模式</li>
     *   <li>属性名以字母开头，支持字母、数字、连字符</li>
     *   <li>属性后跟等号(=)和可能的空格</li>
     *   <li>返回最后一个匹配的属性名称</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>属性值补全功能</li>
     *   <li>属性编辑的上下文识别</li>
     *   <li>智能提示和验证</li>
     *   <li>Vue开发辅助功能</li>
     * </ul>
     * 
     * @param element 当前编辑位置的PSI元素，不能为null
     * @return 当前属性名称，如果未找到或分析失败则返回null
     * 
     * @see #getCurrentComponent(PsiElement)
     * @see #isInsideComponentTag(PsiElement)
     */
    public String getCurrentAttribute(PsiElement element) {
        if (element == null) {
            return null;
        }
        
        try {
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return null;
            }
            
            String fileText = file.getText();
            int offset = element.getTextOffset();
            
            // 向前查找最近的属性
            String beforeText = fileText.substring(0, offset);
            Pattern attrPattern = Pattern.compile("\\s([a-zA-Z][a-zA-Z0-9-]*)\\s*=");
            Matcher matcher = attrPattern.matcher(beforeText);
            
            String lastAttribute = null;
            while (matcher.find()) {
                lastAttribute = matcher.group(1);
            }
            
            return lastAttribute;
            
        } catch (Exception e) {
            LOG.warn("Error getting current attribute", e);
        }
        
        return null;
    }
    
    /**
     * 检查是否是Vue组件库项目（动态检测，避免硬编码）
     * 
     * <p>该方法动态检测当前项目是否使用了Vue组件库，
     * 通过分析文件内容识别组件库的使用情况。</p>
     * 
     * <p>检测策略：</p>
     * <ol>
     *   <li>检查常见的组件前缀模式</li>
     *   <li>分析Vue相关的导入语句</li>
     *   <li>综合判断项目类型</li>
     * </ol>
     * 
     * <p>支持的组件库前缀：</p>
     * <ul>
     *   <li>el-：Element Plus / Element UI</li>
     *   <li>a-：Ant Design Vue</li>
     *   <li>v-：Vuetify</li>
     *   <li>q-：Quasar</li>
     *   <li>n-：Naive UI</li>
     *   <li>p-：PrimeVue</li>
     * </ul>
     * 
     * <p>Vue项目识别：</p>
     * <ul>
     *   <li>检查import语句的存在</li>
     *   <li>检查from或require关键字</li>
     *   <li>支持ES6和CommonJS模块语法</li>
     * </ul>
     * 
     * <p>使用场景：</p>
     * <ul>
     *   <li>自动启用Vue相关功能</li>
     *   <li>组件库功能的动态适配</li>
     *   <li>项目类型识别和配置</li>
     *   <li>开发体验优化</li>
     * </ul>
     * 
     * @param element 当前编辑位置的PSI元素，不能为null
     * @return 如果当前是Vue组件库项目则返回true，否则返回false
     * 
     * @see #getCurrentComponent(PsiElement)
     * @see #isInVueTemplate(PsiElement)
     */
    public boolean isVueComponentLibraryProject(PsiElement element) {
        if (element == null) {
            return false;
        }
        
        try {
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return false;
            }
            
            String fileText = file.getText();
            
            // 动态检测Vue组件库的使用，避免硬编码特定组件库
            // 检查常见的组件前缀模式
            String[] componentPrefixes = {"el-", "a-", "v-", "q-", "n-", "p-"};
            for (String prefix : componentPrefixes) {
                if (fileText.contains(prefix)) {
                    return true;
                }
            }
            
            // 检查是否包含Vue相关的导入或使用
            return fileText.contains("import") && 
                   (fileText.contains("from") || fileText.contains("require"));
            
        } catch (Exception e) {
            LOG.warn("Error checking if Vue component library project", e);
        }
        
        return false;
    }
}
