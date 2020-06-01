package net.sourceforge.ganttproject.parser;

import junit.framework.TestCase;
import net.sourceforge.ganttproject.GanttTask;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManagerImpl;
import net.sourceforge.ganttproject.task.*;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import static net.sourceforge.ganttproject.TestSetupHelper.newFriday;

public class TestAllocationTagHandler extends TestCase {

    private TestSetupHelper.TaskManagerBuilder builder;
    private TaskManager taskManager;
    private HumanResourceManager humanResourceManager;

    public TaskManager getTaskManager() {
        return taskManager;
    }

    public HumanResourceManager getHumanResourceManager() {
        return humanResourceManager;
    }

    private Attributes createAttr(){
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "task-id", "task-id", "CDATA", "1");
        attr.addAttribute("", "resource-id", "resource-id", "CDATA", "1");
        attr.addAttribute("", "load", "load", "CDATA", "2");
        attr.addAttribute("", "responsible", "responsible", "CDATA", "false");
        attr.addAttribute("", "function", "function", "CDATA", "Default:0");

        return attr;
    }

    private void createElements(){
        builder = TestSetupHelper.newTaskManagerBuilder();
        taskManager = builder.build();
        humanResourceManager = builder.getResourceManager();
    }

    public void testAllocationHandler(){
        createElements();
        HumanResourceManager humanResourceManager = getHumanResourceManager();

        HumanResource joe = new HumanResource("Joe", 1, getHumanResourceManager());
        humanResourceManager.addHumanResource(joe);

        Task task = new GanttTask("task1", newFriday(),10, (TaskManagerImpl) getTaskManager(), 1);
        getTaskManager().registerTask(task);

        AllocationTagHandler al = new AllocationTagHandler(humanResourceManager, getTaskManager(), new RoleManagerImpl());

        boolean result = al.onStartElement(createAttr());

        al.parsingFinished();

        assertTrue(result);
    }

}
