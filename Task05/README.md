# Task 5: Apply static Members and the Object Life Cycle (3 marks)

## 1. Where static variables are needed

| Static member | Where | Why an instance variable would be wrong |
|---|---|---|
| `private static int nextTicketNo = 1001` | `ParkingTicket` | Each ticket would have its own counter starting at 1001, so **every** ticket would be #1001. The number must be shared by the class, not owned by one object. |
| `private static int totalOccupancy` | `Facility` (demo) | Campus-wide occupancy across the north and south facilities. Each facility keeps its own `occupancy` as an instance field; the total belongs to all of them. |
| `static int issuedToday` | `Ticket` (demo) | A count over all tickets ever created, which no single ticket can know. |
| `public static final int MINUTES_PER_HOUR`, `RATE_PER_KWH` | `Vehicle`, `Chargeable` | Constants: one copy shared by everyone, and they never change. |
| `static Map CREATORS` | `VehicleFactory` | One registry of vehicle types for the whole program. |

## 2. Static methods and static blocks

- **Static methods:** `Ticket.issuedToday()`, `Facility.totalOccupancy()`, `ParkingTicket.peekNextTicketNo()`, `VehicleFactory.create(type, regNo)`, `Chargeable.validateKWh()`. They work on class-level data or only on their arguments, so callers should not need an object to use them.
- **Static block:** `Facility` loads its rate table, and `VehicleFactory` registers the built-in vehicle types. Each runs **once**, when the class is first used. The output shows `[static block] rate table loaded once` printed *after* `main started`: the class was loaded lazily, at the first `new Facility(...)`.
- `VehicleFactory` has a **private constructor**. It is a utility class with only static members, so creating an object of it would be meaningless.

## 3. Life cycle of a `ParkingTicket`

1. **Created** by `new` in `ParkingService.enter()` on the heap, and gets the next static number.
2. **Reachable** from the `active` HashMap while the vehicle is parked.
3. **On exit:** removed from `active`, closed (exit time and fee set) and added to the `closed` list for the day's report. It is still reachable, so it is **not** collected.
4. **Eligible for garbage collection** when **no reference remains**: after the day's report, when the `closed` list is cleared or the `ParkingService` itself becomes unreachable. The JVM decides *when* to actually collect it; `System.gc()` is only a request.

`StaticDemo` proves step 4: after `north.exit(t1); t1 = null;` the only remaining reference is a `WeakReference`, and after a GC request it reports `Closed ticket collected by GC? true`.

The ticket *number* is not reused after collection, because the counter is static and only ever goes up.

## 4. How objects are passed and returned

- Java is **always pass-by-value**. For objects, the *value* passed is the **reference**.
- `service.enter(vehicle, ...)` passes a copy of the reference. The service stores it in the ticket, so both point to the **same** `Vehicle` object. That is safe because `Vehicle` is immutable.
- `facility.allocate(v)` **returns** a new `int[]{floor, slot}`: a new array object whose reference is handed back to the caller.
- `enter()` **returns** the `ParkingTicket` reference, so the gate can print it.
- `exit(String regNo, ...)` receives the String reference and returns a primitive `double`, which is copied.
- Reassigning a parameter inside a method (`t = null`) does not affect the caller's variable. Calling a method on it (`t.close(...)`) **does** change the shared object.

---

## Alternative Concept and Complexity Analysis: static counter vs instance counter (two facilities)

**Problem shown in the output:**
```
Static counter     : 1001 (NORTH), 1002 (SOUTH), 1003 (NORTH)  -> unique campus-wide
Instance counter   : NORTH 1001, SOUTH 1001  -> SAME number at two gates (bug)
```
If the counter is an instance variable of `ParkingFacility`, the north and south gates each start at 1001, so two cars hold ticket #1001. Ticket lookup, refunds and audits become ambiguous.

**Solutions:**

| Case | Solution | Space for the counter (k facilities, n tickets) |
|---|---|---|
| Ticket numbers unique across the campus (required) | `static int nextTicketNo` in `ParkingTicket` | **O(1)**: one int, independent of k and n |
| Each facility must number its own tickets | Instance counter **plus a facility prefix**: `"N-1001"`, `"S-1001"`; the ticket key is (facility, number) | **O(k)**: one int per facility |
| Wrong: counter stored in each ticket | Each ticket stores the "next" value | O(n), and still not unique |

Time to issue a number is **O(1)** in every case. If the gates run on separate threads, the static counter should become an `AtomicInteger` (still O(1) time and space) so that two gates never read the same value at the same moment.

**Decision:** a static counter. The rule "a ticket number is never reused" is a campus-wide rule, so the counter belongs to the class.

## Files
- `StaticDemo.java`: static counter vs instance counter, static block, static methods, GC eligibility
- `output/StaticDemo.txt` / `.png`

**Run:** `javac StaticDemo.java && java StaticDemo`
