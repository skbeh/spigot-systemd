package jetzt.nicht.SpigotSystemd;

import info.faljse.SDNotify.SDNotify;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

public class SpigotSystemd extends JavaPlugin {
    private BukkitTask watchdogTask;
    private boolean isNotifySent;

    @Override
    public void onEnable() {
        if (!SDNotify.isAvailable()) {
            return;
        }

        if (!isNotifySent) {
            isNotifySent = true;
            SDNotify.sendNotify();
        }

        if (SDNotify.isWatchdogEnabled()) {
            BukkitScheduler scheduler = Bukkit.getScheduler();
            watchdogTask = scheduler.runTaskTimer(this, SDNotify::sendWatchdog, 0, SDNotify.getWatchdogFrequency() / 1000 / 50 /* 50ms per tick */ / 2);
        }
    }

    @Override
    public void onDisable() {
        // Since we cannot discern between a reload and a shutdown, we
        // can't relly notify systemd of reloads or shutdowns without breaking
        // the other. We could however, prolong the watchdog interval until
        // being activated again.
        if (watchdogTask != null) {
            watchdogTask.cancel();
            watchdogTask = null;
        }
    }
}

