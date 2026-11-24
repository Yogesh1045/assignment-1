package parking.app;

import parking.model.Car;
import parking.model.ParkingTicket;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Task 11 alternative concept: time to find a ticket on exit when n tickets are stored in
 * an array, an ArrayList (both linear search) or a HashMap keyed by registration number.
 */
public class TicketLookupBenchmark {
    public static void main(String[] args) {
        System.out.printf("%8s %16s %16s %16s%n", "n", "array (ns)", "ArrayList (ns)", "HashMap (ns)");
        for (int n : new int[]{100, 500, 1000, 5000}) {
            ParkingTicket[] array = new ParkingTicket[n];
            List<ParkingTicket> list = new ArrayList<>(n);
            Map<String, ParkingTicket> map = new HashMap<>(n * 2);
            String[] regs = new String[n];
            for (int i = 0; i < n; i++) {
                regs[i] = String.format("TN%06d", i);
                ParkingTicket t = new ParkingTicket(new Car(regs[i]), i / 100, i % 100, LocalTime.NOON);
                array[i] = t; list.add(t); map.put(regs[i], t);
            }
            int lookups = 20_000;
            long sink = 0;
            // warm-up
            for (int i = 0; i < lookups; i++) { sink += findInArray(array, regs[i % n]).getSlot(); }

            long t0 = System.nanoTime();
            for (int i = 0; i < lookups; i++) sink += findInArray(array, regs[(i * 7919) % n]).getSlot();
            long t1 = System.nanoTime();
            for (int i = 0; i < lookups; i++) sink += findInList(list, regs[(i * 7919) % n]).getSlot();
            long t2 = System.nanoTime();
            for (int i = 0; i < lookups; i++) sink += map.get(regs[(i * 7919) % n]).getSlot();
            long t3 = System.nanoTime();

            System.out.printf("%8d %16.1f %16.1f %16.1f%n", n,
                    (t1 - t0) / (double) lookups, (t2 - t1) / (double) lookups, (t3 - t2) / (double) lookups);
            if (sink == 42) System.out.println();   // keep the JIT from removing the loops
        }
    }

    private static ParkingTicket findInArray(ParkingTicket[] a, String reg) {
        for (ParkingTicket t : a) if (t.getVehicle().getRegNo().equals(reg)) return t;
        return null;
    }

    private static ParkingTicket findInList(List<ParkingTicket> l, String reg) {
        for (ParkingTicket t : l) if (t.getVehicle().getRegNo().equals(reg)) return t;
        return null;
    }
}
