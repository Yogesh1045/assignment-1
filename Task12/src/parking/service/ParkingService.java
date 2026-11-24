package parking.service;

import parking.model.Chargeable;
import parking.model.ParkingTicket;
import parking.model.Pass;
import parking.model.UserCategory;
import parking.model.Vehicle;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entry and exit processing.
 * MODIFIED in Task 12: enter() takes a Pass instead of a boolean; exit() asks the Pass for the
 * payable amount; a pass bought at the gate is added to the day's collection.
 */
public class ParkingService {
    private final ParkingFacility facility;
    private final Map<String, ParkingTicket> active = new HashMap<>();
    private final List<ParkingTicket> closed = new ArrayList<>();
    private double totalCollection;

    public ParkingService(ParkingFacility facility) { this.facility = facility; }

    public ParkingTicket enter(Vehicle v, UserCategory category, Pass pass, LocalTime at) {
        if (active.containsKey(v.getRegNo()))
            throw new IllegalStateException("Duplicate registration already parked: " + v.getRegNo());
        if (!pass.allowedFor(category))
            throw new IllegalArgumentException(category + " cannot hold a " + pass.name() + " pass");
        int[] pos = facility.allocate(v);
        if (pos == null) throw new IllegalStateException("Facility full - " + v.getRegNo() + " turned away");
        ParkingTicket t = new ParkingTicket(v, category, pass, pos[0], pos[1], at);
        active.put(v.getRegNo(), t);
        totalCollection += pass.purchasePrice();         // one-day pass paid at the gate
        return t;
    }

    public double exit(String regNo, LocalTime at, int kWhCharged) {
        ParkingTicket t = active.get(regNo);
        if (t == null) throw new IllegalArgumentException("No active ticket for " + regNo);
        if (at == null) throw new IllegalArgumentException("Exit time required");

        Vehicle v = t.getVehicle();
        double parking = t.getPass().apply(v.calculateFee(t.getEntryTime(), at));   // two dispatches
        double charging = 0;
        if (kWhCharged > 0) {
            if (!(v instanceof Chargeable c))
                throw new IllegalArgumentException(regNo + " is not chargeable");
            charging = c.chargingFee(kWhCharged);
        }
        double fee = parking + charging;
        t.close(at, fee);
        active.remove(regNo);
        facility.release(t.getFloor(), t.getSlot());
        closed.add(t);
        totalCollection += fee;
        return fee;
    }

    public ParkingTicket activeTicket(String regNo) { return active.get(regNo); }
    public int activeCount() { return active.size(); }
    public double getTotalCollection() { return totalCollection; }
    public List<ParkingTicket> closedTickets() { return Collections.unmodifiableList(closed); }
    public ParkingFacility getFacility() { return facility; }
}
