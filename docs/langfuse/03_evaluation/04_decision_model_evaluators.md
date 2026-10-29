# Decision model evaluators

## Context

A decision model is a model that answers typed questions instead of generating free text.
A decision model evaluator asks it typed questions about an observation.
Each question writes one score.
It is usually cheaper than [LLM-as-a-Judge](02_llm_as_a_judge.md), but it returns no written reasoning.
Use an LLM judge when you need the reasoning and a [code evaluator](03_code_evaluators.md) for deterministic checks.

## Question types

| Type         | Langfuse score                                                | Example                                                              |
|--------------|---------------------------------------------------------------|----------------------------------------------------------------------|
| **Choice**   | categorical score with the selected option                    | "Which team should handle this ticket?" gives `billing`, `technical` or `sales` |
| **Score**    | numeric score with a level from `0` to `levels - 1`           | "How frustrated is the customer?" gives `0` (calm) to `3` (angry)    |
| **Yes / no** | numeric score with the probability `P(true)` from `0` to `1`  | "Does the message request a refund?" gives `0.93`                    |

A Yes / no question returns a probability, not a boolean.
Choose a threshold when you filter or alert, for example `>= 0.7`.
Use a Choice question with the options `yes` and `no` when you need a boolean-like label.

Keep each question atomic: one decision per question.
One evaluator can ask 1 to 50 questions, and all of them run in one call.

## Creating the evaluator

1. Open **Evaluators > New evaluator** and choose **New decision model evaluator**.
2. Select a connection and a model. Add the connection under **Settings > LLM Connections** when it does not exist.
3. Add the questions: type, instruction, allowed answers and score name.
4. Map the observation data that the model evaluates.
5. Test on a sample observation. The panel shows one row per question with its probability distribution.
6. Save the evaluator and attach it to a rule.

## Providers

The state is the data that the questions evaluate.

- **OpenAI.** The documentation names the model `gpt-6-luna` and the OpenAI Decisions API. The data is mapped to one state named `input`, and a question evaluates that state as a whole.
- **TypeSafe Jev.** Jev is a decision model of the company TypeSafe. It cannot generate text. The connection uses the adapter `typesafe` and an upstream, which is the service that serves Jev: TypeSafe, Vercel AI Gateway or OpenRouter. The state is a JSON object built from the input, the output, the metadata, the tool calls, the expected output and the experiment item metadata. Jev score levels need a description.
- **Other models of the System One API.** System One is the API of TypeSafe for decision models. Other vendors implement it too. Such a model works through a custom connection with the base URL of the vendor.

OpenAI can refuse a question. One refusal fails the whole run and no scores are written.
Jev always picks an answer, so add an option such as `other` or `unclear` for thin input.

## Limits

| Limit                   | Value                                                      |
|-------------------------|------------------------------------------------------------|
| Questions per evaluator | 1 to 50                                                    |
| Choice options          | 2 to 255 with unique values                                |
| Score levels            | 2 to 10                                                    |
| Input size (TypeSafe)   | about 32k tokens for the state plus the longest question   |

The input and the questions are sent through the selected connection.
Review the terms of your provider before you send sensitive data.

## Debugging

Every run creates a trace named `Execute evaluator: <evaluator name>` in the internal environment `langfuse-llm-as-a-judge`.
Internal environments are hidden from the default Tracing view.
Filter by the environment or open the trace from a score.
The probabilities and the confidence are stored in the metadata of the score (`metadata.openai` or `metadata.typesafe`), not in its value.
