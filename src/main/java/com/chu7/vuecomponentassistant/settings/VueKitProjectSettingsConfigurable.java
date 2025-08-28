package com.chu7.vuecomponentassistant.settings;

import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.util.ui.FormBuilder;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class VueKitProjectSettingsConfigurable implements Configurable {
	private JBCheckBox enableComponentCompletion;
	private JBCheckBox enableAttributeCompletion;
	private JBCheckBox enableEventCompletion;
	private JBCheckBox enableSlotCompletion;
	private JBCheckBox enableHoverDocumentation;
	private JBCheckBox enableRightClickDocumentation;

	private JButton componentLibraryManagementButton;
	private JButton officialLibraryMarketButton;
	private JButton customLibraryManagementButton;

	private JPanel componentLibraryConfigPanel;
	private Map<String, JBCheckBox> libraryCheckBoxes;

	private JPanel mainPanel;
	private Project currentProject;

	@Override
	public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
		return "Vue Kit";
	}

	@Override
	public @Nullable JComponent createComponent() {
		if (mainPanel == null) {
			currentProject = getCurrentProject();
			initializeComponents();

			JPanel container = new JPanel();
			container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
			container.add(createLibraryManagementCard());
			container.add(Box.createVerticalStrut(15));
			container.add(createComponentLibraryConfigCard());
			container.add(Box.createVerticalStrut(15));
			container.add(createProjectSettingsCard());

			JPanel wrapper = new JPanel(new BorderLayout());
			wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
			wrapper.add(container, BorderLayout.NORTH);

			JScrollPane scroll = new JScrollPane(wrapper);
			scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

			mainPanel = new JPanel(new BorderLayout());
			mainPanel.add(scroll, BorderLayout.CENTER);
		}
		reset();
		return mainPanel;
	}

	private JPanel createProjectSettingsCard() {
		JPanel content = new JPanel(new BorderLayout());
		content.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("⚙️ 项目功能设置"),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)
		));

		JPanel leftPanel = FormBuilder.createFormBuilder()
			.addComponent(sectionLabel("🎯 补全功能设置"))
			.addComponent(enableComponentCompletion)
			.addComponent(enableAttributeCompletion)
			.addComponent(enableEventCompletion)
			.addComponent(enableSlotCompletion)
			.addComponentFillVertically(new JPanel(), 0)
			.getPanel();

		JPanel rightPanel = FormBuilder.createFormBuilder()
			.addComponent(sectionLabel("📖 文档功能设置"))
			.addComponent(enableHoverDocumentation)
			.addComponent(enableRightClickDocumentation)
			.addComponentFillVertically(new JPanel(), 0)
			.getPanel();

		JPanel grid = new JPanel(new GridLayout(1, 2, 20, 0));
		grid.add(leftPanel);
		grid.add(rightPanel);

		JBLabel noteLabel = new JBLabel("说明：勾选启用对应功能，取消勾选则禁用。每个项目独立配置。");
		noteLabel.setForeground(new Color(128, 128, 128));
		noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC, 11f));
		JPanel note = new JPanel(new BorderLayout());
		note.add(noteLabel, BorderLayout.CENTER);
		note.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

		content.add(grid, BorderLayout.CENTER);
		content.add(note, BorderLayout.SOUTH);
		return content;
	}

	private JPanel createLibraryManagementCard() {
		JPanel card = new JPanel(new BorderLayout());
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("📚 组件库管理"),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)
		));

		JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
		buttonsPanel.add(componentLibraryManagementButton);
		buttonsPanel.add(officialLibraryMarketButton);
		buttonsPanel.add(customLibraryManagementButton);

		JBLabel noteLabel = new JBLabel("说明：点击按钮打开对应的管理界面，进行组件库的下载、配置、删除等操作");
		noteLabel.setForeground(new Color(128, 128, 128));
		noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC, 11f));

		JPanel main = new JPanel(new BorderLayout());
		main.add(buttonsPanel, BorderLayout.CENTER);
		main.add(noteLabel, BorderLayout.SOUTH);

		card.add(main, BorderLayout.CENTER);
		return card;
	}

	private JPanel createComponentLibraryConfigCard() {
		JPanel card = new JPanel(new BorderLayout());
		card.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createTitledBorder("🗂️ 项目组件库配置"),
			BorderFactory.createEmptyBorder(10, 10, 10, 10)
		));

		componentLibraryConfigPanel = new JPanel();
		componentLibraryConfigPanel.setLayout(new BoxLayout(componentLibraryConfigPanel, BoxLayout.Y_AXIS));

		JBLabel noteLabel = new JBLabel("说明：勾选启用该组件库的补全和文档功能，取消勾选则禁用");
		noteLabel.setForeground(new Color(128, 128, 128));
		noteLabel.setFont(noteLabel.getFont().deriveFont(Font.ITALIC, 11f));

		JPanel main = new JPanel(new BorderLayout());
		main.add(componentLibraryConfigPanel, BorderLayout.CENTER);
		main.add(noteLabel, BorderLayout.SOUTH);

		card.add(main, BorderLayout.CENTER);
		loadComponentLibraryConfig();
		return card;
	}

	private void initializeComponents() {
		enableComponentCompletion = new JBCheckBox("启用组件补全", true);
		enableAttributeCompletion = new JBCheckBox("启用属性补全", true);
		enableEventCompletion = new JBCheckBox("启用事件补全", true);
		enableSlotCompletion = new JBCheckBox("启用插槽补全", true);
		enableHoverDocumentation = new JBCheckBox("启用悬停文档", true);
		enableRightClickDocumentation = new JBCheckBox("启用右键文档", true);

		componentLibraryManagementButton = createStyledButton("管理组件库", "管理VueKit组件库");
		officialLibraryMarketButton = createStyledButton("官方组件库", "浏览和下载官方组件库");
		customLibraryManagementButton = createStyledButton("自定义导入组件库", "管理自定义组件库");

		componentLibraryManagementButton.addActionListener(e -> openComponentLibraryManagement());
		officialLibraryMarketButton.addActionListener(e -> openOfficialLibraryMarket());
		customLibraryManagementButton.addActionListener(e -> openCustomLibraryManagement());
	}

	private JButton createStyledButton(String text, String tooltip) {
		JButton button = new JButton(text);
		button.setToolTipText(tooltip);
		button.setFont(button.getFont().deriveFont(Font.PLAIN, 12f));
		button.setPreferredSize(new Dimension(120, 30));
		button.setBackground(new Color(240, 248, 255));
		button.setBorder(BorderFactory.createCompoundBorder(
			BorderFactory.createLineBorder(new Color(100, 149, 237), 1),
			BorderFactory.createEmptyBorder(5, 10, 5, 10)
		));
		button.setFocusPainted(false);
		return button;
	}

	private JBLabel sectionLabel(String text) {
		JBLabel label = new JBLabel(text);
		label.setFont(JBUI.Fonts.label(12));
		return label;
	}

	private void loadComponentLibraryConfig() {
		componentLibraryConfigPanel.removeAll();
		libraryCheckBoxes = new HashMap<>();
		if (currentProject == null) {
			componentLibraryConfigPanel.add(new JBLabel("未找到项目"));
			return;
		}
		try {
			com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager =
				new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
			java.util.List<com.chu7.vuecomponentassistant.remote.model.ComponentLibrary> installedLibraries =
				libraryManager.getAllLibraries();
			if (installedLibraries == null || installedLibraries.isEmpty()) {
				componentLibraryConfigPanel.add(new JBLabel("暂无已安装的组件库"));
				return;
			}
			com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager configManager =
				com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager.getInstance(currentProject);
			java.util.Set<String> enabledLibraries = configManager.getEnabledLibraryNames(currentProject);
			for (com.chu7.vuecomponentassistant.remote.model.ComponentLibrary library : installedLibraries) {
				String displayText = library.getName();
				if (library.getVersion() != null && !library.getVersion().trim().isEmpty()) {
					displayText += " (" + library.getVersion() + ")";
				}
				JBCheckBox checkBox = new JBCheckBox(displayText);
				checkBox.setFont(checkBox.getFont().deriveFont(Font.PLAIN, 12f));
				checkBox.setToolTipText("版本: " + library.getVersion() +
					(library.getDescription() != null ? "\n描述: " + library.getDescription() : ""));
				boolean isEnabled = enabledLibraries.contains(library.getName());
				checkBox.setSelected(isEnabled);
				libraryCheckBoxes.put(library.getName(), checkBox);
				componentLibraryConfigPanel.add(checkBox);
			}
		} catch (Exception e) {
			componentLibraryConfigPanel.add(new JBLabel("加载组件库配置失败: " + e.getMessage()));
		}
	}

	private void openComponentLibraryManagement() {
		new com.chu7.vuecomponentassistant.ui.ComponentLibraryManagementDialog(currentProject).show();
		// 刷新面板以同步已安装组件库的变化
		if (componentLibraryConfigPanel != null) {
			componentLibraryConfigPanel.removeAll();
			loadComponentLibraryConfig();
			componentLibraryConfigPanel.revalidate();
			componentLibraryConfigPanel.repaint();
		}
	}

	private void openOfficialLibraryMarket() {
		new com.chu7.vuecomponentassistant.ui.OfficialLibraryMarketDialog(currentProject, new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager()).show();
		// 刷新面板以同步已安装组件库的变化
		if (componentLibraryConfigPanel != null) {
			componentLibraryConfigPanel.removeAll();
			loadComponentLibraryConfig();
			componentLibraryConfigPanel.revalidate();
			componentLibraryConfigPanel.repaint();
		}
	}

	private void openCustomLibraryManagement() {
		try {
			if (currentProject != null) {
				com.chu7.vuecomponentassistant.remote.ComponentLibraryManager libraryManager =
					new com.chu7.vuecomponentassistant.remote.ComponentLibraryManager();
				com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog dialog =
					new com.chu7.vuecomponentassistant.ui.CustomLibraryUploadDialog(currentProject, libraryManager);
				dialog.show();
				// 刷新面板
				if (componentLibraryConfigPanel != null) {
					componentLibraryConfigPanel.removeAll();
					loadComponentLibraryConfig();
					componentLibraryConfigPanel.revalidate();
					componentLibraryConfigPanel.repaint();
				}
				// 通知组件提供者刷新，确保删除的库对应组件不再出现
				try { com.chu7.vuecomponentassistant.completion2.ComponentProviderManager.notifyProviderReload(currentProject); } catch (Exception ignore) {}
			} else {
				JOptionPane.showMessageDialog(mainPanel, "项目未找到，请确保当前有打开的项目", "自定义导入组件库", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(mainPanel, "打开自定义组件库管理失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public boolean isModified() {
		if (currentProject == null) return false;
		ProjectSettingsService svc = currentProject.getService(ProjectSettingsService.class);
		boolean featuresChanged = enableComponentCompletion.isSelected() != svc.isEnableComponentCompletion()
			|| enableAttributeCompletion.isSelected() != svc.isEnableAttributeCompletion()
			|| enableEventCompletion.isSelected() != svc.isEnableEventCompletion()
			|| enableSlotCompletion.isSelected() != svc.isEnableSlotCompletion()
			|| enableHoverDocumentation.isSelected() != svc.isEnableHoverDocumentation()
			|| enableRightClickDocumentation.isSelected() != svc.isEnableRightClickDocumentation();
		boolean librariesChanged = false;
		if (libraryCheckBoxes != null) {
			java.util.Set<String> currentEnabled = new java.util.HashSet<>();
			for (java.util.Map.Entry<String, JBCheckBox> e : libraryCheckBoxes.entrySet()) {
				if (e.getValue().isSelected()) currentEnabled.add(e.getKey());
			}
			com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager cfg = com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager.getInstance(currentProject);
			java.util.Set<String> saved = cfg.getEnabledLibraryNames(currentProject);
			librariesChanged = !saved.equals(currentEnabled);
		}
		return featuresChanged || librariesChanged;
	}

	@Override
	public void apply() {
		if (currentProject == null) return;
		ProjectSettingsService svc = currentProject.getService(ProjectSettingsService.class);
		svc.setEnableComponentCompletion(enableComponentCompletion.isSelected());
		svc.setEnableAttributeCompletion(enableAttributeCompletion.isSelected());
		svc.setEnableEventCompletion(enableEventCompletion.isSelected());
		svc.setEnableSlotCompletion(enableSlotCompletion.isSelected());
		svc.setEnableHoverDocumentation(enableHoverDocumentation.isSelected());
		svc.setEnableRightClickDocumentation(enableRightClickDocumentation.isSelected());
		// 保存项目组件库启用配置
		if (libraryCheckBoxes != null) {
			java.util.Set<String> enabled = new java.util.HashSet<>();
			for (java.util.Map.Entry<String, JBCheckBox> entry : libraryCheckBoxes.entrySet()) {
				if (entry.getValue().isSelected()) {
					enabled.add(entry.getKey());
				}
			}
			com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager configManager =
				com.chu7.vuecomponentassistant.settings.ComponentLibraryConfigManager.getInstance(currentProject);
			configManager.setProjectEnabledLibraryNames(currentProject, enabled);
			// 保存后立即刷新补全数据源
			try { com.chu7.vuecomponentassistant.completion2.ComponentProviderManager.notifyProviderReload(currentProject); } catch (Exception ignore) {}
			try { com.chu7.vuecomponentassistant.provider.ProviderManager.notifyReload(currentProject); } catch (Exception ignore) {}
		}
	}

	@Override
	public void reset() {
		if (currentProject == null) return;
		ProjectSettingsService svc = currentProject.getService(ProjectSettingsService.class);
		enableComponentCompletion.setSelected(svc.isEnableComponentCompletion());
		enableAttributeCompletion.setSelected(svc.isEnableAttributeCompletion());
		enableEventCompletion.setSelected(svc.isEnableEventCompletion());
		enableSlotCompletion.setSelected(svc.isEnableSlotCompletion());
		enableHoverDocumentation.setSelected(svc.isEnableHoverDocumentation());
		enableRightClickDocumentation.setSelected(svc.isEnableRightClickDocumentation());
		loadComponentLibraryConfig();
	}

	@Override
	public void disposeUIResources() {
		mainPanel = null;
	}

	private Project getCurrentProject() {
		Project[] projects = ProjectManager.getInstance().getOpenProjects();
		return projects.length > 0 ? projects[0] : null;
	}
} 