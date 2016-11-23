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

import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.security.identitystore.credential.Password;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import javax.security.identitystore.persistence.CachedIdentityStore;
import javax.security.identitystore.persistence.cachedsource.CachedIdentityStoreSource;
import javax.security.identitystore.persistence.cachedsource.MemoryIdentityStoreSource;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests credential validation with the
 * <code>{@link javax.security.identitystore.persistence.CachedIdentityStore CachedIdentityStore}</code>
 * using a <code>{@link javax.security.identitystore.persistence.cachedsource.MemoryIdentityStoreSource MemoryIdentityStoreSource}</code>.
 * <p>
 * This test invokes CDI using <a href="http://jglue.org/cdi-unit">CDI-Unit</a>.
 */
@RunWith(CdiRunner.class) // Runs the test with CDI-Unit
@ActivatedAlternatives({CachedIdentityStore.class})
public class ValidateCredentialTest_MemoryStore {

    @Inject
    CachedIdentityStore credentialStore;

    /**
     * Produces a <code>{@link javax.security.identitystore.persistence.cachedsource.MemoryIdentityStoreSource MemoryIdentityStoreSource}</code>
     * for use by a <code>{@link javax.security.identitystore.persistence.CachedIdentityStore}</code>.
     *
     * @return A <code>CachedIdentityStoreSource</code>
     */
    @Produces
    public CachedIdentityStoreSource getCredentialStoreSource() {
        System.out.println("getCredentialStoreSource called");
        HashSet<CachedIdentityStoreSource.CallerSource> callerSources = new HashSet();
        List<String> groups;
        List<String> roles;
        List<CachedIdentityStoreSource.CredentialSource> credentialSources;
        Map<String, String> attributes;


        // jsmith
        groups = new ArrayList<>();
        groups.add("admin");
        groups.add("user");

        roles = new ArrayList<>();
        roles.add("VIEW_ACCT");
        roles.add("EDIT_ACCT");

        credentialSources = new ArrayList<>();
        credentialSources.add(new CachedIdentityStoreSource.CredentialSource("javax.security.identitystore.credential.UsernamePasswordCredential",
            "70a5b4215270a19492788cf76c9e591a9334ac2363213765674e5181babbea1b", "SHA-256", "NaCl", null));
        credentialSources.add(new CachedIdentityStoreSource.CredentialSource("javax.security.identitystore.credential.TokenCredential",
            "ASDJFWEJ;WKJQDSJKLDVJKVALS;KJ", null, null, null));

        attributes = new HashMap<>();
        attributes.put("locked", "true");
        attributes.put("expired", "true");

        callerSources.add(new CachedIdentityStoreSource.CallerSource("jsmith", groups, roles, credentialSources, attributes));


        // jlee
        groups = new ArrayList<>();
        groups.add("deployer");

        roles = new ArrayList<>();
        roles.add("VIEW_ACCT");

        credentialSources = new ArrayList<>();
        credentialSources.add(new CachedIdentityStoreSource.CredentialSource("javax.security.identitystore.credential.UsernamePasswordCredential",
            "welcome1", null, null, null));
        credentialSources.add(new CachedIdentityStoreSource.CredentialSource("javax.security.identitystore.credential.TokenCredential",
            "ASDJFWEJ;FGSKREJTEJ;KJ", null, null, null));

        callerSources.add(new CachedIdentityStoreSource.CallerSource("jlee", groups, roles, credentialSources, null));

        return new MemoryIdentityStoreSource(callerSources);
    }

    /**
     * Tests validation of a valid username/password credential using a
     * <code>{@link javax.security.identitystore.persistence.CachedIdentityStore CachedIdentityStore}</code>
     * backed by a Memory source.
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
            assertEquals("Groups count", 2, groups.size());
            assertEquals("Group 1", "admin", groups.get(0));
            assertEquals("Group 2", "user", groups.get(1));
            List<String> roles = validationResult.getCallerRoles();
            assertEquals("Roles count", 2, roles.size());
            assertEquals("Role 1", "VIEW_ACCT", roles.get(0));
            assertEquals("Role 2", "EDIT_ACCT", roles.get(1));

            // JASPIC callback handler could occur here

        } else {
            // Invalid
            fail("Should be valid");
        }
    }

    /**
     * Tests validation of an invalid username/password credential using a
     * <code>{@link javax.security.identitystore.persistence.CachedIdentityStore CachedIdentityStore}</code>
     * backed by a Memory source.
     */
    @Test
    public void usernamePasswordCredential_invalid() {

        UsernamePasswordCredential creds = new UsernamePasswordCredential("jsmith", new Password("notwelcome"));
        CredentialValidationResult validationResult = credentialStore.validate(creds);
        creds.clear();

        if (CredentialValidationResult.Status.VALID == validationResult.getStatus()) {
            fail("Should be invalid");
        } else {
            // Invalid
            assertEquals(CredentialValidationResult.Status.INVALID, validationResult.getStatus());
        }
    }

    /**
     * Tests reloading of a
     * <code>{@link javax.security.identitystore.persistence.CachedIdentityStore CachedIdentityStore}</code>.
     */
    @Test
    public void reload() throws IOException {

        // Test with originally produced source
        UsernamePasswordCredential creds = new UsernamePasswordCredential("jsmith", new Password("notwelcome"));
        CredentialValidationResult validationResult = credentialStore.validate(creds);

        if (CredentialValidationResult.Status.VALID == validationResult.getStatus()) {
            fail("Should be invalid");
        } else {
            // Invalid
            assertEquals(CredentialValidationResult.Status.INVALID, validationResult.getStatus());
        }

        // Now reload cache with jsmith password "notwelcome"

        HashSet<CachedIdentityStoreSource.CallerSource> callerSources = new HashSet();
        List<CachedIdentityStoreSource.CredentialSource> credentialSources;

        // jsmith
        credentialSources = new ArrayList<>();
        credentialSources.add(new CachedIdentityStoreSource.CredentialSource("javax.security.identitystore.credential.UsernamePasswordCredential",
            "821601607e40e19ef631ea9ead073f4a10175fb465725a8e6b99ca03f5ce5805", "SHA-256", "NaCl", null));

        callerSources.add(new CachedIdentityStoreSource.CallerSource("jsmith", null, null, credentialSources, null));

        credentialStore.load(new MemoryIdentityStoreSource(callerSources));

        // Retest with previously failed password

        validationResult = credentialStore.validate(creds);

        if (CredentialValidationResult.Status.VALID == validationResult.getStatus()) {
            // Valid
        } else {
            fail("Should be valid");
        }
    }
}
