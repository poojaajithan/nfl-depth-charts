# FanDuel - NFL Depth Charts

This project implements an in-memory Depth Chart Manager for sports teams. It provides a robust, strictly typed, and encapsulated API to add players, remove players, and query backups across various positions.

## How to Build and Run
### Prerequisites
Java 17 or higher

Apache Maven

### Building the Project
To compile the project and manage dependencies:

```bash
mvn clean compile
### Running the Automated Tests
As this is a library-style core engine, validation is performed through a comprehensive JUnit 5 test suite. To execute the tests and verify the domain logic:
```

```bash
mvn test
```

## Design Decisions & Assumptions
### 1. The Domain Model (Java 17 Records)
The requirements state: "Assume that a number within the team uniquely identifies that player." To enforce this, Player is implemented as an immutable Java 17 record. The equals() and hashCode() methods were explicitly overridden to evaluate only the player's unique number. This allows the system to accurately identify and remove players without relying on object reference equality.

### 2. Internal Data Structure
The core state is managed via a LinkedHashMap<String, List<Player>>.

O(1) Lookups: Provides near-instant access to any position's depth chart.

Deterministic Ordering: LinkedHashMap ensures that when getFullDepthChart() is called, positions are printed in the exact order they were initially registered in the system.

### 3. Strict Typing for Removal
The prompt requested a removal method that returns the removed player on success, but an empty list on failure. To maintain strict type safety and avoid returning null or raw Object types, removePlayerFromDepthChart returns a List<Player>.

Success: Returns List.of(removedPlayer).

Failure: Returns Collections.emptyList().

### 4. Encapsulation & Memory Safety
When querying backups via getBackups, returning a direct List.subList() would expose a mutable view of the internal depth chart. To prevent encapsulation leaks, the sublist is wrapped in a new ArrayList before being returned, ensuring external callers cannot inadvertently modify the internal state.

### 5. Console Output vs. Logging
To ensure the project remains lightweight and easy to execute in any environment, standard System.out is used for the requested print functionality. In a production environment, this would be replaced with a standard logging facade such as SLF4J.

## Scaling & Architecture
### 1. Composition Over Modification
The DepthChartManager is designed to be sport-agnostic. It purely handles the logic of roster mathematics (insertions, shifting, and removals). To scale this to an entire league (NBA, MLB, NHL):

A LeagueManager would contain a Map<String, Team>.

Each Team entity would encapsulate its own instance of DepthChartManager.
This maintains the Single Responsibility Principle, keeping the core logic isolated from organizational constraints.

### 2. Addressing Primitive Obsession
To adhere strictly to the requested API signatures, positions are passed as raw Strings. In a production setting, I would refactor this to use Interface-driven Enums to enforce compile-time safety and prevent runtime typos:

Java
public interface Position { String name(); }
public enum NFLPosition implements Position { QB, LWR, RB }
public enum NBAPosition implements Position { PG, SG, SF }
## Notes on Requirement Corrections
While implementing the automated unit tests, the following corrections were applied to the sample data logic to ensure technical accuracy:

Position Mapping: The sample output queried backups for players under the "QB" position, though they were registered as "LWR". The unit tests correctly target the specific position keys.

Removal Scope: The sample removal command attempted to remove a player from "WR", though the registration occurred under "LWR". The test suite uses consistent keys to verify the logic correctly.
