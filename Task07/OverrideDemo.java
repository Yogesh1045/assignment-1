import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Task 7 - overriding and specialising behaviour; final; toString/equals/hashCode.
 * Also the switch-based alternative for comparison.
 */
abstract class Vehicle {
    private final String regNo;                             // final variable: never reassigned
    public static final int MINUTES_PER_HOUR = 60;          // final constant

    protected Vehicle(String regNo) { this.regNo = regNo; }

    public final String getRegNo() { return regNo; }        // final method: cannot be overridden

    protected abstract double ratePerHour();

    public double calculateFee(int minutes) {
        return ((minutes + MINUTES_PER_HOUR - 1) / MINUTES_PER_HOUR) * ratePerHour();
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle)) return false;
        return regNo.equals(((Vehicle) o).regNo);
    }

    @Override public int hashCode() { return regNo.hashCode(); }   // consistent with equals

    @Override public String toString() { return getClass().getSimpleName() + "[" + regNo + "]"; }
}

class TwoWheeler extends Vehicle {
    TwoWheeler(String r) { super(r); }
    @Override protected double ratePerHour() { return 10; }
}

class Car extends Vehicle {
    Car(String r) { super(r); }
    @Override protected double ratePerHour() { return 30; }
}

final class ElectricCar extends Car {                       // final class: no further subclasses
    ElectricCar(String r) { super(r); }

    /** Specialised: EVs get the first 30 minutes free (still a valid override - same signature). */
    @Override public double calculateFee(int minutes) {
        return super.calculateFee(Math.max(0, minutes - 30));
    }
}

/** Equals WITHOUT hashCode - shows why both must be overridden together. */
class BrokenVehicle {
    final String regNo;
    BrokenVehicle(String r) { regNo = r; }
    @Override public boolean equals(Object o) { return o instanceof BrokenVehicle b && b.regNo.equals(regNo); }
}

public class OverrideDemo {
    // Alternative: one method with a switch on a type field
    static double feeBySwitch(String type, int minutes) {
        int hours = (minutes + 59) / 60;
        switch (type) {
            case "TWO_WHEELER": return hours * 10;
            case "CAR":         return hours * 30;
            case "EV":          return ((Math.max(0, minutes - 30) + 59) / 60) * 30;
            default: throw new IllegalArgumentException("Unknown type " + type);
        }
    }

    public static void main(String[] args) {
        Vehicle[] vs = { new TwoWheeler("TN01A1"), new Car("TN02B2"), new ElectricCar("TN03C3") };
        for (Vehicle v : vs) {
            System.out.println(v + " 130 min -> Rs " + v.calculateFee(130));   // toString() override
        }

        Vehicle a = new Car("TN05E5");
        Vehicle b = new Car("TN05E5");
        System.out.println("\na == b          : " + (a == b));
        System.out.println("a.equals(b)     : " + a.equals(b));
        System.out.println("same hashCode   : " + (a.hashCode() == b.hashCode()));
        Set<Vehicle> parked = new HashSet<>();
        parked.add(a);
        System.out.println("HashSet rejects duplicate regNo: " + !parked.add(b));

        Set<BrokenVehicle> broken = new HashSet<>();
        broken.add(new BrokenVehicle("TN05E5"));
        broken.add(new BrokenVehicle("TN05E5"));
        System.out.println("Without hashCode the set holds : " + broken.size() + " 'equal' vehicles (bug)");

        System.out.println("\nSwitch alternative gives the same fees: "
                + feeBySwitch("TWO_WHEELER", 130) + ", " + feeBySwitch("CAR", 130) + ", " + feeBySwitch("EV", 130));

        Map<String, Integer> timing = new HashMap<>();
        int n = 5_000_000;
        double sink = 0;
        String[] types = {"TWO_WHEELER", "CAR", "EV"};
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sink += vs[i % 3].calculateFee(i % 600);
        long t1 = System.nanoTime();
        for (int i = 0; i < n; i++) sink += feeBySwitch(types[i % 3], i % 600);
        long t2 = System.nanoTime();
        System.out.printf("Per call: overriding %.2f ns | switch %.2f ns%n",
                (t1 - t0) / (double) n, (t2 - t1) / (double) n);
        if (sink == -1) System.out.println(timing);
    }
}
