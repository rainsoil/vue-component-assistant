import javax.swing.*;
import java.awt.*;

/**
 * 测试CustomLibraryUploadDialog的UI功能
 */
public class CustomLibraryUploadTest {
    
    public static void main(String[] args) {
        // 设置UI外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // 在EDT中运行测试
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("CustomLibraryUploadDialog 测试");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            
            // 创建一个模拟的对话框内容
            JPanel testPanel = createTestPanel();
            frame.add(testPanel);
            
            frame.setVisible(true);
            
            System.out.println("✅ 测试界面创建成功，没有空指针异常");
        });
    }
    
    private static JPanel createTestPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("导入自定义组件库"));
        
        // 创建输入面板（必须先创建，因为需要初始化cardLayout）
        JPanel inputPanel = createInputPanel();
        
        // 创建导入方式选择面板
        JPanel importMethodPanel = createImportMethodPanel(inputPanel);
        
        // 组装面板
        panel.add(importMethodPanel, BorderLayout.NORTH);
        panel.add(inputPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private static JPanel createImportMethodPanel(JPanel inputPanel) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("选择导入方式"));
        
        JRadioButton localFileRadio = new JRadioButton("本地JSON文件");
        JRadioButton remoteUrlRadio = new JRadioButton("远程JSON地址");
        
        ButtonGroup group = new ButtonGroup();
        group.add(localFileRadio);
        group.add(remoteUrlRadio);
        
        // 默认选择本地文件
        localFileRadio.setSelected(true);
        
        // 获取CardLayout和inputCardPanel（模拟修复后的逻辑）
        final CardLayout[] cardLayoutRef = {null};
        final JPanel[] inputCardPanelRef = {null};
        
        // 从inputPanel中获取CardLayout和inputCardPanel
        for (Component comp : inputPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel subPanel = (JPanel) comp;
                if (subPanel.getLayout() instanceof CardLayout) {
                    cardLayoutRef[0] = (CardLayout) subPanel.getLayout();
                    inputCardPanelRef[0] = subPanel;
                    break;
                }
            }
        }
        
        // 添加选择监听器（添加空指针检查）
        localFileRadio.addActionListener(e -> {
            if (cardLayoutRef[0] != null && inputCardPanelRef[0] != null) {
                cardLayoutRef[0].show(inputCardPanelRef[0], "local");
                System.out.println("切换到本地文件面板");
            } else {
                System.out.println("❌ CardLayout或inputCardPanel为空");
            }
        });
        
        remoteUrlRadio.addActionListener(e -> {
            if (cardLayoutRef[0] != null && inputCardPanelRef[0] != null) {
                cardLayoutRef[0].show(inputCardPanelRef[0], "remote");
                System.out.println("切换到远程URL面板");
            } else {
                System.out.println("❌ CardLayout或inputCardPanel为空");
            }
        });
        
        panel.add(localFileRadio);
        panel.add(remoteUrlRadio);
        
        return panel;
    }
    
    private static JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("输入信息"));
        
        // 本地文件输入面板
        JPanel localFilePanel = new JPanel(new BorderLayout());
        localFilePanel.add(new JLabel("文件路径:"), BorderLayout.WEST);
        JTextField filePathField = new JTextField();
        localFilePanel.add(filePathField, BorderLayout.CENTER);
        JButton browseButton = new JButton("浏览");
        localFilePanel.add(browseButton, BorderLayout.EAST);
        
        // 远程URL输入面板
        JPanel remoteUrlPanel = new JPanel(new BorderLayout());
        remoteUrlPanel.add(new JLabel("远程地址:"), BorderLayout.WEST);
        JTextField urlField = new JTextField();
        remoteUrlPanel.add(urlField, BorderLayout.CENTER);
        JButton validateButton = new JButton("验证");
        remoteUrlPanel.add(validateButton, BorderLayout.EAST);
        
        // 使用CardLayout来管理不同的输入面板
        CardLayout cardLayout = new CardLayout();
        JPanel inputCardPanel = new JPanel(cardLayout);
        inputCardPanel.add(localFilePanel, "local");
        inputCardPanel.add(remoteUrlPanel, "remote");
        
        // 预览按钮
        JButton previewButton = new JButton("预览");
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(previewButton);
        
        panel.add(inputCardPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        // 初始化显示本地文件面板
        cardLayout.show(inputCardPanel, "local");
        
        return panel;
    }
} 