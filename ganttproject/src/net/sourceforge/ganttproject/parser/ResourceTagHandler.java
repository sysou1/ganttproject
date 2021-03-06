/*
GanttProject is an opensource project management tool.
Copyright (C) 2003-2011 GanttProject Team

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 3
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.sourceforge.ganttproject.parser;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import net.sourceforge.ganttproject.CustomPropertyDefinition;
import net.sourceforge.ganttproject.CustomPropertyManager;
import net.sourceforge.ganttproject.GPLogger;
import net.sourceforge.ganttproject.resource.HumanResource;
import net.sourceforge.ganttproject.resource.HumanResourceManager;
import net.sourceforge.ganttproject.roles.Role;
import net.sourceforge.ganttproject.roles.RoleManager;

import org.xml.sax.Attributes;

import com.google.common.collect.ImmutableSet;

/** Class to parse the attribute of resources handler */
public class ResourceTagHandler extends AbstractTagHandler implements ParsingListener {
  private final Set<String> possibleTags = ImmutableSet.of("resource", "custom-property", "custom-property-definition", "rate");
  private final CustomPropertyManager myCustomPropertyManager;

  private HumanResource myCurrentResource;

  private final HashMap<HumanResource, String> myLateResource2roleBinding = new HashMap<>();

  private final HumanResourceManager myResourceManager;

  private final RoleManager myRoleManager;

  public ResourceTagHandler(HumanResourceManager resourceManager, RoleManager roleManager,
      CustomPropertyManager resourceCustomPropertyManager) {
    super(null);
    myResourceManager = resourceManager;
    myCustomPropertyManager = resourceCustomPropertyManager;
    // myResourceManager.clear(); //CleanUP the old stuff
    myRoleManager = roleManager;
  }

  /**
   * @see net.sourceforge.ganttproject.parser.TagHandler#endElement(String,
   *      String, String)
   */
  @Override
  public void endElement(String namespaceURI, String sName, String qName) {
    if (!possibleTags.contains(qName)) {
      return;
    }
    setTagStarted(false);
  }

  /**
   * @see net.sourceforge.ganttproject.parser.TagHandler#startElement(String,
   *      String, String, Attributes)
   */
  @Override
  public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) {
    if (!possibleTags.contains(qName)) {
      return;
    }
    setTagStarted(true);
    if (qName.equals("resource")) {
      loadResource(attrs);
    }
    if ("custom-property".equals(qName)) {
      assert myCurrentResource != null;
      loadCustomProperty(attrs);
    }
    if ("custom-property-definition".equals(qName)) {
      loadCustomPropertyDefinition(attrs);
    }
    if ("rate".equals(qName)) {
      loadRate(attrs);
    }
  }


  private void loadRate(Attributes attrs) {
    String name = attrs.getValue("name");
    if (!"standard".equals(name)) {
      return;
    }
    try {
      BigDecimal value = new BigDecimal(attrs.getValue("value"));
      myCurrentResource.setStandardPayRate(value);
    } catch (NumberFormatException e) {
      GPLogger.log(e);
    }
  }

  private void loadCustomProperty(Attributes attrs) {
    String id = attrs.getValue("definition-id");
    String value = attrs.getValue("value");
    List<CustomPropertyDefinition> definitions = myCustomPropertyManager.getDefinitions();
    for (int i = 0; i < definitions.size(); i++) {
      CustomPropertyDefinition nextDefinition = definitions.get(i);
      if (id.equals(nextDefinition.getID())) {
        myCurrentResource.addCustomProperty(nextDefinition, value);
        break;
      }
    }
  }

  private void loadCustomPropertyDefinition(Attributes attrs) {
    String id = attrs.getValue("id");
    String name = attrs.getValue("name");
    String type = attrs.getValue("type");
    String defaultValue = attrs.getValue("default-value");
    myCustomPropertyManager.createDefinition(id, type, name, defaultValue);
  }

  /** Las a resources */
  private void loadResource(Attributes atts) {
    final HumanResource hr;

    try {
      String id = atts.getValue("id");
      if (id == null) {
        hr = getResourceManager().newHumanResource();
        hr.setName(atts.getValue("name"));
        getResourceManager().addHumanResource(hr);
      } else {
        hr = getResourceManager().createHumanResource(atts.getValue("name"), Integer.parseInt(id));
      }
      myCurrentResource = hr;
    } catch (NumberFormatException e) {
      GPLogger.log("ERROR in parsing XML File id is not numeric: " + e.toString());
      return;
    }

    hr.setMail(atts.getValue("contacts"));
    hr.setPhone(atts.getValue("phone"));
    try {
      String roleID = atts.getValue("function");
      myLateResource2roleBinding.put(hr, roleID);
    } catch (NumberFormatException e) {
      GPLogger.log("ERROR in parsing XML File function id is not numeric: " + e.toString());
    }
  }

  private HumanResourceManager getResourceManager() {
    return myResourceManager;
  }

  private Role findRole(String persistentIDasString) {
    return findRole(myRoleManager ,persistentIDasString);
  }

  @Override
  public void startParsing() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void finishParsing() {
    for (Iterator<Entry<HumanResource, String>> lateBindingEntries = myLateResource2roleBinding.entrySet().iterator(); lateBindingEntries.hasNext();) {
      Map.Entry<HumanResource, String> nextEntry = lateBindingEntries.next();
      String persistentID = nextEntry.getValue();
      Role nextRole = findRole(persistentID);
      if (nextRole != null) {
        lateBindingEntries.remove();
        nextEntry.getKey().setRole(nextRole);
      }
    }
    if (!myLateResource2roleBinding.isEmpty()) {
      GPLogger.log("[ResourceTagHandler] parsingFinished(): not found roles:\n" + myLateResource2roleBinding);
    }
  }
}
