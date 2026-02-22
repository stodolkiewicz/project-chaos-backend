# Application Functionalities

## Project Management

```
• Creating projects ✅  
  Choose any number of custom column names for the project

• Dashboard display ✅  
  View current project with columns, tasks and ability to switch between projects

• Switching between projects ✅  
  Navigate between different projects (from dashboard level)

• Projects list view ❌  
  Separate view listing all user projects with roles and detailed information

• Default project management ✅  
  Set and manage default project per user
```

## Task Management

```
• Creating tasks ✅  
  Add tasks to project columns with title, description (requires MEMBER role)

• Deleting tasks ✅  
  Remove tasks from columns (requires MEMBER role)

• Moving tasks ✅  
  Drag tasks between columns (requires MEMBER role)

• Task assignment ✅  
  Assign tasks to project members (during task creation only)

• Task priorities ✅  
  Set priority levels for tasks (during task creation only)

• Task labels ✅  
  Add colored labels to categorize tasks (during task creation only)
```

## User Management & Authentication

```
• Authentication ✅  
  Google OAuth2 login, after which the user gets JWT access and refresh tokens.  

• Token refresh ❌  
  Refresh token functionality not implemented yet
```

## Collaboration Features

```
• Invite users by email ✅  
  Simple invitation system - just type their email (requires ADMIN role)

• Project user management ✅  
  Assign/remove users from projects (requires ADMIN role)
  
• Role-based access ✅  
   VIEWER: read-only access,
   MEMBER: task operations (create/edit/move), 
   ADMIN: project management + user management
```