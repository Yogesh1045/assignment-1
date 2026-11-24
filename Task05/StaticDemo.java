import java.lang.ref.WeakReference;

/**
 * Task 5 - static members and the object life cycle.
 *  - static counter shared by every facility -> ticket numbers unique campus-wide
 *  - static block loads the rate table once
 *  - instance counter (wrong design) repeats numbers across two facilities
 *  - a closed ticket becomes unreachable and eligible for garbage collection
 */
class Ticket {
    private static int nextNo = 1001;            // ONE copy for the whole class
    private static int issuedToday;

    final int no;
    final String regNo;
    final String gate;

    Ticket(String regNo, String gate) {
        this.no = nextNo++;
        this.regNo = regNo;
        this.gate = gate;
        issuedToday++;
    }

    static int issuedToday() { return issuedToday; }      // static method: no object needed
}

class Facility {
    static final int[] RATE_PER_HOUR;            // class-wide constant table
    static {                                     // static block runs once, when the class loads
        RATE_PER_HOUR = new int[]{10, 30, 30};   // two-wheeler, car, EV
        System.out.println("[static block] rate table loaded once");
    }

    private static int totalOccupancy;           // across ALL facilities
    private final String name;
    private int occupancy;                       // this facility only
    private int localCounter = 1001;             // WRONG design: per-facility counter

    Facility(String name) { this.name = name; }

    Ticket enter(String regNo) {
        occupancy++;
        totalOccupancy++;
        return new Ticket(regNo, name);          // object returned to the caller
    }

    int issueWithInstanceCounter() { return localCounter++; }

    void exit(Ticket t) {                        // object passed in (reference copied)
        occupancy--;
        totalOccupancy--;
    }

    static int totalOccupancy() { return totalOccupancy; }
    int occupancy() { return occupancy; }
}

public class StaticDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("main started");
        Facility north = new Facility("NORTH");
        Facility south = new Facility("SOUTH");

        Ticket t1 = north.enter("TN01AA1111");
        Ticket t2 = south.enter("TN02BB2222");
        Ticket t3 = north.enter("TN03CC3333");
        System.out.println("Static counter     : " + t1.no + " (" + t1.gate + "), " + t2.no + " (" + t2.gate
                + "), " + t3.no + " (" + t3.gate + ")  -> unique campus-wide");

        System.out.println("Instance counter   : NORTH " + north.issueWithInstanceCounter()
                + ", SOUTH " + south.issueWithInstanceCounter() + "  -> SAME number at two gates (bug)");

        System.out.println("Occupancy          : north=" + north.occupancy() + " south=" + south.occupancy()
                + " total(static)=" + Facility.totalOccupancy());
        System.out.println("Tickets issued     : " + Ticket.issuedToday());

        // Object life cycle: after exit, the service drops its reference
        north.exit(t1);
        WeakReference<Ticket> watcher = new WeakReference<>(t1);
        t1 = null;                               // last strong reference gone -> eligible for GC
        for (int i = 0; i < 5 && watcher.get() != null; i++) { System.gc(); Thread.sleep(50); }
        System.out.println("Closed ticket collected by GC? " + (watcher.get() == null));
        System.out.println("Total occupancy after exit    : " + Facility.totalOccupancy());
    }
}
