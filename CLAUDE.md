# Claude Code Configuration

## Project Information
This is a Java Spring Boot backend project for project-chaos.

## Build Commands
- Build: `mvn clean compile`
- Test: `mvn test`  
- Package: `mvn clean package`
- Run: `JWT_SECRET=mysecretkey123456789012345678901234567890 mvn spring-boot:run -Dspring-boot.run.profiles=dev`

## Development Notes
- Maven project with Spring Boot
- Source code in `src/` directory
- Built artifacts in `target/` directory
- **JWT Secret**: Application requires `JWT_SECRET` environment variable to start
- **Test App Startup**: After making changes, ALWAYS test that application starts properly

## Communication Guidelines
- **When asked a question, ALWAYS provide direct answers first**
- **Do NOT suggest code changes or modifications unless explicitly asked**
- **Answer what was asked, not what you think should be changed**

## Testing Protocol
⚠️ **IMPORTANT**: After any code changes, verify application startup with:
```bash
JWT_SECRET=mysecretkey123456789012345678901234567890 mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
Application should show: `"Started ProjectChaos in X.XXX seconds"`

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

## Database Model

### Core Entities & Relationships

#### UserEntity (users table)
- **Primary Key:** `UUID id` (auto-generated)
- **Unique Fields:** `email` (String, @Email validation)
- **Fields:** `firstName`, `lastName`, `role` (RoleEnum), `googlePictureLink`, `lastLogin`
- **Security:** `accountNonExpired`, `accountNonLocked`, `credentialsNonExpired`, `enabled`
- **Relationships:**
  - `@ManyToOne` default_project → ProjectEntity
  - `@OneToMany` projectUsers → Set<ProjectUsersEntity>

#### ProjectEntity (projects table)  
- **Primary Key:** `UUID id`
- **Fields:** `name` (String, required), `description`
- **Relationships:**
  - `@OneToMany` usersWithDefaultProject → List<UserEntity>

#### ProjectUsersEntity (project_users table)
- **Composite Primary Key:** `ProjectUserId` (@EmbeddedId)
  - `UUID projectId` + `UUID userId`
- **Fields:** `projectRole` (ProjectRoleEnum)
- **Relationships:**
  - `@ManyToOne` project → ProjectEntity (@MapsId("project"))
  - `@ManyToOne` user → UserEntity (@MapsId("user"))

#### TaskEntity (tasks table)
- **Primary Key:** `UUID id`
- **Fields:** `title` (required), `description`, `positionInColumn`
- **Relationships:**
  - `@ManyToOne` assignee → UserEntity (assignee_id FK)
  - `@ManyToOne` column → ColumnEntity (column_id FK, required)
  - `@ManyToOne` priority → TaskPriorityEntity (priority_id FK)
  - `@OneToMany` taskLabels → Set<TaskLabelsEntity>

#### ColumnEntity (columns table)
- **Primary Key:** `UUID id`
- **Fields:** `name` (required), `position` (Short)
- **Relationships:**
  - `@ManyToOne` project → ProjectEntity (project_id FK, required)

#### AttachmentEntity (attachments table)
- **Primary Key:** `UUID id`
- **Fields:** `fileName`, `fileUrl`, `uploadedAt`
- **Relationships:**
  - `@ManyToOne` task → TaskEntity (task_id FK, required)
  - `@ManyToOne` uploadedBy → UserEntity (uploaded_by FK)

#### TaskComments (task_comments table)
- **Primary Key:** `UUID id`
- **Relationships:**
  - `@ManyToOne` author → UserEntity (author_id FK, required)

### Key Database Relationships
```
UserEntity (1) ←→ (N) ProjectUsersEntity ←→ (1) ProjectEntity
UserEntity (1) ←→ (N) TaskEntity [assignee]
UserEntity (1) ←→ (N) AttachmentEntity [uploadedBy]
UserEntity (1) ←→ (N) TaskComments [author]
ProjectEntity (1) ←→ (N) ColumnEntity
ColumnEntity (1) ←→ (N) TaskEntity
TaskEntity (1) ←→ (N) AttachmentEntity
```

### Inheritance Hierarchy
- **Auditable**: Base class with `createdDate`, `lastModifiedDate`, `lastModifiedBy`, `version`
  - **Database columns required**: `created_date TIMESTAMP NOT NULL`, `last_modified_date TIMESTAMP`, `last_modified_by VARCHAR(255)`, `version INTEGER`
  - **Spring Data JPA auditing**: Uses `@CreatedDate`, `@LastModifiedDate`, `@LastModifiedBy`, `@Version`
  - Used by: UserEntity, ProjectEntity, ProjectUsersEntity, TaskEntity, AttachmentEntity, InvitationEntity
- **Versioned**: Extends Auditable, adds optimistic locking with `@Version`
  - Used by: ColumnEntity

### Auditable Class Requirements
⚠️ **IMPORTANT**: When creating new entities that extend `Auditable`:
1. **Always add auditing columns to Liquibase migrations**
2. **Column names**: `created_date`, `last_modified_date`, `last_modified_by`, `version`
3. **Types**: TIMESTAMP for dates, VARCHAR(255) for user, INTEGER for version

### Service Layer Rules
⚠️ **CRITICAL**: Service methods must NEVER return Entity objects or xxxResponseDTO objects:
- **Services should return their own service-specific objects** (not DTOs)
- **Never expose JPA entities** outside the service layer
- **RestControllers map service objects to xxxResponseDTOs** using MapStruct
- **Service-to-Controller mapping pattern**: Service returns domain objects → Controller maps to ResponseDTOs

### MapStruct Usage
**Pattern in project:**
```java
@Mapper
public interface EntityMapper {
    EntityMapper INSTANCE = Mappers.getMapper(EntityMapper.class);
    
    ResponseDTO toResponseDTO(Entity entity);
}
```
**Usage:** `EntityMapper.INSTANCE.toResponseDTO(entity)`