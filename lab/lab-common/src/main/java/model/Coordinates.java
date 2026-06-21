package model;

import java.io.Serializable;
import org.jspecify.annotations.NonNull;

public class Coordinates implements Serializable {
    private int x;
    @NonNull Long y; //Значение поля должно быть больше -383, Поле не может быть null
    public Coordinates(int x, @NonNull Long y) {
        if (y <= -383) {
            throw new IllegalArgumentException("У Вас должно быть больше -383");
        }
        this.x = x;
        this.y = y;
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public @NonNull Long getY() {
        return y;
    }
    public void setY(@NonNull Long y) {
        if (y <= -383) {
            throw new IllegalArgumentException("У Вас должно быть больше -383");
        }
        this.y = y;
    }
}
