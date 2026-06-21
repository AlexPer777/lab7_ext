package request;

public class LongRequest extends Request {
    private final long value;
    public LongRequest(String commandName, long value) {
        super(commandName);
        this.value = value;
    }
    public long getValue() {
        return value;
    }
}
