package org.sandcast.canopy.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.StructureGrowEvent;
import org.sandcast.canopy.CanopyPlugin;

public class GlobalEventsListener implements Listener {

    private final CanopyPlugin plugin;

    public GlobalEventsListener(final CanopyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onStructureGrow(final StructureGrowEvent event) {
        if (!event.isCancelled()) {
            plugin.growTree(event);
            event.setCancelled(true);
        }
    }

}