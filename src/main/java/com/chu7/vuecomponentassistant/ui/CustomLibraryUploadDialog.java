package com.chu7.vuecomponentassistant.ui;

import com.chu7.vuecomponentassistant.remote.utils.HttpClient;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ComponentLibrary;
import com.chu7.vuecomponentassistant.remote.model.ImportResult;
import com.chu7.vuecomponentassistant.completion2.ComponentProviderManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;

/**
 * 自定义组件库上传对话框 - 远程组件库版本
 * 
 * 功能说明：
 * - 支持本地JSON文件导入
 * - 支持远程JSON URL导入
 * - 预览组件库信息
 * - 验证组件库格式
 * 
 * @author VueKit Team
 * @version 3.0.0
 */
public class CustomLibraryUploadDialog extends DialogWrapper {
    
    private final Project project;
    private final ComponentLibraryManager libraryManager;
    
    private JRadioButton localFileRadio;
    private JRadioButton remoteUrlRadio;
    private JTextField filePathField;
    private JTextField urlField;
    private JButton browseButton;
    private JButton validateButton;
    private JButton previewButton;
    private JTextArea previewArea;
    private JCheckBox enableAfterImportCheckBox;
    private JPanel inputCardPanel;
    private CardLayout cardLayout;
    
    public CustomLibraryUploadDialog(Project project, ComponentLibraryManager libraryManager) {
        super(project);
        this.project = project;
        this.libraryManager = libraryManager;
        setTitle("📁 导入自定义组件库");
        setSize(800, 600);
        setResizable(true);
        init();
    }
    
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(800, 600));
        
        // 创建导入方式选择面板（必须先创建，因为需要初始化单选按钮）
        JPanel importMethodPanel = createImportMethodPanel();
        
        // 创建输入面板
        JPanel inputPanel = createInputPanel();
        
        // 创建预览面板
        JPanel previewPanel = createPreviewPanel();
        
        // 创建选项面板
        JPanel optionsPanel = createOptionsPanel();
        
        // 组装主面板
        mainPanel.add(importMethodPanel, BorderLayout.NORTH);
        mainPanel.add(inputPanel, BorderLayout.CENTER);
        mainPanel.add(previewPanel, BorderLayout.SOUTH);
        
        // 添加选项面板到底部
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(optionsPanel, BorderLayout.NORTH);
        mainPanel.add(bottomPanel, BorderLayout.EAST);
        
        return mainPanel;
    }
    
    /**
     * 创建导入方式选择面板
     */
    private JPanel createImportMethodPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("选择导入方式"));
        
        localFileRadio = new JRadioButton("本地JSON文件");
        remoteUrlRadio = new JRadioButton("远程JSON地址");
        
        ButtonGroup group = new ButtonGroup();
        group.add(localFileRadio);
        group.add(remoteUrlRadio);
        
        // 默认选择本地文件
        localFileRadio.setSelected(true);
        
        // 添加选择监听器
        localFileRadio.addActionListener(e -> updateInputPanel());
        remoteUrlRadio.addActionListener(e -> updateInputPanel());
        
        panel.add(localFileRadio);
        panel.add(remoteUrlRadio);
        
        // 初始化面板显示状态（在单选按钮初始化完成后调用）
        updateInputPanel();
        
        return panel;
    }
    
    /**
     * 创建输入面板
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("输入信息"));
        
        // 本地文件输入面板
        JPanel localFilePanel = new JPanel(new BorderLayout());
        localFilePanel.add(new JLabel("文件路径:"), BorderLayout.WEST);
        filePathField = new JTextField();
        localFilePanel.add(filePathField, BorderLayout.CENTER);
        browseButton = new JButton("浏览");
        browseButton.addActionListener(e -> browseFile());
        localFilePanel.add(browseButton, BorderLayout.EAST);
        
        // 远程URL输入面板
        JPanel remoteUrlPanel = new JPanel(new BorderLayout());
        remoteUrlPanel.add(new JLabel("远程地址:"), BorderLayout.WEST);
        urlField = new JTextField();
        remoteUrlPanel.add(urlField, BorderLayout.CENTER);
        validateButton = new JButton("验证");
        validateButton.addActionListener(e -> validateUrl());
        remoteUrlPanel.add(validateButton, BorderLayout.EAST);
        
        // 预览按钮
        previewButton = new JButton("预览");
        previewButton.addActionListener(e -> previewLibrary());
        
        // 使用CardLayout来管理不同的输入面板
        cardLayout = new CardLayout();
        inputCardPanel = new JPanel(cardLayout);
        inputCardPanel.add(localFilePanel, "local");
        inputCardPanel.add(remoteUrlPanel, "remote");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(previewButton);
        
        panel.add(inputCardPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * 创建预览面板
     */
    private JPanel createPreviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("预览信息"));
        panel.setPreferredSize(new Dimension(600, 200));
        
        previewArea = new JTextArea();
        previewArea.setEditable(false);
        previewArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(previewArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建选项面板
     */
    private JPanel createOptionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("导入选项"));
        panel.setPreferredSize(new Dimension(200, 100));
        
        enableAfterImportCheckBox = new JCheckBox("导入后立即启用", true);
        
        panel.add(enableAfterImportCheckBox, BorderLayout.NORTH);
        
        return panel;
    }
    
    /**
     * 更新输入面板显示状态
     */
    private void updateInputPanel() {
        // 添加空指针检查，确保组件已初始化
        if (localFileRadio == null || cardLayout == null || inputCardPanel == null) {
            return; // 如果组件未初始化，直接返回
        }
        
        boolean isLocalFile = localFileRadio.isSelected();
        
        // 使用CardLayout切换显示的面板
        if (isLocalFile) {
            cardLayout.show(inputCardPanel, "local");
        } else {
            cardLayout.show(inputCardPanel, "remote");
        }
        
        // 清空另一个输入框的内容
        if (isLocalFile) {
            if (urlField != null) {
                urlField.setText("");
            }
        } else {
            if (filePathField != null) {
                filePathField.setText("");
            }
        }
    }
    
    /**
     * 浏览文件
     */
    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("选择组件库JSON文件");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JSON文件", "json"));
        
        if (fileChooser.showOpenDialog(this.getContentPane()) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            filePathField.setText(file.getAbsolutePath());
        }
    }
    
    /**
     * 验证URL
     */
    private void validateUrl() {
        String url = urlField.getText().trim();
        if (url.isEmpty()) {
            Messages.showWarningDialog("请输入URL", "验证失败");
            return;
        }
        
        // 禁用验证按钮，显示验证状态
        validateButton.setEnabled(false);
        validateButton.setText("验证中...");
        
        // 在后台线程中执行验证
        new Thread(() -> {
            try {
                // 检查URL格式
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    throw new RuntimeException("URL必须以http://或https://开头");
                }
                
                // 检查URL是否可访问
                boolean accessible = HttpClient.checkUrlAccessible(url);
                
                if (accessible) {
                    // 尝试下载一小部分内容来验证是否为JSON
                    String sampleJson = HttpClient.downloadJsonSample(url);
                    if (sampleJson != null && sampleJson.trim().startsWith("{")) {
                        SwingUtilities.invokeLater(() -> {
                            Messages.showInfoMessage("URL验证成功！这是一个有效的JSON文件。", "验证成功");
                        });
                    } else {
                        SwingUtilities.invokeLater(() -> {
                            Messages.showWarningDialog("URL可访问，但可能不是有效的JSON文件。", "验证警告");
                        });
                    }
                } else {
                    throw new RuntimeException("URL不可访问");
                }
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    Messages.showErrorDialog("URL验证失败: " + e.getMessage(), "错误");
                });
            } finally {
                // 恢复按钮状态
                SwingUtilities.invokeLater(() -> {
                    validateButton.setEnabled(true);
                    validateButton.setText("验证");
                });
            }
        }).start();
    }
    
    /**
     * 预览组件库
     */
    private void previewLibrary() {
        // 禁用预览按钮，显示预览状态
        previewButton.setEnabled(false);
        previewButton.setText("预览中...");
        
        // 在后台线程中执行预览
        new Thread(() -> {
            try {
                final ComponentLibrary library;
                
                if (localFileRadio.isSelected()) {
                    library = loadLocalLibrary();
                } else {
                    library = loadRemoteLibrary();
                }
                
                if (library != null) {
                    SwingUtilities.invokeLater(() -> {
                        showLibraryPreview(library);
                        Messages.showInfoMessage("组件库预览成功！", "预览成功");
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        Messages.showWarningDialog("无法加载组件库，请检查输入信息", "预览失败");
                    });
                }
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    Messages.showErrorDialog("预览失败: " + e.getMessage(), "错误");
                });
            } finally {
                // 恢复按钮状态
                SwingUtilities.invokeLater(() -> {
                    previewButton.setEnabled(true);
                    previewButton.setText("预览");
                });
            }
        }).start();
    }
    
         /**
      * 加载本地组件库
      */
     private ComponentLibrary loadLocalLibrary() throws IOException {
         String filePath = filePathField.getText().trim();
         if (filePath.isEmpty()) {
             SwingUtilities.invokeLater(() -> {
                 Messages.showWarningDialog("请选择JSON文件", "提示");
             });
             return null;
         }
         
         File file = new File(filePath);
         if (!file.exists()) {
             SwingUtilities.invokeLater(() -> {
                 Messages.showErrorDialog("文件不存在: " + filePath, "错误");
             });
             return null;
         }
         
         String json = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
         return parseComponentLibrary(json);
     }
    
                   /**
       * 加载远程组件库
       */
      private ComponentLibrary loadRemoteLibrary() {
          String url = urlField.getText().trim();
          if (url.isEmpty()) {
              SwingUtilities.invokeLater(() -> {
                  Messages.showWarningDialog("请输入URL", "提示");
              });
              return null;
          }
          
          try {
              // 使用 HttpClient 下载 JSON
              String json = HttpClient.downloadJson(url);
              
              // 解析组件库
              ComponentLibrary library = parseComponentLibrary(json);
              
              // 验证组件库
              if (library.getName() == null || library.getName().trim().isEmpty()) {
                  throw new RuntimeException("组件库名称不能为空");
              }
              
              if (library.getComponents() == null || library.getComponents().isEmpty()) {
                  throw new RuntimeException("组件库必须包含至少一个组件");
              }
              
              return library;
              
          } catch (Exception e) {
              throw new RuntimeException("远程加载失败: " + e.getMessage());
          }
      }
    
    /**
     * 解析组件库JSON
     */
    private ComponentLibrary parseComponentLibrary(String json) {
        try {
            Gson gson = new Gson();
            ComponentLibrary library = gson.fromJson(json, ComponentLibrary.class);
            
            if (library == null) {
                throw new JsonSyntaxException("解析结果为空");
            }
            
            return library;
        } catch (JsonSyntaxException e) {
            throw new RuntimeException("JSON格式错误: " + e.getMessage());
        }
    }
    
    /**
     * 显示组件库预览
     */
    private void showLibraryPreview(ComponentLibrary library) {
        StringBuilder preview = new StringBuilder();
        preview.append("组件库预览\n");
        preview.append("==========\n\n");
        preview.append("名称: ").append(library.getDisplayName()).append("\n");
        preview.append("版本: ").append(library.getVersion()).append("\n");
        preview.append("描述: ").append(library.getDescription()).append("\n");
        
        if (library.getComponents() != null) {
            preview.append("组件数量: ").append(library.getComponents().size()).append("\n");
            preview.append("\n前5个组件:\n");
            for (int i = 0; i < Math.min(library.getComponents().size(), 5); i++) {
                preview.append(i + 1).append(". ").append(library.getComponents().get(i).getName()).append("\n");
            }
        }
        
        previewArea.setText(preview.toString());
    }
    
    @Override
    protected void doOKAction() {
        // 禁用OK按钮，显示加载状态
        getOKAction().setEnabled(false);
        setTitle("📁 导入自定义组件库 - 加载中...");
        
        // 在预览区域显示加载状态
        previewArea.setText("正在导入组件库...\n\n请稍候，导入完成后会自动关闭窗口。");
        
        // 在后台线程中执行导入操作
        new Thread(() -> {
            try {
                final ComponentLibrary library;
                
                if (localFileRadio.isSelected()) {
                    library = loadLocalLibrary();
                } else {
                    library = loadRemoteLibrary();
                }
                
                if (library == null) {
                    SwingUtilities.invokeLater(() -> {
                        getOKAction().setEnabled(true);
                        setTitle("📁 导入自定义组件库");
                        previewArea.setText("导入失败：无法加载组件库");
                    });
                    return;
                }
                
                // 设置来源信息
                if (localFileRadio.isSelected()) {
                    library.setSource("CUSTOM_LOCAL");
                    library.setSourceUrl(filePathField.getText().trim());
                } else {
                    library.setSource("CUSTOM_REMOTE");
                    library.setSourceUrl(urlField.getText().trim());
                }
                
                // 导入组件库
                final ImportResult result = libraryManager.importLibrary(library);
                
                                 SwingUtilities.invokeLater(() -> {
                     if (result.isSuccess()) {
                         // 通知所有 ComponentProvider 重新加载组件数据
                         ComponentProviderManager.notifyAllProvidersReload();
                         
                         // 在EDT线程中关闭对话框
                         close(OK_EXIT_CODE);
                         // 在EDT线程中显示成功消息
                         SwingUtilities.invokeLater(() -> {
                             Messages.showInfoMessage("组件库导入成功: " + library.getName(), "成功");
                         });
                     } else if (result.isConflict()) {
                         // 处理冲突
                         int choice = Messages.showYesNoDialog(
                             "已存在同名组件库，是否替换？\n" +
                             "现有版本: " + result.getExistingLibrary().getVersion() + "\n" +
                             "新版本: " + library.getVersion(),
                             "组件库冲突",
                             Messages.getQuestionIcon()
                         );
                         
                         if (choice == Messages.YES) {
                             // 在后台线程中执行替换操作
                             new Thread(() -> {
                                 try {
                                     ImportResult replaceResult = libraryManager.replaceLibrary(library, result.getExistingLibrary());
                                                                           SwingUtilities.invokeLater(() -> {
                                          if (replaceResult.isSuccess()) {
                                              // 通知所有 ComponentProvider 重新加载组件数据
                                              ComponentProviderManager.notifyAllProvidersReload();
                                              
                                              // 在EDT线程中关闭对话框
                                              close(OK_EXIT_CODE);
                                              // 在EDT线程中显示成功消息
                                              SwingUtilities.invokeLater(() -> {
                                                  Messages.showInfoMessage("组件库替换成功: " + library.getName(), "成功");
                                              });
                                          } else {
                                             getOKAction().setEnabled(true);
                                             setTitle("📁 导入自定义组件库");
                                             previewArea.setText("替换失败: " + replaceResult.getMessage());
                                             SwingUtilities.invokeLater(() -> {
                                                 Messages.showErrorDialog("替换失败: " + replaceResult.getMessage(), "错误");
                                             });
                                         }
                                     });
                                 } catch (Exception e) {
                                     SwingUtilities.invokeLater(() -> {
                                         getOKAction().setEnabled(true);
                                         setTitle("📁 导入自定义组件库");
                                         previewArea.setText("替换失败: " + e.getMessage());
                                         SwingUtilities.invokeLater(() -> {
                                             Messages.showErrorDialog("替换失败: " + e.getMessage(), "错误");
                                         });
                                     });
                                 }
                             }).start();
                         } else {
                             // 用户取消替换，恢复按钮状态
                             getOKAction().setEnabled(true);
                             setTitle("📁 导入自定义组件库");
                             previewArea.setText("用户取消了组件库替换操作");
                         }
                     } else {
                         getOKAction().setEnabled(true);
                         setTitle("📁 导入自定义组件库");
                         previewArea.setText("导入失败: " + result.getMessage());
                         SwingUtilities.invokeLater(() -> {
                             Messages.showErrorDialog("导入失败: " + result.getMessage(), "错误");
                         });
                     }
                 });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    getOKAction().setEnabled(true);
                    setTitle("📁 导入自定义组件库");
                    previewArea.setText("导入失败: " + e.getMessage());
                    Messages.showErrorDialog("导入失败: " + e.getMessage(), "错误");
                });
            }
        }).start();
    }
}
