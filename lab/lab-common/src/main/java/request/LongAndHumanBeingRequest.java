package request;

import model.HumanBeing;

public class LongAndHumanBeingRequest extends Request {
    private final long id;
    private final HumanBeing humanBeing;
    public LongAndHumanBeingRequest(String commandName, long id, HumanBeing humanBeing) {
        super(commandName);
        this.id = id;
        this.humanBeing = humanBeing;
    }
    public long getId() {
        return id;
    }
    public HumanBeing getHumanBeing() {
        return humanBeing;
    }
}
