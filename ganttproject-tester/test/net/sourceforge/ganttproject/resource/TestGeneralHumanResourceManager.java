package net.sourceforge.ganttproject.resource;

import junit.framework.TestCase;

public class TestGeneralHumanResourceManager extends TestCase {

    public void testGetResourcesArray(){
        HumanResourceManager humanResourceManager = new HumanResourceManager(null, null);
        HumanResource resource = new HumanResource("Joe", 1, humanResourceManager);
        humanResourceManager.addHumanResource(resource);
        HumanResource[] resourceArray = humanResourceManager.getResourcesArray();

        HumanResource[] expectedResourceArray = new HumanResource[1];
        expectedResourceArray[0] = resource;

        assertEquals(expectedResourceArray[0], resourceArray[0]);
    }

    public void testClear(){
        HumanResourceManager humanResourceManager = new HumanResourceManager(null, null);
        HumanResource resource = new HumanResource("Joe", 1, humanResourceManager);
        humanResourceManager.addHumanResource(resource);
        humanResourceManager.clear();

        assertEquals(0, humanResourceManager.getResources().size());
    }

    public void testUpAndDown(){
        HumanResourceManager humanResourceManager = new HumanResourceManager(null, null);
        HumanResource resource1 = new HumanResource("Joe", 1, humanResourceManager);
        HumanResource resource2 = new HumanResource("Alex", 2, humanResourceManager);
        humanResourceManager.addHumanResource(resource1);
        humanResourceManager.addHumanResource(resource2);
        humanResourceManager.moveDown(resource1);

        HumanResourceManager humanResourceManager1 = new HumanResourceManager(null, null);
        humanResourceManager1.addHumanResource(resource2);
        humanResourceManager1.addHumanResource(resource1);
        humanResourceManager1.moveUp(resource1);

        HumanResourceManager humanResourceManagerExpected = new HumanResourceManager(null, null);
        humanResourceManagerExpected.addHumanResource(resource2);
        humanResourceManagerExpected.addHumanResource(resource1);

        HumanResourceManager humanResourceManagerExpected1 = new HumanResourceManager(null, null);
        humanResourceManagerExpected1.addHumanResource(resource1);
        humanResourceManagerExpected1.addHumanResource(resource2);

        assertEquals(humanResourceManagerExpected.getResources(), humanResourceManager.getResources());
        assertEquals(humanResourceManagerExpected1.getResources(), humanResourceManager1.getResources());
    }

}
