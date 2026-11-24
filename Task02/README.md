# Task 2: Choose the Data Representation (3 marks)

## 1. Data type for each field

| Field | Type | Why |
|---|---|---|
| Floor number, slot number | `int` | Small, whole, used as array indexes, which Java requires to be `int`. |
| Vehicle (registration) number | `String` | Contains letters and digits (TN01AB1234). It is an identifier, not a number, so no arithmetic is done on it. |
| Vehicle type | Subclass (`Car`, `TwoWheeler`…), read from input as a `String` | The type decides behaviour, so it becomes a class (Task 6), not a string compared everywhere. |
| Entry/exit time | `java.time.LocalTime` | Gives clock times with correct hour/minute handling. Avoids storing epoch milliseconds in an `int`, which overflows (shown below). |
| Duration | `int` minutes | Whole minutes are enough. The maximum stay (7 days = 10,080 min) easily fits in an `int`. |
| Billable hours | `int` | Always a whole number: every started hour is charged in full. |
| Rate, fee | `double` | The given code (Task 8) uses `double`. All rates are whole rupees, so products such as 3 × 30.0 are exact. If paise were ever needed, I would switch to `BigDecimal` or a `long` count of paise. |
| kWh charged | `int` | Meter readings are whole kWh in this scenario. |
| Ticket number | `int` (`static` counter) | Increments by 1. Starting at 1001, it would take over 2 billion tickets to overflow. |
| Monthly pass | `boolean` (Task 11) → `Pass` object (Task 12) | Yes/no was enough at first. Task 12 shows why it later became an object. |

## 2. Floors and slots: jagged 2-D array

```java
Vehicle[][] slots = new Vehicle[floors][];
for (int f = 0; f < floors; f++) slots[f] = new Vehicle[slotsPerFloor[f]];
// slots[f][s] == null  -> free
```

- **Why 2-D:** a slot is naturally identified by (floor, slot), the same as on a parking ticket. No index arithmetic is needed.
- **Why jagged and not rectangular:** the layouts grow (1×20 → 3×50 → 5×100). Task 12 adds floors with *different* slot counts. A jagged array gives each floor its own length with no wasted cells.
- **Why not 1-D:** see the alternative analysis below.

## 3. Where casting is needed, and the risk

| Place | Conversion | Risk | How the design avoids it |
|---|---|---|---|
| Minutes → billable hours | int ÷ int | **Integer division truncates**: 130 / 60 = 2, so a started hour is lost | Integer ceiling: `(minutes + 59) / 60` |
| `Math.ceil(minutes / 60)` | int ÷ int, *then* to double | The division has already truncated, so `ceil(2.0)` = 2 | Divide by `60.0` *before* ceil, or use the integer ceiling |
| `Duration.toMinutes()` | `long` → `int` | Narrowing cast could overflow | A stay is capped (one day, or 7 days in `Stay`), well inside `int` range |
| Epoch milliseconds | `long` → `int` | Overflow gives a negative, meaningless time | Times are kept as `LocalTime`, never as `int` milliseconds |
| `hours * ratePerHour()` | int × double → double | Widening, so no loss | Safe |

**Example of a wrong fee** (from `FeeCastingDemo`, see `output/FeeCastingDemo.png`):

| minutes | rate | int division `m/60*r` | `ceil(m/60)` (late cast) | fractional `m/60.0*r` | **correct `(m+59)/60*r`** |
|---|---|---|---|---|---|
| 45 | 10 | **0** | **0** | 7.50 | **10** |
| 61 | 30 | **30** | **30** | 30.50 | **60** |
| 130 | 30 | **60** | **60** | 65.00 | **90** |
| 599 | 30 | **270** | **270** | 299.50 | **300** |

A 130-minute car stay should be 3 started hours = Rs 90. Integer division gives Rs 60, so the university loses Rs 30. A 45-minute two-wheeler stay would be **free**. Fractional hours give Rs 65, which breaks the "every started hour" rule. The integer ceiling formula gets every case right with no floating point.

The same program shows `(int) 1764000000000L` = **-1231558656** (overflow), and `0.1 + 0.2 = 0.30000000000000004`. That is why money is kept in whole rupees.

## 4. Reading input

- **Scanner over a text file** for the full system (`Task11/input/scenario.txt`, read by `ParkingApp`). A day's events are line-based commands, so `Scanner.nextLine()` + `split` is simple, and the file can be replayed to reproduce a test.
- **Command-line arguments** for quick one-off checks: `java FeeCastingDemo 130 30`. `args[i]` are strings, so they are converted with `Integer.parseInt`, which throws `NumberFormatException` on bad input.
- `FeeCastingDemo` uses both: no arguments means it reads `input.txt` with `Scanner.nextInt()`.

---

## Alternative Concept and Complexity Analysis: jagged vs 1-D vs ArrayList

| | Jagged `String[f][s]` | 1-D `String[]`, index = f × S + s | `ArrayList<String[]>` |
|---|---|---|---|
| Locate slot (f, s) | **O(1)**: two array lookups | **O(1)** if every floor has S slots | **O(1)**: `get(f)[s]` |
| Floors with *different* sizes | Works as is | `f × S + s` is **wrong**; needs a prefix-sum table `floorStart[f]` (O(F) to build) | Works as is |
| Space | Σ slots + one array header per floor (O(F + N)) | N cells, 1 header (smallest) + O(F) prefix table | Σ slots + F headers + list overhead |
| Add a floor at run time | Copy the outer array (O(F)) | Copy **all** N cells (O(N)) | `add()`, amortised O(1) |

**What changes when floors differ:** with layout {20, 50, 50, 100, 100}, the naive index for (floor 3, slot 7) is 3 × 20 + 7 = **67**, which is really floor 1, slot 47. That is a different vehicle, so the bug is silent. The flat array needs `index = floorStart[f] + s`, where `floorStart = {0, 20, 70, 120, 220}`. `SlotIndexCompare` shows this: `flat[67]` is `null` while the car is at `flat[127]`.

**My measurement** (10 million random lookups): jagged ≈ 3.6 ns, flat with prefix table ≈ 5.0 ns, ArrayList ≈ 3.7 ns. All are O(1) and the differences are negligible. Correctness and simplicity decide the choice: **jagged array**.

## Files

| File | Purpose |
|---|---|
| `FeeCastingDemo.java` | Wrong vs correct fee calculations; Scanner and command-line input |
| `input.txt` | Input for `FeeCastingDemo` (minutes, rate per line) |
| `SlotIndexCompare.java` | Jagged vs flat vs ArrayList, with the wrong-index example and timing |
| `output/` | Captured outputs and screenshots |

**Run:** `javac *.java && java FeeCastingDemo && java SlotIndexCompare`
