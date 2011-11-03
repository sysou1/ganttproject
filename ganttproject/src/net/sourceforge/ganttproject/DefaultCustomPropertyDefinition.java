/*
GanttProject is an opensource project management tool.
Copyright (C) 2011 Dmitry Barashev

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/
package net.sourceforge.ganttproject;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class DefaultCustomPropertyDefinition implements CustomPropertyDefinition {
    private String myName;
    private final String myID;
    private Object myDefaultValue;
    private String myDefaultValueAsString;
    private CustomPropertyClass myPropertyClass;
    private String myTypeAsString;

    public DefaultCustomPropertyDefinition(String name) {
        myName = name;
        myID = null;
        myDefaultValue = null;
        myDefaultValueAsString = null;
        myPropertyClass = CustomPropertyClass.TEXT;
        myTypeAsString = CustomPropertyClass.TEXT.getID();
    }

    public DefaultCustomPropertyDefinition(String name, String id, CustomPropertyDefinition stub) {
        myName = name;
        myID = id;
        myDefaultValue = stub.getDefaultValue();
        myDefaultValueAsString = stub.getDefaultValueAsString();
        myPropertyClass = stub.getPropertyClass();
        myTypeAsString = stub.getTypeAsString();
    }

    public Object getDefaultValue() {
        return myDefaultValue;
    }

    public String getDefaultValueAsString() {
        return myDefaultValueAsString;
    }

    public void setDefaultValueAsString(String value) {
        CustomPropertyDefinition stub = CustomPropertyManager.PropertyTypeEncoder.decodeTypeAndDefaultValue(
                getTypeAsString(), value);
        myDefaultValue = stub.getDefaultValue();
        myDefaultValueAsString = stub.getDefaultValueAsString();
    }

    public String getID() {
        return myID;
    }

    public String getName() {
        return myName;
    }

    public void setName(String name) {
        myName = name;
    }

    public Class<?> getType() {
        return myPropertyClass.getJavaClass();
    }

    public CustomPropertyClass getPropertyClass() {
        return myPropertyClass;
    }

    public String getTypeAsString() {
        return myTypeAsString;
    }

    public IStatus canSetPropertyClass(CustomPropertyClass propertyClass) {
        return Status.OK_STATUS;
    }

    public IStatus setPropertyClass(CustomPropertyClass propertyClass) {
        myPropertyClass = propertyClass;
        myTypeAsString = propertyClass.getID();
        setDefaultValueAsString(getDefaultValueAsString());
        return Status.OK_STATUS;
    }
}