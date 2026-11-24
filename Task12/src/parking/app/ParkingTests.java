package parking.app;

import parking.model.Car;
import parking.model.DeliveryVan;
import parking.model.EBike;
import parking.model.MonthlyPass;
import parking.model.OneDayPass;
import parking.model.Pass;
import parking.model.ElectricCar;
import parking.model.ParkingTicket;
import parking.model.TwoWheeler;
import parking.model.UserCategory;
import parking.model.Vehicle;
import parking.service.ParkingFacility;
import parking.service.ParkingService;
import parking.service.VehicleFactory;

import java.time.LocalTime;
import java.util.Objects;
import java.util.function.Supplier;

/** Test table for Task 11: prints ID, case, expected, actual, PASS/FAIL. No external libraries. */
public class ParkingTests {
    private static int passed, failed;

    public static void main(String[] args) {
        System.out.printf("%-4s %-46s %-28s %-28s %s%n", "ID", "Test case", "Expected", "Actual", "Result");

        // ---- Normal operation ----
        check("N1", "Car 130 min (3 started hours x 30)", "90.0", () -> fee(new Car("TN01A1"), 130));
        check("N2", "TwoWheeler 130 min (3 x 10)", "30.0", () -> fee(new TwoWheeler("TN01A2"), 130));
        check("N3", "EV 130 min + 10 kWh (90 + 120)", "210.0", () -> {
            ParkingService s = service(2);
            s.enter(new ElectricCar("TN03C3"), UserCategory.VISITOR, Pass.none(), t("09:00"));
            return String.valueOf(s.exit("TN03C3", t("11:10"), 10));
        });
        check("N4", "Staff with monthly pass pays 0", "0.0", () -> {
            ParkingService s = service(2);
            s.enter(new Car("TN05E5"), UserCategory.STAFF, new MonthlyPass(), t("08:00"));
            return String.valueOf(s.exit("TN05E5", t("17:00"), 0));
        });
        check("N5", "Exit frees the slot for the next car", "[0, 0]", () -> {
            ParkingService s = service(1);
            s.enter(new Car("TN01A1"), UserCategory.VISITOR, Pass.none(), t("08:00"));
            s.exit("TN01A1", t("09:00"), 0);
            ParkingTicket tk = s.enter(new Car("TN02B2"), UserCategory.VISITOR, Pass.none(), t("09:05"));
            return "[" + tk.getFloor() + ", " + tk.getSlot() + "]";
        });

        // ---- Boundary cases ----
        check("B1", "Last free slot is allocated", "[1, 0]", () -> {
            ParkingFacility f = new ParkingFacility("T", 2, 1);
            f.allocate(new Car("TN01A1")); f.allocate(new Car("TN02B2"));
            return java.util.Arrays.toString(f.allocate(new Car("TN03C3")));
        });
        check("B2", "Facility full -> rejected", "IllegalStateException", () -> {
            ParkingService s = service(1);
            s.enter(new Car("TN01A1"), UserCategory.VISITOR, Pass.none(), t("08:00"));
            s.enter(new Car("TN02B2"), UserCategory.VISITOR, Pass.none(), t("08:01"));
            return "no exception";
        });
        check("B3", "Zero-minute parking", "0.0", () -> fee(new Car("TN01A1"), 0));
        check("B4", "Exactly 60 min (slab boundary)", "30.0", () -> fee(new Car("TN01A1"), 60));
        check("B5", "61 min starts the 2nd hour", "60.0", () -> fee(new Car("TN01A1"), 61));
        check("B6", "13 h capped at daily max 300", "300.0", () -> fee(new Car("TN01A1"), 13 * 60));
        check("B7", "Overnight 22:00 -> 01:00 (3 h)", "90.0",
                () -> String.valueOf(new Car("TN01A1").calculateFee(t("22:00"), t("01:00"))));
        check("B8", "Ticket numbers are unique & increasing", "true", () -> {
            ParkingService s = service(3);
            int a = s.enter(new Car("TN01A1"), UserCategory.VISITOR, Pass.none(), t("08:00")).getTicketNo();
            int b = s.enter(new Car("TN02B2"), UserCategory.VISITOR, Pass.none(), t("08:00")).getTicketNo();
            return String.valueOf(b == a + 1);
        });

        // ---- Invalid cases ----
        check("I1", "Unknown vehicle type BUS", "IllegalArgumentException",
                () -> VehicleFactory.create("BUS", "TN07G7").toString());
        check("I2", "Duplicate registration while parked", "IllegalStateException", () -> {
            ParkingService s = service(3);
            s.enter(new Car("TN01A1"), UserCategory.VISITOR, Pass.none(), t("08:00"));
            s.enter(new TwoWheeler("TN01A1"), UserCategory.VISITOR, Pass.none(), t("08:10"));
            return "no exception";
        });
        check("I3", "Release invalid slot (5, 0)", "false",
                () -> String.valueOf(new ParkingFacility("T", 2).release(5, 0)));
        check("I4", "Release a slot that is already free", "false",
                () -> String.valueOf(new ParkingFacility("T", 2).release(0, 1)));
        check("I5", "Negative duration", "IllegalArgumentException", () -> fee(new Car("TN01A1"), -5));
        check("I6", "Invalid registration number", "IllegalArgumentException",
                () -> new Car("12 34").toString());
        check("I7", "Visitor claiming a monthly pass", "IllegalArgumentException", () -> {
            service(1).enter(new Car("TN01A1"), UserCategory.VISITOR, new MonthlyPass(), t("08:00"));
            return "no exception";
        });
        check("I8", "Exit for a vehicle that is not parked", "IllegalArgumentException",
                () -> String.valueOf(service(1).exit("TN99Z9", t("10:00"), 0)));
        check("I9", "Charging a non-EV", "IllegalArgumentException", () -> {
            ParkingService s = service(1);
            s.enter(new Car("TN01A1"), UserCategory.VISITOR, Pass.none(), t("08:00"));
            return String.valueOf(s.exit("TN01A1", t("09:00"), 5));
        });

        check("I10", "Rejected exit leaves the car parked", "1", () -> {
            ParkingService s = service(1);
            s.enter(new Car("TN01A1"), UserCategory.VISITOR, Pass.none(), t("08:00"));
            try { s.exit("TN01A1", t("09:00"), 5); } catch (IllegalArgumentException ignored) { }
            return String.valueOf(s.activeCount());
        });

        // ---- Object-class contract ----
        check("O1", "Car equals ElectricCar with same regNo", "true",
                () -> String.valueOf(new Car("TN02B2").equals(new ElectricCar("TN02B2"))));
        check("O2", "Equal objects -> equal hashCodes", "true",
                () -> String.valueOf(new Car("TN02B2").hashCode() == new ElectricCar("TN02B2").hashCode()));

        // ---- NEW in Task 12: new categories, one-day pass, 5 uneven floors ----
        ParkingApp.registerNewCategories();
        check("T1", "E-bike 4 h -> flat fee", "20.0", () -> fee(new EBike("TN11E1"), 240));
        check("T2", "E-bike 0 min", "0.0", () -> fee(new EBike("TN11E1"), 0));
        check("T3", "E-bike 25 h -> 2 started days", "40.0", () -> fee(new EBike("TN11E1"), 25 * 60));
        check("T4", "Van 07:30-10:30 (30+30+60)", "120.0",
                () -> String.valueOf(new DeliveryVan("TN10V1").calculateFee(t("07:30"), t("10:30"))));
        check("T5", "Van 13:00-18:00 (4x60 + 30)", "270.0",
                () -> String.valueOf(new DeliveryVan("TN10V1").calculateFee(t("13:00"), t("18:00"))));
        check("T6", "Van 08:59-09:59: hour starts off-peak", "30.0",
                () -> String.valueOf(new DeliveryVan("TN10V1").calculateFee(t("08:59"), t("09:59"))));
        check("T7", "Van 16:59-17:59: hour starts in peak", "60.0",
                () -> String.valueOf(new DeliveryVan("TN10V1").calculateFee(t("16:59"), t("17:59"))));
        check("T8", "Van all day capped at 600", "600.0",
                () -> String.valueOf(new DeliveryVan("TN10V1").calculateFee(t("06:00"), t("23:00"))));
        check("T9", "Visitor one-day pass: Rs 100 at gate, 0 at exit", "100.0/0.0", () -> {
            ParkingService s = service(2);
            s.enter(new Car("TN12C1"), UserCategory.VISITOR, new OneDayPass(), t("08:00"));
            double before = s.getTotalCollection();
            return before + "/" + s.exit("TN12C1", t("18:00"), 0);
        });
        check("T10", "Student buying a one-day pass", "IllegalArgumentException", () -> {
            service(1).enter(new Car("TN15C1"), UserCategory.STUDENT, new OneDayPass(), t("08:00"));
            return "no exception";
        });
        check("T11", "Factory creates registered E_BIKE", "EBike[TN11E1]",
                () -> VehicleFactory.create("E_BIKE", "TN11E1").toString());
        check("T12", "5 floors {3,2,4,1,2}: 12th vehicle -> [4, 1]", "[4, 1]", () -> {
            ParkingFacility f = new ParkingFacility("T", 3, 2, 4, 1, 2);
            int[] last = null;
            for (int i = 0; i < 12; i++) last = f.allocate(new Car("TN" + (10 + i) + "X1"));
            return java.util.Arrays.toString(last);
        });
        check("T13", "13th vehicle on 12-slot facility", "null", () -> {
            ParkingFacility f = new ParkingFacility("T", 3, 2, 4, 1, 2);
            for (int i = 0; i < 12; i++) f.allocate(new Car("TN" + (10 + i) + "X1"));
            return java.util.Arrays.toString(f.allocate(new Car("TN99X9")));
        });

        System.out.println();
        System.out.println("Passed " + passed + " / " + (passed + failed));
    }

    // ---------------- helpers ----------------
    private static ParkingService service(int slots) { return new ParkingService(new ParkingFacility("T", slots)); }
    private static LocalTime t(String hhmm) { return LocalTime.parse(hhmm); }
    private static String fee(Vehicle v, int minutes) { return String.valueOf(v.calculateFee(minutes)); }

    private static void check(String id, String name, String expected, Supplier<String> test) {
        String actual;
        try {
            actual = test.get();
        } catch (RuntimeException e) {
            actual = e.getClass().getSimpleName();
        }
        boolean ok = Objects.equals(expected, actual);
        if (ok) passed++; else failed++;
        System.out.printf("%-4s %-46s %-28s %-28s %s%n", id, name, expected, actual, ok ? "PASS" : "FAIL");
    }
}
