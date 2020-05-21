package net.sourceforge.ganttproject.roles;

import junit.framework.TestCase;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.Role;
import net.sourceforge.ganttproject.roles.RoleSetImpl;
import net.sourceforge.ganttproject.task.CustomColumnsManager;

public class TestHumanResaurceManager  extends TestCase {

    public void testNewHumanResourceCreation() {
        RoleSetImpl roleSet = new RoleSetImpl("", new RoleManagerImpl());
        Role defaultRole = roleSet.createRole("manager");
        HumanResourceManager humanResourceManager = new HumanResourceManager(defaultRole, new CustomColumnsManager());
        HumanResource resource = humanResourceManager.newHumanResource();
        HumanResource result = new HumanResource("", -1, humanResourceManager);
        assertEquals(result, resource);
    }

}
