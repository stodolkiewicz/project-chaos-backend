# Prompt management

## Context

Prompt management means storing, versioning and retrieving prompts in Langfuse instead of in the application code.
A prompt has a name and a content. The application fetches it by name.
A change of the content needs no commit and no deployment.
The page Prompts in the menu lists the prompts and edits them.

## Fetching a prompt over the public API

The Langfuse SDKs for Python and JavaScript/TypeScript fetch prompts and cache them in memory.
An application without these SDKs calls the REST API:

```
GET https://cloud.langfuse.com/api/public/v2/prompts/{promptName}
```

- The authentication is Basic Auth. The login is the public key of the project (it starts with `pk-lf-`) and the password is the secret key (it starts with `sk-lf-`). The setup is described in [Local setup](../01_intro_and_tracing/03_local_setup.md).
- The endpoint returns exactly one version of the prompt, as JSON. The fields include `name`, `type`, `prompt`, `version` and `labels`.
- The query parameter `label` selects a version by label. The parameter `version` selects a version by number.
- Without parameters the endpoint returns the version with the label `production`.
- The endpoint `GET /api/public/v2/prompts` lists the prompts with their types, labels and version numbers, without the content.

## Caching

The SDKs keep a fetched prompt in memory.
After the first fetch they return the prompt from memory without a network request.
The default time to live (TTL) is 60 seconds. After the TTL the SDK returns the old prompt and refreshes it in the background.
A change in Langfuse reaches the application with a delay of up to the TTL.
An application that calls the API directly has to cache the prompt on its own.
