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

import org.junit.Test;

import javax.naming.Context;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.credential.Password;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests the LDAP identity store, {@link javax.security.identitystore.persistence.LdapIdentityStore}.
 * <p>
 * This tests the API without invoking CDI.
 */
public class LdapIdentityStoreTest {

    /**
     * Tests the specialized method implementation <code>{@link LdapIdentityStore#validateUsernamePassword}</code>.
     *
     * @throws IOException
     */
    @Test
    public void  validateUsernamePassword() throws IOException {

        // Test with locally installed LDAP

        // Set up the environment for creating the initial context
        Hashtable<String, Object> env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:7001");

        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=Admin");
        env.put(Context.SECURITY_CREDENTIALS, "welcome1");


        // LDAP entry mapping
        LdapEntryMapping mapping = new LdapEntryMapping();
        mapping
            .getCallerMapping()
                .setDnPattern("uid={0},ou=people,ou=myrealm,dc=base_domain")
                .setGroupAttribute("wlsMemberOf");

        // Instantiate store
        LdapIdentityStore store = new LdapIdentityStore(env, mapping);

        // Test validation
        assertEquals("validate", CredentialValidationResult.Status.VALID,
            store.validateUsernamePassword(new UsernamePasswordCredential("jsmith", new Password("welcome1"))).getStatus());
        assertEquals("validate", CredentialValidationResult.Status.INVALID,
            store.validateUsernamePassword(new UsernamePasswordCredential("jsmith", new Password("badPassword"))).getStatus());

    }

    /**
     * Tests the specialized method implementation <code>{@link LdapIdentityStore#getCallerGroups}</code>.
     *
     * @throws IOException
     */
    @Test
    public void callerGroups() throws IOException {

        // Test with locally installed LDAP

        // Set up the environment for creating the initial context
        Hashtable<String, Object> env = new Hashtable();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, "ldap://localhost:7001");

        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        env.put(Context.SECURITY_PRINCIPAL, "cn=Admin");
        env.put(Context.SECURITY_CREDENTIALS, "welcome1");

        // LDAP entry mapping
        LdapEntryMapping mapping = new LdapEntryMapping();
        mapping
            .getCallerMapping()
            .setDnPattern("uid={0},ou=people,ou=myrealm,dc=base_domain")
            .setGroupAttribute("wlsMemberOf");

        // Instantiate store
        LdapIdentityStore store = new LdapIdentityStore(env, mapping);

        // Get caller groups
        List<String> groups = store.getCallerGroups("jsmith");
        assertNotNull("Groups", groups);
        assertEquals("Groups count", 2, groups.size());
        assertTrue("Deployers", groups.contains("Deployers"));
        assertTrue("Operators", groups.contains("Operators"));
    }
}
