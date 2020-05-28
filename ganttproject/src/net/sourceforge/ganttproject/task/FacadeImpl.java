package net.sourceforge.ganttproject.task;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import net.sourceforge.ganttproject.util.collect.Pair;

import java.util.*;

final class FacadeImpl implements TaskContainmentHierarchyFacade {

  private final TaskManagerImpl taskManager;
  private List<Task> myPathBuffer = new ArrayList<>();

  public FacadeImpl(TaskManagerImpl taskManager) {
    this.taskManager = taskManager;
  }

  @Override
  public Task[] getNestedTasks(Task container) {
    return container.getNestedTasks();
  }

  @Override
  public Task[] getDeepNestedTasks(Task container) {
    ArrayList<Task> result = new ArrayList<>();
    addDeepNestedTasks(container, result);
    return result.toArray(new Task[result.size()]);
  }

  private void addDeepNestedTasks(Task container, ArrayList<Task> result) {
    Task[] nested = container.getNestedTasks();
    result.addAll(Arrays.asList(nested));
    for (int i = 0; i < nested.length; i++) {
      addDeepNestedTasks(nested[i], result);
    }
  }

  @Override
  public boolean hasNestedTasks(Task container) {
    return container.getNestedTasks().length > 0;
  }

  @Override
  public Task getRootTask() {
    return taskManager.getRootTask();
  }

  @Override
  public Task getContainer(Task nestedTask) {
    return nestedTask.getSupertask();
  }

  @Override
  public void sort(Comparator<Task> comparator) {
    throw new UnsupportedOperationException("Sort is not available int this implementation. It is stateless!");
  }

  @Override
  public Task getPreviousSibling(Task nestedTask) {
    int pos = getTaskIndex(nestedTask);
    return pos == 0 ? null : nestedTask.getSupertask().getNestedTasks()[pos - 1];
  }

  @Override
  public Task getNextSibling(Task nestedTask) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getTaskIndex(Task nestedTask) {
    Task container = nestedTask.getSupertask();
    if (container == null) {
      return 0;
    }
    return Arrays.asList(container.getNestedTasks()).indexOf(nestedTask);
  }

  @Override
  public boolean areUnrelated(Task first, Task second) {
    if (first.equals(second)) {
      return false;
    }
    myPathBuffer.clear();
    for (Task container = getContainer(first); container != null; container = getContainer(container)) {
      myPathBuffer.add(container);
    }
    if (myPathBuffer.contains(second)) {
      return false;
    }
    myPathBuffer.clear();
    for (Task container = getContainer(second); container != null; container = getContainer(container)) {
      myPathBuffer.add(container);
    }
    return !myPathBuffer.contains(first);
  }

  @Override
  public void move(Task whatMove, Task whereMove) {
    whatMove.move(whereMove);
  }

  @Override
  public void move(Task whatMove, Task whereMove, int index) {
    whatMove.move(whereMove);
  }

  @Override
  public int getDepth(Task task) {
    int depth = 0;
    while (task != taskManager.getMyRoot()) {
      task = task.getSupertask();
      depth++;
    }
    return depth;
  }

  @Override
  public int compareDocumentOrder(Task task1, Task task2) {
    if (task1 == task2) {
      return 0;
    }
    List<Task> buffer1 = new ArrayList<>();
    for (Task container = task1; container != null; container = getContainer(container)) {
      buffer1.add(0, container);
    }
    List<Task> buffer2 = new ArrayList<>();
    for (Task container = task2; container != null; container = getContainer(container)) {
      buffer2.add(0, container);
    }
    if (buffer1.get(0) != getRootTask() && buffer2.get(0) == getRootTask()) {
      return -1;
    }
    if (buffer1.get(0) == getRootTask() && buffer2.get(0) != getRootTask()) {
      return 1;
    }

    int i = 0;
    Task commonRoot = null;
    while (true) {
      if (i == buffer1.size()) {
        return -1;
      }
      if (i == buffer2.size()) {
        return 1;
      }
      Task root1 = buffer1.get(i);
      Task root2 = buffer2.get(i);
      if (root1 != root2) {
        if(commonRoot == null) {
          throw new IllegalArgumentException("Failure comparing task=" + task1 + " and task=" + task2 + "\n. Path1="
                  + buffer1 + "\nPath2=" + buffer2);
        }
        Task[] nestedTasks = commonRoot.getNestedTasks();
        for (int j = 0; j < nestedTasks.length; j++) {
          if (nestedTasks[j] == root1) {
            return -1;
          }
          if (nestedTasks[j] == root2) {
            return 1;
          }
        }
        throw new IllegalStateException("We should not be here");
      }
      i++;
      commonRoot = root1;
    }
  }

  @Override
  public boolean contains(Task task) {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Task> getTasksInDocumentOrder() {
    List<Task> result = Lists.newArrayList();
    LinkedList<Task> deque = new LinkedList<>();
    deque.addFirst(getRootTask());
    while (!deque.isEmpty()) {
      Task head = deque.poll();
      result.add(head);
      deque.addAll(0, Arrays.asList(head.getNestedTasks()));
    }
    result.remove(0);
    return result;
  }


  @Override
  public void breadthFirstSearch(Task root, Predicate<Pair<Task, Task>> predicate) {
    Preconditions.checkNotNull(root);
    Queue<Task> queue = Queues.newArrayDeque();
    if (predicate.apply(Pair.create((Task) null, root))) {
      queue.add(root);
    }
    while (!queue.isEmpty()) {
      Task head = queue.poll();
      for (Task child : head.getNestedTasks()) {
        if (predicate.apply(Pair.create(head, child))) {
          queue.add(child);
        }
      }
    }
  }

  @Override
  public List<Task> breadthFirstSearch(Task root, final boolean includeRoot) {
    final Task finalRoot = (root == null) ? getRootTask() : root;
    final List<Task> result = Lists.newArrayList();
    breadthFirstSearch(finalRoot, new Predicate<Pair<Task,Task>>() {
      public boolean apply(Pair<Task, Task> parentChild) {
        if (includeRoot || parentChild.first() != null) {
          result.add(parentChild.second());
        }
        return true;
      }
    });
    return result;
  }

  @Override
  public List<Integer> getOutlinePath(Task task) {
    throw new UnsupportedOperationException();
  }
}
