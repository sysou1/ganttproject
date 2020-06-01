/*
 * Created on 17.07.2003
 *
 */
package net.sourceforge.ganttproject.resource;

import java.util.EventObject;

/**
 * @author bard
 */
public class ResourceEvent extends EventObject {
  /**
   * @param source
   */
  public ResourceEvent(HumanResourceManager mgr, HumanResource[] resources) {
    super(mgr);
    myManager = mgr;
    myResources = resources;
    myFirstResource = resources.length > 0 ? resources[0] : null;
  }

  public HumanResourceManager getManager() {
    return myManager;
  }

  public HumanResource getFirstResource() {
    return myFirstResource;
  }

  public HumanResource[] getResources() {
    return myResources;
  }

  private final HumanResource[] myResources;

  private final HumanResourceManager myManager;

  private final HumanResource myFirstResource;

}
