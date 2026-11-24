# Task 4: Design Classes, Objects and Constructors (4 marks)

## 1. Fields and methods of each class (full system, `Task11/src`)

| Class | Fields | Methods |
|---|---|---|
| `Vehicle` (abstract) | `private final String regNo` | `getRegNo()`, `ratePerHour()` (abstract), `dailyMaximum()`, `calculateFee(int)`, `calculateFee(LocalTime, LocalTime)`, `equals`, `hashCode`, `toString` |
| `TwoWheeler`, `Car` | none (inherit) | `ratePerHour()` |
| `ElectricCar` | none | `chargingFee(int kWh)` |
| `ParkingTicket` | `static int nextTicketNo`; `final int ticketNo`; `final Vehicle vehicle`; `final UserCategory category`; `final boolean monthlyPass`; `final int floor, slot`; `final LocalTime entryTime`; `LocalTime exitTime`; `double fee` | 3 constructors, getters, `close(exitTime, fee)`, `toString`, `static peekNextTicketNo()` |
| `ParkingFacility` | `final String name`; `final Vehicle[][] slots`; `int occupied` | `allocate`, `release`, `isValid`, `vehicleAt`, `capacity`, getters |
| `ParkingService` | `final ParkingFacility facility`; `final Map<String, ParkingTicket> active`; `final List<ParkingTicket> closed`; `double totalCollection` | `enter`, `exit`, `activeTicket`, `activeCount`, `getTotalCollection` |

## 2. Constructors and when each is used

`TicketDemo.java` (this folder) shows each kind in isolation:

| Constructor | Signature | Used when |
|---|---|---|
| Default | `ParkingTicket()` | Only for placeholder or demo objects. It chains to the full constructor with safe defaults, so even this object is valid. The real system has **no** default constructor, because a ticket without a vehicle or slot is meaningless. |
| Overloaded (4 args) | `ParkingTicket(regNo, type, floor, slot)` | Live gate entry: entry time = now |
| Parameterised (5 args) | `ParkingTicket(regNo, type, floor, slot, entryTime)` | Replaying a log or testing with fixed times. **This is the only constructor that validates and assigns.** |
| Copy | `ParkingTicket(ParkingTicket other)` | Re-issuing a duplicate for a lost ticket |
| Static factory | `ParkingTicket.forVisitorCar(regNo, floor, slot, at)` | A named shortcut for a common case |

In the full system (`Task11/.../ParkingTicket.java`), the 6-argument constructor is the main one. `ParkingTicket(vehicle, floor, slot, entryTime)` chains to it with `VISITOR` and no pass.

## 3. `this` and constructor chaining

```java
ParkingTicket() { this("UNKNOWN", "CAR", 0, 0); }                         // → 4-arg
ParkingTicket(String r, String t, int f, int s) { this(r, t, f, s, LocalTime.now()); } // → 5-arg
ParkingTicket(String r, String t, int f, int s, LocalTime at) { /* validate + assign once */ }
```
- `this(...)` must be the **first statement** and calls another constructor of the same class.
- `this.regNo = regNo` tells the field apart from the parameter with the same name.
- **Why chain:** the validation and the `counter++` exist in one place only. Without chaining, each constructor would copy them, and a later fix could be applied to one copy and missed in another.

## 4. Is a copy constructor needed?

**Yes: for a duplicate (lost) ticket.** The duplicate must carry the **same ticket number**. Calling the normal constructor would consume a *new* number from the counter and break "a ticket number is never reused or skipped". The copy constructor copies the number directly and does **not** touch the counter.

It is a **shallow copy**, which is safe here: `Vehicle` and `LocalTime` are immutable (`final` fields, no setters), so sharing them cannot let one ticket change the other.

Output: `dup = Ticket#1001 ... (copy of a)`, `a == dup : false`, `same ticketNo : true`.

## 5. How objects are stored in memory

```java
ParkingTicket a = new ParkingTicket("TN01AB1111", "CAR", 0, 3, nine);
```
- `a` is a **reference variable** on the **stack** (in `main`'s stack frame). It holds only an address.
- `new` creates the object on the **heap**: header, `ticketNo` (int stored inline), and references to the `String` and `LocalTime` objects, which are also on the heap.
- `ParkingTicket alias = a;` copies the **reference**, not the object: `alias == a` is `true` (one object, two references).
- `new ParkingTicket(a)` creates a **second heap object**: `a == dup` is `false`.
- The `static counter` is not in any object; it lives once in the class's area (metaspace / class data).
- When `main` returns, its stack frame disappears. Heap objects with no references left become eligible for garbage collection (Task 5).

```
 STACK (main)              HEAP
 ┌──────────┐      ┌──────────────────────────────┐
 │ a     ───┼────► │ ParkingTicket#1001            │──► "TN01AB1111"
 │ alias ───┼────► │  floor=0 slot=3 entry=09:00 ──┼──► LocalTime 09:00
 │ dup   ───┼──┐   └──────────────────────────────┘        ▲
 └──────────┘  └─► ┌──────────────────────────────┐        │ shared
                   │ ParkingTicket#1001 (copy)    │────────┘
                   └──────────────────────────────┘
```

---

## Alternative Concept and Complexity Analysis: constructors vs setters vs factory

**Setter approach:**
```java
MutableTicket t = new MutableTicket();
t.setRegNo("TN04GH4444");   // forgot floor, slot, entryTime
// t exists and is INVALID; nothing stopped it
```
**Static factory approach:**
```java
static ParkingTicket forVisitorCar(String r, int f, int s, LocalTime at) { return new ParkingTicket(r, "CAR", f, s, at); }
```

| | Overloaded constructors + chaining | Default constructor + setters | Static factory |
|---|---|---|---|
| Valid object guaranteed? | **Yes**: validation runs before the object is usable; fields can be `final` | **No**: object exists half-built between calls; fields cannot be `final`; any setter can be skipped (`isValid()` = **false** in the demo) | **Yes**: it calls a validating constructor; it can also return a cached object or a subclass |
| Immutability | Possible (`final` fields) | Not possible | Possible |
| Readability | Long argument lists are easy to mix up | Named setters are clear | **Named** methods (`forVisitorCar`) are the clearest |
| Time for n objects | O(n): 1 call each | O(n): 1 + k calls each (k setters) | O(n): 2 calls each (factory + constructor), usually inlined by the JIT |
| Space for n objects | O(n), same object size | O(n), same size | O(n), same size |

**My measurement** (1,000,000 tickets): constructor ≈ 77 ms, setters ≈ 33 ms, factory ≈ 42 ms (`output/TicketDemo.png`). All are linear. The constructor version is not "slow": it does more work (validation, `static` counter increment, and the first run includes JIT warm-up). The setter version is faster only because it **skips validation**, and that is exactly the problem. The asymptotic cost is the same, so the design choice rests on **safety**: constructors (and a factory for named cases).

## Files
- `TicketDemo.java`: constructors, chaining, copy constructor, factory, setter alternative, timing
- `output/TicketDemo.txt` / `.png`

**Run:** `javac TicketDemo.java && java TicketDemo`
