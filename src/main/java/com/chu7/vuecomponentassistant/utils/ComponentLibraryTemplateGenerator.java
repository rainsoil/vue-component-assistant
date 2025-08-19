package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * 组件库模板生成器
 * 
 * <p>功能说明：</p>
 * <ul>
 *   <li>直接复制 resources 目录下的标准模板文件</li>
 *   <li>简单可靠，避免代码生成错误</li>
 *   <li>符合VueKit远程组件库规范</li>
 *   <li>支持模板文件的批量导出和管理</li>
 * </ul>
 * 
 * <p>设计原则：</p>
 * <ul>
 *   <li>采用文件复制而非代码生成，确保模板的完整性和一致性</li>
 *   <li>使用缓冲流提高文件复制性能</li>
 *   <li>完善的异常处理和日志记录</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>新项目初始化时生成标准组件库配置</li>
 *   <li>现有项目迁移到VueKit时的模板提供</li>
 *   <li>开发者学习和参考标准配置格式</li>
 * </ul>
 * 
 * @author VueKit Team
 * @version 2.0.0
 * @since 1.0.0
 * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
 * @see com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager
 */
public class ComponentLibraryTemplateGenerator {
    
    /**
     * 日志记录器实例
     * 用于记录模板生成过程中的关键操作和错误信息
     */
    private static final Logger LOG = Logger.getInstance(ComponentLibraryTemplateGenerator.class);
    
    /**
     * 默认缓冲区大小（1KB）
     * 用于文件复制时的性能优化
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    
    /**
     * 模板文件在resources中的路径
     * 相对于classpath的路径，用于定位标准模板文件
     */
    private static final String TEMPLATE_RESOURCE_PATH = "/data/component-library-template.json";
    
    /**
     * 生成并导出组件库模板
     * 
     * <p>该方法会执行以下操作：</p>
     * <ol>
     *   <li>从resources目录读取标准模板文件</li>
     *   <li>将模板内容复制到指定的输出文件</li>
     *   <li>记录操作日志和错误信息</li>
     * </ol>
     * 
     * <p>注意事项：</p>
     * <ul>
     *   <li>输出文件如果已存在会被覆盖</li>
     *   <li>确保输出目录具有写入权限</li>
     *   <li>模板文件必须存在于resources目录中</li>
     * </ul>
     * 
     * @param outputFile 输出文件对象，不能为null
     *                  建议使用绝对路径，确保文件位置明确
     * @throws IOException 当以下情况发生时抛出：
     *                    <ul>
     *                      <li>模板文件不存在或无法读取</li>
     *                      <li>输出文件无法创建或写入</li>
     *                      <li>磁盘空间不足</li>
     *                      <li>权限不足</li>
     *                    </ul>
     * @throws IllegalArgumentException 当outputFile为null时抛出
     * 
     * @see #TEMPLATE_RESOURCE_PATH
     * @see #DEFAULT_BUFFER_SIZE
     */
    public static void generateAndExportTemplate(File outputFile) throws IOException {
        // 参数验证
        if (outputFile == null) {
            throw new IllegalArgumentException("输出文件不能为null");
        }
        
        // 检查输出目录是否存在，如果不存在则创建
        File outputDir = outputFile.getParentFile();
        if (outputDir != null && !outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                throw new IOException("无法创建输出目录: " + outputDir.getAbsolutePath());
            }
        }
        
        LOG.info("开始生成组件库模板，输出路径: " + outputFile.getAbsolutePath());
        
        // 直接从 resources 目录复制模板文件
        try (InputStream inputStream = ComponentLibraryTemplateGenerator.class
                .getResourceAsStream(TEMPLATE_RESOURCE_PATH)) {
            
            if (inputStream == null) {
                String errorMsg = "无法找到模板文件: " + TEMPLATE_RESOURCE_PATH;
                LOG.error(errorMsg);
                throw new IOException(errorMsg);
            }
            
            LOG.debug("成功读取模板文件，开始复制内容");
            
            // 复制文件内容，使用缓冲流提高性能
            try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int bytesRead;
                long totalBytes = 0;
                
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytes += bytesRead;
                }
                
                LOG.debug("模板文件复制完成，总字节数: " + totalBytes);
            }
            
            LOG.info("组件库模板已成功导出到: " + outputFile.getAbsolutePath());
            
        } catch (IOException e) {
            String errorMsg = "导出模板文件失败: " + e.getMessage();
            LOG.error(errorMsg, e);
            throw e;
        } catch (Exception e) {
            String errorMsg = "导出模板文件时发生未知错误: " + e.getMessage();
            LOG.error(errorMsg, e);
            throw new IOException(errorMsg, e);
        }
    }
    
    /**
     * 检查模板文件是否存在
     * 
     * <p>该方法用于验证resources目录中是否包含必要的模板文件，
     * 通常在插件启动时调用以确保功能完整性。</p>
     * 
     * @return 如果模板文件存在则返回true，否则返回false
     */
    public static boolean isTemplateAvailable() {
        try (InputStream inputStream = ComponentLibraryTemplateGenerator.class
                .getResourceAsStream(TEMPLATE_RESOURCE_PATH)) {
            return inputStream != null;
        } catch (Exception e) {
            LOG.warn("检查模板文件可用性时发生错误", e);
            return false;
        }
    }
    
    /**
     * 获取模板文件的大小（字节数）
     * 
     * <p>该方法用于获取模板文件的大小信息，可用于：</p>
     * <ul>
     *   <li>预估复制时间</li>
     *   <li>验证文件完整性</li>
     *   <li>性能监控</li>
     * </ul>
     * 
     * @return 模板文件的字节数，如果无法获取则返回-1
     */
    public static long getTemplateSize() {
        try (InputStream inputStream = ComponentLibraryTemplateGenerator.class
                .getResourceAsStream(TEMPLATE_RESOURCE_PATH)) {
            if (inputStream == null) {
                return -1;
            }
            
            return inputStream.available();
        } catch (Exception e) {
            LOG.warn("获取模板文件大小时发生错误", e);
            return -1;
        }
    }
} 