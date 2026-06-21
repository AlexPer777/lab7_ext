package model;

import org.jspecify.annotations.NonNull;

import java.io.Serializable;

public class Car implements Comparable<Car>, Serializable {
    @NonNull private final String name; //Поле не может быть null
    @NonNull private final Boolean cool; //Поле не может быть null

    public Car(@NonNull String name, @NonNull Boolean cool) {
        this.name = name;
        this.cool = cool;
    }
    public @NonNull String getName() {
        return name;
    }
    public @NonNull Boolean getCool() {
        return cool;
    }
    @Override
    public int compareTo(@NonNull Car o) {
        return name.compareTo(o.name);
    }
}
