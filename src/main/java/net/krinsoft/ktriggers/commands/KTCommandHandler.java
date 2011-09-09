package net.krinsoft.ktriggers.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.krinsoft.ktriggers.TriggerPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.util.config.ConfigurationNode;

/**
 *
 * @author krinsdeath
 */
public class KTCommandHandler {

    private TriggerPlugin plugin;

    public KTCommandHandler(TriggerPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean executeCommand(CommandSender sender, List<String> command) {
        if (plugin.getCommandNode(command.get(0)) == null) { return false; }
        boolean cancel = false, override = false;
        String type = plugin.getCommandNode(command.get(0)).getString("type");
        if (type != null) {
            override = type.equalsIgnoreCase("override");
            cancel = type.equalsIgnoreCase("cancel");
        }
        if (hasPermission(sender, command.get(0)) || override || cancel) {
            if (hasPermission(sender, command.get(0)) && cancel) {
                return false;
            }
            String t = null;
            if (sender instanceof Player) {
                t = ((Player)sender).getName();
            } else {
                t = "Console";
            }
            StringBuilder paramString = null;
            if (command.size() > 1) {
                paramString = new StringBuilder(command.get(1));
                for (int i = 2; i < command.size(); i++) {
                    paramString.append(" ").append(command.get(i));
                }
            }
            ConfigurationNode node = plugin.getCommandNode(command.get(0));
            if (node.getBoolean("runOnce", false) && node.getStringList("runOnceList", null).contains(t)) {
                return true;
            }
            List<String> execution = node.getStringList("execute", null);
            List<String> message = node.getStringList("message", null);
            String person = node.getString("executeAs", "<<triggerer>>").replaceAll("<<([^>]+)>>", "$1");
            if (!execution.isEmpty() && person != null) {
                CommandSender executor = sender;
                if (person.equalsIgnoreCase("console")) {
                    executor = new ConsoleCommandSender(plugin.getServer());
                }
                for (String line : execution) {
                    line = line.replaceAll("(?i)&([0-F])", "\u00A7$1");
                    line = line.replaceAll("<<triggerer>>", t);
                    if (paramString != null) {
                        line = line.replaceAll("<<params>>", paramString.toString());
                    }
                    if (executor instanceof Player) {
                        ((Player)executor).chat(line);
                    } else {
                        if (line.startsWith("/")) {
                            line = line.substring(1);
                        }
                        plugin.getServer().getPluginManager().callEvent(new ServerCommandEvent((ConsoleCommandSender)executor, line));
                    }
                    plugin.debug(line);
                }
            }
            if (!message.isEmpty()) {
                String target = node.getString("target");
                List<CommandSender> targets = new ArrayList<CommandSender>();
                if (target != null) {
                    if (target.equalsIgnoreCase("<<everyone>>")) {
                        targets.addAll(Arrays.asList(plugin.getServer().getOnlinePlayers()));
                    } else if (target.equalsIgnoreCase("<<triggerer>")) {
                        targets.addAll(Arrays.asList(sender));
                    }
                } else {
                    targets.addAll(Arrays.asList(sender));
                }
                for (CommandSender dest : targets) {
                    String name = null;
                    if (dest instanceof Player) {
                        name = ((Player)dest).getName();
                    } else {
                        name = "Console";
                    }
                    for (String line : message) {
                        line = line.replaceAll("(?i)&([0-F])", "\u00A7$1");
                        line = line.replaceAll("<<triggerer>>", t);
                        line = line.replaceAll("<<recipient>>", name);
                        dest.sendMessage(line);
                    }
                }
            }
            if (node.getBoolean("runOnce", false)) {
                List<String> runners = node.getStringList("runOnceList", null);
                runners.add(t);
                node.setProperty("runOnceList", runners);
                plugin.getConfiguration().save();
            }
            return true;
        }
        return false;
    }

    private boolean hasPermission(CommandSender sender, String node) {
        String perm = "ktrigger.command." + node;
        boolean has = sender.hasPermission(perm);
        boolean set = sender.isPermissionSet(perm);
        if (has) {
            return true;
        } else if (set && !has) {
            return false;
        } else if (!set && sender.hasPermission("ktrigger.command.*")) {
            return true;
        } else {
            return false;
        }
    }
}
