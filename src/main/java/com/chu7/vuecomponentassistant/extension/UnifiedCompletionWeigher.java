package com.chu7.vuecomponentassistant.extension;

import com.intellij.codeInsight.completion.CompletionLocation;
import com.intellij.codeInsight.completion.CompletionWeigher;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UnifiedCompletionWeigher extends CompletionWeigher {
	public static final Key<Boolean> OURS_KEY = Key.create("vuekit.ours");
	
	@Override
	public @Nullable Comparable weigh(@NotNull LookupElement element, @NotNull CompletionLocation location) {
		Boolean ours = element.getUserData(OURS_KEY);
		// Smaller value means higher priority in default sorter
		return Boolean.TRUE.equals(ours) ? -1000 : 0;
	}
} 