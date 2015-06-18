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
package javax.security.idm;

import java.util.List;

import javax.security.idm.credential.Credentials;
import javax.security.idm.model.Caller;

/**
 * <code>IdentityStore</code> provides read-only identity store access and
 * credential validation. The IdentityStore would be backed by a
 * back-end store, such as a file, LDAP, or database.
 */
public interface IdentityStore {

    ////////////////////////////////////////////////////////////////////////////
    // Callers and Groups

    /**
     * Loads the caller from a backend store.
     * @param loginName Login name
     * @return The {@link javax.security.idm.model.Caller} instance
     */
    Caller loadCaller(String loginName);

    /**
     * Determines whether a {@link javax.security.idm.model.Caller}
     * with the given loginName exists in a backend store.
     * @param loginName Login name
     * @return <code>true</code> if exists, otherwise <code>false</code>
     */
    boolean callerExists(String loginName);

    /**
     * Determines a list of {@link Caller Callers} that are found in a backend store.
     *
     * @param regEx A regular expression to select callers by loginName to return,
     *  <code>null</code> or empty string for all.
     * @return The list of found callers in a backend store.
     */
    List<Caller> getCallers(String regEx);

    /**
     * Determines whether the group with the given name exists in a backend store.
     * @param name Group name
     * @return <code>true</code> if exists, otherwise <code>false</code>
     */
    boolean groupExists(String name);

    /**
     * Determines a list of groups that are found in a backend store.
     *
     * @param regEx A regular expression to select groups by name to return,
     *  <code>null</code> or empty string for all.
     * @return The list of found groups in a backend store.
     */
    List<String> getGroups(String regEx);

    /**
     * Determines whether a {@link javax.security.idm.model.Caller} with the
     * given name exists in a backend store, and the {@link javax.security.idm.model.Group}
     * with the given name exists in a backend store, and the {@link javax.security.idm.model.Caller}
     * is in the Group.
     *
     * @param callerLoginName The <code>Caller</code> login name
     * @param groupName The group name
     * @return <code>true</code> if a Caller with the given name exists,
     * and the Group with the given name exists, and the Caller is in the Group.
     */
    boolean isCallerInGroup(String callerLoginName, String groupName);

    /**
     * Determines a list of {@link javax.security.idm.model.Caller Callers} which
     * have been assigned to the given existing group, based on a backend store.
     *
     * @param group The group name
     * @return The list of {@link javax.security.idm.model.Caller Callers} which
     * are in the given existing group, based on a backend store.
     */
    List<Caller> getCallersInGroup(String group);

    /**
     * Determines the list of groups that the specified
     * {@link javax.security.idm.model.Caller} is in, if the <code>Caller</code>
     * with the given name exists in a backend store.
     *
     * @param callerLoginName The <code>Caller</code> login name
     * @return The list of groups that the specified
     * {@link javax.security.idm.model.Caller} is in, if the <code>Caller</code>
     * with given name exists in a backend store.
     */
    List<String> getCallerGroups(String callerLoginName);

    /**
     * Determines the list of groups that the specified
     * {@link javax.security.idm.model.Caller} is in, if the <code>Caller</code>
     * exists in a backend store.
     *
     * @param caller The <code>Caller</code> instance
     * @return The list of groups that the specified
     * {@link javax.security.idm.model.Caller} is in, if the <code>Caller</code>
     * exists in a backend store.
     */
    List<String> getCallerGroups(Caller caller);

    ////////////////////////////////////////////////////////////////////////////
    // Credentials

    /**
     * Validates the given {@link javax.security.idm.credential.Credentials}.
     * <p>
     * To check the validation status, use the returned
     * {@link javax.security.idm.CredentialValidationResult#getStatus} method.
     *
     * @param credentials The credentials to validate
     * @return The validation result, never <code>null</code>.
     * @see CredentialValidationResult
     */
    CredentialValidationResult validateCredentials(Credentials credentials);

    ////////////////////////////////////////////////////////////////////////////
    // Role Mapping

    /**
     * Determines whether the role with the given name exists in a backend store.
     * @param name Role name
     * @return <code>true</code> if exists, otherwise <code>false</code>
     */
    boolean roleExists(String name);

    /**
     * Role set selector used in {@link #getRoles}.
     */
    enum RoleSetSelector {ALL, ASSIGNED, UNASSIGNED}

    /**
     * Determines the list of roles that are found in a backend store.
     *
     * @param regEx A regular expression to select roles by name to return,
     *  <code>null</code> or empty string for all.
     * @param roleSet The applicable role set selector
     * @return The list of found roles in a backend store.
     */
    List<String> getRoles(String regEx, RoleSetSelector roleSet);

    // Roles mapped to Caller //

    /**
     * Determines whether the existing {@link javax.security.idm.model.Caller}
     * with the given name has the given role based on a backend store.
     *
     * @param loginName <code>Caller</code> login name
     * @param role Role name
     * @return true if the <code>Caller</code> has the role.
     */
    boolean callerHasRole(String loginName, String role);

    /**
     * Determines whether the existing {@link javax.security.idm.model.Caller}
     * has the given role based on a backend store.
     *
     * @param caller The <code>Caller</code>
     * @param role Role name
     * @return true if the <code>Caller</code> exists and has the role.
     */
    boolean callerHasRole(Caller caller, String role);

    /**
     * Determines a list of {@link javax.security.idm.model.Caller Callers} which
     * have been assigned the given existing role, based on the backend stores.
     *
     * @param role The role name
     * @return The list of {@link javax.security.idm.model.Caller Callers} which
     * have been assigned the given existing role, based on the backend stores.
     */
    List<Caller> getCallersWithRole(String role);

    /**
     * Determines a list of roles assigned to the existing
     * {@link javax.security.idm.model.Caller}
     * with the given name, based on the backend stores.
     *
     * @param loginName <code>Caller</code> login name
     * @return The list of roles assigned to the existing {@link javax.security.idm.model.Caller}
     * with the given name, based on the backend stores.
     */
    List<String> getRolesForCaller(String loginName);

    /**
     * Determines a list of roles assigned to the existing
     * {@link javax.security.idm.model.Caller}
     * based on the backend stores.
     *
     * @param caller The <code>Caller</code>
     * @return The list of roles assigned to the existing {@link javax.security.idm.model.Caller}
     * based on the backend stores.
     */
    List<String> getRolesForCaller(Caller caller);

    // Roles mapped to Group //

    /**
     * Determines whether the existing group
     * with the given name has the given role, based on a backend store.
     *
     * @param groupName The group name
     * @param role Role name
     * @return true if the existing group has the role.
     */
    boolean groupHasRole(String groupName, String role);

    /**
     * Determines a list of groups which have been assigned the given existing role,
     * based on the backend stores.
     *
     * @param role The role name
     * @return The list of groups which have been assigned the given existing role,
     * based on the backend stores.
     */
    List<String> getGroupsWithRole(String role);

    /**
     * Determines a list of roles assigned to the existing group
     * with the given name, based on the backend stores.
     *
     * @param groupName The group name
     * @return The list roles assigned to the existing group
     * with the given name, based on the backend stores.
     */
    List<String> getRolesForGroup(String groupName);
}
