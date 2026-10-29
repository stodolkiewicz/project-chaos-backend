# Decision model evaluators

## Context

A decision model evaluator is the third type of evaluator, next to [LLM-as-a-Judge](02_llm_as_a_judge.md) and [code evaluators](03_code_evaluators.md).
It answers questions of a fixed type instead of generating text with reasoning.
Each question writes one score.

It is usually cheaper than an LLM judge.
Use an LLM judge when you need the reasoning in the `comment` of the score.

## Models

An LLM judge calls a chat model through an **LLM Connection**, for example `gpt-5.4-mini`.
A decision model evaluator calls a decision model, which selects an answer from fixed options.
The documentation names three options:

- an **OpenAI** connection with access to the model `gpt-6-luna`, which Langfuse calls through the OpenAI Decisions API,
- the model Jev from TypeSafe,
- other models that are compatible with the TypeSafe System One API.

## Trying it with `gpt-6-luna`

1. Check in **Settings > LLM Connections** that an OpenAI connection with access to `gpt-6-luna` exists.
2. Open **Evaluators**, click **New evaluator** and choose the card **Decision model**.
3. Select that connection and the model `gpt-6-luna`.
