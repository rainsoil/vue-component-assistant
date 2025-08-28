package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;

public final class VueKitLogger {
	private VueKitLogger() {}
	public static Logger getLogger(Class<?> clazz) { return Logger.getInstance(clazz); }

	public static void debug(Logger log, String msg) { if (log != null) log.debug(msg); }
	public static void debug(Logger log, String msg, Throwable t) { if (log != null) log.debug(msg, t); }

	public static void info(Logger log, String msg) { if (log != null) log.info(msg); }

	public static void warn(Logger log, String msg) { if (log != null) log.warn(msg); }
	public static void warn(Logger log, String msg, Throwable t) { if (log != null) log.warn(msg, t); }

	public static void error(Logger log, String msg) { if (log != null) log.error(msg); }
	public static void error(Logger log, String msg, Throwable t) { if (log != null) log.error(msg, t); }

	public static void logAndIgnore(Logger log, String msg, Exception e) { if (log != null) log.warn(msg, e); }
} 