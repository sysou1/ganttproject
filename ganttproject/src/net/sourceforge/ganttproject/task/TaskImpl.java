/*
GanttProject is an opensource project management tool.
Copyright (C) 2004-2011 GanttProject Team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.task;

import biz.ganttproject.core.calendar.AlwaysWorkingTimeCalendarImpl;
import biz.ganttproject.core.calendar.GPCalendar.DayMask;
import biz.ganttproject.core.calendar.GPCalendar.DayType;
import biz.ganttproject.core.calendar.GPCalendarCalc;
import biz.ganttproject.core.calendar.GPCalendarCalc.MoveDirection;
import biz.ganttproject.core.chart.render.ShapePaint;
import biz.ganttproject.core.time.CalendarFactory;
import biz.ganttproject.core.time.GanttCalendar;
import biz.ganttproject.core.time.TimeDuration;
import biz.ganttproject.core.time.TimeDurationImpl;
import biz.ganttproject.core.time.impl.GPTimeUnitStack;
import com.google.common.collect.ImmutableList;
import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.chart.MilestoneTaskFakeActivity;
import net.sourceforge.ganttproject.document.AbstractURLDocument;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.task.algorithm.AlgorithmCollection;
import net.sourceforge.ganttproject.task.algorithm.CostAlgorithmImpl;
import net.sourceforge.ganttproject.task.dependency.TaskDependencyException;
import net.sourceforge.ganttproject.task.dependency.TaskDependencySlice;
import net.sourceforge.ganttproject.task.dependency.TaskDependencySliceAsDependant;
import net.sourceforge.ganttproject.task.dependency.TaskDependencySliceAsDependee;
import net.sourceforge.ganttproject.task.dependency.TaskDependencySliceImpl;
import net.sourceforge.ganttproject.task.hierarchy.TaskHierarchyItem;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author bard
 */
public class TaskImpl implements Task {
  private final int myID;

  private final TaskManagerImpl myManager;

  private String myName;

  private String myWebLink = "";

  private boolean isMilestone;

  boolean isProjectTask;

  private Priority myPriority;

  private GanttCalendar myStart;

  private GanttCalendar myEnd;

  private GanttCalendar myThird;

  private int myThirdDateConstraint;

  private int myCompletionPercentage;

  private TimeDuration myLength;

  private final List<TaskActivity> myActivities = new ArrayList<>();

  private boolean bExpand;

  private final ResourceAssignmentCollectionImpl myAssignments;

  private final TaskDependencySlice myDependencySlice;

  private final TaskDependencySlice myDependencySliceAsDependant;

  private final TaskDependencySlice myDependencySliceAsDependee;

  private boolean myEventsEnabled;

  private final TaskHierarchyItem myTaskHierarchyItem;

  private ShapePaint myShape;

  private Color myColor;

  private String myNotes;

  private MutatorImpl myMutator;

  private final CustomColumnsValues customValues;

  private boolean critical;

  private List<TaskActivity> myMilestoneActivity;

  private final CostImpl myCost = new CostImpl();

  private boolean isUnplugged = false;

  public static final int NONE = 0;

  public static final int EARLIESTBEGIN = 1;

  private static final GPCalendarCalc RESTLESS_CALENDAR = new AlwaysWorkingTimeCalendarImpl();

  private static final TimeDuration EMPTY_DURATION = new TimeDurationImpl(GPTimeUnitStack.DAY, 0);

  protected TaskImpl(TaskManagerImpl taskManager, int taskID) {
    myManager = taskManager;
    myID = taskID;

    myAssignments = new ResourceAssignmentCollectionImpl(this, myManager.getConfig().getResourceManager());
    myDependencySlice = new TaskDependencySliceImpl(this, myManager.getDependencyCollection(), TaskDependencySlice.COMPLETE_SLICE_FXN);
    myDependencySliceAsDependant = new TaskDependencySliceAsDependant(this, myManager.getDependencyCollection());
    myDependencySliceAsDependee = new TaskDependencySliceAsDependee(this, myManager.getDependencyCollection());
    myPriority = DEFAULT_PRIORITY;
    myTaskHierarchyItem = myManager.getHierarchyManager().createItem(this);
    myNotes = "";
    bExpand = true;
    myColor = null;

    customValues = new CustomColumnsValues(myManager.getCustomPropertyManager());
  }

  protected TaskImpl(TaskManagerImpl manager, TaskImpl copy, boolean isUnplugged) {
    this.isUnplugged = isUnplugged;
    myManager = manager;
    // Use a new (unique) ID for the cloned task
    myID = myManager.getAndIncrementId();

    if (!isUnplugged) {
      myTaskHierarchyItem = myManager.getHierarchyManager().createItem(this);
    } else {
      myTaskHierarchyItem = copy.myTaskHierarchyItem;
    }
    myAssignments = new ResourceAssignmentCollectionImpl(this, myManager.getConfig().getResourceManager());
    myAssignments.importData(copy.getAssignmentCollection());
    myName = copy.myName;
    myWebLink = copy.myWebLink;
    isMilestone = copy.isMilestone;
    isProjectTask = copy.isProjectTask;
    myPriority = copy.myPriority;
    myStart = copy.myStart;
    myEnd = copy.myEnd;
    myThird = copy.myThird;
    myThirdDateConstraint = copy.myThirdDateConstraint;
    myCompletionPercentage = copy.myCompletionPercentage;
    myLength = copy.myLength;
    myShape = copy.myShape;
    myColor = copy.myColor;
    myNotes = copy.myNotes;
    bExpand = copy.bExpand;
    myCost.setValue(copy.myCost);

    myDependencySlice = new TaskDependencySliceImpl(this, myManager.getDependencyCollection(), TaskDependencySlice.COMPLETE_SLICE_FXN);
    myDependencySliceAsDependant = new TaskDependencySliceAsDependant(this, myManager.getDependencyCollection());
    myDependencySliceAsDependee = new TaskDependencySliceAsDependee(this, myManager.getDependencyCollection());

    customValues = (CustomColumnsValues) copy.getCustomValues().clone();

    recalculateActivities();
  }

  @Override
  public Task unpluggedClone() {
    return new TaskImpl(myManager, this, true) {
      @Override
      public boolean isSupertask() {
        return false;
      }
    };
  }

  class MutatorException extends RuntimeException {
    public MutatorException(String msg) {
      super(msg);
    }
  }

  @Override
  public TaskMutator createMutator() {
    if (myMutator != null) {
      return myMutator;
    }
    myMutator = new MutatorImpl(this);
    return myMutator;
  }

  @Override
  public TaskMutator createMutatorFixingDuration() {
    if (myMutator != null) {
      throw new MutatorException("Two mutators have been requested for task=" + getName());
    }
    myMutator = new MutatorImpl(this) {
      @Override
      public void setStart(GanttCalendar start) {
        super.setStart(start);
        TaskImpl.this.myEnd = null;
      }
    };
    return myMutator;
  }

  // main properties
  @Override
  public int getTaskID() {
    return myID;
  }

  @Override
  public String getName() {
    return myName;
  }

  public String getWebLink() {
    return myWebLink;
  }

  @Override
  public List<Document> getAttachments() {
    if (getWebLink() != null && !"".equals(getWebLink())) {
      return Collections.singletonList((Document) new AbstractURLDocument() {
        @Override
        public boolean canRead() {
          return true;
        }

        @Override
        public IStatus canWrite() {
          return Status.CANCEL_STATUS;
        }

        @Override
        public String getFileName() {
          return null;
        }

        @Override
        public InputStream getInputStream() throws IOException {
          return null;
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
          return null;
        }

        @Override
        public String getPath() {
          return null;
        }

        @Override
        public URI getURI() {
          try {
            return new URI(new URL(getWebLink()).toString());
          } catch (URISyntaxException e) {
            // Do nothing
          } catch (MalformedURLException e) {
            File f = new File(getWebLink());
            if (f.exists()) {
              return f.toURI();
            }
          }
          try {
            URL context = myManager.getProjectDocument();
            if (context == null) {
              return null;
            }
            URL relative = new URL(context, getWebLink());
            return new URI(URLEncoder.encode(relative.toString(), "utf-8"));
          } catch (URISyntaxException | MalformedURLException | UnsupportedEncodingException e){
            GPLogger.log(e);
          }
          return null;
        }

        @Override
        public boolean isLocal() {
          return false;
        }

        @Override
        public boolean isValidForMRU() {
          return false;
        }

        @Override
        public void write(){
        }
      });
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isMilestone() {
    return isMilestone && myManager.isZeroMilestones();
  }

  public boolean isLegacyMilestone() {
    return isMilestone;
  }
  @Override
  public Priority getPriority() {
    return myPriority;
  }

  @Override
  public GanttCalendar getStart() {
    if (myMutator != null && myMutator.getMyIsolationLevel() == TaskMutator.READ_UNCOMMITED) {
      return myMutator.getStart();
    }
    return myStart;
  }

  @Override
  public GanttCalendar getEnd() {
    GanttCalendar result = null;
    if (myMutator != null && myMutator.getMyIsolationLevel() == TaskMutator.READ_UNCOMMITED) {
      result = myMutator.getEnd();
    }
    if (result == null) {
      if (myEnd == null) {
        myEnd = calculateEnd();
      }
      result = myEnd;
    }
    return result;
  }

  @Override
  public GanttCalendar getDisplayEnd() {
    GanttCalendar modelEnd = getEnd();
    if (modelEnd.equals(getStart())) {
      boolean allMilestones = true;
      Task[] deepNestedTasks = getManager().getTaskHierarchy().getDeepNestedTasks(this);
      for (Task t : deepNestedTasks) {
        if (!t.isSupertask() && !t.isMilestone()) {
          allMilestones = false;
          break;
        }
      }
      if (!allMilestones) {
        String errorMsg = String.format(
                "This is probably a bug. Task #%d (%s) has end date=%s equal to start date." +
                        "It could be possible if all child tasks were milestones, however they are not. Child tasks: %s",
                getTaskID(), getName(), modelEnd, Arrays.asList(deepNestedTasks));
        GPLogger.getLogger(Task.class).warning(errorMsg);
      }
      return modelEnd;
    }
    return isMilestone ? modelEnd : modelEnd.getDisplayValue();
  }

  GanttCalendar calculateEnd() {
    GanttCalendar result = getStart().clone();
    Date newEnd = shiftDate(result.getTime(), getDuration());
    result.setTime(newEnd);
    return result;
  }

  @Override
  public GanttCalendar getThird() {
    if (myMutator != null && myMutator.getMyIsolationLevel() == TaskMutator.READ_UNCOMMITED) {
      return myMutator.getThird();
    }
    return myThird;
  }

  @Override
  public int getThirdDateConstraint() {
    return myThirdDateConstraint;
  }

  @Override
  public List<TaskActivity> getActivities() {
    if (isMilestone) {
      return myMilestoneActivity;
    }
    List<TaskActivity> activities = myMutator == null ? null : myMutator.getActivities();
    if (activities == null) {
      activities = myActivities;
    }
    return activities;
  }

  @Override
  public TimeDuration getDuration() {
    if (isMilestone()) {
      return EMPTY_DURATION;
    }
    return (myMutator != null && myMutator.getMyIsolationLevel() == TaskMutator.READ_UNCOMMITED) ? myMutator.getDuration()
        : myLength;
  }

  @Override
  public int getCompletionPercentage() {
    return (myMutator != null && myMutator.getMyIsolationLevel() == TaskMutator.READ_UNCOMMITED) ? myMutator.getCompletionPercentage()
        : myCompletionPercentage;
  }

  @Override
  public boolean getExpand() {
    return bExpand;
  }

  @Override
  public ShapePaint getShape() {
    return myShape;
  }

  @Override
  public Color getColor() {
    Color result = myColor;
    if (result == null) {
      if (isMilestone() || myManager.getTaskHierarchy().hasNestedTasks(this)) {
        result = Color.BLACK;
      } else {
        result = myManager.getConfig().getDefaultColor();
      }
    }
    return result;
  }

  @Override
  public String getNotes() {
    return myNotes;
  }

  @Override
  public ResourceAssignment[] getAssignments() {
    return myAssignments.getAssignments();
  }

  @Override
  public ResourceAssignmentCollection getAssignmentCollection() {
    return myAssignments;
  }

  @Override
  public Task getSupertask() {
    TaskHierarchyItem container = myTaskHierarchyItem.getContainerItem();
    return container.getTask();
  }

  @Override
  public Task[] getNestedTasks() {
    TaskHierarchyItem[] nestedItems = myTaskHierarchyItem.getNestedItems();
    Task[] result = new Task[nestedItems.length];
    for (int i = 0; i < nestedItems.length; i++) {
      result[i] = nestedItems[i].getTask();
    }
    return result;
  }

  @Override
  public void move(Task targetSupertask) {
    move(targetSupertask, -1);
  }

  @Override
  public void move(Task targetSupertask, int position) {
    TaskImpl supertaskImpl = (TaskImpl) targetSupertask;
    TaskHierarchyItem targetItem = supertaskImpl.myTaskHierarchyItem;
    myTaskHierarchyItem.delete();
    targetItem.addNestedItem(myTaskHierarchyItem, position);
    myManager.onTaskMoved(this);
  }

  @Override
  public void delete() {
    getDependencies().clear();
    getAssignmentCollection().clear();
  }

  @Override
  public TaskDependencySlice getDependencies() {
    return myDependencySlice;
  }

  @Override
  public TaskDependencySlice getDependenciesAsDependant() {
    return myDependencySliceAsDependant;
  }

  @Override
  public TaskDependencySlice getDependenciesAsDependee() {
    return myDependencySliceAsDependee;
  }

  @Override
  public TaskManager getManager() {
    return myManager;
  }

  static interface EventSender {
    void enable();

    void fireEvent();
  }

  class ProgressEventSender implements EventSender {
    private boolean myEnabled;

    @Override
    public void fireEvent() {
      if (myEnabled) {
        myManager.fireTaskProgressChanged(TaskImpl.this);
      }
      myEnabled = false;
    }

    @Override
    public void enable() {
      myEnabled = true;
    }
  }

  class PropertiesEventSender implements EventSender {
    private boolean myEnabled;

    @Override
    public void fireEvent() {
      if (myEnabled) {
        myManager.fireTaskPropertiesChanged(TaskImpl.this);
      }
      myEnabled = false;
    }

    @Override
    public void enable() {
      myEnabled = true;
    }
  }

  static class FieldChange {
    Object myFieldValue;
    Object myOldValue;

    EventSender myEventSender;

    void setValue(Object newValue) {
      myFieldValue = newValue;
      myEventSender.enable();
    }

    public void setOldValue(Object oldValue) {
      myOldValue = oldValue;
    }
  }

  @Override
  public void setName(String name) {
    myName = (name == null ? null : name.trim());
  }

  @Override
  public void setWebLink(String webLink) {
    myWebLink = webLink;
  }

  @Override
  public void setMilestone(boolean milestone) {
    isMilestone = milestone;
    if (milestone) {
      setEnd(null);
    }
  }

  @Override
  public void setPriority(Priority priority) {
    myPriority = priority;
  }

  @Override
  public void setStart(GanttCalendar start) {
    Date closestWorkingStart = myManager.findClosestWorkingTime(start.getTime());
    start.setTime(closestWorkingStart);
    myStart = start;
    recalculateActivities();
    adjustNestedTasks();
  }

  void adjustNestedTasks() {
    assert myManager != null;
    try {
      AlgorithmCollection algorithmCollection = myManager.getAlgorithmCollection();
      if (algorithmCollection != null) {
        algorithmCollection.getAdjustTaskBoundsAlgorithm().adjustNestedTasks(this);
      }
    } catch (TaskDependencyException e) {
      if (!GPLogger.log(e)) {
        e.printStackTrace(System.err);
      }
    }
  }

  @Override
  public boolean isSupertask() {
    return myManager.getTaskHierarchy().hasNestedTasks(this);
  }

  @Override
  public void setEnd(GanttCalendar end) {
    myEnd = end;
    recalculateActivities();
  }

  @Override
  public void setThirdDate(GanttCalendar third) {
    Date closestWorkingStart = myManager.findClosestWorkingTime(third.getTime());
    third.setTime(closestWorkingStart);
    myThird = third;
  }

  @Override
  public void setThirdDateConstraint(int thirdDateConstraint) {
    myThirdDateConstraint = thirdDateConstraint;
  }

  @Override
  public void shift(TimeDuration shift) {
    float unitCount = shift.getLength(myLength.getTimeUnit());
    if (unitCount != 0f) {
      Task resultTask = shift(unitCount);
      GanttCalendar oldStart = myStart;
      GanttCalendar oldEnd = myEnd;
      myStart = resultTask.getStart();
      myLength = resultTask.getDuration();
      myEnd = resultTask.getEnd();
      if (areEventsEnabled()) {
        myManager.fireTaskScheduleChanged(this, oldStart, oldEnd);
      }
      recalculateActivities();
    }
  }

  public Task shift(float unitCount) {
    Task clone = unpluggedClone();
    Date newStart;
    if (unitCount > 0) {
        TimeDuration length = myManager.createLength(myLength.getTimeUnit(), unitCount);
        newStart = RESTLESS_CALENDAR.shiftDate(myStart.getTime(), length);
        if (0 == (getManager().getCalendar().getDayMask(newStart) & DayMask.WORKING)) {
          newStart = getManager().getCalendar().findClosest(newStart, myLength.getTimeUnit(), MoveDirection.FORWARD, DayType.WORKING);
        }
    } else {
        newStart = RESTLESS_CALENDAR.shiftDate(clone.getStart().getTime(),
            getManager().createLength(clone.getDuration().getTimeUnit(), (long) unitCount));
        if (0 == (getManager().getCalendar().getDayMask(newStart) & DayMask.WORKING)) {
          newStart = getManager().getCalendar().findClosest(newStart, myLength.getTimeUnit(), MoveDirection.BACKWARD, DayType.WORKING);
        }
      }
    clone.setStart(CalendarFactory.createGanttCalendar(newStart));
    clone.setDuration(myLength);

    return clone;
  }

  @Override
  public void setDuration(TimeDuration length) {

    if(length.getLength() < 0) {
      throw new IllegalArgumentException("An attempt to set length=" + length + " to task=" + this);
    }

    myLength = length;
    myEnd = null;
    recalculateActivities();
  }

  Date shiftDate(Date input, TimeDuration duration) {
    return myManager.getConfig().getCalendar().shiftDate(input, duration);
  }

  @Override
  public TimeDuration translateDuration(TimeDuration duration) {
    return myManager.createLength(myLength.getTimeUnit(), translateDurationValue(duration));
  }

  private float translateDurationValue(TimeDuration duration) {
    if (myLength.getTimeUnit().equals(duration.getTimeUnit())) {
      return duration.getValue();
    }
    if (myLength.getTimeUnit().isConstructedFrom(duration.getTimeUnit())) {
      return duration.getValue() / myLength.getTimeUnit().getAtomCount(duration.getTimeUnit());
    }
    if (duration.getTimeUnit().isConstructedFrom(myLength.getTimeUnit())) {
      return duration.getValue() * duration.getTimeUnit().getAtomCount(myLength.getTimeUnit());
    }
    throw new IllegalArgumentException("Can't translate duration=" + duration + " into units=" + myLength.getTimeUnit());
  }

  private void recalculateActivities() {
    if (myLength == null || myManager == null) {
      return;
    }
    if (isMilestone) {
      myMilestoneActivity = ImmutableList.<TaskActivity>of(new MilestoneTaskFakeActivity(this));
      return;
    }

    final Date startDate = myStart.getTime();
    final Date endDate = getEnd().getTime();

    myActivities.clear();
    if (startDate.equals(endDate)) {
      myActivities.add(new MilestoneTaskFakeActivity(this));
      return;
    }

    recalculateActivities(myManager.getConfig().getCalendar(), this, myActivities, startDate, endDate);
    int length = 0;
    for (TaskActivity activity : myActivities) {
      if (activity.getIntensity() > 0) {
        length += activity.getDuration().getLength(getDuration().getTimeUnit());
      }
    }
    myLength = getManager().createLength(myLength.getTimeUnit(), length);
  }

  static void recalculateActivities(GPCalendarCalc calendar, Task task, List<TaskActivity> output, Date startDate,
                                    Date endDate) {
    TaskActivitiesAlgorithm alg = new TaskActivitiesAlgorithm(calendar);
    alg.recalculateActivities(task, output, startDate, endDate);
  }

  @Override
  public void setCompletionPercentage(int percentage) {
    if (percentage != myCompletionPercentage) {
      myCompletionPercentage = percentage;
      EventSender progressEventSender = new ProgressEventSender();
      progressEventSender.enable();
      progressEventSender.fireEvent();
    }
  }

  @Override
  public void setShape(ShapePaint shape) {
    myShape = shape;
  }

  @Override
  public void setColor(Color color) {
    myColor = color;
  }

  @Override
  public void setNotes(String notes) {
    myNotes = notes;
  }

  @Override
  public void setExpand(boolean expand) {
    bExpand = expand;
  }

  @Override
  public void addNotes(String notes) {
    myNotes += notes;
  }

  protected void enableEvents(boolean enabled) {
    myEventsEnabled = enabled;
  }

  protected boolean areEventsEnabled() {
    return myEventsEnabled && myManager.areEventsEnabled();
  }

  /**
   * Determines whether a special shape is defined for this task.
   *
   * @return true, if this task has its own shape defined.
   */
  public boolean shapeDefined() {
    return (myShape != null);
  }

  /**
   * Determines whether a special color is defined for this task.
   *
   * @return true, if this task has its own color defined.
   */
  public boolean colorDefined() {
    return (myColor != null);
  }

  @Override
  public String toString() {
    return getName();
  }

  public boolean isUnplugged() {
    return this.isUnplugged;
  }

  /** @return The CustomColumnValues. */
  @Override
  public CustomColumnsValues getCustomValues() {
    return customValues;
  }

  @Override
  public void setCritical(boolean critical) {
    this.critical = critical;
  }

  @Override
  public boolean isCritical() {
    return this.critical;
  }

  @Override
  public void applyThirdDateConstraint() {
  }

  private TaskInfo myTaskInfo;

  @Override
  public TaskInfo getTaskInfo() {
    return myTaskInfo;
  }

  @Override
  public void setTaskInfo(TaskInfo taskInfo) {
    myTaskInfo = taskInfo;
  }

  @Override
  public boolean isProjectTask() {
    return isProjectTask;
  }

  @Override
  public void setProjectTask(boolean projectTask) {
    isProjectTask = projectTask;
  }

  private class CostImpl implements Cost {
    private BigDecimal myValue = BigDecimal.ZERO;
    private boolean isCalculated = true;

    @Override
    public BigDecimal getValue() {
      return (isCalculated) ? getCalculatedValue() : getManualValue();
    }

    @Override
    public BigDecimal getManualValue() {
      return myValue;
    }

    @Override
    public BigDecimal getCalculatedValue() {
      return new CostAlgorithmImpl().getCalculatedCost(TaskImpl.this);
    }

    @Override
    public void setValue(BigDecimal value) {
      myValue = value;
    }

    public void setValue(Cost copy) {
      myValue = copy.getValue();
      isCalculated = copy.isCalculated();
    }

    @Override
    public boolean isCalculated() {
      return isCalculated;
    }

    @Override
    public void setCalculated(boolean calculated) {
      isCalculated = calculated;
    }
  }

  @Override
  public Cost getCost() {
    return myCost;
  }


  public void setMyMutator(MutatorImpl myMutator) {
    this.myMutator = myMutator;
  }

  public GanttCalendar getMyThird() {
    return myThird;
  }

  public GanttCalendar getMyStart() {
    return myStart;
  }

  public GanttCalendar getMyEnd() {
    return myEnd;
  }

  public int getMyCompletionPercentage() {
    return myCompletionPercentage;
  }

  public TimeDuration getMyLength() {
    return myLength;
  }

  public void setMyTaskInfo(TaskInfo myTaskInfo) {
    this.myTaskInfo = myTaskInfo;
  }

  public PropertiesEventSender createPropertiesEventSender(){
    return new PropertiesEventSender();
  }

  public ProgressEventSender createProgressEventSender(){
    return new ProgressEventSender();
  }
}
