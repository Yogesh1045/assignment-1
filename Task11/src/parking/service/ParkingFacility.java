package parking.service;

import parking.model.Vehicle;

/** Floors x slots as a jagged array, so every floor may have its own number of slots. */
public class ParkingFacility {
    private final String name;
    private final Vehicle[][] slots;            // slots[floor][slot] == null -> free
    private int occupied;

    public ParkingFacility(String name, int... slotsPerFloor) {
        if (slotsPerFloor.length == 0) throw new IllegalArgumentException("Need at least one floor");
        this.name = name;
        slots = new Vehicle[slotsPerFloor.length][];
        for (int f = 0; f < slotsPerFloor.length; f++) {
            if (slotsPerFloor[f] <= 0) throw new IllegalArgumentException("Floor " + f + " has no slots");
            slots[f] = new Vehicle[slotsPerFloor[f]];
        }
    }

    /** First free slot, floor by floor. Returns {floor, slot} or null when full. */
    public int[] allocate(Vehicle v) {
        if (occupied == capacity()) return null;            // early exit: nothing to scan
        for (int f = 0; f < slots.length; f++) {
            for (int s = 0; s < slots[f].length; s++) {
                if (slots[f][s] != null) continue;
                slots[f][s] = v;
                occupied++;
                return new int[]{f, s};
            }
        }
        return null;
    }

    public boolean release(int floor, int slot) {
        if (!isValid(floor, slot) || slots[floor][slot] == null) return false;
        slots[floor][slot] = null;
        occupied--;
        return true;
    }

    public boolean isValid(int floor, int slot) {
        return floor >= 0 && floor < slots.length && slot >= 0 && slot < slots[floor].length;
    }

    public Vehicle vehicleAt(int floor, int slot) {
        if (!isValid(floor, slot)) throw new IllegalArgumentException("Invalid slot " + floor + "/" + slot);
        return slots[floor][slot];
    }

    public int capacity() {
        int total = 0;
        for (Vehicle[] floor : slots) total += floor.length;
        return total;
    }

    public int getOccupied() { return occupied; }
    public int getFloors() { return slots.length; }
    public String getName() { return name; }
}
