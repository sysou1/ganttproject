package net.sourceforge.ganttproject.io;

import junit.framework.TestCase;
import net.sourceforge.ganttproject.*;
import net.sourceforge.ganttproject.document.Document;
import net.sourceforge.ganttproject.document.DocumentCreator;
import net.sourceforge.ganttproject.gui.UIConfiguration;
import net.sourceforge.ganttproject.gui.UIFacade;
import net.sourceforge.ganttproject.parser.AbstractTagHandler;
import net.sourceforge.ganttproject.parser.ParsingContext;
import net.sourceforge.ganttproject.parser.TagHandler;
import net.sourceforge.ganttproject.task.TaskManager;

import java.awt.*;
import java.io.File;
import java.io.InputStream;
import org.xml.sax.Attributes;

public class TestGantXMLOpen extends TestCase {

    GanttXMLOpen ganttXMLOpen;
    TestSetupUI testSetupUI;
    UIFacade uiFacade;

    @Override
    protected void setUp() throws Exception {

        Color limeColor = new Color(147, 246, 0);

        PrjInfos prjInfos = new PrjInfos();
        UIConfiguration uiConfiguration = new UIConfiguration(limeColor, true);
        TestSetupHelper.TaskManagerBuilder taskManagerBuilder = new TestSetupHelper.TaskManagerBuilder();
        TaskManager taskManager = taskManagerBuilder.build();
        testSetupUI = new TestSetupUI();
        uiFacade = testSetupUI.getUiFacade();

        ganttXMLOpen = new GanttXMLOpen(prjInfos, uiConfiguration, taskManager, uiFacade);
    }

    public void testLoadInputStream() throws Exception {

        CreateXMLFile createXMLFile = new CreateXMLFile();

        IGanttProject ganttProject = testSetupUI.getGanttProject();
        DocumentCreator documentCreator = new DocumentCreator(ganttProject, uiFacade, null);

        String path = System.getProperty("user.dir");
        path += createXMLFile.getFileName();
        Document physicalDocument = documentCreator.newDocument(path);

        InputStream is = physicalDocument.getInputStream();
        boolean isDone = ganttXMLOpen.load(is);

        assertTrue(isDone);
    }

    public void testLoadFile() throws Exception {

        CreateXMLFile createXMLFile = new CreateXMLFile();

        String path = System.getProperty("user.dir");
        path += createXMLFile.getFileName();
        File file = new File(path);

        assertTrue(ganttXMLOpen.load(file));

        String wrongPath = System.getProperty("user.dir");
        wrongPath += "/wrongFile.xml";
        file = new File(wrongPath);

        assertFalse(ganttXMLOpen.load(file));
    }

    public void testGetContext() {
        assertEquals(ParsingContext.class, ganttXMLOpen.getContext().getClass());
        assertNotNull(ganttXMLOpen.getContext());
    }

    public void testStartElementFromDefaultTagHandler() throws Exception{

        TagHandler defaultTagHandler = ganttXMLOpen.getDefaultTagHandler();
        assertNotNull(defaultTagHandler);

        String namespaceURI = "";
        String sName = "";
        String qName = "ganttproject-options";
        Attributes attributes = null;

        defaultTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertFalse(defaultTagHandler.hasCdata());

        qName = "description";
        defaultTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertTrue(defaultTagHandler.hasCdata());

        sName = "notes";
        qName = "";
        defaultTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertTrue(defaultTagHandler.hasCdata());

        sName = "tasks";
        defaultTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertFalse(defaultTagHandler.hasCdata());

        // rest function is for attributes != null
    }

    public void testEndElementFromDefaultTagHandler() throws Exception{

        TagHandler defaultTagHandler = ganttXMLOpen.getDefaultTagHandler();
        assertNotNull(defaultTagHandler);

        String namespaceURI = "";
        String sName = "description";
        String qName = "ganttproject-options";

        defaultTagHandler.startElement(namespaceURI, sName, qName, null);
        assertTrue(defaultTagHandler.hasCdata());
        defaultTagHandler.endElement(namespaceURI, sName, qName);
        assertTrue(defaultTagHandler.hasCdata());

        defaultTagHandler.startElement(namespaceURI, sName, qName, null);
        assertTrue(defaultTagHandler.hasCdata());
        qName = "description";
        defaultTagHandler.endElement(namespaceURI, sName, qName);
        assertFalse(defaultTagHandler.hasCdata());

//        defaultTagHandler.startElement(namespaceURI, sName, qName, null);
//        assertTrue(defaultTagHandler.hasCdata());
//        qName = "notes";
//        defaultTagHandler.endElement(namespaceURI, sName, qName); --> error EmptyStackException --> ParsingContext (line 35, peekTask)
//        assertFalse(defaultTagHandler.hasCdata());

        sName = "notes";

        defaultTagHandler.startElement(namespaceURI, sName, qName, null);
        assertTrue(defaultTagHandler.hasCdata());
        qName = "project";
        defaultTagHandler.endElement(namespaceURI, sName, qName);
        assertFalse(defaultTagHandler.hasCdata());

        defaultTagHandler.startElement(namespaceURI, sName, qName, null);
        assertTrue(defaultTagHandler.hasCdata());
        qName = "tasks";
        defaultTagHandler.endElement(namespaceURI, sName, qName);
        assertFalse(defaultTagHandler.hasCdata());
    }

    public void testTimelineTagHandler() throws  Exception{
        TagHandler timelineTagHandler = ganttXMLOpen.getTimelineTagHandler();
        assertNotNull(timelineTagHandler);

        String namespaceURI = "";
        String sName = "";
        String qName = "ganttproject-options";
        Attributes attributes = null;

        timelineTagHandler.startElement(namespaceURI, sName, qName, attributes);

        qName = "timeline";
        timelineTagHandler.startElement(namespaceURI, sName, qName, attributes);

        timelineTagHandler.endElement(namespaceURI, sName, qName);
    }
}