package ru.xikki.plugins.adventurepapi.placeholders;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TargetedPlaceholder<E> extends AbstractPlaceholder {

	protected TargetedPlaceholder(@NotNull String id, @Nullable Plugin parent) {
		super(id, parent);
	}

	@Nullable
	public abstract Object apply(@NotNull E target, @NotNull String[] args);

}
