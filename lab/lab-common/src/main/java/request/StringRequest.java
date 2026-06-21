package request;

public class StringRequest extends Request {
    private final String value;
    public StringRequest(String commandName, String value) {
        super(commandName);
        this.value = value;
    }
    public String getValue() {
        return value;
    }
}
