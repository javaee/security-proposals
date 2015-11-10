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
package javax.security.identitystore.persistence;

import org.junit.Test;

import javax.security.identitystore.persistence.cachedsource.CachedIdentityStoreSource;
import javax.security.identitystore.persistence.cachedsource.MemoryIdentityStoreSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Tests the Memory source
 * {@link javax.security.identitystore.persistence.cachedsource.MemoryIdentityStoreSource}.
 * This would supply identity data to <code>{@link CachedIdentityStore}</code>.
 */
public class MemoryIdentityStoreSourceTest {

    @Test
    public void iterator() throws IOException {
        HashSet<CachedIdentityStoreSource.CallerSource> callerSources = new HashSet();
        List<String> groups;
        List<String> roles;
        List<CachedIdentityStoreSource.CredentialSource> credentialSources;

        // Populate collections

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

        callerSources.add(new CachedIdentityStoreSource.CallerSource("jsmith", groups, roles, credentialSources, null));


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

        // Get the source, using populated collections
        MemoryIdentityStoreSource source = new MemoryIdentityStoreSource(callerSources);

        // Test iterator
        Iterator<CachedIdentityStoreSource.CallerSource> iterator = source.getCallerIterator();

        while (iterator.hasNext()) {
            CachedIdentityStoreSource.CallerSource caller = iterator.next();
            System.out.println("Caller:");
            System.out.println(caller.toString());
            assertNotNull("Caller name", caller.getName());
        }
    }
}
