package jetzt.nicht.SpigotSystemd;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import info.faljse.SDNotify.SDNotify;

import java.util.concurrent.TimeUnit;


@Plugin(id = "velocitysystemd", name = "Velocity Systemd", version = "0.1.0-SNAPSHOT",
        authors = {"Aviana Cruz"})
public class VelocitySystemd {
    private final ProxyServer proxyServer;

    private ScheduledTask watchdogTask;

    @Inject
    public VelocitySystemd(ProxyServer server) {
        this.proxyServer = server;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        if (!SDNotify.isAvailable()) {
            return;
        }

        SDNotify.sendNotify();

        if (SDNotify.isWatchdogEnabled()) {
            watchdogTask = proxyServer.getScheduler().buildTask(this, SDNotify::sendWatchdog).repeat(SDNotify.getWatchdogFrequency() / 2, TimeUnit.MICROSECONDS).schedule();
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (watchdogTask != null) {
            watchdogTask.cancel();
        }
    }
}
