package jetzt.nicht.minecraft.spigotSystemd;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import info.faljse.SDNotify.SDNotify;

import jetzt.nicht.minecraft.spigotSystemd.PluginListener;
import jetzt.nicht.minecraft.spigotSystemd.StartupListener;

public class SpigotSystemdPlugin extends JavaPlugin {
	PluginListener pluginListener;
	StartupListener startupListener;

	@Override
	public void onEnable() {
		// we have been loaded, so the server has just been started
		// XXX: Check some other state, to become reload-safe!
		// Check systemd if our service is started!

		// Register our listener that waits for the server to be ready.
		// We pass in a reference to ourselves so the listener can call back.
		this.startupListener = new StartupListener(this);
		System.out.println("Registering StartupListener...");
		getServer().getPluginManager()
			.registerEvents(this.startupListener, this);
	}

	@Override
	public void onDisable() {
	}

	public void onServerMostlyReady() {
		System.out.println("The server is now \"mostly ready\"!");

		// Unregister the StartupListener, as the server is now
		// deemed "mostly ready"
		System.out.println("Unregistering StartupListener...");
		HandlerList.unregisterAll(this.startupListener);

		// Register our listener that waits for plugins to be enabled. See above.
		this.pluginListener = new PluginListener(this);
		System.out.println("Registering PluginListener...");
		getServer().getPluginManager()
			.registerEvents(this.pluginListener, this);

		// Trigger one execution of onPluginEnabled, as all plugins might have
		// been enabled already. This is a bit ugly but technically correct,
		// because at least we ourselves have been enabled.
		this.onPluginEnabled();
	}

	public void onPluginEnabled() {
		if (this.checkPluginsEnabled()) {
			System.out.println("All plugins have been enabled!");
			System.out.println("Unregistering PluginListener...");
			HandlerList.unregisterAll(this.pluginListener);

			System.out.println("Signalling readyness to systemd...");
			SDNotify.sendNotify();
		} else {
			System.out.println("There are still plugins to be enabled, continuing...");
		}
	}

	public boolean checkPluginsEnabled() {
		System.out.println("Checking whether all plugins have been enabled...");
		Plugin[] loadedPlugins = getServer().getPluginManager().getPlugins();
		boolean allPluginsEnabled = true;
		for (Plugin plugin : loadedPlugins) {
			System.out.println(
					String.format("%s: %b", plugin, plugin.isEnabled())
					);
			if (plugin.isEnabled() == false) {
				allPluginsEnabled = false;
				break;
			}
		}
		return allPluginsEnabled;
	}
}
