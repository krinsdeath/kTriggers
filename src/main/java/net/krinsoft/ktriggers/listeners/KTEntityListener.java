package net.krinsoft.ktriggers.listeners;

import net.krinsoft.ktriggers.TriggerPlugin;
import org.bukkit.event.entity.EntityListener;

/**
 *
 * @author krinsdeath
 */
public class KTEntityListener extends EntityListener {

    private TriggerPlugin plugin;

    public KTEntityListener(TriggerPlugin plugin) {
        this.plugin = plugin;
    }

}
