#!/usr/bin/env bash
# Compiles and runs every task, saving each program's console output to TaskXX/output/<Program>.txt
# Usage:  ./run_all.sh          (needs JDK 17 or newer on PATH)
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"

run() {   # run <task> <main class> [args...]
  local task="$1" main="$2"; shift 2
  local name="${main##*.}"
  mkdir -p "$ROOT/$task/output"
  echo "== $task : $main $*"
  ( cd "$ROOT/$task" && java -cp "$ROOT/out/$task" "$main" "$@" ) 2>&1 \
     | grep -v '^Picked up' | tee "$ROOT/$task/output/$name.txt"
  echo
}

compile() {   # compile <task>
  local task="$1"
  rm -rf "$ROOT/out/$task"; mkdir -p "$ROOT/out/$task"
  javac -d "$ROOT/out/$task" $(find "$ROOT/$task" -name '*.java') 2>&1 | grep -v '^Picked up' || true
}

for t in Task01 Task02 Task03 Task04 Task05 Task06 Task07 Task08 Task09 Task10 Task11 Task12; do
  compile "$t"
done

run Task01 RepresentationCompare
run Task02 FeeCastingDemo
run Task02 SlotIndexCompare
run Task03 ParkingFacility
run Task03 FeeCalculator
run Task03 FreeSlotStackFacility
run Task04 TicketDemo
run Task05 StaticDemo
run Task06 ConstructorTrace
run Task07 OverrideDemo
run Task08 FeeDemo
run Task08 PolymorphismDemo
run Task09 AbstractionDemo
run Task10 parking.app.EncapsulationDemo
run Task11 parking.app.ParkingApp input/scenario.txt
run Task11 parking.app.ParkingTests
run Task11 parking.app.TicketLookupBenchmark
run Task12 parking.app.ParkingApp input/scenario.txt
run Task12 parking.app.ParkingTests
