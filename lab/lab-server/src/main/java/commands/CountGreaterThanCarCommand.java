package commands;

import manager.CollectionManager;
import request.CarRequest;
import response.Response;

public class CountGreaterThanCarCommand extends Command<CarRequest> {
    public CountGreaterThanCarCommand(CollectionManager collectionManager) {
        super(collectionManager, CarRequest.class);
    }
    @Override
    public Response execute(CarRequest request) {
        return success(collectionManager.countGreaterThanCar(request.getCar()));
    }
}
