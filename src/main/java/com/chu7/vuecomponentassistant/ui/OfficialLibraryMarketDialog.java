package com.chu7.vuecomponentassistant.ui;

import com.chu7.vuecomponentassistant.remote.ComponentLibraryManager;
import com.chu7.vuecomponentassistant.remote.model.ImportResult;
import com.chu7.vuecomponentassistant.remote.model.OfficialLibrary;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.components.*;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 官方组件库市场对话框 - 简化版
 *
 * <p>功能说明：</p>
 * <ul>
 *   <li>显示官方组件库列表</li>
 *   <li>支持搜索和筛选功能</li>
 *   <li>下载官方组件库到本地</li>
 *   <li>实时刷新官方组件库数据</li>
 *   <li>显示组件库详细信息和评分</li>
 *   <li>异步下载和导入组件库</li>
 * </ul>
 *
 * <p>设计特点：</p>
 * <ul>
 *   <li>左右分栏布局：左侧列表，右侧详情</li>
 *   <li>顶部搜索栏：支持关键词搜索</li>
 *   <li>实时数据获取：不使用缓存，确保数据最新</li>
 *   <li>异步下载：避免UI阻塞</li>
 *   <li>智能状态管理：下载中状态显示</li>
 *   <li>完整的错误处理和用户反馈</li>
 * </ul>
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>开发者浏览官方组件库</li>
 *   <li>搜索特定功能的组件库</li>
 *   <li>下载新的官方组件库</li>
 *   <li>查看组件库评分和下载统计</li>
 *   <li>团队项目组件库扩展</li>
 * </ul>
 *
 * @author VueKit Team
 * @version 3.0.0
 * @since 1.0.0
 * @see com.intellij.openapi.ui.DialogWrapper
 * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
 * @see com.chu7.vuecomponentassistant.remote.model.OfficialLibrary
 * @see com.chu7.vuecomponentassistant.remote.model.ImportResult
 */
public class OfficialLibraryMarketDialog extends DialogWrapper {
    
    private final Project project;
    private final ComponentLibraryManager libraryManager;
    
    private JList<OfficialLibrary> libraryList;
    private DefaultListModel<OfficialLibrary> listModel;
    private JTextField searchField;
    private JButton searchButton;
    private JButton refreshButton;
    private JButton downloadButton;
    private JTextArea detailArea;
    
    /**
     * 构造函数
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>初始化官方组件库市场对话框</li>
     *   <li>设置对话框基本属性和样式</li>
     *   <li>初始化组件库管理器</li>
     *   <li>配置对话框尺寸和可调整性</li>
     * </ul>
     *
     * <p>初始化流程：</p>
     * <ol>
     *   <li>调用父类构造函数，传入项目实例</li>
     *   <li>初始化项目实例和组件库管理器</li>
     *   <li>设置对话框标题、尺寸和可调整性</li>
     *   <li>调用init()方法完成初始化</li>
     * </ol>
     *
     * @param project 当前项目实例，用于获取项目配置和组件库信息，不能为null
     * @param libraryManager 组件库管理器实例，用于管理官方组件库，不能为null
     * @throws IllegalArgumentException 如果project或libraryManager参数为null
     * @see #init()
     * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager
     */
    public OfficialLibraryMarketDialog(Project project, ComponentLibraryManager libraryManager) {
        // 调用父类构造函数，传入项目实例
        super(project);
        
        // 参数验证
        if (project == null) {
            throw new IllegalArgumentException("项目实例不能为null");
        }
        if (libraryManager == null) {
            throw new IllegalArgumentException("组件库管理器不能为null");
        }
        
        this.project = project;
        this.libraryManager = libraryManager;
        setTitle("🌐 官方组件库市场 - VueKit");
        setSize(1000, 700);
        setResizable(true);
        init();
    }
    
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setPreferredSize(new Dimension(1000, 700));
        
        // 创建搜索面板
        JPanel searchPanel = createSearchPanel();
        
        // 创建左侧列表面板
        JPanel listPanel = createListPanel();
        
        // 创建右侧详情面板
        JPanel detailPanel = createDetailPanel();
        
        // 创建底部按钮面板
        JPanel buttonPanel = createButtonPanel();
        
        // 组装主面板
        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(listPanel, BorderLayout.WEST);
        mainPanel.add(detailPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 加载官方组件库列表
        loadOfficialLibraries();
        
        return mainPanel;
    }
    
    /**
     * 创建搜索面板
     */
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        searchField = new JTextField(20);
        searchField.setToolTipText("搜索组件库名称或描述");
        
        searchButton = new JButton("🔍 搜索");
        searchButton.addActionListener(e -> performSearch());
        
        refreshButton = new JButton("🔄 刷新");
        refreshButton.addActionListener(e -> refreshOfficialLibraries());
        
        panel.add(new JLabel("搜索: "));
        panel.add(searchField);
        panel.add(searchButton);
        panel.add(refreshButton);
        
        return panel;
    }
    
    /**
     * 创建列表面板
     */
    private JPanel createListPanel() {
        listModel = new DefaultListModel<>();
        libraryList = new JList<>(listModel);
        libraryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        libraryList.setCellRenderer(new OfficialLibraryListCellRenderer());
        
        // 添加选择监听器
        libraryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showLibraryDetails();
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(libraryList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("官方组件库"));
        scrollPane.setPreferredSize(new Dimension(400, 500));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建详情面板
     */
    private JPanel createDetailPanel() {
        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        detailArea.setLineWrap(true);
        detailArea.setWrapStyleWord(true);
        
        JScrollPane scrollPane = new JScrollPane(detailArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("组件库详情"));
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * 创建按钮面板
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        downloadButton = new JButton("📥 下载");
        downloadButton.addActionListener(e -> downloadSelectedLibrary());
        downloadButton.setEnabled(false);
        
        panel.add(downloadButton);
        
        return panel;
    }
    
    /**
     * 覆盖默认按钮，只保留下载按钮
     */
    @Override
    protected Action[] createActions() {
        // 返回空数组，不显示任何默认按钮
        return new Action[0];
    }
    
    /**
     * 加载官方组件库列表（实时刷新，不使用缓存）
     */
    private void loadOfficialLibraries() {
        try {
            // 直接调用刷新方法，不使用缓存
            List<OfficialLibrary> libraries = libraryManager.getOfficialManager().refreshOfficialLibraries().get();
            updateLibraryList(libraries);
        } catch (Exception e) {
            Messages.showErrorDialog("加载官方组件库列表失败: " + e.getMessage(), "错误");
        }
    }
    
    /**
     * 刷新官方组件库列表（强制从远程获取最新数据）
     */
    private void refreshOfficialLibraries() {
        try {
            // 显示刷新提示
            Messages.showInfoMessage("正在刷新官方组件库列表...", "刷新中");
            
            // 调用真正的刷新方法，清除缓存并获取最新数据
            List<OfficialLibrary> libraries = libraryManager.getOfficialManager().refreshOfficialLibraries().get();
            updateLibraryList(libraries);
            
            Messages.showInfoMessage("官方组件库列表刷新完成！", "刷新成功");
        } catch (Exception e) {
            Messages.showErrorDialog("刷新官方组件库列表失败: " + e.getMessage(), "错误");
        }
    }
    
    /**
     * 更新组件库列表
     */
    private void updateLibraryList(List<OfficialLibrary> libraries) {
        listModel.clear();
        if (libraries.isEmpty()) {
            detailArea.setText("暂无官方组件库\n\n可能的原因：\n1. 网络连接问题\n2. 官方组件库市场暂时不可用\n3. 配置文件格式错误\n\n请检查网络连接或稍后重试。");
            downloadButton.setEnabled(false);
        } else {
            for (OfficialLibrary library : libraries) {
                listModel.addElement(library);
            }
            detailArea.setText("请选择一个组件库查看详情");
        }
    }
    
    /**
     * 执行搜索
     */
    private void performSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadOfficialLibraries();
            return;
        }
        
        try {
            List<OfficialLibrary> libraries = libraryManager.getOfficialManager().searchOfficialLibraries(keyword).get();
            updateLibraryList(libraries);
        } catch (Exception e) {
            Messages.showErrorDialog("搜索失败: " + e.getMessage(), "错误");
        }
    }
    
    /**
     * 显示组件库详情
     */
    private void showLibraryDetails() {
        OfficialLibrary library = libraryList.getSelectedValue();
        if (library == null) {
            detailArea.setText("请选择一个组件库查看详情");
            downloadButton.setEnabled(false);
            return;
        }
        
        StringBuilder details = new StringBuilder();
        details.append("组件库详情\n");
        details.append("==========\n\n");
        details.append("名称: ").append(library.getDisplayName()).append("\n");
        details.append("版本: ").append(library.getVersion()).append("\n");
        details.append("框架: ").append(library.getFramework()).append("\n");
        details.append("分类: ").append(library.getCategory()).append("\n");
        details.append("作者: ").append(library.getAuthor()).append("\n");
        details.append("描述: ").append(library.getDescription()).append("\n");
        details.append("下载次数: ").append(library.getDownloadCount()).append("\n");
        details.append("评分: ").append(library.getRating()).append("\n");
        details.append("主页: ").append(library.getHomepage()).append("\n");
        details.append("下载地址: ").append(library.getDownloadUrl()).append("\n");
        
        detailArea.setText(details.toString());
        downloadButton.setEnabled(true);
    }
    
    /**
     * 下载选中的组件库
     *
     * <p>功能说明：</p>
     * <ul>
     *   <li>异步下载选中的官方组件库</li>
     *   <li>自动导入到组件库管理器</li>
     *   <li>提供下载状态反馈</li>
     *   <li>处理下载成功和失败情况</li>
     * </ul>
     *
     * <p>执行流程：</p>
     * <ol>
     *   <li>显示下载确认对话框</li>
     *   <li>更新UI状态为下载中</li>
     *   <li>异步执行下载操作</li>
     *   <li>下载完成后自动导入</li>
     *   <li>根据结果更新UI和显示消息</li>
     * </ol>
     *
     * <p>状态管理：</p>
     * <ul>
     *   <li>下载中：按钮显示"⏳ 下载中..."，禁用状态</li>
     *   <li>下载完成：自动关闭对话框，显示成功消息</li>
     *   <li>下载失败：恢复按钮状态，显示错误信息</li>
     *   <li>导入失败：显示导入错误，允许重试</li>
     * </ul>
     *
     * @see com.chu7.vuecomponentassistant.remote.ComponentLibraryManager#importLibrary(Object, String)
     * @see com.chu7.vuecomponentassistant.remote.model.ImportResult
     */
    private void downloadSelectedLibrary() {
        OfficialLibrary library = libraryList.getSelectedValue();
        if (library == null) {
            return;
        }
        
        int result = Messages.showYesNoDialog(
            "确定要下载组件库 '" + library.getDisplayName() + "' 吗？",
            "下载确认",
            Messages.getQuestionIcon()
        );
        
        if (result == Messages.YES) {
            try {
                // 更新下载按钮状态，显示下载中
                downloadButton.setText("⏳ 下载中...");
                downloadButton.setEnabled(false);
                
                // 更新对话框标题，显示下载状态
                setTitle("🌐 官方组件库市场 - 下载中...");
                
                // 在详情区域显示下载状态
                detailArea.setText("正在下载组件库 '" + library.getDisplayName() + "'...\n\n请稍候，下载完成后会自动关闭窗口。");
                
                // 执行异步下载
                libraryManager.getOfficialManager().downloadOfficialLibrary(library.getId())
                    .thenAccept(downloadedLibrary -> {
                        // 导入到组件库管理器（指定为官方来源）
                        ImportResult importResult = libraryManager.importLibrary(downloadedLibrary, "OFFICIAL");
                        
                        if (importResult.isSuccess()) {
                            // 在EDT中更新UI
                            SwingUtilities.invokeLater(() -> {
                                // 在EDT线程中关闭对话框
                                close(OK_EXIT_CODE);
                                // 在EDT线程中显示成功消息
                                SwingUtilities.invokeLater(() -> {
                                    Messages.showInfoMessage(
                                        "组件库 '" + library.getDisplayName() + "' 下载成功！\n" +
                                        "已添加到组件库列表，现在可以使用了。",
                                        "下载成功"
                                    );
                                });
                            });
                        } else {
                            // 导入失败
                            SwingUtilities.invokeLater(() -> {
                                // 恢复下载按钮状态
                                downloadButton.setText("📥 下载");
                                downloadButton.setEnabled(true);
                                
                                // 恢复对话框标题
                                setTitle("🌐 官方组件库市场 - VueKit");
                                
                                // 显示导入失败信息
                                detailArea.setText("导入失败: " + importResult.getMessage() + "\n\n请重试。");
                                
                                SwingUtilities.invokeLater(() -> {
                                    Messages.showErrorDialog(
                                        "导入失败: " + importResult.getMessage(),
                                        "导入错误"
                                    );
                                });
                            });
                        }
                    })
                    .exceptionally(throwable -> {
                        // 在EDT中更新UI
                        SwingUtilities.invokeLater(() -> {
                            // 恢复下载按钮状态
                            downloadButton.setText("📥 下载");
                            downloadButton.setEnabled(true);
                            
                            // 恢复对话框标题
                            setTitle("🌐 官方组件库市场 - VueKit");
                            
                            // 显示下载失败信息
                            detailArea.setText("下载失败: " + throwable.getMessage() + "\n\n请重试或检查网络连接。");
                            
                                                         // 显示错误对话框
                             SwingUtilities.invokeLater(() -> {
                                 Messages.showErrorDialog(
                                     "下载失败: " + throwable.getMessage(),
                                     "下载错误"
                                 );
                             });
                        });
                        return null;
                    });
                    
            } catch (Exception e) {
                // 恢复下载按钮状态
                downloadButton.setText("📥 下载");
                downloadButton.setEnabled(true);
                
                // 恢复对话框标题
                setTitle("🌐 官方组件库市场 - VueKit");
                
                // 显示错误信息
                detailArea.setText("下载失败: " + e.getMessage() + "\n\n请重试或检查网络连接。");
                
                                 SwingUtilities.invokeLater(() -> {
                     Messages.showErrorDialog("下载失败: " + e.getMessage(), "错误");
                 });
            }
        }
    }
    
    /**
     * 官方组件库列表单元格渲染器
     */
    private static class OfficialLibraryListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, 
                                                     boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof OfficialLibrary) {
                OfficialLibrary library = (OfficialLibrary) value;
                String displayText = String.format("%s %s ⭐ %.1f 📥 %s", 
                    library.getDisplayName(), 
                    library.getVersion(),
                    library.getRating(),
                    formatDownloadCount(library.getDownloadCount())
                );
                setText(displayText);
            }
            
            return this;
        }
        
        private String formatDownloadCount(int count) {
            if (count >= 1000000) {
                return String.format("%.1fM", count / 1000000.0);
            } else if (count >= 1000) {
                return String.format("%.1fK", count / 1000.0);
            } else {
                return String.valueOf(count);
            }
        }
    }
} 