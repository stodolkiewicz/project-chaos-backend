# Score analytics

## Context

Score analytics analyzes scores without any configuration.
It is the tab **Analytics** on the page **Scores**.
The documentation describes it as a beta feature.
It analyzes one score and compares two scores of the same data type.

## Analyzing one score

1. Open **Scores > Analytics**.
2. Select a score from the first list.
3. Select the object type: Traces, Observations, Sessions or Dataset Run Items (the items of a dataset run, a later lesson).
4. Set the time range.

The page shows three things:

- statistics: the count, the mean or the mode, and the standard deviation,
- the distribution of the values,
- the trend over time.

Text scores are not supported.

## Comparing two scores

Select a second score from the second list.
It must have the same data type as the first one.
The scores can have different scales, but the error metrics then depend on the scales.

The page shows metrics that fit the data type:

| Data type             | Distribution          | Comparison       | Metrics                                              |
|-----------------------|-----------------------|------------------|------------------------------------------------------|
| Numeric               | histogram, 10 bins    | 10x10 heatmap    | Pearson, Spearman, MAE, RMSE                         |
| Categorical           | bar chart             | confusion matrix | Cohen's Kappa, F1, overall agreement                 |
| Boolean               | bar chart, 2 values   | 2x2 matrix       | Cohen's Kappa, F1, overall agreement                 |

Cohen's Kappa measures the agreement of two raters and corrects for agreement by chance.
Values from 0.81 to 1.0 mean almost perfect agreement.
Values below 0.41 mean fair or slight agreement.

A diagonal pattern in the matrix means that the two scores agree.
An anti-diagonal pattern means a negative relation.

## Matched and all data

A match exists when two scores are attached to the same object.
The tab **Matched** counts only objects that have both scores.
The tabs of the single scores count all objects with that score.
A matched count much lower than the single counts shows a gap in the evaluation coverage.

## Typical uses

- Check that an LLM judge agrees with human annotations (compare an `EVAL` score with an `ANNOTATION` score of the same data type). When the agreement is low, change the judge prompt and compare again.
- Check that two judge models give consistent results.
- Detect a quality regression: set the time range around a deployment and read the trend.

## Limits

- Only two scores are compared at a time.
- Only scores of the same data type are compared.
- A query that expects more than 100,000 scores is sampled. A visible indicator shows the sampling.

## Scores with different meanings

The page has no filter on the version of an evaluator.
Scores of one name from versions with opposite meanings of `true` and `false` distort the distribution.
Restrict the time range to the period after the last change of the judge prompt.
Another option is to rename the evaluator, because its name is the name of the score.
