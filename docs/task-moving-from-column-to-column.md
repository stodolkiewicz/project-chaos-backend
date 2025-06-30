## Moving tasks between columns
### Current situation:

Each task has a floating point number - positionInColumn.
The higher it is the lower the task is in a given column.

**Frontend:**
- keeps track of the max positionInColumn for each column.
- When a task in dragged from column A to column B, frontend computes
new positionInColumn for the task dragged into another column as:  
_= max position in the target column + 1_

**Backend**
- Just saves what is sent to it.

**Problems with this approach:**
- Dragged task always goes to the end of column
- Can not switch tasks in a column

Pros: 
- no reindexing! :-)

### Possible improvements:
Frontend should detect between what tasks, a task is dropped.  
Then, you could switch tasks in a column between themselves and move a task to another column to 
whatever place you choose.

Then, reindexing from time to time will be needed.

There is some (currently unused) code on the frontend and backend to handle this.  
On the frontend:
```
    moveTask({
      projectId: defaultProjectId,
      taskId: taskBeingDraggedId,
      updateTaskColumnDTO: {
        targetColumnId: columnBeingDroppedIntoId,
        positionInColumn: maxPositions[columnBeingDroppedIntoId] + 1,
        // to do: fix it. Not used on the backend at all.
        nearestNeighboursPositionInColumn: [1, 3],
      },
    });
```

On the backend:
```java
    private boolean checkIfTaskPositionsInColumnNeedReindexing(Double positionInColumn, List<Double> nearestNeighboursPositionInColumn) {
        return nearestNeighboursPositionInColumn.stream()
                .map(neighbourPositionInColumn -> Math.abs(positionInColumn - neighbourPositionInColumn))
                .anyMatch(distance -> distance < MINIMUM_DISTANCE_BETWEEN_TASKS);
    }
    }
```