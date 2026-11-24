package parking.app;

import parking.model.ParkingSlot;
import parking.model.Stay;
import parking.service.FeeService;

/** Task 10 - packages, access modifiers, validating setters, wrapper-class pitfalls. */
public class EncapsulationDemo {

    /** The alternative: public fields, validation left to every caller. */
    static class PublicStay {
        public int minutes;
    }

    public static void main(String[] args) {
        FeeService fees = new FeeService();

        Stay s = new Stay(130);
        System.out.println("Valid stay 130 min -> fee Rs " + fees.carFee(s));

        attempt("setMinutes(-15)", () -> s.setMinutes(-15));
        System.out.println("   stay still holds " + s.getMinutes() + " min (state protected)");
        attempt("new Stay(20000)", () -> new Stay(20000));
        attempt("new ParkingSlot(7, 0, 20)", () -> new ParkingSlot(7, 0, 20));
        attempt("new ParkingSlot(0, 25, 20)", () -> new ParkingSlot(0, 25, 20));

        ParkingSlot slot = new ParkingSlot(0, 3, 20);
        slot.occupy("TN01AB1111");
        attempt("occupy an occupied slot", () -> slot.occupy("TN02CD2222"));
        slot.free();
        attempt("free an already free slot", slot::free);

        // Public-field alternative: nothing stops a bad value
        PublicStay p = new PublicStay();
        p.minutes = -15;
        System.out.println("\nPublic field accepted minutes = " + p.minutes
                + " -> fee would be Rs " + ((p.minutes + 59) / 60) * 30 + " (silently wrong)");

        // Wrapper classes and autoboxing
        Integer a = 127, b = 127, c = 1000, d = 1000;
        System.out.println("\nInteger 127 == 127   : " + (a == b) + "   (cached objects)");
        System.out.println("Integer 1000 == 1000 : " + (c == d) + "  (different objects!)");
        System.out.println("c.equals(d)          : " + c.equals(d) + "   (compare values with equals)");
        s.setKWhCharged(null);
        try {
            int kWh = s.getKWhCharged();                 // auto-unboxing null
            System.out.println(kWh);
        } catch (NullPointerException e) {
            System.out.println("Unboxing a null Integer -> NullPointerException (checked before use in the system)");
        }
    }

    private static void attempt(String label, Runnable action) {
        try {
            action.run();
            System.out.println("ACCEPTED " + label);
        } catch (RuntimeException e) {
            System.out.println("REJECTED " + label + " -> " + e.getMessage());
        }
    }
}
