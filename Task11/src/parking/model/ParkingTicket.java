package parking.model;

import java.time.LocalTime;

/** One ticket per stay. Ticket numbers come from a class-wide counter and are never reused. */
public class ParkingTicket {
    private static int nextTicketNo = 1001;              // shared by all tickets (Task 5)

    private final int ticketNo;
    private final Vehicle vehicle;
    private final UserCategory category;
    private final boolean monthlyPass;
    private final int floor;
    private final int slot;
    private final LocalTime entryTime;
    private LocalTime exitTime;                          // set once, on exit
    private double fee = -1;                             // -1 = not yet computed

    /** Full constructor - every other constructor chains here. */
    public ParkingTicket(Vehicle vehicle, UserCategory category, boolean monthlyPass,
                         int floor, int slot, LocalTime entryTime) {
        if (vehicle == null || category == null || entryTime == null)
            throw new IllegalArgumentException("vehicle, category and entry time are required");
        if (floor < 0 || slot < 0)
            throw new IllegalArgumentException("Invalid slot position " + floor + "/" + slot);
        if (monthlyPass && !category.canHoldMonthlyPass())
            throw new IllegalArgumentException("Visitors cannot hold a monthly pass");
        this.ticketNo = nextTicketNo++;
        this.vehicle = vehicle;
        this.category = category;
        this.monthlyPass = monthlyPass;
        this.floor = floor;
        this.slot = slot;
        this.entryTime = entryTime;
    }

    /** Overloaded: a visitor with no pass. */
    public ParkingTicket(Vehicle vehicle, int floor, int slot, LocalTime entryTime) {
        this(vehicle, UserCategory.VISITOR, false, floor, slot, entryTime);
    }

    /** Copy constructor: re-issues a duplicate of a lost ticket with the SAME ticket number. */
    public ParkingTicket(ParkingTicket original) {
        this.ticketNo = original.ticketNo;               // duplicate keeps its number
        this.vehicle = original.vehicle;                 // Vehicle is immutable: sharing is safe
        this.category = original.category;
        this.monthlyPass = original.monthlyPass;
        this.floor = original.floor;
        this.slot = original.slot;
        this.entryTime = original.entryTime;
        this.exitTime = original.exitTime;
        this.fee = original.fee;
    }

    public static int peekNextTicketNo() { return nextTicketNo; }

    public int getTicketNo() { return ticketNo; }
    public Vehicle getVehicle() { return vehicle; }
    public UserCategory getCategory() { return category; }
    public boolean hasMonthlyPass() { return monthlyPass; }
    public int getFloor() { return floor; }
    public int getSlot() { return slot; }
    public LocalTime getEntryTime() { return entryTime; }
    public LocalTime getExitTime() { return exitTime; }
    public double getFee() { return fee; }

    /** Closes the ticket. Validates that it is not closed twice and that the fee is not negative. */
    public void close(LocalTime exitTime, double fee) {
        if (this.exitTime != null) throw new IllegalStateException("Ticket already closed");
        if (exitTime == null) throw new IllegalArgumentException("Exit time required");
        if (fee < 0) throw new IllegalArgumentException("Fee cannot be negative");
        this.exitTime = exitTime;
        this.fee = fee;
    }

    @Override public String toString() {
        return "Ticket#" + ticketNo + " " + vehicle + " " + category
                + (monthlyPass ? "(pass)" : "") + " F" + floor + "-S" + slot + " in " + entryTime
                + (exitTime != null ? " out " + exitTime + " Rs " + fee : "");
    }
}
