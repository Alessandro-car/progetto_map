package mapClient;

public enum Command {
	START("/start", "Start a new prediction"),
	LEARN("/learn", "Learn regression tree from data"),
	LOAD("/load", "Load regression tree from archive"),
	END("/end", "Exit the current prediction");

	private final String command;
	private final String description;

	Command(String command, String description) {
		this.command = command;
		this.description = description;
	}

	public String getCommand() { return command; }
	public String getDescription() { return description; }
}
