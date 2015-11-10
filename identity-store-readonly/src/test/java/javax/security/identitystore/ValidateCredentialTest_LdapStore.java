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
package javax.security.identitystore;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.Context;
import javax.security.identitystore.credential.AbstractCredential;
import javax.security.identitystore.credential.Password;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import javax.security.identitystore.persistence.LdapEntryMapping;
import javax.security.identitystore.persistence.LdapIdentityStore;
import java.nio.charset.StandardCharsets;
import java.util.Hashtable;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests credential validation using the
 * <code>{@link javax.security.identitystore.persistence.LdapIdentityStore LdapIdentityStore}</code>.
 * <p>
 * This test invokes CDI using <a href="http://jglue.org/cdi-unit">CDI-Unit</a>.
 */
@RunWith(CdiRunner.class) // Runs the test with CDI-Unit
public class ValidateCredentialTest_LdapStore {

    /**
     * <code>ByteCredential</code> is a credential used for testing
     * and is not supported by
     * <code>{@link IdentityStore}</code> by default.
     */
    public static class ByteCredential extends AbstractCredential {

        private final String caller;
        private final byte[] credentialValue;


        public ByteCredential(String caller, String credentialValue) {
            this.caller = caller;
            this.credentialValue = credentialValue.getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public String getCaller() {
            return caller;
        }


        public byte[] getCredentialValue() {
            return credentialValue;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        protected void clearCredential() {
        }
    }

    @Inject
    private IdentityStore credentialStore;

    /**
     * An <code>{@link IdentityStore}</code> producer which configures an
     * <code>{@link LdapIdentityStore}</code>.
     *
     * @return A configured <code>IdentityStore</code>.
     */
    @Produces
    public IdentityStore getIdentityStore() {
        System.out.println("getIdentityStore called");

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
        return new LdapIdentityStore(env, mapping);
    }

    /**
     * Tests validation of a valid username/password credential using LDAP.
     */
    @Test
    public void usernamePasswordCredential_valid() {

        UsernamePasswordCredential creds = new UsernamePasswordCredential("jsmith", new Password("welcome1"));
        CredentialValidationResult validationResult = credentialStore.validate(creds);
        creds.clear();

        if (CredentialValidationResult.Status.VALID == validationResult.getStatus()) {
            String callerName = validationResult.getCallerName();
            assertEquals("CallerName", "jsmith", callerName);
            List<String> groups = validationResult.getCallerGroups();
            assertNotNull("Groups", groups);
            assertEquals("Groups count", 2, groups.size());
            assertEquals("Group 1", "Deployers", groups.get(0));
            assertEquals("Group 2", "Operators", groups.get(1));
            List<String> roles = validationResult.getCallerRoles();
            assertNull("Roles", roles);
        } else {
            fail("Expected valid credentials.");
        }

    }

    /**
     * Tests validation of an unsupported custom credential using LDAP.
     */
    @Test
    public void byteCredential_invalid() {

        ByteCredential creds = new ByteCredential("jsmith", "welcome1");
        CredentialValidationResult validationResult = credentialStore.validate(creds);
        creds.clear();

        assertEquals(CredentialValidationResult.Status.NOT_VALIDATED, validationResult.getStatus());
    }

    /**
     * Tests validation of an invalid username/password credential using LDAP.
     */
    @Test
    public void usernamePasswordCredential_invalid() {

        UsernamePasswordCredential creds = new UsernamePasswordCredential("jsmith", new Password("notwelcome"));
        CredentialValidationResult validationResult = credentialStore.validate(creds);
        creds.clear();

        assertEquals(CredentialValidationResult.Status.INVALID, validationResult.getStatus());
    }
}
