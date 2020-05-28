package net.sourceforge.ganttproject.parser;

import net.sourceforge.ganttproject.roles.Role;
import net.sourceforge.ganttproject.roles.RoleManager;
import net.sourceforge.ganttproject.roles.RolePersistentID;
import net.sourceforge.ganttproject.roles.RoleSet;

public class FindRoleClass {

    RoleManager myRoleManager;

    public FindRoleClass(RoleManager myRoleManager){
        this.myRoleManager = myRoleManager;
    }

    public Role findRole(String persistentIDasString) {
        RolePersistentID persistentID = new RolePersistentID(persistentIDasString);
        String rolesetName = persistentID.getRoleSetID();
        int roleID = persistentID.getRoleID();
        RoleSet roleSet;
        if (rolesetName == null) {
            roleSet = myRoleManager.getProjectRoleSet();
            if (roleSet.findRole(roleID) == null) {
                if (roleID <= 10 && roleID > 2) {
                    roleSet = myRoleManager.getRoleSet(RoleSet.SOFTWARE_DEVELOPMENT);
                    roleSet.setEnabled(true);
                } else if (roleID <= 2) {
                    roleSet = myRoleManager.getRoleSet(RoleSet.DEFAULT);
                }
            }
        } else {
            roleSet = myRoleManager.getRoleSet(rolesetName);
        }
        return roleSet.findRole(roleID);
    }
}
