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
 * 功能说明：
 * - 直接复制 resources 目录下的标准模板文件
 * - 简单可靠，避免代码生成错误
 * - 符合VueKit远程组件库规范
 * 
 * @author VueKit Team
 * @version 2.0.0
 */
public class ComponentLibraryTemplateGenerator {
    
    private static final Logger LOG = Logger.getInstance(ComponentLibraryTemplateGenerator.class);
    
    /**
     * 生成并导出组件库模板
     */
    public static void generateAndExportTemplate(File outputFile) throws IOException {
        // 直接从 resources 目录复制模板文件
        try (InputStream inputStream = ComponentLibraryTemplateGenerator.class
                .getResourceAsStream("/data/component-library-template.json")) {
            
            if (inputStream == null) {
                throw new IOException("无法找到模板文件: /data/component-library-template.json");
            }
            
            // 复制文件内容
            try (OutputStream outputStream = Files.newOutputStream(outputFile.toPath())) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
            
            LOG.info("组件库模板已导出到: " + outputFile.getAbsolutePath());
            
        } catch (IOException e) {
            LOG.error("导出模板文件失败", e);
            throw e;
        }
    }
} 