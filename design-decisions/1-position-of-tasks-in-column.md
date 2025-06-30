# Tracking the position of tasks in a column

## Solution 1. Task table, position_in_column field
The position_in_column should be a floating point number.  

**Example with Short:**  
Assuming you have a column with 5 tasks:

| Task | position_in_column (Short) |
|:-----|:---------------------------|
| A    | 1                          |
| B    | 2                          |
| C    | 3                          |
| D    | 4                          |
| E    | 5                          |

You want to drag task E to the the second place   
-> **You have to update 4 tasks:**  
E → 2  
B → 3  
C → 4  
D → 5  

**Example with Double:** 

| Task | position_in_column (Double) |
|:-----|:----------------------------|
| A    | 1                           |
| E    | 1.5                         |
| B    | 2                           |
| C    | 3                           |
| D    | 4                           |

-> **You have to update only 1 task**

The problem will occur after moving a task between 2 other tasks after about 50 moves.
(Double precision is 15-17 decimal digits). Assuming position_in_column are 1 and 2 initially:
Moving a task between them:

| position_in_column of the "moved" task | distance |
|:-----|:---------------------------|
| 1.5   | 0.5                       |
| 1.25  | 0.25                      |
| 1.125 | 0.125                     |
| 1.0625| 0.0625                    |
| 1.03125| 0.03125                  |
...
2^49.8=10^15
At that point, a reindexing of tasks in a given column will be needed to restore larger gaps.  

In order to make a reindexing decision, minimal_distance field could be added to the column table.

## FK to previous and next tasks
Complicates the backend a little bit, but requires no reindexing.

## Decision
Decision - The position_in_column floating point number field in the Task table.  
Why? - Break of reindexing should be super rare in normal usage.

## Update 30.06.2025
- minimal_distance field should be hardcoded on the backend.- 
- backend should get 2 nearest neighbours' position in columns.
- when task is moved, the distance between nearest neighbours should be computed and compared to minimal distance.
- If minimal distance < distance between nearest neighbours -> do reindexing in the target column
