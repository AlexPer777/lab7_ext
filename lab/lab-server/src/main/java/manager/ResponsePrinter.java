package manager;

import model.HumanBeing;
import response.Response;

public class ResponsePrinter {

    public void print(Response response) {
        if (response == null) {
            return;
        }
        if (response.getMessage() != null && !response.getMessage().isBlank()) {
            System.out.println(response.getMessage());
        }
        for (HumanBeing humanBeing : response.getHumanBeings()) {
            System.out.println(humanBeing);
            System.out.println();
        }
    }
}
