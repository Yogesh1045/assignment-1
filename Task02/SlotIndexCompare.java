import java.util.ArrayList;
import java.util.List;

/**
 * Task 2 - Alternative concept: jagged String[floor][slot] vs a flat 1-D array
 * (index = floor * slotsPerFloor + slot) vs ArrayList<String[]>.
 * Floors here have DIFFERENT slot counts to show where the flat formula breaks.
 */
public class SlotIndexCompare {
    public static void main(String[] args) {
        int[] slotsPerFloor = {20, 50, 50, 100, 100};

        // 1. Jagged array - each floor its own length
        String[][] jagged = new String[slotsPerFloor.length][];
        for (int f = 0; f < slotsPerFloor.length; f++) jagged[f] = new String[slotsPerFloor[f]];

        // 2. Flat array - needs a prefix-sum table once floors differ in size
        int total = 0;
        int[] floorStart = new int[slotsPerFloor.length];
        for (int f = 0; f < slotsPerFloor.length; f++) { floorStart[f] = total; total += slotsPerFloor[f]; }
        String[] flat = new String[total];

        // 3. ArrayList<String[]> - floors can be added at run time
        List<String[]> list = new ArrayList<>();
        for (int n : slotsPerFloor) list.add(new String[n]);

        int floor = 3, slot = 7;
        jagged[floor][slot] = "TN01AB1234";
        flat[floorStart[floor] + slot] = "TN01AB1234";
        list.get(floor)[slot] = "TN01AB1234";

        int naiveIndex = floor * 20 + slot;     // WRONG: assumes every floor has 20 slots
        int correctIndex = floorStart[floor] + slot;

        System.out.println("Total slots                    : " + total);
        System.out.println("jagged[3][7]                   : " + jagged[floor][slot]);
        System.out.println("flat[floorStart[3] + 7] = flat[" + correctIndex + "] : " + flat[correctIndex]);
        System.out.println("list.get(3)[7]                 : " + list.get(floor)[slot]);
        System.out.println("naive floor*20+slot = " + naiveIndex + " -> " + flat[naiveIndex]
                + "  (wrong slot: formula only works when all floors are equal)");

        // Timing: 10 million random locates
        int reps = 10_000_000;
        long sink = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < reps; i++) { int f = i % 5; sink += jagged[f][i % slotsPerFloor[f]] == null ? 0 : 1; }
        long t1 = System.nanoTime();
        for (int i = 0; i < reps; i++) { int f = i % 5; sink += flat[floorStart[f] + i % slotsPerFloor[f]] == null ? 0 : 1; }
        long t2 = System.nanoTime();
        for (int i = 0; i < reps; i++) { int f = i % 5; sink += list.get(f)[i % slotsPerFloor[f]] == null ? 0 : 1; }
        long t3 = System.nanoTime();
        System.out.printf("%nLocate a slot (avg of %,d): jagged %.2f ns | flat %.2f ns | ArrayList %.2f ns   (all O(1))%n",
                reps, (t1 - t0) / (double) reps, (t2 - t1) / (double) reps, (t3 - t2) / (double) reps);
        if (sink == -1) System.out.println();
    }
}
