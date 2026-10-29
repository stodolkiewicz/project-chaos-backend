---
name: notes-review
description: Review a Markdown lesson note in docs/ against the chat lessons and the writing rules, then apply the fixes. Use when Dawid asks for a review of notes or docs files.
---

# Notes review

Review the note files that Dawid names, or the files in `docs/` that changed in this session.
Work through the steps in order.
Do not start a step before the previous one is checked off in the progress file.

## General rules

These rules apply in every step.

- Do exactly what Dawid asked. Add no unrequested sections, suggestions or refactors.
- Write every sentence as a full sentence with a verb. Do not write verbless fragments.
- Use short, single-clause sentences.
- Let every sentence carry concrete information. Remove obvious statements and filler.
- State the working approach. Do not describe what is impossible, unsupported or absent.
- Do not use a term or API before saying what it does.
- Keep technical terms in English (judge, prompt, score, evaluator, rule, trace, span, dataset).
- Write the notes in English and the chat report in Polish.
- Keep Dawid's email and user ids out of notes and screenshots.
- Save nothing in the Langfuse project.
- Ask Dawid before acting when something seems illogical.

## Step 0. Create the progress file

1. Make the list of files to review.
2. Write the progress file `notes-review-progress.md` in the scratchpad directory.
3. Put one block per file in it. Use this template and replace `<file>` with the file path:

```markdown
## <file>
- [ ] 1. Context gathered
- [ ] 2. Lessons found in the transcript
- [ ] 3. Untaught facts checked
- [ ] 4. Missing explanations checked
- [ ] 5. Sentences checked
- [ ] 6. Screenshots checked
- [ ] 7. Edits applied
- [ ] 8. Report sent
```

4. After you finish a step, change its `[ ]` to `[x]` with the Edit tool.
5. Before each step, read the progress file to see which step is next.
6. Review one file at a time. Start the next file after step 8 of the current file is checked.

Write findings under the block of the file, below the checklist, as a list.
Use the prefixes `REMOVE:`, `EXPLAIN:`, `REWRITE:` and `SCREENSHOT:`.

## Step 1. Gather context

1. List the directory of the file.
2. Read the other Markdown files in that directory. A term explained in an earlier file counts as explained.
3. Read `LEARNING.md`.
4. Check off step 1.

## Step 2. Find the lessons in the transcript

1. Search the session transcript (the JSONL file named in the context summary) for the topic of the file.
2. Extract only the assistant text messages. Skip the documentation pages that tool calls fetched.
3. Save the extracted lessons to a file in the scratchpad directory.
4. Read that file.
5. Check off step 2.

## Step 3. Check the facts against the lessons

Notes contain only what was taught in the chat.

1. Go through the note section by section. Go through every sentence, table row, limit and example.
2. Look for each of them in the extracted lessons.
3. Write `REMOVE: <quote>` for every one that no lesson covered.
4. Keep a "not verified" caveat when the lesson gave one.
5. Do not fill gaps with material from the documentation.
   If a fact is worth keeping but was not taught, tell Dawid and teach it first.
6. Check off step 3.

## Step 4. Find missing explanations

The reader is a mid/senior engineer.
He knows software engineering and LLM basics: tokens, prompts, completions, models.
He does not know the technology he is learning.

1. Go through the note sentence by sentence.
2. Write `EXPLAIN: <term>` for every term, attribute or tool-specific behavior used without an explanation.
3. Write `EXPLAIN: <element>` for every UI element (icon, button, tab) that no earlier sentence introduced.
4. Check off step 4.

## Step 5. Check each sentence

Go through the note sentence by sentence.
For each sentence, answer these questions in order.
Write `REMOVE:` or `REWRITE:` with the quote and a one-line reason at the first question answered with "yes".

1. Is it obvious? Can the reader infer it from the heading, the previous sentence, another file in the directory or general engineering knowledge?
2. Does it describe what is impossible, unsupported or absent? Examples: "There is no X", "Y is not supported", "must not".
3. Is it a verbless fragment or filler?
4. Does it use a pronoun with an unclear referent? Example: "It must have…".
5. Is it long or does it have nested clauses?

Do not flag a sentence that is the first explanation of a concept of the learned technology.

Rewrite rules:

- Rewrite a sentence only when the result is clearly better.
- Describe the working approach instead of what does not work.
- Use short sentences with a verb, one clause each.
- Put each sentence on its own line.
- Keep English terms in English (judge, prompt, score, evaluator, rule, trace, span, dataset).
- Write the notes in English.
- Keep Dawid's email and user ids out of text and screenshots.

Check off step 5 after the last sentence.

## Step 6. Screenshots

1. Go through the sections of the note.
2. Write `SCREENSHOT: <section>, <what to show>` where an image helps the reader, for example for a UI screen, a form or a panel.
3. Check off step 6.

Take the screenshots in step 7 with Playwright.
Crop each one to the relevant part when the full page is not needed.
Do not save anything in the Langfuse project while taking it.
Keep Dawid's email and user ids out of the image.

## Step 7. Apply the edits

1. Go through the findings of the file one by one, from top to bottom.
2. Apply each finding with the Edit tool.
3. Change the prefix of the applied finding to `DONE:` in the progress file.
4. Take the screenshots from the `SCREENSHOT:` findings and add them to the note.
5. Read the note once more to check that the edits did not break the structure.
6. Check off step 7.

## Step 8. Report

Send Dawid a short report in Polish for the file:

- facts removed because no lesson covered them,
- explanations added,
- sentences removed or rewritten, with a one-line reason,
- screenshots added.

Check off step 8.

## Step 9. Finish

1. Read the progress file.
2. Check that every box of every file is checked.
3. Go back to the first unchecked box if any remains.
