package net.krinsoft.ktriggers.listeners;

import net.krinsoft.ktriggers.TriggerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
@SuppressWarnings("unused")
public class KTPlayerListener implements Listener {
    private TriggerPlugin plugin;

    public KTPlayerListener(TriggerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    void playerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) { return; }
        List<String> arguments = new ArrayList<String>(Arrays.asList(event.getMessage().substring(1).split(" ")));
        event.setCancelled(plugin.getCommandHandler().executeCommand(event.getPlayer(), arguments));
    }

}
