# Playground

## Context

The page Playground in the menu sends a prompt to a model and shows the answer.
It tests a prompt in the browser, without running the application.

On the page you set:

- the content of the prompt (the messages `system`, `user` and `assistant`),
- the values of the variables `{{...}}`,
- the model and its parameters, for example the temperature.

Side-by-side comparison runs several prompt variants at once.
Each variant keeps its own model settings, variables, tool definitions and placeholders.

## Opening a prompt

- A prompt from Prompt management opens in the Playground from the page of the prompt.
- A generation from Tracing opens through the button `Open in Playground` on the page of the generation.
- The Playground saves a finished prompt to Prompt management with the save button.

## LLM connection

The Playground calls the model with an API key stored in the project.
This key is an LLM connection. Without it the Playground cannot call a model.

1. Open **Project Settings > LLM Connections**.
2. Click **Add new LLM API key**.
3. Enter the connection name and the credentials of the provider.

The same connection serves LLM-as-a-Judge evaluators.
It does not affect the token prices of ingested traces. Langfuse infers the cost from the model definitions.
The calls of the Playground are billed by the provider of the key.

Supported providers: OpenAI, Azure OpenAI, Anthropic, Google AI Studio, Google Vertex AI and Amazon Bedrock.
The option **Enable default models** makes the default models of the provider available in the model selector.
A model that is not on this list (for example a newer model) is added under **Custom models** in the connection.

## Tool calling and structured output

The Playground does not call the tools of the application.
It supports tool definitions with a JSON schema. The user mocks the response of the tool by hand.
A tool-type observation opens in the Playground only in the OpenAI ChatML format.
Structured output enforces a response format with a JSON schema.

## What the Playground fits

The Playground fits tests of the text of a prompt, for example the tone of the answers and the rules.
For structured regression tests of a prompt on a dataset use Experiments.
