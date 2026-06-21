package response;

import model.HumanBeing;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Response implements Serializable {
    private final String message;
    private final ResponseStatus status;
    private final List<HumanBeing> humanBeings;

    public Response(String message, ResponseStatus status, List<HumanBeing> humanBeings) {
        this.message = message;
        this.status = status;
        this.humanBeings = humanBeings == null ? new ArrayList<>() : new ArrayList<>(humanBeings);
    }
    public String getMessage() {
        return message;
    }
    public ResponseStatus getStatus() {
        return status;
    }
    public List<HumanBeing> getHumanBeings() {
        return new ArrayList<>(humanBeings);
    }
}
