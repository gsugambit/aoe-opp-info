---
sessionId: session-261012-164209-17gm
---

# Requirements

### Overview & Goals
The deprecated `/api/v1/aoe4-opp/game-summary/{gameId}/timeline` endpoint and its associated DTOs and service helper methods are no longer needed now that the single-game analysis endpoint (`GET /{gameId}/analysis`) is fully operational. To maximize maintainability, reduce dead code, and optimize codebase clarity, this task removes the deprecated timeline endpoint, its dedicated DTOs (`TimelineMetricsDto`, `PlayerTimelineDto`, `TimelineDataPointDto`), and all internal parsing methods that exist solely to support it. Any shared helpers used by active endpoints (such as `extractLeaderboard`, `extractMapName`, and `extractDuration`) are strictly preserved.

### Scope
- **In Scope:**
  - Remove `getTimelineMetrics` from `GameSummaryController`.
  - Remove `getTimelineMetrics`, `parseTimelineMetrics` overloads, and exclusive timeline parsing helpers from `GameSummaryService`.
  - Delete unused timeline DTO classes: `TimelineMetricsDto.java`, `PlayerTimelineDto.java`, and `TimelineDataPointDto.java`.
  - Clean up obsolete timeline test cases and imports in `GameSummaryControllerTest` and `GameSummaryServiceTest`.
  - Retain all shared utility methods in `GameSummaryService` utilized by `parseGameAnalysis` and `scrapeMatchup`.
- **Out of Scope:**
  - Changes to `getGameAnalysis` or any other active REST endpoints.
  - Modifying `PlayerSummaryAnalysisService` or matchup analysis logic.

### User Stories
- **As an API maintainer**, I want dead and deprecated timeline code removed from the controller, service, and DTO layers so that the codebase remains clean, focused, and free of unnecessary maintenance overhead.
- **As a client consumer**, I interact with the unified `GET /{gameId}/analysis` endpoint for comprehensive match insights without ambiguity over legacy timeline routes.

### Functional Requirements
- `GameSummaryController`:
  - Remove `@GetMapping("/{gameId}/timeline")` and its handler method `getTimelineMetrics`.
- `GameSummaryService`:
  - Remove `getTimelineMetrics(String gameId)` and `parseTimelineMetrics(...)` methods.
  - Remove private helper methods used exclusively for timeline aggregation: `sortPlayersByOutcome`, `getOutcomeRank`, `extractPlayersTimeline`, `mapToPlayerTimelineDto`, `extractDataPoints`, `isVillagerUnit`, `appendTimestamps`, `countLessOrEqual`, `parseLegacyIntervals`, and timeline-specific JsonNode getters.
  - Preserve `extractLeaderboard`, `extractMapName`, and `extractDuration` required by `parseGameAnalysis`.
- DTO cleanup:
  - Delete `TimelineMetricsDto.java`, `PlayerTimelineDto.java`, and `TimelineDataPointDto.java`.
- Test verification:
  - Remove obsolete timeline tests from `GameSummaryControllerTest` and `GameSummaryServiceTest`.
  - Ensure all remaining test suites pass cleanly.

# Technical Design

### Current Implementation
- `GameSummaryController` retains a `@Deprecated` route `GET /{gameId}/timeline` returning `TimelineMetricsDto`.
- `GameSummaryService` maintains `getTimelineMetrics` and `parseTimelineMetrics` along with over 150 lines of private helper functions for interval bucket and population calculation.
- Three DTO files (`TimelineMetricsDto.java`, `PlayerTimelineDto.java`, `TimelineDataPointDto.java`) exist solely to support this endpoint.
- Existing tests in `GameSummaryControllerTest` and `GameSummaryServiceTest` validate timeline metrics responses.

### Key Decisions
- **Total Dead Code Elimination:** Remove the endpoint, its dedicated DTOs, and exclusive service helpers entirely rather than leaving zombie code in the codebase, maximizing net developer productivity and reducing cognitive burden.
- **Strict Preservation of Shared Utilities:** Retain `extractLeaderboard`, `extractMapName`, and `extractDuration` in `GameSummaryService` since they are shared by `parseGameAnalysis`.

### Proposed Changes
- **Controller Layer (`GameSummaryController.java`):**
  - Delete `getTimelineMetrics(String gameId)` endpoint and remove `TimelineMetricsDto` import.
- **Service Layer (`GameSummaryService.java`):**
  - Delete `getTimelineMetrics(String gameId)` and `parseTimelineMetrics(...)`.
  - Delete exclusive private helper methods (`sortPlayersByOutcome`, `getOutcomeRank`, `extractPlayersTimeline`, `mapToPlayerTimelineDto`, `extractDataPoints`, `isVillagerUnit`, `appendTimestamps`, `countLessOrEqual`, `parseLegacyIntervals`, `getFirstText`, `getFirstInt`, `getFirstInteger`, `findFirstArray`).
  - Remove unused DTO imports.
- **DTO Layer:**
  - Delete `src/main/java/com/theboys/aoe/opp/info/dto/TimelineMetricsDto.java`.
  - Delete `src/main/java/com/theboys/aoe/opp/info/dto/PlayerTimelineDto.java`.
  - Delete `src/main/java/com/theboys/aoe/opp/info/dto/TimelineDataPointDto.java`.
- **Test Layer:**
  - In `GameSummaryControllerTest.java`, remove `testGetTimelineMetrics`.
  - In `GameSummaryServiceTest.java`, remove all `test*TimelineMetrics*` test cases and unused timeline DTO imports.

### File Structure
- `src/main/java/com/theboys/aoe/opp/info/controller/GameSummaryController.java` (Modified)
- `src/main/java/com/theboys/aoe/opp/info/service/GameSummaryService.java` (Modified)
- `src/main/java/com/theboys/aoe/opp/info/dto/TimelineMetricsDto.java` (Deleted)
- `src/main/java/com/theboys/aoe/opp/info/dto/PlayerTimelineDto.java` (Deleted)
- `src/main/java/com/theboys/aoe/opp/info/dto/TimelineDataPointDto.java` (Deleted)
- `src/test/java/com/theboys/aoe/opp/info/controller/GameSummaryControllerTest.java` (Modified)
- `src/test/java/com/theboys/aoe/opp/info/service/GameSummaryServiceTest.java` (Modified)

### Risks & Mitigations
- **Accidental Removal of Shared Methods:** Removing helpers like `extractLeaderboard`, `extractMapName`, or `extractDuration` would break `parseGameAnalysis`. Mitigation: Confirm call hierarchy and usage of all preserved methods before deletion, followed by compiler build checks and test runs.

# Testing

### Validation Approach
Automated testing via JUnit 5 and Mockito, verifying compiler integrity and all test suites pass without regressions.

### Key Scenarios
- **Single Game Analysis Integrity:**
  - Verify `GET /{gameId}/analysis` and `parseGameAnalysis` continue to work flawlessly with `extractLeaderboard`, `extractMapName`, and `extractDuration`.
- **Clean Build and Test Suite:**
  - Verify project compiles with zero references to deleted timeline DTOs.
  - Run `./gradlew test` to ensure all remaining tests pass.

# Delivery Steps

### ✓ Step 1: Remove Timeline Endpoint and Exclusive Service Logic
The deprecated timeline endpoint, exclusive service methods, and timeline DTOs are removed from src/main/java.

- Remove `getTimelineMetrics` method and `TimelineMetricsDto` import from `GameSummaryController.java`.
- Remove `getTimelineMetrics`, `parseTimelineMetrics`, and private helpers (`sortPlayersByOutcome`, `getOutcomeRank`, `extractPlayersTimeline`, `mapToPlayerTimelineDto`, `extractDataPoints`, `isVillagerUnit`, `appendTimestamps`, `countLessOrEqual`, `parseLegacyIntervals`, `getFirstText`, `getFirstInt`, `getFirstInteger`, `findFirstArray`) from `GameSummaryService.java`.
- Retain `extractLeaderboard`, `extractMapName`, and `extractDuration` in `GameSummaryService.java`.
- Delete `TimelineMetricsDto.java`, `PlayerTimelineDto.java`, and `TimelineDataPointDto.java` from `src/main/java/com/theboys/aoe/opp/info/dto/`.

### ✓ Step 2: Clean Up Tests and Validate Build
Obsolete timeline tests are removed and full test suite passes cleanly.

- Remove `testGetTimelineMetrics` and unused imports from `GameSummaryControllerTest.java`.
- Remove timeline test methods (`testGetTimelineMetrics_*`, `testParseTimelineMetrics_*`) and unused imports from `GameSummaryServiceTest.java`.
- Run compiler checks and `./gradlew test` to ensure full test suite success with zero regressions.