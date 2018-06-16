package jetzt.nicht.minecraft.spigotSystemd;

import java.util.List;
import java.util.logging.Logger;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import info.faljse.SDNotify.SDNotify;

import jetzt.nicht.minecraft.spigotSystemd.PluginListener;
import jetzt.nicht.minecraft.spigotSystemd.StartupListener;

public class SpigotSystemdPlugin extends JavaPlugin {
	private PluginListener pluginListener;
	private StartupListener startupListener;
	private Logger log;

	@Override
	public void onEnable() {
		// First of all, get a logger.
		this.log = getLogger();

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
			log.warning("The server has already loaded a world! This " +
					"either means that the server/plugin has been reloaded, " +
					"or the plugin has somehow been loaded late " +
					"(please don't do that).");
			SDNotify.sendStatus("Spigot-systemd has been reloaded / loaded late.");
		} else {
			// Register our listener that waits for the server to be "ready".
			// We pass in a reference to ourselves so the listener can call back.
			this.startupListener = new StartupListener(this);
			log.finer("Registering StartupListener...");
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

	void onWorldInitialized() {
		log.finer("Some World has been initialized!");
		this.onServerMostlyReady();
	}

	void onServerMostlyReady() {
		log.fine("The server is now \"mostly ready\"!");

		// Unregister the StartupListener, as the server is now
		// deemed "mostly ready"
		log.finer("Unregistering StartupListener...");
		HandlerList.unregisterAll(this.startupListener);

		// Register our listener that waits for plugins to be enabled. See above.
		this.pluginListener = new PluginListener(this);
		log.finer("Registering PluginListener...");
		getServer().getPluginManager()
			.registerEvents(this.pluginListener, this);

		// Trigger one execution of onPluginEnabled, as all plugins might have
		// been enabled already. This is a bit ugly but technically correct,
		// because at least we ourselves have been enabled.
		this.onPluginEnabled();
	}

	void onPluginEnabled() {
		if (allPluginsEnabled()) {
			log.fine("All plugins have been enabled!");
			log.finer("Unregistering PluginListener...");
			HandlerList.unregisterAll(this.pluginListener);

			log.info("Signalling readiness to systemd...");
			SDNotify.sendNotify();
			SDNotify.sendStatus("Ready to accept connections!");
		} else {
			log.fine("There are still plugins to be enabled, continuing...");
		}
	}

	private boolean allPluginsEnabled() {
		log.finer("Some plugin has been enabled!");
		log.fine("Checking whether all plugins have been enabled...");
		Plugin[] loadedPlugins = getServer().getPluginManager().getPlugins();
		boolean allPluginsEnabled = true;
		for (Plugin plugin : loadedPlugins) {
			log.finest(String.format("%s: %b", plugin, plugin.isEnabled()));
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
