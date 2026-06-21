package util;

import model.*;

public class HumanBeingParser {

    public HumanBeing parse(String data) {
        try {
            String[] parts = data.trim().split("\\s+");
            int i = 0;
            String name = parts[i++];
            int x = Integer.parseInt(parts[i++]);
            Long y = Long.parseLong(parts[i++]);
            Coordinates coordinates = new Coordinates(x, y);
            boolean realHero = Boolean.parseBoolean(parts[i++]);
            Boolean hasToothpick = Boolean.parseBoolean(parts[i++]);
            Integer impactSpeed = parts[i].equals("null") ? null : Integer.parseInt(parts[i]);
            i++;
            WeaponType weaponType = WeaponType.valueOf(parts[i++].toUpperCase());
            Mood mood = parts[i].equals("null") ? null : Mood.valueOf(parts[i].toUpperCase());
            i++;
            Car car = null;
            if (!parts[i].equals("null")) {
                String carName = parts[i++];
                Boolean carCool = Boolean.parseBoolean(parts[i]);
                car = new Car(carName, carCool);
            }
            return new HumanBeing(
                    name,
                    coordinates,
                    realHero,
                    hasToothpick,
                    impactSpeed,
                    weaponType,
                    mood,
                    car
            );
        } catch (Exception e) {
            System.out.println("Error parsing HumanBeing: " + e.getMessage());
            return null;
        }
    }
}
