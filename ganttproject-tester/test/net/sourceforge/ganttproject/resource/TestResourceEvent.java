package net.sourceforge.ganttproject.resource;

import junit.framework.TestCase;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.task.TaskManager;

public class TestResourceEvent extends TestCase {

    public void testResourceevent(){


        TestSetupHelper.TaskManagerBuilder builder = TestSetupHelper.newTaskManagerBuilder();
        TaskManager taskManager = builder.build();
        HumanResourceManager humanResourceManager = builder.getResourceManager();

        HumanResource joe = new HumanResource("Joe", 1, humanResourceManager);
        HumanResource jane = new HumanResource("Jane", 2, humanResourceManager);

        HumanResource[] resources = {joe, jane};

        ResourceEvent resourceEvent = new ResourceEvent(humanResourceManager, resources);

        assertEquals(humanResourceManager, resourceEvent.getManager());
        assertEquals(resources, resourceEvent.getResources());
        assertEquals(joe, resourceEvent.getFirstResource());

    }

}
