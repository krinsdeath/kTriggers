package net.krinsoft.ktriggers.commands;

import net.krinsoft.ktriggers.TriggerPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author krinsdeath
 */
public class kTriggerCommand implements Command {
    private TriggerPlugin plugin;
    private String rootCommand;
    private boolean logCommands;
    private CommandType type;

    private String executeAs;
    private List<CommandSender> executors = new ArrayList<CommandSender>();
    private List<CommandSender> targets   = new ArrayList<CommandSender>();

    private List<String> messages = new ArrayList<String>();
    private List<String> executes = new ArrayList<String>();

    private boolean once;
    private List<String> onceList = new ArrayList<String>();

    public kTriggerCommand(TriggerPlugin instance, String command) {
        plugin = instance;
        logCommands = plugin.getConfig().getBoolean("plugin.log_commands", false);
        rootCommand = (command.startsWith("/") ? command.substring(1) : command);
        if (plugin.getCommandNode(rootCommand) == null) {
            throw new InvalidCommandException("Unknown command.");
        }
        ConfigurationSection node = plugin.getCommandNode(rootCommand);
        if (node.getString("type") == null) {
            type = CommandType.NORMAL;
        } else {
            type = CommandType.fromName(node.getString("type"));
        }
        String execAs = node.getString("executeAs");
        if (execAs != null) {
            executeAs = execAs.replaceAll("<<([^>]+)>>", "$1").toLowerCase();
        } else {
            executeAs = "triggerer";
        }

        // builds and colorizes a message list to send
        List<String> msgs = node.getStringList("message");
        for (String line : msgs) {
            line = line.replaceAll("&([0-9a-fA-F])", "\u00A7$1");
            messages.add(line);
        }

        // builds the execution list
        executes = node.getStringList("execute");

        // builds the runonce list
        once = node.getBoolean("runOnce");
        if (once) { onceList.addAll(node.getStringList("runOnceList")); }

        if (executes.isEmpty() && messages.isEmpty()) {
            throw new InvalidCommandException("Incomplete command: missing execute, message");
        }
    }

    public String getCommand() {
        return this.rootCommand;
    }

    public CommandType getType() {
        return type;
    }

    public boolean execute(CommandSender sender, List<String> params) {
        if (once) {
            if (onceList.contains(sender.getName())) {
                return true;
            } else {
                onceList.add(sender.getName());
            }
        }
        // does the user have permission for the command?
        if (canExecute(sender)) {
            if (type == CommandType.CANCEL) {
                // user c
                return false;
            }
        } else {
            sender.sendMessage("\u00A7CYou do not have access to this command.");
            return true;
        }
        executors.clear();
        if (executeAs.equals("triggerer")) {
            executors.add(sender);
        } else if (executeAs.equals("console")) {
            executors.add(plugin.getServer().getConsoleSender());
        } else if (executeAs.equals("everyone")) {
            executors.addAll(plugin.getServer().getOnlinePlayers());
        } else {
            try {
                int param = Integer.parseInt(executeAs.replaceAll("param([0-9]+)", "$1"));
                String executor = params.get(param);
                executors.add(plugin.getServer().getPlayer(executor));
            } catch (NullPointerException e) {
                plugin.warn("The player fetched was null: " + e.getLocalizedMessage());
            } catch (ArrayIndexOutOfBoundsException e) {
                plugin.warn("A required parameter was not found: " + e.getLocalizedMessage());
            } catch (NumberFormatException e) {
                plugin.warn("An error occurred while parsing a parameter string: " + e.getLocalizedMessage());
            }
        }
        if (executors.isEmpty()) {
            executors.add(sender);
        }
        String allParams = "";
        for (String param : params) {
            allParams += param + " ";
        }
        for (CommandSender sndr : executors) {
            if (sndr == null) { continue; }
            plugin.debug("Executing command '" + rootCommand + "' for '" + sndr.getName() + "'...");
            for (String msg : messages) {
                msg = msg.replaceAll("<<triggerer>>", sender.getName());
                msg = msg.replaceAll("<<params>>", allParams.trim());
                for (int i = 0; i < params.size(); i++) {
                    String str = "<<param" + i + ">>";
                    msg = msg.replaceAll(str, params.get(i));
                }
                sndr.sendMessage(msg);
            }
            for (String msg : executes) {
                if (msg.startsWith("/") && !(sndr instanceof Player)) {
                    msg = msg.substring(1);
                }
                msg = msg.replaceAll("<<triggerer>>", sender.getName());
                msg = msg.replaceAll("<<params>>", allParams.trim());
                for (int i = 0; i < params.size(); i++) {
                    String str = "<<param" + i + ">>";
                    msg = msg.replaceAll(str, params.get(i));
                }
                if (sndr instanceof Player) {
                    ((Player)sndr).chat(msg);
                } else {
                    ServerCommandEvent event = new ServerCommandEvent(plugin.getServer().getConsoleSender(), msg);
                    plugin.getServer().getPluginManager().callEvent(event);
                }
            }
        }
        if (logCommands) {
            plugin.log("[Command] " + sender.getName() + "->/" + rootCommand + " " + allParams);
        }
        return true;
    }

    boolean canExecute(CommandSender sender) {
        return sender instanceof ConsoleCommandSender || sender.hasPermission("ktrigger.command." + rootCommand);
    }

}
