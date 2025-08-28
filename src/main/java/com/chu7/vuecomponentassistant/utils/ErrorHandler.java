package com.chu7.vuecomponentassistant.utils;

import com.intellij.openapi.diagnostic.Logger;

public final class ErrorHandler {
	private static final Logger LOG = Logger.getInstance(ErrorHandler.class);
	private ErrorHandler() {}
	public static void handleException(String message, Exception e, boolean rethrow) {
		if (e != null) {
			LOG.warn(message, e);
		} else {
			LOG.warn(message);
		}
		if (rethrow && e instanceof RuntimeException) {
			throw (RuntimeException) e;
		}
	}
} 