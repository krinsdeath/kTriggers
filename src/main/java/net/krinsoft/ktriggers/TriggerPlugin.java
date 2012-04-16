package net.krinsoft.ktriggers;

import net.krinsoft.ktriggers.commands.KTCommandHandler;
import net.krinsoft.ktriggers.listeners.KTPlayerListener;
import net.krinsoft.ktriggers.listeners.KTServerListener;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 *
 * @author krinsdeath
 */
@SuppressWarnings("unused")
public class TriggerPlugin extends JavaPlugin {
    private PluginManager pm;
    private boolean debug;

    private FileConfiguration configuration;
    private File configFile;

    private KTPlayerListener playerListener;
    private KTServerListener serverListener;
    private KTCommandHandler commandListener;

    @Override
    public void onEnable() {
        long t = System.currentTimeMillis();
        registerConfiguration();
        registerListeners();
        registerEvents();
        registerCommands();
        buildTasks();
        t = System.currentTimeMillis() - t;
        log("Enabled successfully. ("+ t +"ms)");
    }

    @Override
    public void onDisable() {
        long t = System.currentTimeMillis();
        commandListener.cleanup();
        configuration = null;
        getServer().getScheduler().cancelTasks(this);
        t = System.currentTimeMillis() - t;
        log("Disabled successfully. ("+ t +"ms)");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            if ((args[0].equals("reload") || args[0].equals("-r")) && sender.hasPermission("ktrigger.reload")) {
                long t = System.currentTimeMillis();
                configuration = null;
                registerConfiguration();
                registerCommands();
                commandListener.cleanup();
                commandListener = new KTCommandHandler(this);
                buildTasks();
                t = System.currentTimeMillis() - t;
                sender.sendMessage(ChatColor.GOLD + "[kTriggers] " + ChatColor.WHITE + "Configuration reloaded. (" + t + "ms)");
            } else if ((args[0].equals("version") || args[0].equals("-v")) && sender.hasPermission("ktrigger.version")) {
                sender.sendMessage(ChatColor.GOLD + "[kTriggers] " + ChatColor.WHITE + "Version: " + ChatColor.GREEN + getDescription().getVersion());
                sender.sendMessage(ChatColor.GOLD + "[kTriggers] " + ChatColor.WHITE + "By: " + ChatColor.GREEN + "krinsdeath");
            } else if ((args[0].equals("debug") || args[0].equals("-d")) && sender.hasPermission("ktrigger.debug")) {
                debug = !debug;
                sender.sendMessage(ChatColor.GOLD + "[kTriggers] " + ChatColor.WHITE + "Debug mode: " + (debug ? ChatColor.GREEN + "enabled" : ChatColor.RED + "disabled"));
            }
        }
        return true;
    }

    public FileConfiguration getConfig() {
        if (configuration == null) {
            configuration = YamlConfiguration.loadConfiguration(configFile);
            configuration.setDefaults(YamlConfiguration.loadConfiguration(configFile));
        }
        return configuration;
    }

    public void saveConfig() {
        try {
            getConfig().save(configFile);
        } catch (IOException e) {
            log("An error occurred while saving 'config.yml'... check your file permissions!");
        }
    }

    private void registerConfiguration() {
        log("Registering configuration...");
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            getConfig().setDefaults(YamlConfiguration.loadConfiguration(this.getClass().getResourceAsStream("/config.yml")));
            getConfig().options().copyDefaults(true);
            getConfig().options().header(
                    "Each command can be given a 'runOnce' key.\n" +
                            "If this key is specified, it will only be run ONE TIME for that triggerer\n" +
                            "and cannot be executed ever again by that person, unless you edit that command's\n" +
                            "'runOnceList' key and remove their name.\n" +
                            "\n" +
                            "Each non-override command is given a permission node: ktrigger.command.[command name]\n" +
                            "Each permission is registered to ktrigger.command.*, which is registered to ktrigger.*\n" +
                            "ktrigger.reload allows the use of /ktrigger reload, and defaults to Op (but can be overridden)");
            getConfig().set("plugin.version", getDescription().getVersion());
            saveConfig();
        }
        debug = getConfig().getBoolean("plugin.debug", false);
    }

    public void registerListeners() {
        playerListener = new KTPlayerListener(this);
        serverListener = new KTServerListener(this);
        commandListener = new KTCommandHandler(this);
    }

    public void registerEvents() {
        pm = this.getServer().getPluginManager();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(serverListener, this);
    }

    public void registerCommands() {
        log("Registering dynamic commands...");
        Set<String> aliases = getConfig().getConfigurationSection("commands").getKeys(false);
        debug(aliases.toString());
        Permission root = new Permission("ktrigger.*");
        root.setDefault(PermissionDefault.OP);
        Permission reload = new Permission("ktrigger.reload");
        reload.setDefault(PermissionDefault.OP);
        Permission version = new Permission("ktrigger.version");
        version.setDefault(PermissionDefault.OP);
        Permission commands = new Permission("ktrigger.command.*");
        commands.setDefault(PermissionDefault.OP);
        if (pm.getPermission(reload.getName()) == null) {
            pm.addPermission(reload);
        }
        if (pm.getPermission(version.getName()) == null) {
            pm.addPermission(version);
        }
        if (pm.getPermission(commands.getName()) == null) {
            pm.addPermission(commands);
        }
        if (pm.getPermission(root.getName()) == null) {
            pm.addPermission(root);
        }
        for (String key : aliases) {
            ConfigurationSection node = getCommandNode(key);
            Permission perm = new Permission("ktrigger.command." + key);
            if (node.getString("type") == null || (!node.getString("type").equalsIgnoreCase("override") && !node.getString("type").equalsIgnoreCase("cancel"))) {
                String who = node.getString("who");
                if (who != null && who.equalsIgnoreCase("<<operators>>")) {
                    perm.setDefault(PermissionDefault.OP);
                } else {
                    perm.setDefault(PermissionDefault.TRUE);
                }
                commands.getChildren().put("ktrigger.command." + key, true);
                if (pm.getPermission(perm.getName()) == null) {
                    pm.addPermission(perm);
                }
            }
            log("... " + key + " done! (" + perm.getName() + ")");
        }
        commands.recalculatePermissibles();
        root.getChildren().put("ktrigger.command.*", true);
        root.getChildren().put("ktrigger.reload", true);
        root.getChildren().put("ktrigger.version", true);
        root.recalculatePermissibles();
    }

    public void buildTasks() {
        log("Starting timed tasks...");
        getServer().getScheduler().cancelTasks(this);
        ConfigurationSection node = getConfig().getConfigurationSection("tasks");
        if (node != null) {
            Set<String> tasks = node.getKeys(false);
            for (String task : tasks) {
                long delay = node.getInt(task + ".delay", 0);
                String target = node.getString(task + ".target");
                String source = node.getString(task + ".source");
                debug(target + " / " + source);
                TriggerTask triggerTask = new TriggerTask(this, target, source);
                this.getServer().getScheduler().scheduleSyncRepeatingTask(this, triggerTask, 20, delay * 20);
                log("... " + task + " done.");
            }
        }
    }

    public ConfigurationSection getCommandNode(String key) {
        return this.getConfig().getConfigurationSection("commands." + key);
    }

    public void log(String message) {
        getLogger().info(message);
    }

    public void debug(String message) {
        if (debug && message != null) {
            message = "[Debug] " + message;
            getLogger().info(message);
        }
    }

    public void warn(String message) {
        getLogger().warning(message);
    }

    public KTCommandHandler getCommandHandler() {
        return this.commandListener;
    }

    public List<String> getList(String key) {
        return getConfig().getStringList("lists." + key);
    }

}
