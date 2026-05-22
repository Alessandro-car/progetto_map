package database;

public class EmptySetException extends Exception {

    public EmptySetException() {
        super("The result set is empty.");
    }

    public EmptySetException(String message) {
        super(message);
    }
}