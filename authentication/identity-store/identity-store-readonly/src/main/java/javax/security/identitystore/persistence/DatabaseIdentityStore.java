/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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
package javax.security.identitystore.persistence;

import javax.enterprise.inject.Alternative;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import java.util.List;

/**
 * <code>DatabaseIdentityStore</code> is an {@link javax.security.identitystore.IdentityStore}
 * implementation which uses an external database as a persistence mechanism.
 */
@Alternative
public class DatabaseIdentityStore extends AbstractIdentityStore {

    /**
     * Determines the list of groups that the specified Caller is in,
     * based on the associated persistence store..
     *
     * @param callerName The Caller name
     * @return The list of groups that the specified Caller is in, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getCallerGroups(String callerName) {
        return null;
    }

    /**
     * Determines the list of roles that the specified Caller has,
     * based on the associated persistence store. The returned role list
     * would include roles directly assigned to the Caller, and roles assigned
     * to groups which contain the Caller.
     *
     * @param callerName The Caller name
     * @return The list of roles that the specified Caller has, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getCallerRoles(String callerName) {
        return null;
    }

    /**
     * Default validation behavior for username/password credentials.
     *
     * @param usernamePasswordCredential Credential to validate
     * @return The result
     */
    @Override
    protected CredentialValidationResult validateUsernamePassword(UsernamePasswordCredential usernamePasswordCredential) {
        return null;
    }
}
