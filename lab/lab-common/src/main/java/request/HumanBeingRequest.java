package request;

import model.HumanBeing;

public class HumanBeingRequest extends Request {
    private final HumanBeing humanBeing;
    public HumanBeingRequest(String commandName, HumanBeing humanBeing) {
        super(commandName);
        this.humanBeing = humanBeing;
    }
    public HumanBeing getHumanBeing() {
        return humanBeing;
    }
}
