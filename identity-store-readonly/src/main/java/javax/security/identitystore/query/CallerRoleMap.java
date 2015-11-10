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
package javax.security.identitystore.query;

import java.util.List;

/**
 * <code>CallerRoleMap</code> provides optional, additional caller-role-mapping-related
 * queries implemented by the identity store.
 */
public interface CallerRoleMap {
    /**
     * Determines a list of callers found in the identity store, which have been
     * assigned the given role.
     * <p>
     * NOTE THAT depending on the identity store implementation, this method may
     * only consider callers which are explicitly assigned a role. Callers assigned
     * via evaluated expressions (e.g., XACML expressions) may not be considered.
     *
     * @param role The role name
     * @param includeGroupRoles <code>true</code> to include roles assigned via
     *                          group memberships.
     * @return The list of callers which have been assigned the given role, empty list if none,
     * <code>null</code> if not supported.
     */
    List<String> getCallersWithRole(String role, boolean includeGroupRoles);

    /**
     * Determines a list of roles found in the identity store, which have been
     * assigned to the given caller.
     * <p>
     * NOTE THAT depending on the identity store implementation, this method may
     * only consider callers which are explicitly assigned a role. Callers assigned
     * via evaluated expressions (e.g., XACML expressions) may not be considered.
     *
     * @param callerName The Caller name
     * @param includeGroupRoles <code>true</code> to include roles assigned via
     *                          group memberships.
     * @return The list of roles that the specified caller has, empty list if none,
     * <code>null</code> if not supported.
     */
    List<String> getCallerRoles(String callerName, boolean includeGroupRoles);
}
