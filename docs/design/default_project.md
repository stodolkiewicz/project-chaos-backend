## Default Project

Table 'users' has default_project_id UUID column.  

The backend exposes `GET /api/v1/projects/default`, which returns the `projectId` of the user's default project.

When user accesses dashboard, it gets the default_project_id using the above endpoint.
If user has no projects then a "Create your first project" dialog is shown.

default_project_id changes when:
 - user creates their first project (endpoint `POST /api/v1/projects`)
 - user switches to a different project (endpoint `PATCH /api/v1/users/default-project`)
