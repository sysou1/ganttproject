package net.sourceforge.ganttproject.io;

import junit.framework.TestCase;
import net.sourceforge.ganttproject.*;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.DocumentCreator;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.task.TaskManager;

import java.awt.*;
import java.io.InputStream;

public class TestGantXMLOpen extends TestCase {

    public void testOpen() throws Exception {

        CreateXMLFile createXMLFile = new CreateXMLFile();

        Color limeColor = new Color(147, 246, 0);

        PrjInfos prjInfos = new PrjInfos();
        UIConfiguration uiConfiguration = new UIConfiguration(limeColor, true);
        TestSetupHelper.TaskManagerBuilder taskManagerBuilder = new TestSetupHelper.TaskManagerBuilder();
        TaskManager taskManager = taskManagerBuilder.build();
        TestSetupUI testSetupUI = new TestSetupUI();
        UIFacade uiFacade = testSetupUI.getUiFacade();

        GanttXMLOpen ganttXMLOpen = new GanttXMLOpen(prjInfos, uiConfiguration, taskManager, uiFacade);

        IGanttProject ganttProject = testSetupUI.getGanttProject();
        DocumentCreator documentCreator = new DocumentCreator(ganttProject, uiFacade, null);

        String path = System.getProperty("user.dir");
        path += createXMLFile.getFileName();
        Document physicalDocument = documentCreator.newDocument(path);

        InputStream is = physicalDocument.getInputStream();
        boolean isDone = ganttXMLOpen.load(is);

        assertTrue(isDone);
    }
}