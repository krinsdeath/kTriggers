package net.krinsoft.ktriggers.listeners;

import net.krinsoft.ktriggers.TriggerPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author krinsdeath
 */
@SuppressWarnings("unused")
public class KTServerListener implements Listener {
    private TriggerPlugin plugin;

    public KTServerListener(TriggerPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    void serverCommand(ServerCommandEvent event) {
        plugin.debug("[Command] " + event.getSender().getName() + ":" + event.getCommand());
        String cmd = event.getCommand();
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        List<String> arguments = new ArrayList<String>(Arrays.asList(cmd.split(" ")));
        if (plugin.getCommandHandler().validateCommand(arguments)) {
            plugin.getCommandHandler().executeCommand(event.getSender(), arguments);
            event.setCommand("ktrigger");
        }
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), cmd);
        event.setCommand("ktrigger");
    }

}
