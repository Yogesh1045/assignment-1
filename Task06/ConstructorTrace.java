/**
 * Task 6 - inheritance hierarchy with a constructor-execution trace, plus the composition
 * alternative (Vehicle has-a VehicleType).
 */
abstract class Vehicle {
    private final String regNo;
    protected final int wheels;

    static { System.out.println("  [static] Vehicle class loaded"); }

    protected Vehicle(String regNo, int wheels) {
        System.out.println("  1. Vehicle(regNo, wheels) runs  - sets regNo and wheels");
        this.regNo = regNo;
        this.wheels = wheels;
    }

    public String getRegNo() { return regNo; }
    protected abstract double ratePerHour();
    public double calculateFee(int minutes) { return ((minutes + 59) / 60) * ratePerHour(); }
    public String describe() { return getClass().getSimpleName() + " " + regNo + ", " + wheels + " wheels"; }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String regNo) { super(regNo, 2); }
    @Override protected double ratePerHour() { return 10; }
}

class Car extends Vehicle {
    static { System.out.println("  [static] Car class loaded"); }

    Car(String regNo) {
        super(regNo, 4);                                   // must be the first statement
        System.out.println("  2. Car(regNo) runs              - after Vehicle");
    }
    @Override protected double ratePerHour() { return 30; }
}

class ElectricCar extends Car {
    private final int batteryKWh;                          // added only in this subclass

    static { System.out.println("  [static] ElectricCar class loaded"); }

    ElectricCar(String regNo, int batteryKWh) {
        super(regNo);
        System.out.println("  3. ElectricCar(regNo, kWh) runs - after Car");
        this.batteryKWh = batteryKWh;
    }

    @Override public String describe() {
        return super.describe() + ", battery " + batteryKWh + " kWh";   // super.method()
    }
}

// ---------- Composition alternative ----------
final class VehicleType {
    final String name;
    final double ratePerHour;
    VehicleType(String name, double ratePerHour) { this.name = name; this.ratePerHour = ratePerHour; }
}

final class ComposedVehicle {
    final String regNo;
    final VehicleType type;                                // has-a
    ComposedVehicle(String regNo, VehicleType type) { this.regNo = regNo; this.type = type; }
    double calculateFee(int minutes) { return ((minutes + 59) / 60) * type.ratePerHour; }
}

public class ConstructorTrace {
    public static void main(String[] args) {
        System.out.println("new ElectricCar(\"TN03C3\", 40):");
        ElectricCar ev = new ElectricCar("TN03C3", 40);
        System.out.println("Result: " + ev.describe());
        System.out.println("Fee 130 min (inherited calculateFee, Car's rate): Rs " + ev.calculateFee(130));

        System.out.println("\nComposition alternative:");
        VehicleType car = new VehicleType("CAR", 30);
        VehicleType truck = new VehicleType("TRUCK", 50);      // new type = new DATA, no new class
        ComposedVehicle c1 = new ComposedVehicle("TN02B2", car);
        ComposedVehicle c2 = new ComposedVehicle("TN09T9", truck);
        System.out.println(c1.regNo + " " + c1.type.name + " 130 min -> Rs " + c1.calculateFee(130));
        System.out.println(c2.regNo + " " + c2.type.name + " 130 min -> Rs " + c2.calculateFee(130));
    }
}
