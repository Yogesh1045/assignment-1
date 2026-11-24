package parking.model;

import java.time.LocalTime;

/**
 * One ticket per stay. Ticket numbers come from a class-wide counter and are never reused.
 * MODIFIED in Task 12: 'boolean monthlyPass' replaced by a Pass object.
 */
public class ParkingTicket {
    private static int nextTicketNo = 1001;

    private final int ticketNo;
    private final Vehicle vehicle;
    private final UserCategory category;
    private final Pass pass;                             // was: boolean monthlyPass
    private final int floor;
    private final int slot;
    private final LocalTime entryTime;
    private LocalTime exitTime;
    private double fee = -1;

    public ParkingTicket(Vehicle vehicle, UserCategory category, Pass pass,
                         int floor, int slot, LocalTime entryTime) {
        if (vehicle == null || category == null || pass == null || entryTime == null)
            throw new IllegalArgumentException("vehicle, category, pass and entry time are required");
        if (floor < 0 || slot < 0)
            throw new IllegalArgumentException("Invalid slot position " + floor + "/" + slot);
        if (!pass.allowedFor(category))
            throw new IllegalArgumentException(category + " cannot hold a " + pass.name() + " pass");
        this.ticketNo = nextTicketNo++;
        this.vehicle = vehicle;
        this.category = category;
        this.pass = pass;
        this.floor = floor;
        this.slot = slot;
        this.entryTime = entryTime;
    }

    public ParkingTicket(Vehicle vehicle, int floor, int slot, LocalTime entryTime) {
        this(vehicle, UserCategory.VISITOR, Pass.none(), floor, slot, entryTime);
    }

    public ParkingTicket(ParkingTicket original) {
        this.ticketNo = original.ticketNo;
        this.vehicle = original.vehicle;
        this.category = original.category;
        this.pass = original.pass;
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
    public Pass getPass() { return pass; }
    public int getFloor() { return floor; }
    public int getSlot() { return slot; }
    public LocalTime getEntryTime() { return entryTime; }
    public LocalTime getExitTime() { return exitTime; }
    public double getFee() { return fee; }

    public void close(LocalTime exitTime, double fee) {
        if (this.exitTime != null) throw new IllegalStateException("Ticket already closed");
        if (exitTime == null) throw new IllegalArgumentException("Exit time required");
        if (fee < 0) throw new IllegalArgumentException("Fee cannot be negative");
        this.exitTime = exitTime;
        this.fee = fee;
    }

    @Override public String toString() {
        return "Ticket#" + ticketNo + " " + vehicle + " " + category + "/" + pass.name()
                + " F" + floor + "-S" + slot + " in " + entryTime
                + (exitTime != null ? " out " + exitTime + " Rs " + fee : "");
    }
}
