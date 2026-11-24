package parking.model;

/** A slot position plus its occupant. All fields private; every change goes through a check. */
public class ParkingSlot {
    public static final int MAX_FLOORS = 5;          // public constant: safe, cannot change

    private final int floor;                          // private: callers cannot corrupt it
    private final int number;
    private String occupantRegNo;                     // null = free

    public ParkingSlot(int floor, int number, int slotsOnFloor) {
        if (floor < 0 || floor >= MAX_FLOORS)
            throw new IllegalArgumentException("Invalid floor " + floor + " (0-" + (MAX_FLOORS - 1) + ")");
        if (number < 0 || number >= slotsOnFloor)
            throw new IllegalArgumentException("Invalid slot " + number + " on floor " + floor);
        this.floor = floor;
        this.number = number;
    }

    public int getFloor() { return floor; }
    public int getNumber() { return number; }
    public boolean isFree() { return occupantRegNo == null; }
    public String getOccupantRegNo() { return occupantRegNo; }

    /** Setter with business-rule validation: a slot cannot hold two vehicles. */
    public void occupy(String regNo) {
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("Registration required");
        if (!isFree()) throw new IllegalStateException("Slot F" + floor + "-S" + number + " already holds " + occupantRegNo);
        occupantRegNo = regNo;
    }

    public void free() {
        if (isFree()) throw new IllegalStateException("Slot F" + floor + "-S" + number + " is already free");
        occupantRegNo = null;
    }
}
