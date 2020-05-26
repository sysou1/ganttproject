package net.sourceforge.ganttproject.resource;

import junit.framework.TestCase;
import net.sourceforge.ganttproject.CustomPropertyDefinition;
import net.sourceforge.ganttproject.DefaultCustomPropertyDefinition;
import net.sourceforge.ganttproject.ResourceDefaultColumn;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.roles.Role;
import net.sourceforge.ganttproject.roles.RoleImpl;
import net.sourceforge.ganttproject.task.TaskManager;

import java.math.BigDecimal;

public class TestResourceNode extends TestCase {

    public void testGetStandardField(){
        TestSetupHelper.TaskManagerBuilder builder = TestSetupHelper.newTaskManagerBuilder();
        TaskManager taskManager = builder.build();
        HumanResourceManager humanResourceManager = builder.getResourceManager();

        HumanResource joe = new HumanResource("Joe", 1, humanResourceManager);
        HumanResource jane = new HumanResource("Jane", 2, humanResourceManager);

        CustomPropertyDefinition customPropertyDefinition = new DefaultCustomPropertyDefinition("property 1");

        Role role = new RoleImpl(1, "Programmer", null);

        ResourceNode resourceNode = new ResourceNode(joe);
        ResourceNode resourceNode1 = new ResourceNode(jane);

        resourceNode.setStandardField(ResourceDefaultColumn.values()[0], "Alex");
        resourceNode.setStandardField(ResourceDefaultColumn.values()[1], role);
        resourceNode.setStandardField(ResourceDefaultColumn.values()[2], "alex@hotmail.com");
        resourceNode.setStandardField(ResourceDefaultColumn.values()[3], "0101");
        resourceNode.setStandardField(ResourceDefaultColumn.values()[5], 10.0);
        resourceNode.setCustomField(customPropertyDefinition, "new val");

        assertEquals("Alex", resourceNode.getStandardField(ResourceDefaultColumn.values()[0]));
        assertEquals(role, resourceNode.getStandardField(ResourceDefaultColumn.values()[1]));
        assertEquals("alex@hotmail.com", resourceNode.getStandardField(ResourceDefaultColumn.values()[2]));
        assertEquals("0101", resourceNode.getStandardField(ResourceDefaultColumn.values()[3]));
        assertEquals(BigDecimal.valueOf(10.0), resourceNode.getStandardField(ResourceDefaultColumn.values()[5]));
        assertEquals("new val", resourceNode.getCustomField(customPropertyDefinition));
        assertEquals("Alex", resourceNode.toString());
        assertTrue(resourceNode.equals(resourceNode));
        assertFalse(resourceNode.equals(resourceNode1));
    }

}
