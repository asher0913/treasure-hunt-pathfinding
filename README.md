# Treasure Hunt Pathfinding

An interactive Java Swing game that turns shortest-path algorithms into a
gameplay mechanic. A player explores a hidden 20×20 board, avoids obstacles,
collects three treasures, and can spend score points to request the next move
from either A* or breadth-first search.

## Highlights

- A* search with a Manhattan-distance heuristic to the nearest remaining goal.
- Breadth-first search as an uninformed shortest-path baseline.
- Hidden obstacles and treasures, score penalties, turn countdown, pause and
  restart controls, and chess-style grid coordinates.
- Swing UI updates on the Event Dispatch Thread with keyboard actions rather
  than focus-sensitive key listeners.
- JUnit tests compare both algorithms on controlled maps and cover unreachable
  goals and coordinate notation.

## Requirements

- JDK 21
- Maven 3.9+

## Run

```bash
mvn clean compile exec:java
```

Use `W`, `A`, `S`, and `D` to move. Each hint reveals only the next step so the
search implementation assists play without exposing the full route.

## Test

```bash
mvn test
```

The tests reset the randomized board to deterministic fixtures before invoking
the pathfinders.
