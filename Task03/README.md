# Task 3: Design the Control Logic (4 marks)

## 1. Searching for a free slot

- An **outer `for`** loop goes over floors 0 … F-1. An **inner `for`** loop goes over the slots on that floor (`slots[f].length`, so floors may differ in size).
- `continue` skips an occupied slot straight to the next one.
- When the first free slot is found, the vehicle is placed and `return new int[]{f, s}` **ends both loops at once**. A `break` would only leave the inner loop and need a flag.
- **When the search stops:** at the first free slot (best case O(1)), or after checking all slots on all floors (worst case O(F × S)), when it returns `null`.
- **No free slot:** `allocate` returns `null`. The caller (`ParkingService.enter`) does **not** issue a ticket and reports "Facility full - vehicle turned away". In the full system an `occupied == capacity()` check returns `null` immediately, without scanning.

## 2. Fee slabs with if–else / switch

- **`switch` on vehicle type** chooses the slab values (first hour, each additional hour, daily maximum). Each case ends in `break`, and `default` rejects an unknown type.
- **if–else chain** applies the slabs:
  - `hours == 0` → Rs 0
  - `hours == 1` → first-hour rate
  - otherwise → first hour + (hours - 1) × additional rate
  - then `if (fee > dailyMax) fee = dailyMax`
- **Early `return`** for a pass holder or 0 minutes, so no calculation is done.
- `FeeCalculator.java` implements this slab model. Its output is in `output/FeeCalculator.png`, for example Car 61 min = 30 + 20 = Rs 50, and Car 25 h 1 min = 300 + 50 = Rs 350.

(The full system in Task 11 uses the simpler rule given in the Task 8 code, *rate × started hours*, capped at a daily maximum. The control logic is the same.)

## 3. Pseudocode (SIMATS Engineering standard for algorithmic pseudocode)

### Algorithm 1: ALLOCATE_SLOT

```
ALGORITHM AllocateSlot
INPUT  : slots[0..F-1][ ]  — jagged array; slots[f][s] = NULL means free
         vehicleNo         — registration number of the arriving vehicle
OUTPUT : (floor, slot) of the allocated slot, or NULL if the facility is full

BEGIN
    FOR f ← 0 TO F - 1 DO
        FOR s ← 0 TO LENGTH(slots[f]) - 1 DO
            IF slots[f][s] ≠ NULL THEN
                CONTINUE                         // occupied, try next slot
            END IF
            slots[f][s] ← vehicleNo              // occupy first free slot
            RETURN (f, s)                        // stops both loops
        END FOR
    END FOR
    RETURN NULL                                  // no free slot on any floor
END
```

### Algorithm 2: RELEASE_SLOT

```
ALGORITHM ReleaseSlot
INPUT  : slots[ ][ ], floor, slot
OUTPUT : TRUE if the slot was freed, FALSE otherwise

BEGIN
    IF floor < 0 OR floor ≥ F THEN
        RETURN FALSE                             // invalid floor
    END IF
    IF slot < 0 OR slot ≥ LENGTH(slots[floor]) THEN
        RETURN FALSE                             // invalid slot
    END IF
    IF slots[floor][slot] = NULL THEN
        RETURN FALSE                             // already free
    END IF
    slots[floor][slot] ← NULL
    RETURN TRUE
END
```

### Algorithm 3: COMPUTE_FEE

```
ALGORITHM ComputeFee
INPUT  : vehicleType, minutes (integer ≥ 0), hasPass (boolean)
OUTPUT : fee in rupees (integer)

BEGIN
    IF minutes < 0 THEN
        RAISE ERROR "negative duration"
    END IF
    IF hasPass = TRUE OR minutes = 0 THEN
        RETURN 0
    END IF

    SWITCH vehicleType
        CASE TWO_WHEELER : first ← 10, extra ← 5,  dailyMax ← 60
        CASE CAR, EV     : first ← 30, extra ← 20, dailyMax ← 300
        DEFAULT          : RAISE ERROR "unknown vehicle type"
    END SWITCH

    fullDays   ← minutes DIV 1440
    remMinutes ← minutes MOD 1440
    hours      ← (remMinutes + 59) DIV 60        // ceiling, integer arithmetic only

    IF hours = 0 THEN
        remFee ← 0
    ELSE IF hours = 1 THEN
        remFee ← first
    ELSE
        remFee ← first + (hours - 1) × extra
    END IF

    IF remFee > dailyMax THEN
        remFee ← dailyMax
    END IF

    RETURN fullDays × dailyMax + remFee
END
```

## 4. Complete the Code: `ParkingFacility.java`

`TODO 1` (allocate) and `TODO 2` (release) are completed exactly as the pseudocode above; `main()` is unchanged.

**Output (matches the expected output exactly):**
```
[0, 0]
[0, 1]
[1, 0]
null
true false false
[0, 1]
```
Screenshot: `output/ParkingFacility.png`

Trace: the facility has floor 0 with 2 slots and floor 1 with 1 slot. Three cars fill [0,0], [0,1], [1,0]; the 4th gets `null`. `release(0,1)` → true; the same slot again → false (already free); `release(5,0)` → false (no floor 5). The 4th car is then allocated the freed [0,1].

---

## Alternative Concept and Complexity Analysis: free-count + free-slot stack

**Alternative design** (`FreeSlotStackFacility.java`):
```
freeCount[f]  — number of free slots on floor f
freeStack[f]  — stack of free slot numbers on floor f (lowest on top)

allocate(v):  FOR f ← 0 TO F-1: IF freeCount[f] > 0 THEN s ← POP(freeStack[f]); place; freeCount[f]--; RETURN (f,s)
              RETURN NULL
release(f,s): validate; slots[f][s] ← NULL; PUSH(freeStack[f], s); freeCount[f]++; RETURN TRUE
```

| | Linear scan (given) | Free-count + stack |
|---|---|---|
| `allocate()` worst case | **O(F × S)**: every slot is checked | **O(F)**: full floors skipped by their count, then O(1) pop |
| `allocate()` best case | O(1) | O(1) |
| `release()` | O(1) | O(1) (plus a push) |
| Extra space | **O(1)** | **O(F + F×S)**: one counter per floor plus up to S stack entries per floor |
| Slot order | Always lowest free slot | Lowest free slot only while released slots are pushed back in order. A released slot is reused first (LIFO). |

**My measurement** (5 floors × 100 slots, facility full except the last slot, which is the worst case for scanning): linear scan ≈ 270–310 ns per allocate, stacks ≈ 75–115 ns per allocate (`output/FreeSlotStackFacility.png`). Both versions give identical results for the `main()` test.

**Conclusion:** for this campus (at most 500 slots) the linear scan is fast enough and needs no extra memory or bookkeeping. If the facility grew to many thousands of slots, the stack version would be worth its extra memory.

## Files

| File | Purpose |
|---|---|
| `ParkingFacility.java` | Complete the Code: allocate/release (TODO 1, TODO 2) |
| `FeeCalculator.java` | Fee slabs with switch + if–else, matching Algorithm 3 |
| `FreeSlotStackFacility.java` | Alternative concept with timing comparison |
| `output/` | Captured outputs and screenshots |

**Run:** `javac *.java && java ParkingFacility && java FeeCalculator && java FreeSlotStackFacility`
