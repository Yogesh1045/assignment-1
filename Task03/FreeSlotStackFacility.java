import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Task 3 - Alternative concept: keep a free-slot count per floor and a stack
 * of free slot numbers per floor, so allocate() does not scan occupied slots.
 * Also contains a small timing comparison against the linear-scan ParkingFacility.
 */
public class FreeSlotStackFacility {
    private final String[][] slots;
    private final int[] freeCount;                 // free slots per floor
    private final Deque<Integer>[] freeStack;      // free slot numbers per floor

    @SuppressWarnings("unchecked")
    public FreeSlotStackFacility(int[] slotsPerFloor) {
        int floors = slotsPerFloor.length;
        slots = new String[floors][];
        freeCount = new int[floors];
        freeStack = new Deque[floors];
        for (int f = 0; f < floors; f++) {
            slots[f] = new String[slotsPerFloor[f]];
            freeCount[f] = slotsPerFloor[f];
            freeStack[f] = new ArrayDeque<>();
            for (int s = slotsPerFloor[f] - 1; s >= 0; s--) {
                freeStack[f].push(s);              // lowest slot number on top
            }
        }
    }

    public int[] allocate(String vehicleNo) {
        for (int f = 0; f < slots.length; f++) {   // O(F) - skip full floors immediately
            if (freeCount[f] == 0) continue;
            int s = freeStack[f].pop();            // O(1)
            slots[f][s] = vehicleNo;
            freeCount[f]--;
            return new int[]{f, s};
        }
        return null;
    }

    public boolean release(int floor, int slot) {
        if (floor < 0 || floor >= slots.length) return false;
        if (slot < 0 || slot >= slots[floor].length) return false;
        if (slots[floor][slot] == null) return false;
        slots[floor][slot] = null;
        freeStack[floor].push(slot);               // O(1)
        freeCount[floor]++;
        return true;
    }

    public static void main(String[] args) {
        FreeSlotStackFacility p = new FreeSlotStackFacility(new int[]{2, 1});
        System.out.println(java.util.Arrays.toString(p.allocate("TN01AB1111")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN02CD2222")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN03EF3333")));
        System.out.println(java.util.Arrays.toString(p.allocate("TN04GH4444")));
        System.out.println(p.release(0, 1) + " " + p.release(0, 1) + " " + p.release(5, 0));
        System.out.println(java.util.Arrays.toString(p.allocate("TN04GH4444")));

        // Measurement: fill a 5 x 100 facility completely, then time 10,000 allocate/release
        // pairs on the very last slot (worst case for the linear scan).
        int[] layout = {100, 100, 100, 100, 100};
        System.out.println("\nTiming (5 floors x 100 slots, facility full except the last slot):");
        System.out.printf("Linear scan      : %8.1f ns per allocate%n", timeLinear(layout));
        System.out.printf("Free-slot stacks : %8.1f ns per allocate%n", timeStack(layout));
    }

    private static double timeLinear(int[] layout) {
        ParkingFacility p = new ParkingFacility(layout);
        int total = 0; for (int n : layout) total += n;
        for (int i = 0; i < total; i++) p.allocate("V" + i);
        int lastF = layout.length - 1, lastS = layout[lastF] - 1;
        int rounds = 200_000;
        long start = System.nanoTime();
        for (int i = 0; i < rounds; i++) { p.release(lastF, lastS); p.allocate("X"); }
        return (System.nanoTime() - start) / (double) rounds;
    }

    private static double timeStack(int[] layout) {
        FreeSlotStackFacility p = new FreeSlotStackFacility(layout);
        int total = 0; for (int n : layout) total += n;
        for (int i = 0; i < total; i++) p.allocate("V" + i);
        int lastF = layout.length - 1, lastS = layout[lastF] - 1;
        int rounds = 200_000;
        long start = System.nanoTime();
        for (int i = 0; i < rounds; i++) { p.release(lastF, lastS); p.allocate("X"); }
        return (System.nanoTime() - start) / (double) rounds;
    }
}
