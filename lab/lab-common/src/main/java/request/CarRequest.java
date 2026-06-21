package request;

import model.Car;

public class CarRequest extends Request {
    private final Car car;
    public CarRequest(String commandName, Car car) {
        super(commandName);
        this.car = car;
    }
    public Car getCar() {
        return car;
    }
}
