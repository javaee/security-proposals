/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package javax.security.idm.model;

import org.picketlink.idm.IdentityManagementException;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.IdentityQuery;
import org.picketlink.idm.query.IdentityQueryBuilder;
import org.picketlink.idm.query.RelationshipQuery;

import java.util.List;

import static org.picketlink.common.util.StringUtil.isNullOrEmpty;
import static org.picketlink.idm.IDMMessages.MESSAGES;

/**
 * A helper class which provides convenience methods for working with the identity model.
 */
public class Helper {

    /**
     * Determines a {@link Caller} instance with the given <code>loginName</code>.
     *
     * @param identityManager The associated identity manager
     * @param loginName The caller's login name.
     * @return The found {@link Caller} instance or
     *  <code>null</code> if none found or the <code>loginName</code> is null or empty.
     * @throws IdentityManagementException An error occurred
     */
    public static Caller getCaller(IdentityManager identityManager, String loginName) throws IdentityManagementException {
        if (identityManager == null) {
            throw MESSAGES.nullArgument("IdentityManager");
        }

        if (isNullOrEmpty(loginName)) {
            return null;
        }

        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<Caller> callers = queryBuilder.createIdentityQuery(Caller.class)
                .where(queryBuilder.equal(Caller.LOGIN_NAME, loginName)).getResultList();

        if (callers.isEmpty()) {
            return null;
        } else if (callers.size() == 1) {
            return callers.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Caller objects found with same login name");
        }
    }

    /**
     * Determines a {@link Role} instance with the given <code>name</code>.
     *
     * @param identityManager The associated identity manager
     * @param name The role's name.
     * @return The found {@link Role} instance or
     *  <code>null</code> if none found or the <code>name</code> is null or empty.
     * @throws IdentityManagementException An error occurred
     */
    public static Role getRole(IdentityManager identityManager, String name) throws IdentityManagementException {
        if (identityManager == null) {
            throw MESSAGES.nullArgument("IdentityManager");
        }

        if (isNullOrEmpty(name)) {
            return null;
        }

        IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
        List<Role> roles = queryBuilder.createIdentityQuery(Role.class)
                .where(queryBuilder.equal(Role.NAME, name)).getResultList();

        if (roles.isEmpty()) {
            return null;
        } else if (roles.size() == 1) {
            return roles.get(0);
        } else {
            throw new IdentityManagementException("Error - multiple Role objects found with same name");
        }
    }

    /**
     * Determines a {@link Group} instance with the specified <code>groupPath</code>.
     * <p>
     * An example groupPath:<br>
     * <code>/Sales/North American/Federal/Defense/this Group</code>
     *
     * @param identityManager The associated identity manager
     * @param groupPath The group's path or its name without the group separator,
     *  which we be assumed to be a root path prepended with a group separator.<br>
     *  For example: <code>Administrators == /Administrators</code>
     * @return The found {@link Group} instance or <code>null</code> if not found or
     *  if the <code>groupPath</code> is <code>null</code> or an empty.
     * @throws IdentityManagementException An error occurred
     */
    public static Group getGroup(IdentityManager identityManager, String groupPath) throws IdentityManagementException {
        if (identityManager == null) {
            throw MESSAGES.nullArgument("IdentityManager");
        }

        if (isNullOrEmpty(groupPath)) {
            return null;
        }

        if (!groupPath.startsWith("/")) {
            groupPath = "/" + groupPath;
        }

        String[] paths = groupPath.split("/");

        if (paths.length > 0) {
            String name = paths[paths.length - 1];
            IdentityQueryBuilder queryBuilder = identityManager.getQueryBuilder();
            IdentityQuery<Group> query = queryBuilder.createIdentityQuery(Group.class)
                    .where(queryBuilder.equal(Group.NAME, name));

            List<Group> result = query.getResultList();

            for (Group storedGroup : result) {
                if (storedGroup.getPath().equals(groupPath)) {
                    return storedGroup;
                }
            }
        }

        return null;
    }

    /**
     * Determines the {@link Group} with the given <code>groupName</code> and
     * given <code>parent</code> {@link Group}.
     *
     * @param identityManager The associated identity manager
     * @param groupName The group's name.
     * @param parent A {@link Group} instance with a valid identifier or <code>null</code>.
     *  In this last case, the returned group will be always a root group.
     * @return The found {@link Group} instance. <code>null</code> if not found or
     * if the <code>groupName</code> is null or an empty string.
     * @throws IdentityManagementException An error occurred
     */
    public static Group getGroup(IdentityManager identityManager, String groupName, Group parent) throws IdentityManagementException {
        if (identityManager == null) {
            throw MESSAGES.nullArgument("IdentityManager");
        }

        if (isNullOrEmpty(groupName)) {
            return null;
        }

        if (parent == null) {
            return getGroup(identityManager, new Group(groupName).getPath());
        } else {
            return getGroup(identityManager, new Group(groupName, parent).getPath());
        }
    }

    // Relationship management

    /**
     * Checks if the given {@link Account} is a member of the given {@link Group}
     * or any child <code>Group</code>.
     *
     * @param relationshipManager The associated relationship manager
     * @param member Check for this {@link Account}.
     * @param group Check for this {@link Group}.
     * @return <code>true</code> if the {@link Account} is a member of the {@link Group}
     *  or any child <code>Group</code>, otherwise false.
     * @throws IdentityManagementException An error occurred
     */
    public static boolean isMember(RelationshipManager relationshipManager, Account member, Group group) throws IdentityManagementException {
        if (relationshipManager == null) {
            throw MESSAGES.nullArgument("RelationshipManager");
        }

        if (member == null) {
            throw MESSAGES.nullArgument("Account");
        }

        if (group == null) {
            throw MESSAGES.nullArgument("Group");
        }

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, member);

        List<GroupMembership> result = query.getResultList();

        for (GroupMembership membership : result) {
            if (membership.getGroup().getId().equals(group.getId())) {
                return true;
            }

            if (membership.getGroup().getPath().startsWith(group.getPath())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Adds the given {@link Account} as a member of the given {@link Group}.
     *
     * @param relationshipManager The associated relationship manager
     * @param member A previously loaded {@link Account} instance.
     * @param group A previously loaded {@link Group} instance.
     * @throws IdentityManagementException An error occurred
     */
    public static void addToGroup(RelationshipManager relationshipManager, Account member, Group group) throws IdentityManagementException {
        if (relationshipManager == null) {
            throw MESSAGES.nullArgument("RelationshipManager");
        }

        if (member == null) {
            throw MESSAGES.nullArgument("Account");
        }

        if (group == null) {
            throw MESSAGES.nullArgument("Group");
        }

        relationshipManager.add(new GroupMembership(member, group));
    }

    /**
     * Removes the given {@link Account} from the given {@link Group}.
     *
     * @param relationshipManager The associated relationship manager
     * @param member A previously loaded {@link Account} instance.
     * @param group A previously loaded {@link Group} instance.
     * @throws IdentityManagementException An error occurred
     */
    public static void removeFromGroup(RelationshipManager relationshipManager, Account member, Group group) throws IdentityManagementException {
        if (relationshipManager == null) {
            throw MESSAGES.nullArgument("RelationshipManager");
        }

        if (member == null) {
            throw MESSAGES.nullArgument("Account");
        }

        if (group == null) {
            throw MESSAGES.nullArgument("Group");
        }

        RelationshipQuery<GroupMembership> query = relationshipManager.createRelationshipQuery(GroupMembership.class);

        query.setParameter(GroupMembership.MEMBER, member);
        query.setParameter(GroupMembership.GROUP, group);

        for (GroupMembership membership : query.getResultList()) {
            relationshipManager.remove(membership);
        }
    }

    /**
     * Checks if the given {@link Role} is granted to the provided account or group.
     *
     * @param relationshipManager The associated relationship manager
     * @param assignee Check for this acccount or group
     * @param role Check for this role
     * @return <code>true</code> if the given {@link Role} is granted, otherwise false.
     * @throws IdentityManagementException An error occurred
     */
    public static boolean hasRole(RelationshipManager relationshipManager, RoleAssignable assignee, Role role) throws IdentityManagementException {
        if (relationshipManager == null) {
            throw MESSAGES.nullArgument("RelationshipManager");
        }

        if (assignee == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        if (role == null) {
            throw MESSAGES.nullArgument("Role");
        }

        RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ASSIGNEE, assignee);
        query.setParameter(Grant.ROLE, role);

        boolean hasRole = !query.getResultList().isEmpty();

        if (!hasRole) {
            return relationshipManager.inheritsPrivileges(assignee, role);
        }

        return hasRole;
    }

    /**
     * Grants the given {@link Role} to the provided account or group.
     *
     * @param relationshipManager The associated relationship manager
     * @param assignee A previously loaded account or group.
     * @param role A previously loaded {@link Role} instance.
     * @throws IdentityManagementException An error occurred
     */
    public static void grantRole(RelationshipManager relationshipManager, RoleAssignable assignee, Role role) throws IdentityManagementException {
        if (relationshipManager == null) {
            throw MESSAGES.nullArgument("RelationshipManager");
        }

        if (assignee == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        if (role == null) {
            throw MESSAGES.nullArgument("Role");
        }

        relationshipManager.add(new Grant(assignee, role));
    }

    /**
     * Revokes the given {@link Role} from the provided account or group.
     * Note that this does not revoke an inherited <code>Role</code>.
     *
     * @param relationshipManager The associated relationship manager
     * @param assignee A previously loaded account or group.
     * @param role A previously loaded {@link Role} instance.
     * @throws IdentityManagementException An error occurred
     */
    public static void revokeRole(RelationshipManager relationshipManager, RoleAssignable assignee, Role role) throws IdentityManagementException {
        if (relationshipManager == null) {
            throw MESSAGES.nullArgument("RelationshipManager");
        }

        if (assignee == null) {
            throw MESSAGES.nullArgument("IdentityType");
        }

        if (role == null) {
            throw MESSAGES.nullArgument("Role");
        }

        RelationshipQuery<Grant> query = relationshipManager.createRelationshipQuery(Grant.class);

        query.setParameter(Grant.ASSIGNEE, assignee);
        query.setParameter(Grant.ROLE, role);

        for (Grant grant : query.getResultList()) {
            relationshipManager.remove(grant);
        }
    }

}