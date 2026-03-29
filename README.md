# FanDuel Trading Solutions - NFL Depth Charts

This project implements an in-memory Depth Chart Manager for sports teams. It provides a robust, strictly typed, and encapsulated API to add players, remove players, and query backups across various positions.

## 🚀 Architecture & Design Decisions

### 1. The Domain Model & Strict Validation
To guarantee data integrity, `Player` is implemented as an immutable Java 17 `record`. 
* **Identity Management:** The `equals()` and `hashCode()` methods are explicitly overridden to evaluate *only* the player's unique jersey number. This allows the system to accurately identify and remove players without relying on object reference equality.
* **Fail-Fast Instantiation:** A custom `InvalidPlayerException` is thrown at the model level if a player is instantiated with invalid data (e.g., negative numbers, empty strings, or invalid lengths), completely protecting the service layer from bad data.

### 2. Internal Data Structure & Memory Safety
The core state is managed via a `LinkedHashMap<String, List<Player>>`.
* **Deterministic Ordering:** `LinkedHashMap` ensures that when `getFullDepthChart()` is called, positions are printed in the exact order they were initially registered in the system.
* **Encapsulation:** When querying backups via `getBackups()`, returning a direct `List.subList()` would expose a mutable view of the internal depth chart. To prevent encapsulation leaks, the sublist is wrapped in a `new ArrayList<>()` before being returned, ensuring external callers cannot inadvertently modify the internal state.

### 3. Business Logic & Roster Limits
A custom `DepthChartException` governs the strict sports logic of the application. The system automatically enforces:
* **Max Roster Size:** Capped at 53 active players.
* **Max Position Depth:** Capped at 5 players per position.
* Attempting to violate these rules throws a unified domain exception, keeping the core state pristine.

### 4. Overcoming Primitive Obsession
To ensure compile-time safety and prevent runtime typos, raw string inputs for positions and sports are sanitized and validated against strictly defined `Position` and `Sport` Enums before any map lookups occur. 

## 🛠️ How to Build and Run

### Prerequisites
* Java 17 or higher
* Apache Maven

### 1. Building the Project
To compile the source code and download all required dependencies, run:
```bash
mvn clean compile
```

2. Running the Application
To execute the main runner and see the sample FanDuel inputs/outputs in your console:
```bash
mvn exec:java -Dexec.mainClass="com.trading.depthcharts.DepthChartApplication"
```

3. Running Unit Tests
This core engine is heavily fortified by a comprehensive JUnit 5 test suite covering all edge cases, exceptions, and boundary limits. To execute the tests, run:
```bash
mvn test
```

4. Generating the Surefire Test Report
To generate a formatted HTML report of the test executions and coverage, run:
```bash
mvn surefire-report:report
```

After the build finishes, open the target/site/surefire-report.html file in your web browser to view the visual dashboard.

📋 Assumptions Made
Thread Safety: As an in-memory coding challenge, the current DepthChartManager relies on standard collections and is not thread-safe. In a production environment with concurrent user requests, the underlying map would be swapped for a ConcurrentHashMap and CopyOnWriteArrayList, or managed via standard database row-locks.

Data Ingestion Boundary: Processing an external HTML or JSON document to seed the initial data is considered out-of-scope for the core manager to adhere to the Single Responsibility Principle. In a real system, an external adapter or factory class would parse the DOM and pass the sanitized Player records to the manager.

Data Model Integrity over Output Typos: The assignment PDF listed players like Jaelon Darden and Mike Evans occasionally as "WR" or "QB" in the sample outputs, but the data model clearly defined them as "LWR". I assumed the initial data model table was the source of truth, and updated the DepthChartApplication.java runner to use the correct positions.

📝 Notes on Requirement Corrections
While implementing the system and verifying the prompt's Sample Data, the following corrections were applied to the runner logic to ensure technical accuracy:

Position Mapping: The sample output queried backups for players (like Jaelon Darden) under the "QB" position, though they were explicitly registered as "LWR". The DepthChartApplication.java runner correctly targets their registered position keys.

Removal Scope: The sample removal command attempted to remove Mike Evans from "WR", though his registration occurred under "LWR". The system enforces strict matching, so the runner was updated to use the correct "LWR" key.
***