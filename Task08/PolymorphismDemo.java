import java.time.Duration;
import java.time.LocalTime;

/**
 * Task 8 - compile-time polymorphism (overloading) and runtime polymorphism (dynamic dispatch),
 * with a printed dispatch trace and the instanceof-chain alternative.
 */
abstract class PVehicle {
    final String regNo;
    PVehicle(String regNo) { this.regNo = regNo; }

    abstract double ratePerHour();

    // Overload 1: duration in minutes
    double calculateFee(int minutes) {
        System.out.println("    -> compiler chose calculateFee(int)");
        return ((minutes + 59) / 60) * ratePerHour();
    }

    // Overload 2: clock times - converts and delegates
    double calculateFee(LocalTime in, LocalTime out) {
        System.out.println("    -> compiler chose calculateFee(LocalTime, LocalTime)");
        return calculateFee((int) Duration.between(in, out).toMinutes());
    }
}

class PTwoWheeler extends PVehicle {
    PTwoWheeler(String r) { super(r); }
    @Override double ratePerHour() {
        System.out.println("    -> runtime executed PTwoWheeler.ratePerHour()");
        return 10;
    }
}

class PCar extends PVehicle {
    PCar(String r) { super(r); }
    @Override double ratePerHour() {
        System.out.println("    -> runtime executed PCar.ratePerHour()");
        return 30;
    }
}

class PElectricCar extends PCar {
    PElectricCar(String r) { super(r); }
    @Override double ratePerHour() {
        System.out.println("    -> runtime executed PElectricCar.ratePerHour()");
        return 25;                                  // EV discount on parking
    }
}

public class PolymorphismDemo {

    /** Alternative without dynamic dispatch: test the type and cast. */
    static double feeByInstanceof(Object v, int minutes) {
        int hours = (minutes + 59) / 60;
        if (v instanceof PElectricCar) return hours * 25;     // most specific first, or it is wrong
        else if (v instanceof PCar) return hours * 30;
        else if (v instanceof PTwoWheeler) return hours * 10;
        throw new IllegalArgumentException("Unknown vehicle");
    }

    public static void main(String[] args) {
        System.out.println("Compile-time polymorphism (overloading):");
        PVehicle car = new PCar("TN02B2");
        System.out.println("  car.calculateFee(130) = Rs " + car.calculateFee(130));
        System.out.println("  car.calculateFee(09:00, 11:10) = Rs "
                + car.calculateFee(LocalTime.of(9, 0), LocalTime.of(11, 10)));

        System.out.println("\nRuntime polymorphism - one array of PVehicle references (upcasting):");
        PVehicle[] parked = { new PTwoWheeler("TN01A1"), new PCar("TN02B2"), new PElectricCar("TN03C3") };
        for (PVehicle v : parked) {
            System.out.println("  declared type PVehicle, actual type " + v.getClass().getSimpleName());
            System.out.println("  fee = Rs " + v.calculateFee(130));
        }

        System.out.println("\ninstanceof alternative gives: " + feeByInstanceof(parked[0], 130) + ", "
                + feeByInstanceof(parked[1], 130) + ", " + feeByInstanceof(parked[2], 130));
    }
}
