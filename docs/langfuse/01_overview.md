# Langfuse Overview

## What Langfuse is

Langfuse is a tool for observing LLM applications.
It shows what the application did in each request.
It shows the model calls, the tool calls, the time of each step and the tokens.

One chat request in an LLM application can contain several model calls and tool calls.
Logs do not show how these steps connect. Langfuse shows them as one tree.
Example from this project: one chat request with a tool contains two model calls and one tool call.

## Trace and span

A trace is the record of one request.
A span is one step inside a trace.
Example: a chat request is one trace. A model call and a tool call are two spans inside it.

## How data reaches Langfuse

Langfuse receives traces over the OpenTelemetry protocol (OTLP).
The application can send them directly or through a collector.
The collector is explained in [02_collector.md](02_collector.md).
