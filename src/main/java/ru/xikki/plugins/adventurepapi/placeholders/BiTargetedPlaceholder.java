package ru.xikki.plugins.adventurepapi.placeholders;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BiTargetedPlaceholder<T, K> extends AbstractPlaceholder {

	protected BiTargetedPlaceholder(@NotNull String id, @Nullable Plugin parent) {
		super(id, parent);
	}

	@Nullable
	public abstract Object apply(@NotNull T firstTarget, @NotNull K secondTarget, @NotNull String[] args);

}
