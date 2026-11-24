import java.time.LocalTime;

/**
 * Task 4 - classes, objects and constructors.
 * Default / parameterised / overloaded constructors, this(...) chaining, a copy constructor,
 * and the alternatives (setters, static factory) with a timing comparison.
 */
class ParkingTicket {
    private static int counter = 1001;

    private final int ticketNo;
    private final String regNo;
    private final String vehicleType;
    private final int floor;
    private final int slot;
    private final LocalTime entryTime;

    /** Default constructor: placeholder ticket (used only by tests / demos). */
    ParkingTicket() {
        this("UNKNOWN", "CAR", 0, 0);                         // chains to the 4-arg constructor
    }

    /** Overloaded: entry time defaults to "now". */
    ParkingTicket(String regNo, String vehicleType, int floor, int slot) {
        this(regNo, vehicleType, floor, slot, LocalTime.now().withNano(0));
    }

    /** Main parameterised constructor - the only place that validates and assigns. */
    ParkingTicket(String regNo, String vehicleType, int floor, int slot, LocalTime entryTime) {
        if (regNo == null || regNo.isBlank()) throw new IllegalArgumentException("regNo required");
        if (floor < 0 || slot < 0) throw new IllegalArgumentException("bad slot");
        this.ticketNo = counter++;
        this.regNo = regNo;
        this.vehicleType = vehicleType;
        this.floor = floor;
        this.slot = slot;
        this.entryTime = entryTime;
    }

    /** Copy constructor: duplicate of a lost ticket keeps the SAME ticket number. */
    ParkingTicket(ParkingTicket other) {
        this.ticketNo = other.ticketNo;
        this.regNo = other.regNo;
        this.vehicleType = other.vehicleType;
        this.floor = other.floor;
        this.slot = other.slot;
        this.entryTime = other.entryTime;                     // LocalTime is immutable - safe to share
    }

    /** Static factory alternative: a descriptive name instead of another overload. */
    static ParkingTicket forVisitorCar(String regNo, int floor, int slot, LocalTime at) {
        return new ParkingTicket(regNo, "CAR", floor, slot, at);
    }

    int getTicketNo() { return ticketNo; }

    @Override public String toString() {
        return "Ticket#" + ticketNo + " " + regNo + " " + vehicleType + " F" + floor + "-S" + slot + " " + entryTime;
    }
}

/** The "default constructor + setters" alternative - object can exist half-built. */
class MutableTicket {
    private int ticketNo;
    private String regNo;
    private int floor = -1, slot = -1;
    private LocalTime entryTime;

    void setTicketNo(int t) { ticketNo = t; }
    void setRegNo(String r) { regNo = r; }
    void setFloor(int f) { floor = f; }
    void setSlot(int s) { slot = s; }
    void setEntryTime(LocalTime t) { entryTime = t; }
    boolean isValid() { return regNo != null && floor >= 0 && slot >= 0 && entryTime != null; }
}

public class TicketDemo {
    public static void main(String[] args) {
        LocalTime nine = LocalTime.of(9, 0);
        ParkingTicket a = new ParkingTicket("TN01AB1111", "CAR", 0, 3, nine);   // parameterised
        ParkingTicket b = new ParkingTicket("TN02CD2222", "TWO_WHEELER", 1, 0, LocalTime.of(9, 15));
        ParkingTicket c = ParkingTicket.forVisitorCar("TN03EF3333", 2, 5, LocalTime.of(10, 0));
        ParkingTicket dup = new ParkingTicket(a);                                // copy constructor

        System.out.println("a   = " + a);
        System.out.println("b   = " + b);
        System.out.println("c   = " + c + "   (static factory)");
        System.out.println("dup = " + dup + "   (copy of a)");
        System.out.println("a == dup        : " + (a == dup) + "  (two different heap objects)");
        System.out.println("same ticketNo   : " + (a.getTicketNo() == dup.getTicketNo()));

        ParkingTicket alias = a;                                                 // second reference, same object
        System.out.println("alias == a      : " + (alias == a) + "  (one heap object, two stack references)");

        MutableTicket m = new MutableTicket();
        m.setRegNo("TN04GH4444");                     // forgot floor, slot, entry time
        System.out.println("Setter-built ticket valid? " + m.isValid() + "  <- invalid object exists");

        // Timing: create n tickets each way
        int n = 1_000_000;
        long sink = 0;
        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) sink += new ParkingTicket("TN" + (i % 1000), "CAR", i % 5, i % 100, nine).getTicketNo();
        long t1 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            MutableTicket t = new MutableTicket();
            t.setTicketNo(i); t.setRegNo("TN" + (i % 1000)); t.setFloor(i % 5); t.setSlot(i % 100); t.setEntryTime(nine);
            sink += t.isValid() ? 1 : 0;
        }
        long t2 = System.nanoTime();
        for (int i = 0; i < n; i++) sink += ParkingTicket.forVisitorCar("TN" + (i % 1000), i % 5, i % 100, nine).getTicketNo();
        long t3 = System.nanoTime();
        System.out.printf("%nCreate %,d tickets: constructor %.1f ms | setters %.1f ms | factory %.1f ms  (all O(n))%n",
                n, (t1 - t0) / 1e6, (t2 - t1) / 1e6, (t3 - t2) / 1e6);
        if (sink == 42) System.out.println();
    }
}
