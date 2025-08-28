package com.chu7.vuecomponentassistant.remote.model;

/**
 * 组件库导入结果
 */
public class ImportResult {
	private boolean success;
	private String message;
	private ComponentLibrary library;
	private ImportType importType;
	private ComponentLibrary existingLibrary; // 当存在冲突时

	public enum ImportType {
		NEW("新建"),
		REPLACE("替换"),
		UPDATE("更新");

		private final String displayName;

		ImportType(String displayName) {
			this.displayName = displayName;
		}

		public String getDisplayName() {
			return displayName;
		}
	}

	// 构造函数
	public ImportResult() {}

	public ImportResult(boolean success, String message) {
		this.success = success;
		this.message = message;
	}

	public ImportResult(boolean success, String message, ComponentLibrary library, ImportType importType) {
		this.success = success;
		this.message = message;
		this.library = library;
		this.importType = importType;
	}

	// 静态工厂方法
	public static ImportResult success(String message, ComponentLibrary library) {
		return new ImportResult(true, message, library, ImportType.NEW);
	}

	public static ImportResult success(String message, ComponentLibrary library, ImportType importType) {
		return new ImportResult(true, message, library, importType);
	}

	public static ImportResult conflict(String message, ComponentLibrary newLibrary, ComponentLibrary existingLibrary) {
		ImportResult result = new ImportResult(false, message, newLibrary, ImportType.REPLACE);
		result.existingLibrary = existingLibrary;
		return result;
	}

	public static ImportResult error(String message) {
		return new ImportResult(false, message);
	}

	// Getter和Setter方法
	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ComponentLibrary getLibrary() {
		return library;
	}

	public void setLibrary(ComponentLibrary library) {
		this.library = library;
	}

	public ImportType getImportType() {
		return importType;
	}

	public void setImportType(ImportType importType) {
		this.importType = importType;
	}

	public ComponentLibrary getExistingLibrary() {
		return existingLibrary;
	}

	public void setExistingLibrary(ComponentLibrary existingLibrary) {
		this.existingLibrary = existingLibrary;
	}

	public boolean isConflict() {
		return !success && importType == ImportType.REPLACE;
	}

	@Override
	public String toString() {
		return "ImportResult{" +
				"success=" + success +
				", message='" + message + '\'' +
				", importType=" + importType +
				'}';
	}
} 
 