package net.krinsoft.ktriggers.commands;

/**
 * @author krinsdeath
 */
public class InvalidCommandException extends RuntimeException {
    private String message;

    public InvalidCommandException(String msg) {
        super(msg);
        message = msg;
    }

    public String getLocalizedMessage() {
        return message;
    }

}
