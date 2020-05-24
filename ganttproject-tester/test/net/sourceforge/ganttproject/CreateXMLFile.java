package net.sourceforge.ganttproject;

import java.io.FileWriter;
import java.io.IOException;

public class CreateXMLFile {

    private String fileName;

    public CreateXMLFile() throws IOException {

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><project name=\"Untitled Gantt Project\" company=\"\" webLink=\"http://\" view-date=\"2020-04-18\" view-index=\"0\" gantt-divider-location=\"300\" resource-divider-location=\"300\" version=\"3.0\" locale=\"en_US\">\n" +
                "    <description/>\n" +
                "    <view zooming-state=\"default:2\" id=\"gantt-chart\">\n" +
                "        <field id=\"tpd3\" name=\"Name\" width=\"167\" order=\"0\"/>\n" +
                "        <field id=\"tpd4\" name=\"Begin date\" width=\"65\" order=\"1\"/>\n" +
                "        <field id=\"tpd5\" name=\"End date\" width=\"64\" order=\"2\"/>\n" +
                "    </view>\n" +
                "    <view id=\"resource-table\">\n" +
                "        <field id=\"0\" name=\"Name\" width=\"210\" order=\"0\"/>\n" +
                "        <field id=\"1\" name=\"Default role\" width=\"86\" order=\"1\"/>\n" +
                "    </view>\n" +
                "    <!-- -->\n" +
                "    <calendars>\n" +
                "        <day-types>\n" +
                "            <day-type id=\"0\"/>\n" +
                "            <day-type id=\"1\"/>\n" +
                "            <default-week id=\"1\" name=\"default\" sun=\"1\" mon=\"0\" tue=\"0\" wed=\"0\" thu=\"0\" fri=\"0\" sat=\"1\"/>\n" +
                "            <only-show-weekends value=\"false\"/>\n" +
                "            <overriden-day-types/>\n" +
                "            <days/>\n" +
                "        </day-types>\n" +
                "    </calendars>\n" +
                "    <tasks empty-milestones=\"true\">\n" +
                "        <taskproperties>\n" +
                "            <taskproperty id=\"tpd0\" name=\"type\" type=\"default\" valuetype=\"icon\"/>\n" +
                "            <taskproperty id=\"tpd1\" name=\"priority\" type=\"default\" valuetype=\"icon\"/>\n" +
                "            <taskproperty id=\"tpd2\" name=\"info\" type=\"default\" valuetype=\"icon\"/>\n" +
                "            <taskproperty id=\"tpd3\" name=\"name\" type=\"default\" valuetype=\"text\"/>\n" +
                "            <taskproperty id=\"tpd4\" name=\"begindate\" type=\"default\" valuetype=\"date\"/>\n" +
                "            <taskproperty id=\"tpd5\" name=\"enddate\" type=\"default\" valuetype=\"date\"/>\n" +
                "            <taskproperty id=\"tpd6\" name=\"duration\" type=\"default\" valuetype=\"int\"/>\n" +
                "            <taskproperty id=\"tpd7\" name=\"completion\" type=\"default\" valuetype=\"int\"/>\n" +
                "            <taskproperty id=\"tpd8\" name=\"coordinator\" type=\"default\" valuetype=\"text\"/>\n" +
                "            <taskproperty id=\"tpd9\" name=\"predecessorsr\" type=\"default\" valuetype=\"text\"/>\n" +
                "        </taskproperties>\n" +
                "        <task id=\"0\" name=\"task_0\" meeting=\"false\" start=\"2020-04-20\" duration=\"1\" complete=\"0\" expand=\"true\"/>\n" +
                "    </tasks>\n" +
                "    <resources>\n" +
                "        <resource id=\"0\" name=\"h\" function=\"Default:0\" contacts=\"\" phone=\"\"/>\n" +
                "    </resources>\n" +
                "    <allocations>\n" +
                "        <allocation task-id=\"0\" resource-id=\"0\" function=\"Default:0\" responsible=\"false\" load=\"100.0\"/>\n" +
                "    </allocations>\n" +
                "    <vacations/>\n" +
                "    <previous/>\n" +
                "    <roles roleset-name=\"Default\"/>\n" +
                "    <roles roleset-name=\"SoftwareDevelopment\"/>\n" +
                "</project>\n";

        fileName = "xmlOpenTest.xml";
        FileWriter fileWriter = new FileWriter(fileName);
        fileWriter.write(xml);
        fileWriter.close();
    }

    public String getFileName(){

        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win"))
            fileName = "\\" + fileName;
        else if(os.contains("mac") || os.contains("linux"))
            fileName = "/" + fileName;
        return fileName;
    }

}