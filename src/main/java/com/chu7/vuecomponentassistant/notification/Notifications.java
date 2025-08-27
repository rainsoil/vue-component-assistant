package com.chu7.vuecomponentassistant.notification;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Notifications {
	private static final String GROUP_ID = "VueKit";
	
	public static void info(@Nullable Project project, @NotNull String content) {
		show(project, content, NotificationType.INFORMATION);
	}
	public static void warn(@Nullable Project project, @NotNull String content) {
		show(project, content, NotificationType.WARNING);
	}
	public static void error(@Nullable Project project, @NotNull String content) {
		show(project, content, NotificationType.ERROR);
	}
	
	private static void show(@Nullable Project project, @NotNull String content, @NotNull NotificationType type) {
		Notification n = NotificationGroupManager.getInstance()
			.getNotificationGroup(GROUP_ID)
			.createNotification(content, type);
		n.notify(project);
	}
} 