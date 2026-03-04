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

## Architecture Patterns

### Key Principles
- **Package-by-Feature Architecture** - Organized around business domains
- **Service Layer Separation** - Services return domain objects, Controllers map to DTOs
- **Consistent DTO Structure** - Standardized package organization
- **MapStruct Integration** - All mapping done via MapStruct

### Package-by-Feature Structure
```
features/
├── {domain}/               # Business domain (invitation, project, task, etc.)
│   ├── dto/
│   │   ├── service/        # Domain objects returned by services
│   │   ├── mapper/         # All MapStruct mappers
│   │   ├── request/        # Request DTOs
│   │   ├── response/       # Response DTOs
│   │   └── query/          # Query result DTOs
│   ├── {Domain}Service.java
│   ├── {Domain}Repository.java
│   └── {Domain}Controller.java
model/
├── entity/                 # JPA Entities
└── enums/                  # Enumerations
```

### DTO Package Organization
**Each feature follows standardized structure:**
```
dto/
├── service/        # Domain objects for service layer
├── mapper/         # MapStruct mappers (Entity→Domain, Domain→Response)
├── request/        # Input DTOs
├── response/       # Output DTOs
└── query/          # Query result DTOs
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

### Service Layer Architecture
⚠️ **CRITICAL**: Service Layer Rules:
- **Services NEVER return Entity objects or ResponseDTO objects**
- **Services return domain objects from dto/service/ package**
- **Controllers use MapStruct to map domain objects to ResponseDTOs**
- **All mapping done via MapStruct - NO manual mapping**

### Mapping Flow
```
Entity → (EntityMapper) → Domain Object → (ResponseMapper) → ResponseDTO
              ↓                           ↓                      ↓
         dto/mapper/              dto/service/            dto/response/
```

### MapStruct Patterns
**Two types of mappers in each feature:**

1. **Entity to Domain Object** (dto/mapper/):
```java
@Mapper
public interface {Feature}EntityMapper {
    {Feature}EntityMapper INSTANCE = Mappers.getMapper({Feature}EntityMapper.class);
    
    {Domain} to{Domain}({Feature}Entity entity);
}
```

2. **Domain Object to ResponseDTO** (dto/mapper/):
```java
@Mapper
public interface {Feature}Mapper {
    {Feature}Mapper INSTANCE = Mappers.getMapper({Feature}Mapper.class);
    
    {Response}DTO to{Response}DTO({Domain} domain);
}
```

**Usage Pattern:**
- Service: `return {Feature}EntityMapper.INSTANCE.to{Domain}(entity);`
- Controller: `return {Feature}Mapper.INSTANCE.to{Response}DTO(domain);`