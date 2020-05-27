package net.sourceforge.ganttproject.task;

import biz.ganttproject.core.chart.render.ShapePaint;
import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.GanttCalendar;
import biz.ganttproject.core.time.TimeDuration;
import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.task.algorithm.AlgorithmException;
import net.sourceforge.ganttproject.task.algorithm.ShiftTaskTreeAlgorithm;
import net.sourceforge.ganttproject.util.collect.Pair;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class MutatorImpl implements TaskMutator {
  private final TaskImpl task;
  private TaskImpl.EventSender myPropertiesEventSender;

  private TaskImpl.EventSender myProgressEventSender;

  private TaskImpl.FieldChange myCompletionPercentageChange;

  private TaskImpl.FieldChange myStartChange;

  private TaskImpl.FieldChange myEndChange;

  private TaskImpl.FieldChange myThirdChange;

  private TaskImpl.FieldChange myDurationChange;

  private List<TaskActivity> myActivities;

  private Pair<TaskImpl.FieldChange, TaskImpl.FieldChange> myShiftChange;

  private final List<Runnable> myCommands = new ArrayList<>();

  private int myIsolationLevel;

  public final Exception myException = new Exception();

  public MutatorImpl(TaskImpl task) {
    this.task = task;
    myPropertiesEventSender = task.createPropertiesEventSender();
    myProgressEventSender = task.createProgressEventSender();
  }

  @Override
  public void commit() {
    boolean oneElementNotNull = false;
    GanttCalendar oldStart;
    GanttCalendar oldEnd;
    try {
      oldStart = task.getStart();
      oldEnd = task.getEnd();
      if (myShiftChange != null) {
        oldStart = (GanttCalendar) myShiftChange.first().myOldValue;
        oldEnd = (GanttCalendar) myShiftChange.second().myOldValue;
        oneElementNotNull = true;
      }
      if (myStartChange != null) {
        GanttCalendar start = getStart();
        task.setStart(start);
        oneElementNotNull = true;
        oldStart = (GanttCalendar) myStartChange.myOldValue;
        if (task.isSupertask())
          task.adjustNestedTasks();
      }
      if (myDurationChange != null) {
        TimeDuration duration = getDuration();
        task.setDuration(duration);
        myEndChange = null;
        oneElementNotNull = true;
      }
      if (myCompletionPercentageChange != null) {
        int newValue = getCompletionPercentage();
        task.setCompletionPercentage(newValue);
      }
      if (myEndChange != null) {
        GanttCalendar end = getEnd();
        if (end.getTime().compareTo(task.getStart().getTime()) > 0) {
          task.setEnd(end);
        }
        oneElementNotNull = true;
        oldEnd = (GanttCalendar) myEndChange.myOldValue;
      }
      if (myThirdChange != null) {
        GanttCalendar third = getThird();
        task.setThirdDate(third);
        oneElementNotNull = true;
      }
      for (Runnable command : myCommands) {
        command.run();
      }
      myCommands.clear();
      myPropertiesEventSender.fireEvent();
      myProgressEventSender.fireEvent();
    } finally {
      task.setMyMutator(null);
    }
    if (oneElementNotNull && task.areEventsEnabled()) {
      task.getMyManager().fireTaskScheduleChanged(task, oldStart, oldEnd);
    }
  }

  public GanttCalendar getThird() {
    return myThirdChange == null ? task.getMyThird() : (GanttCalendar) myThirdChange.myFieldValue;
  }

  public List<TaskActivity> getActivities() {
    if (myActivities == null && (myStartChange != null) || (myDurationChange != null)) {
      myActivities = new ArrayList<>();
      TaskImpl.recalculateActivities(task.getMyManager().getConfig().getCalendar(), task, myActivities,
          getStart().getTime(), task.getEnd().getTime());
    }
    return myActivities;
  }

  @Override
  public void setName(final String name) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setName(name);
      }
    });
  }

  @Override
  public void setProjectTask(final boolean projectTask) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setProjectTask(projectTask);
      }
    });
  }

  @Override
  public void setMilestone(final boolean milestone) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setMilestone(milestone);
      }
    });
  }

  @Override
  public void setPriority(final Task.Priority priority) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setPriority(priority);
      }
    });
  }

  @Override
  public void setStart(final GanttCalendar start) {
    if(start == null) {
      throw new IllegalArgumentException("Start argument cannot be null");
    }
    GanttCalendar currentStart = getStart();
    if (currentStart != null && start.equals(currentStart)) {
      return;
    }
    if (myStartChange == null) {
      myStartChange = new TaskImpl.FieldChange();
      myStartChange.myEventSender = myPropertiesEventSender;
    }
    myStartChange.setOldValue(task.getMyStart());
    myStartChange.setValue(start);
    myActivities = null;
  }

  @Override
  public void setEnd(final GanttCalendar end) {
    if (myEndChange == null) {
      myEndChange = new TaskImpl.FieldChange();
      myEndChange.myEventSender = myPropertiesEventSender;
    }
    myEndChange.setOldValue(task.getMyEnd());
    myEndChange.setValue(end);
    myActivities = null;
  }

  @Override
  public void setThird(final GanttCalendar third, final int thirdDateConstraint) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setThirdDateConstraint(thirdDateConstraint);
      }
    });
    if (myThirdChange == null) {
      myThirdChange = new TaskImpl.FieldChange();
      myThirdChange.myEventSender = myPropertiesEventSender;
    }
    myThirdChange.setValue(third);
    myActivities = null;
  }

  @Override
  public void setDuration(final TimeDuration length) {
    // If duration of task was set to 0 or less do not change it
    if (length.getLength() <= 0) {
      return;
    }

    if (myDurationChange == null) {
      myDurationChange = new TaskImpl.FieldChange();
      myDurationChange.myEventSender = myPropertiesEventSender;
      myDurationChange.setValue(length);
    } else {
      TimeDuration currentLength = (TimeDuration) myDurationChange.myFieldValue;
      if (currentLength.getLength() - length.getLength() == 0) {
        return;
      }
    }

    myDurationChange.setValue(length);
    Date shifted = task.shiftDate(getStart().getTime(), length);
    GanttCalendar newEnd = CalendarFactory.createGanttCalendar(shifted);
    setEnd(newEnd);
    myActivities = null;
  }

  @Override
  public void setExpand(final boolean expand) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setExpand(expand);
      }
    });
  }

  @Override
  public void setCompletionPercentage(final int percentage) {
    if (myCompletionPercentageChange == null) {
      myCompletionPercentageChange = new TaskImpl.FieldChange();
      myCompletionPercentageChange.myEventSender = myProgressEventSender;
    }
    myCompletionPercentageChange.setValue(percentage);
  }

  @Override
  public void setCritical(final boolean critical) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setCritical(critical);
      }
    });
  }

  @Override
  public void setShape(final ShapePaint shape) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setShape(shape);
      }
    });
  }

  @Override
  public void setColor(final Color color) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setColor(color);
      }
    });
  }

  @Override
  public void setWebLink(final String webLink) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setWebLink(webLink);
      }
    });
  }

  @Override
  public void setNotes(final String notes) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.setNotes(notes);
      }
    });
  }

  @Override
  public void addNotes(final String notes) {
    myCommands.add(new Runnable() {
      @Override
      public void run() {
        task.addNotes(notes);
      }
    });
  }

  @Override
  public int getCompletionPercentage() {
    return myCompletionPercentageChange == null ? task.getMyCompletionPercentage()
        : ((Integer) myCompletionPercentageChange.myFieldValue).intValue();
  }

  GanttCalendar getStart() {
    return myStartChange == null ? task.getMyStart() : (GanttCalendar) myStartChange.myFieldValue;
  }

  GanttCalendar getEnd() {
    return myEndChange == null ? null : (GanttCalendar) myEndChange.myFieldValue;
  }

  TimeDuration getDuration() {
    return myDurationChange == null ? task.getMyLength() : (TimeDuration) myDurationChange.myFieldValue;
  }

  @Override
  public void shift(float unitCount) {
    Task result = getPrecomputedShift(unitCount);
    if (result == null) {
      result = task.shift(unitCount);
      cachePrecomputedShift(result, unitCount);
    }

    setStart(result.getStart());
    setDuration(result.getDuration());
    setEnd(result.getEnd());
  }

  @Override
  public void shift(TimeDuration shift) {
    if (myShiftChange == null) {
      myShiftChange = Pair.create(new TaskImpl.FieldChange(), new TaskImpl.FieldChange());
      myShiftChange.first().setOldValue(task.getMyStart());
      myShiftChange.second().setOldValue(task.getMyEnd());
    }
    ShiftTaskTreeAlgorithm shiftAlgorithm = new ShiftTaskTreeAlgorithm(task.getMyManager(), null);
    try {
      shiftAlgorithm.run(task, shift, ShiftTaskTreeAlgorithm.DEEP);
    } catch (AlgorithmException e) {
      GPLogger.log(e);
    }
  }

  @Override
  public void setIsolationLevel(int level) {
    myIsolationLevel = level;
  }

  private void cachePrecomputedShift(Task result, float unitCount) {
    // TODO Implement cache
  }

  private Task getPrecomputedShift(float unitCount) {
    // TODO Use cache to grab value
    return null;
  }

  @Override
  public void setTaskInfo(TaskInfo taskInfo) {
    task.setMyTaskInfo(taskInfo);
  }

  public int getMyIsolationLevel() {
    return myIsolationLevel;
  }


}
