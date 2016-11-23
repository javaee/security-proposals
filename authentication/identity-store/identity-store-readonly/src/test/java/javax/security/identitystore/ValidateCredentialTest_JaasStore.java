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
package javax.security.identitystore;

import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.security.identitystore.credential.Password;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import javax.security.identitystore.persistence.*;
import java.util.*;

import static org.junit.Assert.*;
import static javax.security.identitystore.persistence.JaasTestArtifacts.*;

/**
 * Tests credential validation using the
 * <code>{@link javax.security.identitystore.persistence.JaasIdentityStore JaasIdentityStore}</code>.
 * <p>
 * This test invokes CDI using <a href="http://jglue.org/cdi-unit">CDI-Unit</a>.
 */
@RunWith(CdiRunner.class) // Runs the test with CDI-Unit
public class ValidateCredentialTest_JaasStore {

    @Inject
    private IdentityStore credentialStore;

    /**
     * An <code>{@link IdentityStore}</code> producer which configures a
     * <code>{@link JaasIdentityStore}</code>.
     *
     * @return A configured <code>IdentityStore</code>.
     */
    @Produces
    public IdentityStore getIdentityStore() {
        System.out.println("getIdentityStore called");

        return new JaasIdentityStore(CONFIG_ENTRY_NAME, new TestConfiguration(), new TestJaasSubjectPrincipalResolver() );
    }

    /**
     * Tests validation of a valid username/password credential using JAAS.
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
            assertTrue("Deployers", groups.contains("Deployers"));
            assertTrue("Operators", groups.contains("Operators"));
            List<String> roles = validationResult.getCallerRoles();
            assertNull("Roles", roles);
        } else {
            fail("Expected valid credentials");
        }
    }

    /**
     * Tests validation of an invalid username/password credential using JAAS.
     */
    @Test
    public void usernamePasswordCredential_invalid() {

        UsernamePasswordCredential creds = new UsernamePasswordCredential("jsmith", new Password("notwelcome"));
        CredentialValidationResult validationResult = credentialStore.validate(creds);
        creds.clear();

        assertEquals(CredentialValidationResult.Status.INVALID, validationResult.getStatus());
    }
}
