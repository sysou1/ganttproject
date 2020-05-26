package net.sourceforge.ganttproject.parser;

import junit.framework.TestCase;
import net.sourceforge.ganttproject.CustomPropertyDefinition;
import net.sourceforge.ganttproject.CustomPropertyManager;
import net.sourceforge.ganttproject.TestSetupHelper;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.roles.RoleManagerImpl;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import java.math.BigDecimal;

public class TestResourceTagHandler extends TestCase {

    private TestSetupHelper.TaskManagerBuilder builder;
    private HumanResourceManager humanResourceManager;
    private RoleManager roleManager;
    private CustomPropertyManager customPropertyManager;

    private void createElements() {
        builder = TestSetupHelper.newTaskManagerBuilder();
        humanResourceManager = builder.getResourceManager();
        roleManager = new RoleManagerImpl();
        customPropertyManager = humanResourceManager.getCustomPropertyManager();
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

    private Attributes createCorrectAttr() {
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "id", "id", "CDATA", "1");
        attr.addAttribute("", "name", "name", "CDATA", "standard");
        attr.addAttribute("", "contacts", "contacts", "CDATA", "john@gmail.com");
        attr.addAttribute("", "phone", "phone", "CDATA", "888888888");
        attr.addAttribute("", "function", "function", "CDATA", "3");
        attr.addAttribute("", "definition-id", "definition-id", "CDATA", "1");
        attr.addAttribute("", "value", "value", "CDATA", "4");
        attr.addAttribute("", "type", "type", "CDATA", "type1");
        attr.addAttribute("", "default-value", "default-value", "CDATA", "2");

        return attr;
    }

    public void testStartElementWithWrongQName() {
        createElements();
        ResourceTagHandler resourceTagHandler = new ResourceTagHandler(humanResourceManager, roleManager, customPropertyManager);
        assertNotNull(resourceTagHandler);

        String namespaceURI = "";
        String sName = "description";
        String qName = "ganttproject";
        Attributes attributes = null;
        resourceTagHandler.setTagStarted(false);
        resourceTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertFalse(resourceTagHandler.isTagStarted());
    }

    public void testLoadResource() {
        createElements();
        ResourceTagHandler resourceTagHandler = new ResourceTagHandler(humanResourceManager, roleManager, customPropertyManager);
        assertNotNull(resourceTagHandler);

        String namespaceURI = "";
        String sName = "description";
        String qName = "resource";
        int startSizeOfResources = humanResourceManager.getResources().size();

        Attributes attributes = createAttr();
        resourceTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertTrue(resourceTagHandler.isTagStarted());

        assertEquals(startSizeOfResources + 1, humanResourceManager.getResources().size());
        assertEquals("", humanResourceManager.getResources().get(0).getName());
        assertEquals("", humanResourceManager.getResources().get(0).getMail());
        assertEquals("", humanResourceManager.getResources().get(0).getPhone());
        assertEquals(0, humanResourceManager.getResources().get(0).getRole().getID());

        attributes = createCorrectAttr();
        resourceTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertTrue(resourceTagHandler.isTagStarted());

        assertEquals(startSizeOfResources + 2, humanResourceManager.getResources().size());
        assertEquals(1, humanResourceManager.getResources().get(1).getId());
        assertEquals("standard", humanResourceManager.getResources().get(1).getName());
        assertEquals("john@gmail.com", humanResourceManager.getResources().get(1).getMail());
        assertEquals("888888888", humanResourceManager.getResources().get(1).getPhone());
    }

    public void testLoadCustomProperty() {

        createElements();
        HumanResource humanResource = new HumanResource("Foo", 1, humanResourceManager);
        humanResourceManager.add(humanResource);
        CustomPropertyDefinition def1 = customPropertyManager.createDefinition("1", "1", "1", "1");
        customPropertyManager.createDefinition("2", "2", "2", "2");
        ResourceTagHandler resourceTagHandler = new ResourceTagHandler(humanResourceManager, roleManager, customPropertyManager);
        assertNotNull(resourceTagHandler);

        String namespaceURI = "";
        String sName = "description";
        String qName = "resource";
        Attributes attributes = createCorrectAttr();

        // initialization of myCurrentResource
        resourceTagHandler.startElement(namespaceURI, sName, qName, attributes);

        // going into loadCustomProperty
        qName = "custom-property";
        int sizeOfResources = humanResourceManager.getResources().size();
        int sizeOfCustomProperties = humanResourceManager.getResources().get(sizeOfResources - 1).getCustomProperties().size();

        resourceTagHandler.setTagStarted(false);
        resourceTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertTrue(resourceTagHandler.isTagStarted());

        assertEquals(sizeOfCustomProperties + 1, humanResourceManager.getResources().get(1).getCustomProperties().size());
        assertEquals(def1, humanResourceManager.getResources().get(1).getCustomProperties().get(0).getDefinition());
        assertEquals("4", humanResourceManager.getResources().get(1).getCustomProperties().get(0).getValueAsString());
    }

    public void testLoadCustomPropertyDefinition() {

        createElements();
        ResourceTagHandler resourceTagHandler = new ResourceTagHandler(humanResourceManager, roleManager, customPropertyManager);
        assertNotNull(resourceTagHandler);

        String namespaceURI = "";
        String sName = "description";
        String qName = "custom-property-definition";
        Attributes attributes = createCorrectAttr();
        int sizeOfDefinitions = customPropertyManager.getDefinitions().size();

        resourceTagHandler.setTagStarted(false);
        resourceTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertTrue(resourceTagHandler.isTagStarted());

        assertEquals(sizeOfDefinitions + 1, customPropertyManager.getDefinitions().size());
        assertEquals("standard", customPropertyManager.getDefinitions().get(sizeOfDefinitions).getName());
    }

    public void testLoadRate() {

        createElements();
        ResourceTagHandler resourceTagHandler = new ResourceTagHandler(humanResourceManager, roleManager, customPropertyManager);
        assertNotNull(resourceTagHandler);

        String namespaceURI = "";
        String sName = "description";
        String qName = "rate";

        Attributes attributes = createAttr();

        resourceTagHandler.setTagStarted(false);
        resourceTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertTrue(resourceTagHandler.isTagStarted());

        attributes = createCorrectAttr();

        // initialization of myCurrentResource
        qName = "resource";
        resourceTagHandler.startElement(namespaceURI, sName, qName, attributes);

        int sizeOfResources = humanResourceManager.getResources().size();
        qName = "rate";
        resourceTagHandler.setTagStarted(false);
        resourceTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertTrue(resourceTagHandler.isTagStarted());

        BigDecimal val = new BigDecimal("4");
        assertEquals(val, humanResourceManager.getResources().get(sizeOfResources - 1).getStandardPayRate());
    }

    public void testEndElement() {

        createElements();
        ResourceTagHandler resourceTagHandler = new ResourceTagHandler(humanResourceManager, roleManager, customPropertyManager);
        assertNotNull(resourceTagHandler);

        String namespaceURI = "";
        String sName = "description";
        String qName = "";

        resourceTagHandler.setTagStarted(true);
        resourceTagHandler.endElement(namespaceURI, sName, qName);
        assertTrue(resourceTagHandler.isTagStarted());

        qName = "resource";
        resourceTagHandler.setTagStarted(true);
        resourceTagHandler.endElement(namespaceURI, sName, qName);
        assertFalse(resourceTagHandler.isTagStarted());
    }

    public void testParsingFinished() {

        createElements();
        ResourceTagHandler resourceTagHandler = new ResourceTagHandler(humanResourceManager, roleManager, customPropertyManager);
        assertNotNull(resourceTagHandler);

        String namespaceURI = "";
        String sName = "description";
        String qName = "resource";

        // adding values to myLateResource2roleBinding
        Attributes attributes = createCorrectAttr();
        resourceTagHandler.startElement(namespaceURI, sName, qName, attributes);
        assertTrue(resourceTagHandler.isTagStarted());

        resourceTagHandler.parsingFinished();
        assertNotNull(humanResourceManager.getResources().get(0).getRole());
    }
    
}