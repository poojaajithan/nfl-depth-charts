# FanDuel Trading Solutions - Multi-Sport Depth Charts

This project implements an in-memory, multi-sport Depth Chart Manager. It provides a robust, strictly typed, and encapsulated API to add players, remove players, and query backups across various leagues (e.g., NFL, NBA, MLB).

## 🚀 Architecture & Design Decisions

### 1. The Domain Model & Strict Validation
To guarantee data integrity, `Player` is implemented as an immutable Java 17 `record`. 
* **Fail-Fast Validation:** The `Player` record enforces strict validation at the moment of instantiation. This ensures that no "invalid" player objects can exist within the system. Key constraints include:
    * **Jersey Numbers:** Must be within the standard range (0-99).
    * **Name Integrity:** Names cannot be null, blank, and must be between 2 and 50 characters.
    * **Auto-Formatting:** Input names are automatically converted to "Proper Case" (e.g., "tom brady" becomes "Tom Brady") to ensure consistent console output and searchability.
* **Identity Management:** The `equals()` and `hashCode()` methods are explicitly overridden to evaluate *only* the player's unique jersey number.
* **Fail-Fast Instantiation:** A custom InvalidPlayerException is thrown at the model level if a player is instantiated with invalid data.
* **Team Identity & Metadata:** The DepthChartManager is initialized with both a teamNameShort (e.g., "TB") for concise logging and a teamNameLong (e.g., "Tampa Bay Buccaneers") for formal reporting, ensuring clear ownership of the data.
* **Constructor Guardrails:** The DepthChartManager constructor acts as a domain "bouncer," throwing a DepthChartException if initialized with null sports, invalid team name lengths, or blank strings.
* **Global Roster Integrity:** The system ensures that players assigned to multiple positions (e.g., a player starting at both LT and RT) are only counted once against the global maximum roster limit by dynamically calculating distinct player instances.

### 2. Internal Data Structure & Memory Safety
The core state is managed via a `LinkedHashMap<String, List<Player>>`.
* **Deterministic Ordering:** `LinkedHashMap` ensures that when `getFullDepthChart()` is called, positions are printed in the exact order they were initially registered in the system.
* **Encapsulation:** When querying backups via `getBackups()`, returning a direct `List.subList()` would expose a mutable view of the internal depth chart. To prevent encapsulation leaks, the sublist is wrapped in a `new ArrayList<>()` before being returned, ensuring external callers cannot inadvertently modify the internal state.

### 3. Dynamic Business Logic (Open/Closed Principle)
The system is built to scale beyond the NFL. By utilizing a data-driven Sport Enum, the application automatically enforces limits dynamically:

* **Dynamic Roster Size:** Limits (e.g., 53 for NFL) are fetched from the Enum, allowing the same service to support MLB or NBA without code changes.
* **Position Depth:** Maximum depth per position is validated against the specific sport's requirements.

Attempting to violate these rules throws a unified DepthChartException.

### 4. Overcoming Primitive Obsession & Scaling Sports
To ensure compile-time safety and prevent runtime typos, raw string inputs for positions are sanitized and validated against a strictly defined set of valid positions encapsulated within the Sport Enum itself. This prevents the "God Enum" anti-pattern; instead of a single massive Position enum holding every sport's positions, the Sport.NFL configuration independently knows to accept "QB" and reject "PG".

## 🛠️ How to Build and Run

### Prerequisites
* Java 17 or higher
* Apache Maven

### 1. Building the Project
To compile the source code and download all required dependencies, run:
```bash
mvn clean compile
```

### 2. Running the Application
To execute the main runner and see the sample FanDuel inputs/outputs in your console:
```bash
mvn exec:java -Dexec.mainClass="com.trading.depthcharts.DepthChartApplication"
```

### 3. Running Unit Tests
This core engine is heavily fortified by a comprehensive JUnit 5 test suite covering all edge cases, exceptions, and boundary limits. To execute the tests, run:
```bash
mvn test
```

### 4. Generating the Surefire Test Report
To generate a formatted HTML report of the test executions and coverage, run:
```bash
mvn surefire-report:report
```

After the build finishes, open the ```target/site/surefire-report.html``` file in your web browser to view the visual dashboard.

## 📋 Assumptions Made

1. **Thread Safety:** As an in-memory coding challenge, the current `DepthChartManager` relies on standard collections and is not thread-safe. In a production environment with concurrent user requests, the underlying map would be swapped for a `ConcurrentHashMap` and `CopyOnWriteArrayList`, or managed via standard database row-locks.
2. **Data Ingestion Boundary:** Parsing the provided HTML to seed initial data was treated as out-of-scope to adhere to the Single Responsibility Principle. The system is designed as an API that accepts clean Player objects.
3. **Data Model Integrity over Output Typos:** The assignment PDF listed players like Jaelon Darden and Mike Evans occasionally as "WR" or "QB" in the sample outputs, but the data model clearly defined them as "LWR". I assumed the initial data model table was the source of truth, and updated the `DepthChartApplication.java` runner to use the correct positions.
4. **Team Identity Validation:** To avoid hardcoding all 32 NFL franchises into a static Enum, the system relies on constructor validation for basic string integrity. It is assumed that in a production environment, team identity strings are validated upstream by the database or calling service.

## 📝 Notes on Requirement Corrections

While implementing the system and verifying the prompt's Sample Data, the following corrections were applied to the runner logic to ensure technical accuracy:

1. **Position Mapping:** The sample output queried backups for players (like Jaelon Darden) under the `"QB"` position, though they were explicitly registered as `"LWR"`. The `DepthChartApplication.java` runner correctly targets their registered position keys.
2. **Removal Scope:** The sample removal command attempted to remove Mike Evans from `"WR"`, though his registration occurred under `"LWR"`. The system enforces strict matching, so the runner was updated to use the correct `"LWR"` key.