package com.chu7.vuecomponentassistant.settings;

import com.chu7.vuecomponentassistant.notification.Notifications;
import com.chu7.vuecomponentassistant.provider.ProviderManager;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class VueKitSettingsConfigurable implements Configurable {
	private JPanel panel;
	private JTextField libraryIdField;
	private JTextField versionField;
	private JTextField remoteUrlField;
	private JCheckBox offlineCheck;
	private JTextArea customPathsArea;
	
	@Override
	public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
		return "VueKit";
	}
	
	@Override
	public @Nullable JComponent createComponent() {
		if (panel == null) {
			panel = new JPanel(new BorderLayout(8, 8));
			JPanel form = new JPanel();
			form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
			libraryIdField = new JTextField();
			versionField = new JTextField();
			remoteUrlField = new JTextField();
			offlineCheck = new JCheckBox("离线模式");
			customPathsArea = new JTextArea(5, 60);
			customPathsArea.setLineWrap(true);
			customPathsArea.setBorder(BorderFactory.createLineBorder(new Color(0xCCCCCC)));
			form.add(labeled("库 ID (element-plus/element-ui)", libraryIdField));
			form.add(labeled("版本 (留空使用最新/内置)", versionField));
			form.add(labeled("远程源 Base URL", remoteUrlField));
			form.add(offlineCheck);
			form.add(labeled("自定义库 JSON 路径(一行一个)", new JScrollPane(customPathsArea)));
			panel.add(form, BorderLayout.NORTH);
		}
		reset();
		return panel;
	}
	
	private JComponent labeled(String label, JComponent comp) {
		JPanel p = new JPanel(new BorderLayout(6, 6));
		p.add(new JLabel(label), BorderLayout.NORTH);
		p.add(comp, BorderLayout.CENTER);
		return p;
	}
	
	@Override
	public boolean isModified() {
		PluginSettings s = PluginSettings.getInstance();
		if (s == null) return false;
		String paths = String.join("\n", s.getCustomLibraryJsonPaths());
		return !safeEq(libraryIdField.getText(), s.getSelectedLibraryId())
			|| !safeEq(versionField.getText(), s.getSelectedVersion())
			|| !safeEq(remoteUrlField.getText(), s.getRemoteBaseUrl())
			|| offlineCheck.isSelected() != s.isOfflineMode()
			|| !safeEq(customPathsArea.getText().trim(), paths.trim());
	}
	
	@Override
	public void apply() {
		PluginSettings s = PluginSettings.getInstance();
		if (s == null) return;
		s.getState().selectedLibraryId = libraryIdField.getText().trim();
		s.getState().selectedVersion = versionField.getText().trim();
		s.getState().remoteBaseUrl = remoteUrlField.getText().trim();
		s.getState().offlineMode = offlineCheck.isSelected();
		s.getState().customLibraryJsonPaths.clear();
		for (String line : customPathsArea.getText().split("\r?\n")) {
			String v = line.trim();
			if (!v.isEmpty()) s.getState().customLibraryJsonPaths.add(v);
		}
		// 应用后热更新
		ProviderManager.notifyReloadAll();
		Notifications.info(null, "VueKit 设置已应用，组件库已刷新");
	}
	
	@Override
	public void reset() {
		PluginSettings s = PluginSettings.getInstance();
		if (s == null) return;
		libraryIdField.setText(s.getSelectedLibraryId());
		versionField.setText(s.getSelectedVersion());
		remoteUrlField.setText(s.getRemoteBaseUrl());
		offlineCheck.setSelected(s.isOfflineMode());
		customPathsArea.setText(String.join("\n", s.getCustomLibraryJsonPaths()));
	}
	
	private boolean safeEq(String a, String b) {
		return (a == null ? "" : a).equals(b == null ? "" : b);
	}
} 