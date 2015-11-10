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

import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.security.identitystore.annotation.Validator;
import javax.security.identitystore.credential.*;
import javax.security.identitystore.persistence.CachedIdentityStore;
import javax.security.identitystore.persistence.cachedsource.CachedIdentityStoreSource;
import javax.security.identitystore.persistence.cachedsource.JsonFileIdentityStoreSource;
import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Tests credential validation with the
 * <code>{@link javax.security.identitystore.persistence.CachedIdentityStore CachedIdentityStore}</code>
 * using a <code>{@link javax.security.identitystore.persistence.cachedsource.JsonFileIdentityStoreSource JsonFileIdentityStoreSource}</code>.
 * <p>
 * This test invokes CDI using <a href="http://jglue.org/cdi-unit">CDI-Unit</a>.
 */
@RunWith(CdiRunner.class) // Runs the test with CDI-Unit
@ActivatedAlternatives({CachedIdentityStore.class})
@AdditionalClasses({ValidateCredentialTest_JsonStore.ByteCredentialValidator.class})
public class ValidateCredentialTest_JsonStore {

    /**
     * <code>ByteCredential</code> is a credential used for testing that
     * has a byte array credential value, which is not supported by
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

    /**
     * <code>ByteCredentialValidator</code> validates <code>ByteCredential</code>
     * against a <code>CachedIdentityStore</code>.
     */
    @Validator(credentialClass = ByteCredential.class, identityStoreClass = CachedIdentityStore.class)
    public static class ByteCredentialValidator implements CredentialValidator {

        public static final AtomicInteger timesCalled = new AtomicInteger(0);

        public CredentialValidationResult validate(Credential credential, IdentityStore identityStore) {
            timesCalled.incrementAndGet();

            if (!(identityStore instanceof CachedIdentityStore))
                throw new IllegalStateException("Expected CachedIdentityStore");
            CachedIdentityStore cachedIdentityStore = (CachedIdentityStore)identityStore;

            if (credential instanceof ByteCredential) {
                return cachedIdentityStore.validate(
                    credential.getCaller(),
                    credential.getClass().getName(),
                    ((ByteCredential) credential).getCredentialValue());
            }
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }
    }

    @Inject
    private IdentityStore credentialStore;

    /**
     * Produces a <code>{@link javax.security.identitystore.persistence.cachedsource.JsonFileIdentityStoreSource JsonFileIdentityStoreSource}</code>
     * for use by a <code>{@link javax.security.identitystore.persistence.CachedIdentityStore}</code>.
     *
     * @return A <code>CachedIdentityStoreSource</code>
     */
    @Produces
    public CachedIdentityStoreSource getCredentialStoreSource() {
        System.out.println("getCredentialStoreSource called");
        URL resource = Thread.currentThread().getContextClassLoader().getResource("identitystore/testIdStore.json");
        if (null == resource)
            throw new NullPointerException("\"identitystore/testIdStore.json\" not in classpath.");
        String idStoreFilePath = resource.getFile();
        File idStoreFile = new File(idStoreFilePath);

        JsonFileIdentityStoreSource source = new JsonFileIdentityStoreSource(idStoreFile);
        return source;
    }

    /**
     * Tests validation of a valid username/password credential using a
     * <code>{@link javax.security.identitystore.persistence.CachedIdentityStore CachedIdentityStore}</code>
     * backed by a JSON file source.
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
     * Tests validation of a valid Basic Authentication credential using a
     * <code>{@link javax.security.identitystore.persistence.CachedIdentityStore CachedIdentityStore}</code>
     * backed by a JSON file source.
     */
    @Test
    public void basicAuthenticationCredential_valid() {

        BasicAuthenticationCredential basicCreds = new BasicAuthenticationCredential(
            Base64.getEncoder().encodeToString("jsmith:welcome1".getBytes(StandardCharsets.ISO_8859_1)));
        assertEquals("username", "jsmith", basicCreds.getCaller());
        assertTrue("password", Arrays.equals("welcome1".toCharArray(), basicCreds.getPassword().getValue()));

        CredentialValidationResult validationResult = credentialStore.validate(basicCreds);
        basicCreds.clear();

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
     * Tests validation of a valid custom credential using a
     * <code>{@link javax.security.identitystore.persistence.CachedIdentityStore CachedIdentityStore}</code>
     * backed by a JSON file source. This invokes the
     * <code>{@link javax.security.identitystore.ValidateCredentialTest_JsonStore.ByteCredentialValidator ByteCredentialValidator}</code>
     * to do the validation.
     */
    @Test
    public void byteCredential_valid() {

        ByteCredential creds = new ByteCredential("jsmith", "welcome1");
        assertEquals("times CredentialValidator called", 0, ByteCredentialValidator.timesCalled.get());
        CredentialValidationResult validationResult = credentialStore.validate(creds);
        assertEquals("times CredentialValidator called", 1, ByteCredentialValidator.timesCalled.get());
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
     * backed by a JSON file source.
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
}
