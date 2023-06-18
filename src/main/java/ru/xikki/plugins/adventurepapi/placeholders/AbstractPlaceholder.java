package ru.xikki.plugins.adventurepapi.placeholders;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.xikki.plugins.adventurepapi.AdventurePAPI;

import java.util.regex.Pattern;

public abstract class AbstractPlaceholder {

	public static final Pattern PLACEHOLDER_ID_PATTERN = Pattern.compile("[a-z0-9_-]+");
	public static final Pattern PLACEHOLDER_FORMAT_PATTERN = Pattern.compile("\\{(" + PLACEHOLDER_ID_PATTERN.pattern() + ")(?:\\:(.+?))?\\}\\+?");

	protected final String id;
	protected final Plugin parent;

	protected AbstractPlaceholder(@NotNull String id, @Nullable Plugin parent) {
		if (!AbstractPlaceholder.PLACEHOLDER_ID_PATTERN.matcher(id).matches())
			throw new IllegalArgumentException("Incorrect placeholder id " + id);
		this.id = id;
		this.parent = parent;
	}

	@NotNull
	public final String getId() {
		return id;
	}

	@Nullable
	public Plugin getParent() {
		return parent;
	}

	public final void register() {
		AdventurePAPI.getInstance().register(this);
	}

	public final boolean isRegistered() {
		return AdventurePAPI.getInstance().isRegistered(this);
	}

	public final void unregister() {
		AdventurePAPI.getInstance().unregister(this);
	}

	@Nullable
	public static AbstractPlaceholder from(@NotNull String placeholderId) {
		return AdventurePAPI.getInstance().getPlaceholder(placeholderId);
	}

}
