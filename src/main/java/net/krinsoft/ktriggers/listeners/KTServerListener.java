package net.krinsoft.ktriggers.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.krinsoft.ktriggers.TriggerPlugin;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.server.ServerListener;

/**
 *
 * @author krinsdeath
 */
public class KTServerListener extends ServerListener {
    private TriggerPlugin plugin;

    public KTServerListener(TriggerPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onServerCommand(ServerCommandEvent event) {
        String cmd = event.getCommand();
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        if (plugin.getCommandNode(cmd.split(" ")[0]) != null) {
            if (!plugin.getCommandNode(cmd.split(" ")[0]).getString("type", "normal").equalsIgnoreCase("cancel")) {
                List<String> arguments = new ArrayList<String>(Arrays.asList(cmd.split(" ")));
                plugin.getCommandHandler().executeCommand(event.getSender(), arguments);
                event.setCommand("ktrigger");
            }
        }
        plugin.getServer().dispatchCommand(event.getSender(), event.getCommand());
        event.setCommand("ktrigger");
    }

}
