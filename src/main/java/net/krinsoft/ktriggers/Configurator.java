package net.krinsoft.ktriggers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author krinsdeath
 */
class Configurator {
    private TriggerPlugin plugin;

    public Configurator(TriggerPlugin plugin) {
        this.plugin = plugin;
        makeFile(new File(plugin.getDataFolder(), "config.yml"));
    }

    public final void makeFile(File file) {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            InputStream in = TriggerPlugin.class.getResourceAsStream("/config.yml");
            if (in != null) {
                FileOutputStream out = null;
                try {
                    out = new FileOutputStream(file);
                    byte[] buf = new byte[5];
                    int len = 0;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    plugin.log("Successfully created " + file.getName());
                } catch (IOException e) {
                    plugin.log(e.getLocalizedMessage());
                } finally {
                    try {
                        in.close();
                        out.close();
                    } catch (IOException e) {
                        plugin.log(e.getLocalizedMessage());
                    }
                }
            }
        }
    }

}
