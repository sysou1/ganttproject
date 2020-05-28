/*
 * This code is provided under the terms of GPL version 3.
 * Please see LICENSE file for details
 * (C) Dmitry Barashev, GanttProject team, 2004-2008
 */
package net.sourceforge.ganttproject.resource;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultListModel;

import biz.ganttproject.core.calendar.GanttDaysOff;

import net.sourceforge.ganttproject.task.ResourceAssignment;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskActivity;

/**
 * Represents load of of one particular resource in the given time range
 */
public class LoadDistribution {
  public static class Load {
    private float loadValue;

    public final Task refTask;

    Load(Date startDate, Date endDate, float loadValue, Task ref) {
      this.loadValue = loadValue;
      this.refTask = ref;
      this.startDate = startDate;
      this.endDate = endDate;
    }

    @Override
    public String toString() {
      return "start=" + this.startDate + " load=" + this.loadValue + " refTask = " + this.refTask;
    }

    public boolean isResourceUnavailable() {
      return loadValue == -1;
    }

    public float getLoadValue() {
      return loadValue;
    }

    public final Date startDate;
    public final Date endDate;
  }

  private final List<Load> myDaysOff = new LinkedList<>();

  private final List<Load> myLoads = new ArrayList<>();

  private final List<Load> myTasksLoads = new ArrayList<>();

  private final HumanResource myResource;

  public LoadDistribution(HumanResource resource) {
    myLoads.add(new Load(null, null, 0, null));
    myDaysOff.add(new Load(null, null, 0, null));
    myResource = resource;
    ResourceAssignment[] assignments = myResource.getAssignments();
    for (int j = 0; j < assignments.length; j++) {
      processAssignment(assignments[j]);
    }
    processDaysOff(myResource);
  }

  private void processDaysOff(HumanResource resource) {
    DefaultListModel daysOff = resource.getDaysOff();
    if (daysOff != null) {
      for (int l = 0; l < daysOff.size(); l++) {
        processDayOff((GanttDaysOff) daysOff.get(l));
      }
    }

  }

  private void processDayOff(GanttDaysOff dayOff) {
    Date dayOffEnd = dayOff.getFinish().getTime();
    addLoad(dayOff.getStart().getTime(), dayOffEnd, -1, myDaysOff, null);
  }

  private void processAssignment(ResourceAssignment assignment) {
    Task task = assignment.getTask();
    for (TaskActivity ta : task.getActivities()) {
      processActivity(ta, assignment.getLoad());
    }
  }

  private void processActivity(TaskActivity activity, float loadValue) {
    if (activity.getIntensity() == 0) {
      return;
    }
    addLoad(activity.getStart(), activity.getEnd(), loadValue, myLoads, activity.getOwner());
  }

  private void addLoad(Date startDate, Date endDate, float loadValue, List<Load> loads, Task t) {
    Load taskLoad = new Load(startDate, endDate, loadValue, t);

    myTasksLoads.add(taskLoad);

    float currentLoad = 0;
    int idxStart = getIdxStart(startDate, loads, currentLoad);
    if (idxStart == -1) {
      idxStart = loads.size();
      loads.add(new Load(startDate, null, 0, t));
    }
    int idxEnd = getIdxEnd(endDate, loadValue, loads, idxStart);
    if (idxEnd == -1) {
      idxEnd = loads.size();
      loads.add(new Load(endDate, null, 0, t));
    }
  }

  private int getIdxEnd(Date endDate, float loadValue, List<Load> loads, int idxStart) {
    int idxEnd = -1;
    if (endDate == null) {
      idxEnd = loads.size() - 1;
    } else {
      for (int i = idxStart; i < loads.size(); i++) {
        Load nextLoad = loads.get(i);
        if (endDate.compareTo(nextLoad.startDate) > 0) {
          nextLoad.loadValue += loadValue;
          continue;
        }
        idxEnd = i;
        if (endDate.compareTo(nextLoad.startDate) < 0) {
          Load prevLoad = loads.get(i - 1);
          loads.add(i, new Load(endDate, null, prevLoad.getLoadValue() - loadValue, null));
        }
        break;
      }
    }
    return idxEnd;
  }

  private int getIdxStart(Date startDate, List<Load> loads, float currentLoad) {
    int idxStart = -1;
    if (startDate == null) {
      idxStart = 0;
    } else {
      for (int i = 1; i < loads.size(); i++) {
        Load nextLoad = loads.get(i);
        if (startDate.compareTo(nextLoad.startDate) >= 0) {
          currentLoad = loads.get(i).getLoadValue();
        }
        if (startDate.compareTo(nextLoad.startDate) > 0) {
          continue;
        }
        idxStart = i;
        if (startDate.compareTo(nextLoad.startDate) < 0) {
          loads.add(i, new Load(startDate, null, currentLoad, null));
        }
        break;
      }
    }
    return idxStart;
  }

  public HumanResource getResource() {
    return myResource;
  }

  public List<Load> getLoads() {
    return myLoads;
  }

  public List<Load> getDaysOff() {
    return myDaysOff;
  }

  /**
   * @return a list of lists of <code>Load</code> instances. Each list contains
   *         a set of <code>Load</code>
   */
  public List<Load> getTasksLoads() {
    return myTasksLoads;
  }

  public Map<Task, List<Load>> getSeparatedTaskLoads() {
    HashMap<Task, List<Load>> result = new HashMap<>();
    List<Load> taskLoads = getTasksLoads();
    for (Load nextLoad : taskLoads) {
      Task nextTask = nextLoad.refTask;
      List<Load> partition = result.computeIfAbsent(nextTask, k -> new ArrayList<>());
      partition.add(nextLoad);
    }
    return result;
  }
}