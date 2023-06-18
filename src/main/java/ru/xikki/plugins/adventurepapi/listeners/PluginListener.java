package ru.xikki.plugins.adventurepapi.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.jetbrains.annotations.NotNull;
import ru.xikki.plugins.adventurepapi.AdventurePAPI;

public final class PluginListener implements Listener {

	@EventHandler
	public void onPluginDisable(@NotNull PluginDisableEvent event) {
		AdventurePAPI.getInstance().unregister(event.getPlugin());
	}

}
