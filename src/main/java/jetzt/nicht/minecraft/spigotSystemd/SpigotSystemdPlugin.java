package jetzt.nicht.minecraft.spigotSystemd;

import java.util.List;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import info.faljse.SDNotify.SDNotify;

import jetzt.nicht.minecraft.spigotSystemd.PluginListener;
import jetzt.nicht.minecraft.spigotSystemd.StartupListener;

public class SpigotSystemdPlugin extends JavaPlugin {
	PluginListener pluginListener;
	StartupListener startupListener;

	@Override
	public void onEnable() {
		// When the plugin is being enabled, the server might just be starting,
		// or the plugin is being reloaded, or the plugin is loaded late. We
		// only want to start the whole readiness-listener-thing if the server
		// is not already running, so we want to check that.
		// However, spigot or bukkit do not seem to provide any kind of
		// interface for that. So the best thing we can do is to check if any
		// world is loaded already, and just pretend that we are already
		// started if they are. At the very least this prevents us from
		// registering a StartupListener if ALL the worlds have been loaded.
		if (someWorldIsLoaded()) {
			System.out.println("The server has already loaded a world! This " +
					"either means that the server/plugin has been reloaded, " +
					"or the plugin has somehow been loaded late " +
					"(please don't do that).");
			SDNotify.sendStatus("Spigot-systemd has been reloaded / loaded late.");
		} else {
			// Register our listener that waits for the server to be "ready".
			// We pass in a reference to ourselves so the listener can call back.
			this.startupListener = new StartupListener(this);
			System.out.println("Registering StartupListener...");
			getServer().getPluginManager()
				.registerEvents(this.startupListener, this);
			SDNotify.sendStatus("Starting...");
		}
	}

	@Override
	public void onDisable() {
		// Unregistering all our handlers, just to be safe.
		HandlerList.unregisterAll(this);

		// XXX: Since we cannot discern between a reload and a shutdown, we
		// can't relly notify systemd of reloads or shutdowns without breaking
		// the other. We could however, prolong the watchdog interval until
		// being activated again, if we implement watchdog functionality.
	}

	void onServerMostlyReady() {
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

	void onPluginEnabled() {
		if (allPluginsEnabled()) {
			System.out.println("All plugins have been enabled!");
			System.out.println("Unregistering PluginListener...");
			HandlerList.unregisterAll(this.pluginListener);

			System.out.println("Signalling readiness to systemd...");
			SDNotify.sendNotify();
			SDNotify.sendStatus("Ready to accept connections!");
		} else {
			System.out.println("There are still plugins to be enabled, continuing...");
		}
	}

	private boolean allPluginsEnabled() {
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

	private boolean someWorldIsLoaded() {
		List<World> availableWorlds = getServer().getWorlds();
		if (availableWorlds.isEmpty()) {
			return false;
		} else {
			return true;
		}
	}
}
