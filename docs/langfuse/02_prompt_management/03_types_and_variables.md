# Prompt types and variables

## Types

The type is set when the prompt is created. It cannot be changed afterwards.

- A `text` prompt is one string. It fits simple cases, for example a system message.
- A `chat` prompt is a list of messages. Each message has a role (`system`, `user` or `assistant`) and a content.

A `chat` prompt fits these cases:

- A prompt with several messages, for example a `system` instruction and a template of the `user` message.
- Example exchanges (few-shot): pairs of a `user` and an `assistant` message that show the expected style of the answer.
- A chat history inserted by a message placeholder.

The documentation recommends starting with `text` and switching to `chat` when the logic needs several messages.
Example exchanges can also be written as text in a `text` prompt.

## Variables

A variable is a place in a prompt that the application fills at runtime.
The syntax is `{{variable}}`. It works in `text` and in `chat` prompts.
The method `compile` of an SDK replaces the variables with values.

Example: the prompt `As a {{criticLevel}} movie critic, do you like {{movie}}?` compiled with `criticLevel=expert` and `movie=Dune 2` is `As an expert movie critic, do you like Dune 2?`.

Langfuse recognizes only `{{...}}` as variables.
The UI, the Playground and experiments show input fields only for these variables.
An application that uses a different template syntax (for example `{variable}`) replaces the brackets after fetching the prompt.
