package net.sourceforge.ganttproject.resource;

import junit.framework.TestCase;
import net.sourceforge.ganttproject.roles.Role;
import net.sourceforge.ganttproject.roles.RoleImpl;

import java.math.BigDecimal;

public class TestResourceBuilder extends TestCase {

    public void testResourceBuilderNullName(){
        HumanResourceManager humanResourceManager = new HumanResourceManager(null, null);

        HumanResource nullName = humanResourceManager.newResourceBuilder().withName(null).build();
        HumanResource resultNullName = humanResourceManager.newResourceBuilder().build();

        assertEquals(null, resultNullName);
    }

    public void testResourceBuilder(){
        Role defaultRole = new RoleImpl(1, "Default", null);
        HumanResourceManager humanResourceManager = new HumanResourceManager(defaultRole, null);

        HumanResource hr = humanResourceManager.newResourceBuilder()
                .withName("Joe")
                .withID("1")
                .withEmail("joe@hotmail.com")
                .withPhone("040404")
                .withRole("Manager")
                .withStandardRate("10.5")
                .build();

        HumanResource resultExpected = new HumanResource("Joe", 1, humanResourceManager);
        resultExpected.setMail("joe@hotmail.com");
        resultExpected.setPhone("040404");
        resultExpected.setStandardPayRate(BigDecimal.valueOf(10.5));

        assertEquals(resultExpected, hr);
    }
}
