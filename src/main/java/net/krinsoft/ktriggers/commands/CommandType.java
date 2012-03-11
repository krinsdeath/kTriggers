package net.krinsoft.ktriggers.commands;

/**
 * @author krinsdeath
 */
public enum CommandType {

    OVERRIDE,
    CANCEL,
    NORMAL;

    public static CommandType fromName(String name) {
        name = name.toUpperCase();
        for (CommandType t : values()) {
            if (t.name().equals(name)) {
                return t;
            }
        }
        return NORMAL;
    }

}
