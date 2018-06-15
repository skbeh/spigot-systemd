package jetzt.nicht.minecraft.spigotSystemd;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

import jetzt.nicht.minecraft.spigotSystemd.SpigotSystemdPlugin;

public class PluginListener implements Listener {
	SpigotSystemdPlugin mainPlugin;

	public PluginListener(SpigotSystemdPlugin mainPlugin) {
		this.mainPlugin = mainPlugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPluginEnabled(PluginEnableEvent event) {
		System.out.println("Some plugin has been enabled!");
		this.mainPlugin.onPluginEnabled();
	}
}
