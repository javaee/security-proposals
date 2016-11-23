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

import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.jglue.cdiunit.ActivatedAlternatives;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.CdiRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;
import javax.security.identitystore.annotation.Validator;
import javax.security.identitystore.credential.*;
import javax.security.identitystore.persistence.CachedIdentityStore;
import javax.security.identitystore.persistence.cachedsource.CachedIdentityStoreSource;
import javax.security.identitystore.persistence.cachedsource.JsonFileIdentityStoreSource;
import java.io.File;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests extending <code>{@link javax.security.identitystore.credential.Credential Credential}</code>
 * support using a <code>{@link javax.security.identitystore.credential.CredentialValidator CredentialValidator}</code>.
 * <p>
 * This test invokes CDI using <a href="http://jglue.org/cdi-unit">CDI-Unit</a>.
 */
@RunWith(CdiRunner.class) // Runs the test with CDI-Unit
@ActivatedAlternatives(CachedIdentityStore.class)
@AdditionalClasses({CredentialValidatorTest.X509CredentialValidator.class, CredentialValidatorTest.OverridingCredentialValidator2.class})
public class CredentialValidatorTest {

    /**
     * <code>X509CredentialValidator</code> validates
     * <code>{@link javax.security.identitystore.CredentialValidatorTest.X509ClientCertCredential X509ClientCertCredential}</code> against
     * <code>{@link javax.security.identitystore.persistence.CachedIdentityStore CachedIdentityStore}</code>.
     * <p>
     * Note the CDI Qualifier <code>{@link javax.security.identitystore.annotation.Validator Validator}</code>
     * is used to register the <code>CredentialValidator</code> for specific
     * combinations of <code>Credential</code> and <code>IdentityStore</code>.
     * <p>
     * Note that repeating Annotations are NOT SUPPORTED by CDI,
     * see https://issues.jboss.org/browse/CDI-471.
     */
    @Validator(credentialClass = X509ClientCertCredential.class, identityStoreClass = CachedIdentityStore.class)
    public static class X509CredentialValidator implements CredentialValidator {

        // For testing the number of times this validator is called.
        public static final AtomicInteger timesCalled = new AtomicInteger(0);

        // Validate the credential against the identity store.
        public CredentialValidationResult validate(Credential credential, IdentityStore identityStore) {
            timesCalled.incrementAndGet();

            if (!(identityStore instanceof CachedIdentityStore))
                throw new IllegalStateException("Expected CachedIdentityStore");
            CachedIdentityStore cachedIdentityStore = (CachedIdentityStore)identityStore;

            if (credential instanceof X509ClientCertCredential) {
                return cachedIdentityStore.validate(credential.getCaller(), credential.getClass().getName(), new char[0]);
            }
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        }
    }

    /**
     * <code>OverridingCredentialValidator2</code> is used to test that
     * the default validation in IdentityStore has been overridden.
     */
    // Repeating Annotations NOT SUPPORTED by CDI, see https://issues.jboss.org/browse/CDI-471
    //@Validator(credentialClass = BasicAuthenticationCredential.class, identityStoreClass = CachedIdentityStore.class)
    @Validator(credentialClass = UsernamePasswordCredential.class, identityStoreClass = CachedIdentityStore.class)
    public static class OverridingCredentialValidator2 implements CredentialValidator {

        // For testing the number of times this validator is called.
        public static final AtomicInteger timesCalled = new AtomicInteger(0);

        // Validate the credential against the identity store.
        public CredentialValidationResult validate(Credential credential, IdentityStore identityStore) {
            timesCalled.incrementAndGet();

            // Always invalid for test
            return CredentialValidationResult.INVALID_RESULT;
        }
    }

    /**
     * Custom credential type. The received X.509 cert is assumed retrieved from
     * HttpServletRequest attribute "javax.servlet.request.X509Certificate".
     */
    static class X509ClientCertCredential extends AbstractCredential {

        private final String caller;


        public X509ClientCertCredential(X509Certificate[] certs) {
            // 1. Assume Subject DN Common Name is caller name
            // 2. Assume cert is trusted
            String callerName = null;
            if (null != certs && certs.length > 0) {
                X509Certificate clientCert = certs[0];
                String dn = clientCert.getSubjectX500Principal().getName();
                try {
                    LdapName ldapDN = new LdapName(dn);
                    for (Rdn rdn : ldapDN.getRdns()) {
                        if ("CN".equals(rdn.getType())) {
                            callerName = (String) rdn.getValue();
                            break;
                        }
                    }
                } catch (InvalidNameException e) {
                    e.printStackTrace();
                }
            }
            this.caller = callerName;
        }

        @Override
        public void clearCredential() {
            // no op
        }

        @Override
        public String getCaller() {
            return caller;
        }

        @Override
        public boolean isValid() {
            return true;
        }
    }

    /**
     * Valid credential test certificate
     */
    @SimpleStub
    public static abstract class TestX509Certificate_Found extends X509Certificate {
        public X500Principal getSubjectX500Principal() {
            return new X500Principal("CN=jsmith");
        }
    }

    /**
     * Invalid credential test certificate
     */
    @SimpleStub
    public static abstract class TestX509Certificate_NotFound extends X509Certificate {
        public X500Principal getSubjectX500Principal() {
            return new X500Principal("CN=foo");
        }
    }

    @Inject
    private IdentityStore credentialStore;

    /**
     * <code>{@link javax.security.identitystore.persistence.cachedsource.CachedIdentityStoreSource CachedIdentityStoreSource}</code>
     * producer, used by <code>{@link javax.security.identitystore.persistence.CachedIdentityStore CachedIdentityStore}</code>.
     *
     * @return A configured <code>CachedIdentityStoreSource</code>.
     */
    @Produces
    public CachedIdentityStoreSource getCredentialStoreSource() {
        System.out.println("getCredentialStoreSource");

        // Using JSON file as CachedIdentityStore source
        URL resource = Thread.currentThread().getContextClassLoader().getResource("identitystore/testIdStore.json");
        if (null == resource)
            throw new NullPointerException("\"identitystore/testIdStore.json\" not in classpath.");
        String idStoreFilePath = resource.getFile();
        File idStoreFile = new File(idStoreFilePath);

        // Instantiate source
        JsonFileIdentityStoreSource source = new JsonFileIdentityStoreSource(idStoreFile);

        return source;
    }

    /**
     * Tests that the extended <code>Credential</code> may be validated.
     */
    @Test
    public void availableCredentialValidator_validCreds() {

        X509Certificate[] certs = new java.security.cert.X509Certificate[1];
        certs[0] = Stub.create(TestX509Certificate_Found.class);

        X509ClientCertCredential creds = new X509ClientCertCredential(certs);
        X509CredentialValidator.timesCalled.set(0);
        assertEquals("times CredentialValidator called", 0, X509CredentialValidator.timesCalled.get());
        CredentialValidationResult validationResult = credentialStore.validate(creds);
        assertEquals("times CredentialValidator called", 1, X509CredentialValidator.timesCalled.get());
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
     * Tests that the extended <code>Credential</code> may be invalid.
     */
    @Test
    public void availableCredentialValidator_invalidCreds() {

        X509Certificate[] certs = new java.security.cert.X509Certificate[1];
        certs[0] = Stub.create(TestX509Certificate_NotFound.class);

        X509ClientCertCredential creds = new X509ClientCertCredential(certs);
        X509CredentialValidator.timesCalled.set(0);
        assertEquals("times CredentialValidator called", 0, X509CredentialValidator.timesCalled.get());
        CredentialValidationResult validationResult = credentialStore.validate(creds);
        assertEquals("times CredentialValidator called", 1, X509CredentialValidator.timesCalled.get());
        creds.clear();

        if (CredentialValidationResult.Status.VALID == validationResult.getStatus()) {
            fail("Should be invalid");
        } else {
            assertEquals("INVALID", CredentialValidationResult.Status.INVALID, validationResult.getStatus());
            // Invalid
        }
    }

    /**
     * Tests that the default credential validation may be overridden by a
     * <code>CredentialValidator</code>.
     */
    @Test
    public void overridingCredentialValidator() {

        // Correct creds info, override will return INVALID
        UsernamePasswordCredential creds = new UsernamePasswordCredential("jsmith", new Password("welcome1"));

        X509CredentialValidator.timesCalled.set(0);
        OverridingCredentialValidator2.timesCalled.set(0);

        assertEquals("times CredentialValidator called", 0, X509CredentialValidator.timesCalled.get());
        assertEquals("times CredentialValidator2 called", 0, OverridingCredentialValidator2.timesCalled.get());

        CredentialValidationResult validationResult = credentialStore.validate(creds);

        assertEquals("times CredentialValidator called", 0, X509CredentialValidator.timesCalled.get());
        assertEquals("times CredentialValidator2 called", 1, OverridingCredentialValidator2.timesCalled.get());

        // The override always returns invalid.
        assertEquals("Always invalid", CredentialValidationResult.Status.INVALID, validationResult.getStatus());
    }

}
