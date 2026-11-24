/**
 * Task 9 - abstraction: an abstract class for WHAT a vehicle is (shared state + constructor),
 * interfaces for capabilities a class may add (Chargeable, Payable), default and static methods.
 */
interface Chargeable {
    double RATE_PER_KWH = 12;

    double chargingFee(int kWh);

    /** Default method: every chargeable vehicle gets this without writing it. */
    default String chargingReceipt(int kWh) {
        return kWh + " kWh x Rs " + RATE_PER_KWH + " = Rs " + chargingFee(kWh);
    }
}

interface Payable {
    double amountDue(double parkingFee);

    /** Static helper shared by all implementations. */
    static double roundToRupee(double amount) { return Math.round(amount); }
}

abstract class Vehicle {
    private final String regNo;                            // state - interfaces cannot hold this
    protected Vehicle(String regNo) {                      // constructor - interfaces have none
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("regNo required");
        this.regNo = regNo;
    }
    public String getRegNo() { return regNo; }
    protected abstract double ratePerHour();               // WHAT must be supplied
    public final double calculateFee(int minutes) {        // HOW it is computed - one place
        return ((minutes + 59) / 60) * ratePerHour();
    }
}

class Car extends Vehicle {
    Car(String r) { super(r); }
    @Override protected double ratePerHour() { return 30; }
}

/** One superclass, two interfaces: multiple behaviours without multiple inheritance of state. */
class ElectricCar extends Car implements Chargeable {
    ElectricCar(String r) { super(r); }
    @Override public double chargingFee(int kWh) { return kWh * RATE_PER_KWH; }
}

/** An electric two-wheeler - unrelated class, same capability. */
class ElectricScooter extends Vehicle implements Chargeable {
    ElectricScooter(String r) { super(r); }
    @Override protected double ratePerHour() { return 10; }
    @Override public double chargingFee(int kWh) { return kWh * RATE_PER_KWH * 0.5; }   // slow charger
}

/** Users who pay: the interface describes the capability, not the kind of user. */
class Visitor implements Payable {
    @Override public double amountDue(double fee) { return fee; }
}

class PassHolder implements Payable {
    @Override public double amountDue(double fee) { return 0; }
}

public class AbstractionDemo {
    public static void main(String[] args) {
        Vehicle[] vs = { new Car("TN02B2"), new ElectricCar("TN03C3"), new ElectricScooter("TN04D4") };
        Payable[] payers = { new Visitor(), new Visitor(), new PassHolder() };

        for (int i = 0; i < vs.length; i++) {
            Vehicle v = vs[i];
            double parking = payers[i].amountDue(v.calculateFee(130));
            String line = v.getClass().getSimpleName() + " " + v.getRegNo() + " parking Rs " + parking
                    + " (" + payers[i].getClass().getSimpleName() + ")";
            if (v instanceof Chargeable c) line += " | charging " + c.chargingReceipt(10);
            System.out.println(line);
        }
        System.out.println("Payable.roundToRupee(89.6) = " + Payable.roundToRupee(89.6));
        // new Vehicle("X");   // does not compile: Vehicle is abstract
    }
}
