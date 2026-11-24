import java.util.HashMap;
import java.util.Map;

/**
 * Task 1 - Alternative concept: vehicles as objects vs. vehicles as parallel String arrays.
 * Shows both designs and measures the cost of finding a vehicle by registration number.
 */
public class RepresentationCompare {

    // ---------- Design A: one object per vehicle ----------
    static class Vehicle {
        final String regNo;
        final String type;
        final int entryMinute;
        Vehicle(String regNo, String type, int entryMinute) {
            this.regNo = regNo; this.type = type; this.entryMinute = entryMinute;
        }
    }

    static Vehicle findObject(Vehicle[] vehicles, int count, String regNo) {
        for (int i = 0; i < count; i++) if (vehicles[i].regNo.equals(regNo)) return vehicles[i];
        return null;
    }

    // ---------- Design B: parallel arrays ----------
    static int findParallel(String[] regNo, int count, String target) {
        for (int i = 0; i < count; i++) if (regNo[i].equals(target)) return i;   // index ties the arrays
        return -1;
    }

    public static void main(String[] args) {
        int n = 5000;
        Vehicle[] objects = new Vehicle[n];
        String[] regNo = new String[n];
        String[] type = new String[n];
        int[] entry = new int[n];
        Map<String, Vehicle> index = new HashMap<>();
        String[] types = {"TWO_WHEELER", "CAR", "EV"};

        for (int i = 0; i < n; i++) {
            String r = String.format("TN%06d", i);
            objects[i] = new Vehicle(r, types[i % 3], 480 + i % 600);
            regNo[i] = r; type[i] = types[i % 3]; entry[i] = 480 + i % 600;
            index.put(r, objects[i]);
        }

        // Same answer from both designs
        String target = "TN004321";
        Vehicle v = findObject(objects, n, target);
        int k = findParallel(regNo, n, target);
        System.out.println("Object design   : " + v.regNo + " " + v.type + " entry " + v.entryMinute);
        System.out.println("Parallel arrays : " + regNo[k] + " " + type[k] + " entry " + entry[k]);

        // Timing (average of many searches over random targets)
        int lookups = 20_000;
        long sink = 0;
        for (int i = 0; i < lookups; i++) sink += findParallel(regNo, n, regNo[i % n]);   // warm-up
        for (int i = 0; i < lookups; i++) sink += findObject(objects, n, regNo[i % n]).entryMinute;

        long t0 = System.nanoTime();
        for (int i = 0; i < lookups; i++) sink += findObject(objects, n, regNo[(i * 7919) % n]).entryMinute;
        long t1 = System.nanoTime();
        for (int i = 0; i < lookups; i++) sink += findParallel(regNo, n, regNo[(i * 7919) % n]);
        long t2 = System.nanoTime();
        for (int i = 0; i < lookups; i++) sink += index.get(regNo[(i * 7919) % n]).entryMinute;
        long t3 = System.nanoTime();

        System.out.printf("%nAverage search time over %d lookups, n = %d vehicles%n", lookups, n);
        System.out.printf("Vehicle[] linear search      : %8.1f ns  O(n)%n", (t1 - t0) / (double) lookups);
        System.out.printf("Parallel arrays linear search: %8.1f ns  O(n)%n", (t2 - t1) / (double) lookups);
        System.out.printf("HashMap<String,Vehicle>      : %8.1f ns  O(1) average%n", (t3 - t2) / (double) lookups);
        if (sink == 42) System.out.println();
    }
}
