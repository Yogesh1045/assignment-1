package parking.service;

import parking.model.Car;
import parking.model.ElectricCar;
import parking.model.TwoWheeler;
import parking.model.Vehicle;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Creates vehicles from a type name read from input. New types are added with register(),
 * so this class never has to be edited when a vehicle category is added.
 */
public final class VehicleFactory {
    private static final Map<String, Function<String, Vehicle>> CREATORS = new LinkedHashMap<>();

    static {                                    // static block: register the built-in types once
        register("TWO_WHEELER", TwoWheeler::new);
        register("CAR", Car::new);
        register("EV", ElectricCar::new);
    }

    private VehicleFactory() { }                // utility class - no objects

    public static void register(String type, Function<String, Vehicle> creator) {
        CREATORS.put(type.toUpperCase(), creator);
    }

    public static Vehicle create(String type, String regNo) {
        Function<String, Vehicle> creator = CREATORS.get(type.toUpperCase());
        if (creator == null) throw new IllegalArgumentException("Unknown vehicle type: " + type);
        return creator.apply(regNo);
    }

    public static Iterable<String> knownTypes() { return CREATORS.keySet(); }
}
