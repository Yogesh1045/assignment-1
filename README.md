# Programming in Java: Assignment 1
## Campus Smart-Parking System: Design, Implement and Validate an Object-Oriented Model

| | |
|---|---|
| **Name** | Yogesh |
| **Register Number** | 192371045 |
| **Course** | CSA09 – Programming in Java |
| **CO** | CO1: Java fundamentals and object-oriented programming |
| **Java version** | OpenJDK 21 (code needs Java 17 or newer: it uses `switch` arrows and `instanceof` pattern matching) |

## Repository layout

One folder per task. Each contains the Java source, any input files, an `output/` folder with the captured console output (`.txt`) and a screenshot (`.png`), and a `README.md` with the written answers (analysis, pseudocode, UML, test tables and the *Alternative Concept and Complexity Analysis*).

| Task | Topic | Marks | Main program(s) |
|---|---|---|---|
| [Task01](Task01) | Problem analysis and objects | 4 | `RepresentationCompare` |
| [Task02](Task02) | Data representation, casting | 3 | `FeeCastingDemo`, `SlotIndexCompare` |
| [Task03](Task03) | Control logic and pseudocode (**Complete the Code**) | 4 | `ParkingFacility`, `FeeCalculator`, `FreeSlotStackFacility` |
| [Task04](Task04) | Classes, objects, constructors | 4 | `TicketDemo` |
| [Task05](Task05) | static members, object life cycle | 3 | `StaticDemo` |
| [Task06](Task06) | Inheritance hierarchy + UML | 4 | `ConstructorTrace` |
| [Task07](Task07) | Method overriding, `final`, equals/hashCode | 4 | `OverrideDemo` |
| [Task08](Task08) | Polymorphism (**Complete the Code**) | 4 | `FeeDemo`, `PolymorphismDemo` |
| [Task09](Task09) | Abstraction: abstract classes and interfaces | 4 | `AbstractionDemo` |
| [Task10](Task10) | Packages, access modifiers, encapsulation | 3 | `parking.app.EncapsulationDemo` |
| [Task11](Task11) | Full implementation, testing, defence | 5 | `parking.app.ParkingApp`, `ParkingTests`, `TicketLookupBenchmark` |
| [Task12](Task12) | Day-4 design challenge (modify) | 8 | `parking.app.ParkingApp`, `ParkingTests` |

## How to compile and run

**Everything at once** (Linux/macOS/Git Bash):
```bash
./run_all.sh
```
This compiles every task into `out/` and saves each program's output to `TaskXX/output/`.

**One task at a time:**

```bash
# Tasks 01–09: single-folder programs
cd Task03
javac *.java
java ParkingFacility

# Tasks 10–12: packaged programs
cd Task11
javac -d out $(find src -name "*.java")          # Windows PowerShell: javac -d out (Get-ChildItem -Recurse src -Filter *.java).FullName
java -cp out parking.app.ParkingApp input/scenario.txt
java -cp out parking.app.ParkingTests
```

Run each program from inside its own task folder, because some of them read input files by relative path (`input.txt`, `input/scenario.txt`).

## Results of each task

| Task | Result |
|---|---|
| 1 | Candidate classes justified; business rules and assumptions listed. Objects vs parallel arrays: both O(n) to search (≈7.3 µs vs ≈8.1 µs for 5,000 vehicles); a HashMap of objects is O(1) (≈0.15 µs). |
| 2 | Jagged array for floors × slots. Integer division under-charges a 130-min car (Rs 60 instead of Rs 90); fixed with `(minutes + 59) / 60`. A flat 1-D index is wrong once floors differ in size. |
| 3 | **Complete the Code output matches the expected output exactly.** Pseudocode for allocate/release/fee. Free-slot stack alternative: O(F) vs O(F×S) allocation, ≈2–4× faster when nearly full. |
| 4 | Constructor chaining with `this(...)`; copy constructor keeps the ticket number for duplicates; the setter alternative allows an invalid object (shown). |
| 5 | Static ticket counter gives unique numbers across two facilities; an instance counter repeats #1001 (shown). Closed ticket shown to be garbage-collected. |
| 6 | Hierarchical + multilevel inheritance; UML diagram; constructor order traced (Vehicle → Car → ElectricCar). |
| 7 | `@Override`, `final` usage; equals/hashCode by registration number; missing hashCode lets a HashSet hold duplicates (shown). |
| 8 | **Complete the Code output matches the expected output exactly**, including `true` on the last line, explained and defended. Dynamic dispatch traced. |
| 9 | Abstract `Vehicle` (state + constructor), interface `Chargeable` (capability), default and static interface methods. |
| 10 | `parking.model` / `parking.service` / `parking.app`; validating setters reject bad values; the `Integer == Integer` pitfall shown. |
| 11 | **25/25 tests pass.** Scenario total **Rs 740**, matching the hand calculation. HashMap ticket lookup O(1) vs O(n) for array/list (measured). |
| 12 | **MODIFY.** 6 classes added, 2 modified, 8 unchanged. **38/38 tests pass** (25 regression + 13 new). Scenario total **Rs 720**, matching the hand calculation. |
