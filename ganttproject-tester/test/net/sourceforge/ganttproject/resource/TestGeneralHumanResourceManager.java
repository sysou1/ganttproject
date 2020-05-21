package net.sourceforge.ganttproject.resource;

import junit.framework.TestCase;

public class TestGeneralHumanResourceManager extends TestCase {

    public void testGetResourcesArray(){
        HumanResourceManager humanResourceManager = new HumanResourceManager(null, null);
        HumanResource resource = new HumanResource("Joe", 1, humanResourceManager);
        humanResourceManager.add(resource);
        HumanResource[] resourceArray = humanResourceManager.getResourcesArray();

        HumanResource[] expectedResourceArray = new HumanResource[1];
        expectedResourceArray[0] = resource;

        assertEquals(expectedResourceArray[0], resourceArray[0]);
    }

    public void testClear(){
        HumanResourceManager humanResourceManager = new HumanResourceManager(null, null);
        HumanResource resource = new HumanResource("Joe", 1, humanResourceManager);
        humanResourceManager.add(resource);
        humanResourceManager.clear();

        assertEquals(0, humanResourceManager.getResources().size());
    }

    public void testUpAndDown(){
        HumanResourceManager humanResourceManager = new HumanResourceManager(null, null);
        HumanResource resource1 = new HumanResource("Joe", 1, humanResourceManager);
        HumanResource resource2 = new HumanResource("Alex", 2, humanResourceManager);
        humanResourceManager.add(resource1);
        humanResourceManager.add(resource2);
        humanResourceManager.down(resource1);

        HumanResourceManager humanResourceManager1 = new HumanResourceManager(null, null);
        humanResourceManager1.add(resource2);
        humanResourceManager1.add(resource1);
        humanResourceManager1.up(resource1);

        HumanResourceManager humanResourceManagerExpected = new HumanResourceManager(null, null);
        humanResourceManagerExpected.add(resource2);
        humanResourceManagerExpected.add(resource1);

        HumanResourceManager humanResourceManagerExpected1 = new HumanResourceManager(null, null);
        humanResourceManagerExpected1.add(resource1);
        humanResourceManagerExpected1.add(resource2);

        assertEquals(humanResourceManagerExpected.getResources(), humanResourceManager.getResources());
        assertEquals(humanResourceManagerExpected1.getResources(), humanResourceManager1.getResources());
    }

}
