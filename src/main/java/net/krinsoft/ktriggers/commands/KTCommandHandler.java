package net.krinsoft.ktriggers.commands;

import net.krinsoft.ktriggers.TriggerPlugin;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krinsdeath
 */
public class KTCommandHandler {

    private TriggerPlugin plugin;
    private Map<String, Command> commandMap = new HashMap<String, Command>();

    public KTCommandHandler(TriggerPlugin instance) {
        plugin = instance;
        for (String cmd : plugin.getConfig().getConfigurationSection("commands").getKeys(false)) {
            try {
                Command command = new kTriggerCommand(plugin, cmd);
                commandMap.put(cmd, command);
                plugin.debug("-> Command '" + cmd + "' successfully registered.");
            } catch (InvalidCommandException e) {
                plugin.warn("An exception occurred while registering the command '" + cmd + "': " + e.getLocalizedMessage());
            }
        }
    }

    public boolean executeCommand(CommandSender sender, List<String> command) {
        if (command.size() == 0) {
            plugin.log("The command can't be empty...");
            return false;
        }
        Command cmd = getCommand(command.remove(0));
        if (cmd == null) {
            plugin.log("Unknown command.");
            return false;
        }
        return cmd.execute(sender, command);
    }

    public Command getCommand(String cmd) {
        if (cmd == null) { return null; }
        if (cmd.startsWith("/")) {
            cmd = cmd.substring(1);
        }
        return commandMap.get(cmd);
    }

    public boolean validateCommand(List<String> command) {
        try {
            String cmd = command.get(0);
            if (cmd.startsWith("/")) {
                cmd = cmd.substring(1);
            }
            return getCommand(cmd) != null;
        } catch (NullPointerException e) {
            plugin.warn("Please don't enter null strings; it's confusing.");
        } catch (ArrayIndexOutOfBoundsException e) {
            plugin.warn("How are you doing that!? Empty lists are not my friend!");
        }
        return false;
    }
}
