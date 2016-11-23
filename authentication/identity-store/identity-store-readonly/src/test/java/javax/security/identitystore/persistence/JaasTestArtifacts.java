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

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.util.*;

/**
 * <code>JaasTestArtifacts</code> contains test classes used by JAAS unit tests.
 */
public class JaasTestArtifacts {

    /** TestConfiguration entry index */
    public final static String CONFIG_ENTRY_NAME = "testEntry";

    /**
     * <code>TestCallerPrincipal</code> supports caller name.
     */
    public static class TestCallerPrincipal implements Principal {
        private final String name;

        public TestCallerPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean implies(Subject subject) {
            return false;
        }
    }

    /**
     * <code>TestGroupPrincipal</code> supports group name.
     */
    public static class TestGroupPrincipal implements Principal {
        private final String name;

        public TestGroupPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean implies(Subject subject) {
            return false;
        }
    }

    /**
     * <code>TestRolePrincipal</code> supports role name.
     */
    public static class TestRolePrincipal implements Principal {
        private final String name;

        public TestRolePrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean implies(Subject subject) {
            return false;
        }
    }

    /**
     * <code>TestLoginModule</code> uses username/password credentials to authenticate the caller.
     */
    public static class TestLoginModule implements LoginModule {
        private Subject subject;
        private CallbackHandler callbackHandler;

        @Override
        public void initialize(
            Subject subject,
            CallbackHandler callbackHandler,
            Map<String, ?> sharedState,
            Map<String, ?> options) {

            Objects.requireNonNull(subject, "Requires Subject");
            Objects.requireNonNull(callbackHandler, "Requires CallbackHandler");

            this.subject = subject;
            this.callbackHandler = callbackHandler;
        }

        @Override
        public boolean login() throws LoginException {

            Callback[] callbacks = new Callback[] {new NameCallback("Name"), new PasswordCallback("Password", false)};
            try {
                callbackHandler.handle(callbacks);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ("jsmith".equals(((NameCallback)callbacks[0]).getName()) &&
                Arrays.equals("welcome1".toCharArray(), ((PasswordCallback)callbacks[1]).getPassword())) {

            } else {
                throw new LoginException("Bad credentials");
            }

            return true;
        }

        @Override
        public boolean commit() throws LoginException {

            subject.getPrincipals().add(new TestCallerPrincipal("jsmith"));

            subject.getPrincipals().add(new TestGroupPrincipal("Deployers"));
            subject.getPrincipals().add(new TestGroupPrincipal("Operators"));

            // No roles for now

            return true;
        }

        @Override
        public boolean abort() throws LoginException {
            return true;
        }

        @Override
        public boolean logout() throws LoginException {
            return true;
        }
    }

    /**
     * <code>TestConfiguration</code> supports one AppConfigurationEntry for above
     * TestLoginModule.
     */
    public static class TestConfiguration extends Configuration {
        @Override
        public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
            return new AppConfigurationEntry[] {
                new AppConfigurationEntry(
                    "javax.security.identitystore.persistence.JaasTestArtifacts$TestLoginModule",
                    AppConfigurationEntry.LoginModuleControlFlag.REQUIRED, new HashMap<>())};
        }
    }

    /**
     * <code>TestJaasSubjectPrincipalResolver</code> extracts required
     * principals from the JAAS subject.
     */
    public static class TestJaasSubjectPrincipalResolver implements JaasSubjectPrincipalResolver {
        @Override
        public String getCaller(Subject subject) {
            Set<TestCallerPrincipal> principals = subject.getPrincipals(TestCallerPrincipal.class);
            if (null != principals && !principals.isEmpty()) {
                return principals.iterator().next().getName();
            }
            return null;
        }

        @Override
        public List<String> getCallerGroups(Subject subject, String callerName) {
            Set<TestGroupPrincipal> principals = subject.getPrincipals(TestGroupPrincipal.class);
            if (null != principals && !principals.isEmpty()) {
                ArrayList<String> groups = new ArrayList<>();
                for ( TestGroupPrincipal p : principals ) {
                    groups.add(p.getName());
                }
                return groups;
            }
            return null;
        }

        @Override
        public List<String> getCallerRoles(Subject subject, String callerName) {
            Set<TestRolePrincipal> principals = subject.getPrincipals(TestRolePrincipal.class);
            if (null != principals && !principals.isEmpty()) {
                ArrayList<String> roles = new ArrayList<>();
                for ( TestRolePrincipal p : principals ) {
                    roles.add(p.getName());
                }
                return roles;
            }
            return null;
        }
    }
}
