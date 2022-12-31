package jetzt.nicht.minecraft.spigotSystemd;

import info.faljse.SDNotify.SDNotify;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Timer;
import java.util.TimerTask;

public class SpigotSystemdPlugin extends JavaPlugin {
    private Timer watchdogTimer;

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
            watchdogTimer = new Timer(true);
            watchdogTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    SDNotify.sendWatchdog();
                }
            }, 0, SDNotify.getWatchdogFrequency() / 1000 / 2);
        }
    }

    @Override
    public void onDisable() {
        // Since we cannot discern between a reload and a shutdown, we
        // can't relly notify systemd of reloads or shutdowns without breaking
        // the other. We could however, prolong the watchdog interval until
        // being activated again.
        if (watchdogTimer != null) {
            watchdogTimer.cancel();
            watchdogTimer = null;
        }
    }
}

