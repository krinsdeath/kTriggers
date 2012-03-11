package net.krinsoft.ktriggers.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * @author krinsdeath
 */
public interface Command {

    /**
     * Returns the command handle associated with this command object
     * @return The command (with the first leading slash stripped)
     */
    public String getCommand();

    /**
     * Gets the type of this command: Override, Cancel, Normal
     * @return The Command type
     */
    public CommandType getType();

    /**
     * Executes this command object with the given params
     * @param sender The person issuing the command
     * @param params The parameters passed to the command
     * @return true if the command succeeds, otherwise false
     */
    public boolean execute(CommandSender sender, List<String> params);

}
