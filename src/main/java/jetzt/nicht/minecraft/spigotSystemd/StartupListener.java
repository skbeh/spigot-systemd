package jetzt.nicht.minecraft.spigotSystemd;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldInitEvent;

import jetzt.nicht.minecraft.spigotSystemd.SpigotSystemdPlugin;

public class StartupListener implements Listener {
	SpigotSystemdPlugin mainPlugin;

	public StartupListener(SpigotSystemdPlugin mainPlugin) {
		this.mainPlugin = mainPlugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldInitialized(WorldInitEvent event) {
		this.mainPlugin.onWorldInitialized();
	}
}
