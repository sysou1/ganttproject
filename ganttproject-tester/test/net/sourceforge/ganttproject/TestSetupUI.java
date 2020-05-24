package net.sourceforge.ganttproject;

import biz.ganttproject.core.calendar.GPCalendarCalc;
import net.sourceforge.ganttproject.action.zoom.ZoomActionSet;
import net.sourceforge.ganttproject.chart.GanttChart;
import net.sourceforge.ganttproject.chart.TimelineChart;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.gui.*;
import net.sourceforge.ganttproject.parser.ParserFactory;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.task.TaskContainmentHierarchyFacade;
import net.sourceforge.ganttproject.task.TaskManager;

import java.io.IOException;
import java.util.List;

public class TestSetupUI {

    private UIFacade uiFacade;
    private GanttProjectBase ganttProject;

    public TestSetupUI() throws Exception {
        String[] args = {};
        AppKt.main(args);
        ganttProject = new GanttProjectBase() {
            @Override
            public String getProjectName() {
                return null;
            }

            @Override
            public void setProjectName(String projectName) {

            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void setDescription(String description) {

            }

            @Override
            public String getOrganization() {
                return null;
            }

            @Override
            public void setOrganization(String organization) {

            }

            @Override
            public String getWebLink() {
                return null;
            }

            @Override
            public void setWebLink(String webLink) {

            }

            @Override
            public UIConfiguration getUIConfiguration() {
                return null;
            }

            @Override
            public HumanResourceManager getHumanResourceManager() {
                return null;
            }

            @Override
            public RoleManager getRoleManager() {
                return null;
            }

            @Override
            public TaskManager getTaskManager() {
                return null;
            }

            @Override
            public TaskContainmentHierarchyFacade getTaskContainment() {
                return null;
            }

            @Override
            public GPCalendarCalc getActiveCalendar() {
                return null;
            }

            @Override
            public void setModified() {

            }

            @Override
            public void close() {

            }

            @Override
            public Document getDocument() {
                return null;
            }

            @Override
            protected ParserFactory getParserFactory() {
                return null;
            }

            @Override
            public void setModified(boolean modified) {

            }

            @Override
            public void setDocument(Document document) {

            }

            @Override
            public boolean isModified() {
                return false;
            }

            @Override
            public void open(Document document) throws IOException, Document.DocumentException {

            }

            @Override
            public List<GanttPreviousState> getBaselines() {
                return null;
            }

            @Override
            public ZoomActionSet getZoomActionSet() {
                return null;
            }

            @Override
            public GanttChart getGanttChart() {
                return null;
            }

            @Override
            public TimelineChart getResourceChart() {
                return null;
            }

            @Override
            public int getViewIndex() {
                return 0;
            }

            @Override
            public void setViewIndex(int viewIndex) {

            }

            @Override
            public int getGanttDividerLocation() {
                return 0;
            }

            @Override
            public void setGanttDividerLocation(int location) {

            }

            @Override
            public int getResourceDividerLocation() {
                return 0;
            }

            @Override
            public void setResourceDividerLocation(int location) {

            }

            @Override
            public void refresh() {

            }

            @Override
            public TaskTreeUIFacade getTaskTree() {
                return null;
            }

            @Override
            public ResourceTreeUIFacade getResourceTree() {
                return null;
            }
        };
        uiFacade = ganttProject.getUIFacade();
    }

    public UIFacade getUiFacade() {
        return uiFacade;
    }

    public IGanttProject getGanttProject(){
        return ganttProject;
    }

}