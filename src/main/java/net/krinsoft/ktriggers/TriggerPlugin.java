package net.krinsoft.ktriggers;

// event stuff
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import net.krinsoft.ktriggers.commands.KTCommandHandler;
import net.krinsoft.ktriggers.listeners.KTPlayerListener;
import net.krinsoft.ktriggers.listeners.KTEntityListener;
import net.krinsoft.ktriggers.listeners.KTServerListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginManager;
import org.bukkit.event.Event;
// plugin stuff
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;
import org.bukkit.util.config.ConfigurationNode;

/**
 * @author krinsdeath
 * @license MIT OSD
 */
public class TriggerPlugin extends JavaPlugin {
    private final static Logger LOGGER = Logger.getLogger("kTriggers");
    private boolean debug;
    private Configurator configurator;
    private Configuration configuration;
    private PluginManager pm;

    private KTPlayerListener playerListener;
    private KTServerListener serverListener;
    private KTEntityListener entityListener;
    private KTCommandHandler commandListener;

    @Override
    public void onEnable() {
        registerConfiguration();
        registerListeners();
        registerEvents();
        registerCommands();
        buildTasks();
        log("is now enabled.");
    }

    @Override
    public void onDisable() {
        this.getServer().getScheduler().cancelTasks(this);
        log("is now disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && args[0].equals("reload") && sender.hasPermission("ktrigger.reload")) {
            registerConfiguration();
            registerCommands();
            buildTasks();
            sender.sendMessage("Configuration for kTriggers has been reloaded.");
        }
        return true;
    }

    private void registerConfiguration() {
        configurator = new Configurator(this);
        configuration = new Configuration(new File(getDataFolder(), "config.yml"));
        configuration.load();
        configuration.setHeader(
                "# Each command can be given a 'runOnce' key.",
                "# If this key is specified, it will only be run ONE TIME for that triggerer",
                "# and cannot be executed ever again by that person, unless you edit that command's",
                "# 'runOnceList' key and remove their name.",
                "# ",
                "# Each non-override command is given a permission node: ktrigger.command.[command name]",
                "# Each permission is registered to ktrigger.command.*, which is registered to ktrigger.*",
                "# ktrigger.reload allows the use of /ktrigger reload, and defaults to Op (but can be overridden)");
        debug = getConfiguration().getBoolean("plugin.debug", false);
        configuration.save();
    }

    public void registerListeners() {
        playerListener = new KTPlayerListener(this);
        serverListener = new KTServerListener(this);
        entityListener = new KTEntityListener(this);
        commandListener = new KTCommandHandler(this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    public void registerEvents() {
        pm = this.getServer().getPluginManager();
        pm.registerEvent(Event.Type.PLAYER_COMMAND_PREPROCESS, playerListener, Event.Priority.Low, this);
        pm.registerEvent(Event.Type.SERVER_COMMAND, serverListener, Event.Priority.Low, this);
    }

    public void registerCommands() {
        List<String> aliases = getConfiguration().getKeys("commands");
        debug(aliases.toString());
        Permission root = new Permission("ktrigger.*");
        root.setDefault(PermissionDefault.OP);
        Permission reload = new Permission("ktrigger.reload");
        reload.setDefault(PermissionDefault.OP);
        Permission commands = new Permission("ktrigger.command.*");
        commands.setDefault(PermissionDefault.OP);
        if (pm.getPermission(reload.getName()) == null) {
            pm.addPermission(reload);
        }
        if (pm.getPermission(commands.getName()) == null) {
            pm.addPermission(commands);
        }
        if (pm.getPermission(root.getName()) == null) {
            pm.addPermission(root);
        }
        for (String key : aliases) {
            ConfigurationNode node = getCommandNode(key);
            if (node.getString("type") == null || (!node.getString("type").equalsIgnoreCase("override") && !node.getString("type").equalsIgnoreCase("cancel"))) {
                Permission perm = new Permission("ktrigger.command." + key);
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
        }
        commands.recalculatePermissibles();
        root.getChildren().put("ktrigger.command.*", true);
        root.getChildren().put("ktrigger.reload", true);
        root.recalculatePermissibles();
    }

    public void buildTasks() {
        getServer().getScheduler().cancelTasks(this);
        ConfigurationNode node = getConfiguration().getNode("tasks");
        if (node != null) {
            List<String> tasks = node.getKeys();
            for (String task : tasks) {
                long delay = node.getInt(task + ".delay", 0);
                String target = node.getString(task + ".target");
                String source = node.getString(task + ".source");
                debug(target + " / " + source);
                TriggerTask triggerTask = new TriggerTask(this, target, source);
                this.getServer().getScheduler().scheduleSyncRepeatingTask(this, triggerTask, 20, delay * 20);
            }
        }
    }

    public ConfigurationNode getCommandNode(String key) {
        return this.getConfiguration().getNode("commands." + key);
    }

    public void log(Object msg) {
        LOGGER.info(String.valueOf("[" + this + "] " + msg.toString()));
    }

    public void debug(Object msg) {
        if (debug && msg != null) {
            LOGGER.info(String.valueOf("[" + this + "] [Debug] " + msg.toString()));
        }
    }

    public KTCommandHandler getCommandHandler() {
        return this.commandListener;
    }

    public List<String> getList(String key) {
        return getConfiguration().getStringList("lists." + key, null);
    }

}
