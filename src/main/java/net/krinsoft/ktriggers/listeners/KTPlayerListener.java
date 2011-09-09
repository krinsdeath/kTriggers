package net.krinsoft.ktriggers.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.krinsoft.ktriggers.TriggerPlugin;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerListener;

/**
 *
 * @author krinsdeath
 */
public class KTPlayerListener extends PlayerListener {
    private TriggerPlugin plugin;

    public KTPlayerListener(TriggerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) { return; }
        List<String> arguments = new ArrayList<String>(Arrays.asList(event.getMessage().substring(1).split(" ")));
        event.setCancelled(plugin.getCommandHandler().executeCommand(event.getPlayer(), arguments));
    }

}
