# Langfuse MCP Servers

An MCP server gives a coding agent extra tools.
Langfuse offers two MCP servers.

## Langfuse Docs MCP

This public server provides the Langfuse documentation.
It needs no API key.
The agent reads current docs instead of guessing.

```bash
claude mcp add --scope user --transport http langfuse-docs https://langfuse.com/api/mcp
```

## Langfuse MCP (platform)

This server gives the agent access to the data of one Langfuse project, for example prompts.
Each API key belongs to one Langfuse project.
Use `--scope local`, so that the server is available only in the current repository.

```bash
TOKEN=$(echo -n "pk-lf-...:sk-lf-..." | base64 -w0)
claude mcp add --transport http langfuse https://cloud.langfuse.com/api/public/mcp \
  --header "Authorization: Basic $TOKEN"
```
