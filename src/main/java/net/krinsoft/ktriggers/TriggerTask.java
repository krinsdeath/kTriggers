package net.krinsoft.ktriggers;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 * @author krinsdeath
 */
class TriggerTask implements Runnable {
    private TriggerPlugin plugin;
    private List<List<String>> lines = new ArrayList<List<String>>();
    private String type;
    private String target;
    private List<String> groups = new ArrayList<String>();
    private int index = -1;
    
    public TriggerTask(TriggerPlugin plugin, String target, String source) {
        try {
            this.plugin = plugin;
            String dest = target.replaceAll("<<([^>]+)>>", "$1");
            if (dest.toLowerCase().startsWith("groups")) {
                this.target = "groups";
                List<String> groups = Arrays.asList(dest.split(":")[1].split(","));
                plugin.debug(groups.toString());
                if (!groups.isEmpty()) {
                    this.groups.addAll(groups);
                }
            } else {
                this.target = "everyone";
            }
            String from = source.split(":")[0].replaceAll("<<([^>]+)>>", "$1");
            plugin.debug(from);
            if (from.startsWith("lists")) {
                Object obj = plugin.getConfig().get(from);
                if (obj instanceof List) {
                    List<Object> objects = (List<Object>) obj;
                    if (objects.get(0) instanceof List) {
                        this.lines = (List<List<String>>) obj;
                    }
                }
            }
            this.type = "random";
            if (source.split(":").length > 1) {
                type = source.split(":")[1].replaceAll("<<([^>]+)>>", "$1");
                if (type.equalsIgnoreCase("sequence")) {
                    this.type = "sequence";
                } else {
                    this.type = "random";
                }
            }
        } catch (NullPointerException e) {
            plugin.warn("A null occurred where a null shouldn't have been. Please check 'config.yml' for improper formatting!");
            e.printStackTrace();
        } catch (ArrayIndexOutOfBoundsException e) {
            plugin.warn("One of your targets or sources is invalid! Please check 'config.yml'!");
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        nextIndex();
        if (target.equalsIgnoreCase("groups")) {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (index >= lines.size()) { break; }
                if (hasAnyPermission(p, this.groups)) {
                    plugin.debug("Raw lines: " + lines.get(index).toString());
                    for (String line : lines.get(index)) {
                        line = line.replaceAll("(?i)&([0-F])", "\u00A7$1");
                        line = line.replaceAll("<<recipient>>", p.getName());
                        p.sendMessage(line);
                    }
                }
            }
        } else {
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                if (index >= lines.size()) { break; }
                for (String line : lines.get(index)) {
                    line = line.replaceAll("(?i)&([0-F])", "\u00A7$1");
                    line = line.replaceAll("<<recipient>>", p.getName());
                    if (line.startsWith("<<execute:")) {
                        String tmp = line.replaceAll("<<([^>]+)>>", "$1");
                        String command = tmp.split(":")[1];
                        String as = tmp.split(":")[2];
                        if (as.equalsIgnoreCase("console")) {
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), command);
                        } else {
                            plugin.getServer().dispatchCommand(p, command);
                        }
                        continue;
                    }
                    p.sendMessage(line);
                }
            }
        }
    }

    private void nextIndex() {
        if (type.equals("sequence")) {
            index += 1;
            if (index >= lines.size()) {
                index = 0;
            }
        } else {
            Random rand = new Random(System.currentTimeMillis());
            index = rand.nextInt(lines.size());
        }
        plugin.debug("Current index: " + index + "/" + lines.size());
    }

    public boolean hasPermission(CommandSender sender, String node) {
        return sender.hasPermission("group." + node);
    }

    public boolean hasAnyPermission(CommandSender sender, List<String> nodes) {
        for (String node : nodes) {
            if (hasPermission(sender, node)) {
                return true;
            }
        }
        return false;
    }

}
