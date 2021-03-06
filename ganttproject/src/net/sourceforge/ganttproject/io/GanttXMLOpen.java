/*
GanttProject is an opensource project management tool. License: GPL3
Copyright (C) 2002-2011 Thomas Alexandre, GanttProject Team

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
package net.sourceforge.ganttproject.io;

import biz.ganttproject.core.time.GanttCalendar;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.PrjInfos;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.parser.AbstractTagHandler;
import net.sourceforge.ganttproject.parser.GPParser;
import net.sourceforge.ganttproject.parser.ParsingContext;
import net.sourceforge.ganttproject.parser.ParsingListener;
import net.sourceforge.ganttproject.parser.TagHandler;
import net.sourceforge.ganttproject.task.Task;
import net.sourceforge.ganttproject.task.TaskManager;
import org.xml.sax.Attributes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Allows to load a gantt file from xml format, using SAX parser
 */
public class GanttXMLOpen implements GPParser {

  private final ArrayList<TagHandler> myTagHandlers = new ArrayList<>();

  private final ArrayList<ParsingListener> myListeners = new ArrayList<>();

  private final ParsingContext myContext;

  private final TaskManager myTaskManager;

  private int viewIndex;

  private int ganttDividerLocation;

  private int resourceDividerLocation;

  private PrjInfos myProjectInfo = null;

  private UIFacade myUIFacade = null;

  private TagHandler myTimelineTagHandler = new TimelineTagHandler();

  public GanttXMLOpen(PrjInfos info, TaskManager taskManager, UIFacade uiFacade) {
    this(taskManager);
    myProjectInfo = info;
    this.viewIndex = 0;
    this.ganttDividerLocation = 300;
    this.resourceDividerLocation = 300;
    myUIFacade = uiFacade;
  }

  public GanttXMLOpen(TaskManager taskManager) {
    myContext = new ParsingContext();
    myTaskManager = taskManager;
  }

  @Override
  public boolean load(InputStream inStream) throws IOException {
    XmlParser parser = new XmlParser(myTagHandlers, myListeners);
    parser.parse(inStream);
    myUIFacade.setViewIndex(viewIndex);
    myUIFacade.setGanttDividerLocation(ganttDividerLocation);
    if (resourceDividerLocation != 0) {
      myUIFacade.setResourceDividerLocation(resourceDividerLocation);
    }
    return true;
  }

  public boolean load(File file) {
    XmlParser parser = new XmlParser(myTagHandlers, myListeners);
    try {
      parser.parse(new BufferedInputStream(new FileInputStream(file)));
    } catch (Exception e) {
      myUIFacade.showErrorDialog(e);
      return false;
    }
    return true;
  }

  @Override
  public void addTagHandler(TagHandler handler) {
    myTagHandlers.add(handler);
  }

  @Override
  public void addParsingListener(ParsingListener listener) {
    myListeners.add(listener);
  }

  @Override
  public ParsingContext getContext() {
    return myContext;
  }

  @Override
  public TagHandler getDefaultTagHandler() {
    return new DefaultTagHandler();
  }

  private class DefaultTagHandler extends AbstractTagHandler {
    private String projectTag = "project";
    private String tasksTag = "tasks";
    private String descriptionTag = "description";
    private String notesTag = "notes";
    private final Set<String> myTags = ImmutableSet.of(projectTag, tasksTag, descriptionTag, notesTag);
    private boolean hasCdata = false;

    DefaultTagHandler() {
      super(null, true);
    }

    @Override
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) {
      clearCdata();
      String eName = getName(sName, qName);
      setTagStarted(myTags.contains(eName));
      hasCdata = descriptionTag.equals(eName) || notesTag.equals(eName);
      if (eName.equals(tasksTag)) {
        myTaskManager.setZeroMilestones(null);
      }
      if (attrs != null) {
        for (int i = 0; i < attrs.getLength(); i++) {
          String aName = getName(attrs.getLocalName(i), attrs.getQName(i));
          if (eName.equals(projectTag)) {
            setProjectInfo(attrs, i, aName);
          } else if (eName.equals(tasksTag) && eName.equals("empty-milestones")) {
            myTaskManager.setZeroMilestones(Boolean.parseBoolean(attrs.getValue(i)));
          }
        }
      }
    }

    @Override
    public void endElement(String namespaceURI, String sName, String qName) {
      if (!myTags.contains(qName)) {
        return;
      }
      if (descriptionTag.equals(qName)) {
        myProjectInfo.setDescription(getCdata());
      } else if (notesTag.equals(qName)) {
        Task currentTask = getContext().peekTask();
        currentTask.setNotes(getCdata());
      }
      hasCdata = false;
      setTagStarted(false);
    }

    @Override
    public boolean hasCdata() {
      return hasCdata;
    }
  }

  private String getName(String localName, String qName2) {
    String aName = localName;
    if ("".equals(aName)) {
      aName = qName2;
    }
    return aName;
  }

  private void setProjectInfo(Attributes attrs, int i, String aName) {
    switch (aName) {
      case "name":
        myProjectInfo.setName(attrs.getValue(i));
        break;
      case "company":
        myProjectInfo.setOrganization(attrs.getValue(i));
        break;
      case "webLink":
        myProjectInfo.setWebLink(attrs.getValue(i));
        break;
      case "view-date":
        myUIFacade.getScrollingManager().scrollTo(GanttCalendar.parseXMLDate(attrs.getValue(i)).getTime());
        break;
      case "view-index":
        viewIndex = Integer.valueOf(attrs.getValue(i)).hashCode();
        break;
      case "gantt-divider-location":
        ganttDividerLocation = Integer.parseInt(attrs.getValue(i));
        break;
      case "resource-divider-location":
        resourceDividerLocation = Integer.parseInt(attrs.getValue(i));
        break;
      default:
        break;
    }
  }

  @Override
  public TagHandler getTimelineTagHandler() {
    return myTimelineTagHandler;
  }

  class TimelineTagHandler extends AbstractTagHandler implements ParsingListener {
    private final List<Integer> myIds = Lists.newArrayList();

    public TimelineTagHandler() {
      super("timeline", true);
    }

    @Override
    public void startParsing() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void finishParsing() {
      myUIFacade.getCurrentTaskView().getTimelineTasks().clear();
      for (Integer id : myIds) {
        Task t = myTaskManager.getTask(id);
        if (t != null) {
          myUIFacade.getCurrentTaskView().getTimelineTasks().add(t);
        }
      }
    }

    @Override
    protected boolean onStartElement(Attributes attrs) {
      clearCdata();
      return super.onStartElement(attrs);
    }

    @Override
    protected void onEndElement() {
      String[] ids = getCdata().split(",");
      for (String id : ids) {
        try {
          myIds.add(Integer.valueOf(id.trim()));
        } catch (NumberFormatException e) {
          GPLogger.logToLogger(e);
        }
      }
      clearCdata();
    }
  }
}
