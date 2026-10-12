## Package Structure
- config - @Configuration classes for bean/property creation during startup
- controller - controllers for handling REST path/parameter definitions go here
- service - business logic for handling REST requests go into this package. Reuse existing services if that makes sense.
- repository - Database Access Repositories or External HTTP connectors go in this package
- dto - here are DTO (Data Transfer Object) for returning to the UI
- dao - here are DAO (Data Access Object) for deserializing database results into a POJO (Plan Old Java Object)
    - hibernate/Spring Data will use these
- exception - all created exceptions go here
- utils - utility functions (usually static) go here

- 
## Exception Handling

- Custom Exceptions: Create custom domain exception classes extending `RuntimeException`.
- Global Exception Handler: Use `@ControllerAdvice` and `@ExceptionHandler` to handle exceptions globally.
- HTTP Status Codes: Map exceptions to appropriate HTTP status codes in REST controllers.
- Error Response Structure: Define a consistent error response structure

## Testing

- Use JUnit 5 for unit and integration testing.
- Use Mockito for mocking dependencies in unit tests.
- Look at existing patterns in tests and match them where possible
- Use `@SpringBootTest` for integration tests that require the Spring context.
- Use `given/when/then` structure in test methods for clarity.
- Method naming could follow snake_case or camelCaset convention for test methods (e.g., `get_user_by_id_ok`, `get_user_by_id_not_found_ko`).
- Avoid reflection in tests.
- Avoid business logic in tests; focus on behavior verification.
