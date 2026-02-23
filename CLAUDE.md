# Claude Code Configuration

## Project Information
This is a Java Spring Boot backend project for project-chaos.

## Build Commands
- Build: `mvn clean compile`
- Test: `mvn test`  
- Package: `mvn clean package`
- Run: `mvn spring-boot:run`

## Development Notes
- Maven project with Spring Boot
- Source code in `src/` directory
- Built artifacts in `target/` directory

## Model Package Architecture Patterns

### Key Principles
- **Feature-based organization** - DTOs grouped by domain and operation
- **Separation of concerns** - Request/Response/Query DTOs separated
- **Consistent naming** - Predictable naming patterns across all layers
- **Domain-driven structure** - Organized around business concepts (project, task, user)

### Package Structure
```
model/
├── dto/           # Data Transfer Objects (42 files)
├── entity/        # JPA Entities (14 files)  
└── enums/         # Enumerations (2 files)
```

### DTO Organization Pattern
DTOs follow **hierarchical functional structure**:
```
dto/{domain}/{operation}/{type}/
├── request/       # Input DTOs
├── response/      # Output DTOs  
└── query/         # Query result DTOs
```

### Naming Conventions

#### Request DTOs
- **Pattern:** `{Operation}{Entity}RequestDTO`
- **Examples:** `CreateProjectRequestDTO`, `UpdateTaskColumnRequestDTO`

#### Response DTOs  
- **Pattern:** `{Operation}{Entity}ResponseDTO` or `{Entity}ResponseDTO`
- **Examples:** `CreateProjectResponseDTO`, `ProjectResponseDTO`, `BoardTasksResponseDTO`

#### Query DTOs
- **Pattern:** `{Entity}QueryResponseDTO` 
- **Examples:** `UserProjectQueryResponseDTO`, `SimpleProjectQueryResponseDTO`