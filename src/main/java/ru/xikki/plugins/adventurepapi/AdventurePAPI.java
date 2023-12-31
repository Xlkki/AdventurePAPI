package ru.xikki.plugins.adventurepapi;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.xikki.plugins.adventurepapi.listeners.PluginListener;
import ru.xikki.plugins.adventurepapi.placeholders.AbstractPlaceholder;
import ru.xikki.plugins.adventurepapi.placeholders.BiTargetedPlaceholder;
import ru.xikki.plugins.adventurepapi.placeholders.NonTargetedPlaceholder;
import ru.xikki.plugins.adventurepapi.placeholders.TargetedPlaceholder;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public final class AdventurePAPI extends JavaPlugin {

	private static AdventurePAPI instance;

	private final Map<String, AbstractPlaceholder> placeholders = new HashMap<>();
	private final Map<String, AbstractPlaceholder> unmodifiablePlaceholders = Collections.unmodifiableMap(this.placeholders);

	@Override
	public void onLoad() {
		instance = this;
	}

	@Override
	public void onEnable() {
		Bukkit.getPluginManager().registerEvents(new PluginListener(), this);
	}

	@Override
	public void onDisable() {
		this.placeholders.clear();
	}

	@NotNull
	public Map<String, AbstractPlaceholder> getPlaceholders() {
		return unmodifiablePlaceholders;
	}

	@Nullable
	public AbstractPlaceholder getPlaceholder(@NotNull String placeholderId) {
		return this.placeholders.get(placeholderId);
	}

	public boolean isRegistered(@NotNull AbstractPlaceholder placeholder) {
		return this.placeholders.get(placeholder.getId()) == placeholder;
	}

	public void register(@NotNull AbstractPlaceholder placeholder) {
		this.placeholders.put(placeholder.getId(), placeholder);
	}

	public void unregister(@NotNull AbstractPlaceholder placeholder) {
		if (!this.isRegistered(placeholder))
			return;
		this.placeholders.remove(placeholder.getId());
	}

	public void unregister(@NotNull Plugin parent) {
		this.placeholders.entrySet().removeIf((entry) -> entry.getValue().getParent() == parent);
	}

	@NotNull
	public static AdventurePAPI getInstance() {
		return instance;
	}

	@NotNull
	public static String applyPlaceholders(@NotNull String raw, @Nullable Object firstTarget, @Nullable Object secondTarget) {
		return LegacyComponentSerializer.legacySection().serialize(
				AdventurePAPI.applyPlaceholders(
						LegacyComponentSerializer.legacySection().deserialize(raw),
						firstTarget,
						secondTarget
				)
		);
	}

	@NotNull
	public static String applyPlaceholders(@NotNull String raw, @Nullable Object target) {
		return AdventurePAPI.applyPlaceholders(raw, target, null);
	}

	@NotNull
	public static String applyPlaceholders(@NotNull String raw) {
		return AdventurePAPI.applyPlaceholders(raw, null, null);
	}

	@NotNull
	public static String stripPlaceholders(@NotNull String raw) {
		return LegacyComponentSerializer.legacySection().serialize(
				AdventurePAPI.stripPlaceholders(
						LegacyComponentSerializer.legacySection().deserialize(raw)
				)
		);
	}

	@NotNull
	public static Component applyPlaceholders(@NotNull ComponentLike raw, @Nullable Object firstTarget, @Nullable Object secondTarget) {
		Component rawComponent = raw.asComponent();
		Component result = Component.empty().style(rawComponent.style());
		if (rawComponent instanceof TextComponent textComponent) {
			Component parent = result;
			String content = textComponent.content();

			int findPosition = 0;
			Matcher matcher = AbstractPlaceholder.PLACEHOLDER_FORMAT_PATTERN.matcher(content);
			while (matcher.find(findPosition)) {
				boolean applyStyle = matcher.group().endsWith("+");
				int startPosition = matcher.start();
				int endPosition = matcher.end();
				String placeholderId = matcher.group(1);
				AbstractPlaceholder placeholder = AbstractPlaceholder.from(placeholderId);
				if (placeholder == null) {
					findPosition = startPosition + 1;
					continue;
				}
				String argumentsRaw = matcher.group(2);
				String[] arguments = new String[0];
				if (argumentsRaw != null)
					arguments = argumentsRaw.split("\\|");
				Object value = null;
				if (placeholder instanceof NonTargetedPlaceholder nonTargetedPlaceholder)
					value = nonTargetedPlaceholder.apply(arguments);
				else if (placeholder instanceof TargetedPlaceholder<?>) {
					ParameterizedType type = (ParameterizedType) placeholder.getClass().getGenericSuperclass();
					Class<?> targetType = (Class<?>) type.getActualTypeArguments()[0];
					if (targetType.isInstance(firstTarget)) {
						TargetedPlaceholder<Object> targetedPlaceholder = (TargetedPlaceholder<Object>) placeholder;
						value = targetedPlaceholder.apply(firstTarget, arguments);
					} else if (targetType.isInstance(secondTarget)) {
						TargetedPlaceholder<Object> targetedPlaceholder = (TargetedPlaceholder<Object>) placeholder;
						value = targetedPlaceholder.apply(secondTarget, arguments);
					}
				} else if (placeholder instanceof BiTargetedPlaceholder<?, ?>) {
					ParameterizedType type = (ParameterizedType) placeholder.getClass().getGenericSuperclass();
					Class<?> firstTargetType = (Class<?>) type.getActualTypeArguments()[0];
					Class<?> secondTargetType = (Class<?>) type.getActualTypeArguments()[1];

					Object first = null;
					Object second = null;

					if (firstTargetType.isInstance(firstTarget) && secondTargetType.isInstance(secondTarget)) {
						first = firstTarget;
						second = secondTarget;
					} else if (firstTargetType.isInstance(secondTarget) && secondTargetType.isInstance(firstTarget)) {
						first = secondTarget;
						second = firstTarget;
					}

					if (first != null) {
						BiTargetedPlaceholder<Object, Object> targetedPlaceholder = (BiTargetedPlaceholder<Object, Object>) placeholder;
						value = targetedPlaceholder.apply(first, second, arguments);
					}
				}
				if (value == null) {
					findPosition = startPosition + 1;
					continue;
				}
				if (value instanceof ComponentLike componentLike) {
					Component componentValue = componentLike.asComponent();
					componentValue = AdventurePAPI.applyPlaceholders(componentValue, firstTarget, secondTarget);
					if (parent == result) {
						result = result.append(Component.text(content.substring(0, startPosition)));
						if (applyStyle)
							parent = componentValue;
						else {
							result = result.append(componentValue);
							parent = result;
						}
					} else {
						parent = parent.append(
								Component.text(content.substring(0, startPosition))
						);
						if (applyStyle) {
							result = result.append(parent);
							parent = componentValue;
						} else
							parent = parent.append(componentValue);
					}
					content = content.substring(endPosition);
				} else
					content = content.substring(0, startPosition) + value + content.substring(endPosition);
				matcher = AbstractPlaceholder.PLACEHOLDER_FORMAT_PATTERN.matcher(content);
				findPosition = 0;
			}
			if (parent == result)
				result = result.append(Component.text(content));
			else {
				parent = parent.append(Component.text(content));
				result = result.append(parent);
			}
			List<Component> oldChildrenComponents = result.children();
			List<Component> newChildrenComponents = new ArrayList<>();
			Component lastComponent;
			if (oldChildrenComponents.size() > 0)
				lastComponent = oldChildrenComponents.get(oldChildrenComponents.size() - 1);
			else
				lastComponent = Component.empty();
			for (Component children : rawComponent.children())
				lastComponent = lastComponent.append(AdventurePAPI.applyPlaceholders(children, firstTarget, secondTarget));
			for (int i = 0; i < oldChildrenComponents.size() - 1; i++)
				newChildrenComponents.add(oldChildrenComponents.get(i));
			newChildrenComponents.add(lastComponent);
			result = result.children(newChildrenComponents);
		} else {
			result = rawComponent.children(Collections.emptyList());
			for (Component children : rawComponent.children())
				result = result.append(AdventurePAPI.applyPlaceholders(children, firstTarget, secondTarget));
		}
		HoverEvent<?> hover = result.hoverEvent();
		if (hover != null && hover.action().equals(HoverEvent.Action.SHOW_TEXT))
			result = result.hoverEvent(HoverEvent.showText(
					AdventurePAPI.applyPlaceholders((Component) hover.value(), firstTarget, secondTarget)
			));
		return result;
	}

	@NotNull
	public static Component applyPlaceholders(@NotNull ComponentLike raw, @Nullable Object target) {
		return AdventurePAPI.applyPlaceholders(raw, target, null);
	}

	@NotNull
	public static Component applyPlaceholders(@NotNull ComponentLike raw) {
		return AdventurePAPI.applyPlaceholders(raw, null, null);
	}

	@NotNull
	public static Component stripPlaceholders(@NotNull ComponentLike raw) {
		return raw.asComponent().replaceText(TextReplacementConfig.builder()
				.match(AbstractPlaceholder.PLACEHOLDER_FORMAT_PATTERN)
				.replacement(((matchResult, builder) -> {
					String placeholderId = matchResult.group(1);
					if (AbstractPlaceholder.from(placeholderId) == null)
						return Component.text(matchResult.group());
					else
						return Component.empty();
				}))
				.build()
		);
	}

}
