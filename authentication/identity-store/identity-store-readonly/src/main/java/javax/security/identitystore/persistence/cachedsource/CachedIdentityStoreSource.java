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
package javax.security.identitystore.persistence.cachedsource;

import java.io.IOException;
import java.util.*;
import javax.security.identitystore.persistence.CachedIdentityStore;

/**
 * <code>CachedIdentityStoreSource</code> provides common behavior for
 * <code>{@link CachedIdentityStore}</code> identity data source implementations.
 */
public abstract class CachedIdentityStoreSource {

    /**
     * <code>CredentialSource</code> is an in-memory representation of the
     * credential data stored in a source.
     */
    public static class CredentialSource {
        private final String type;
        private final String value;
        private final String hashAlgorithm;
        private final String hashSalt;
        private final Map<String, String> attributes;

        public CredentialSource(String type,
                                String value,
                                String hashAlgorithm,
                                String hashSalt,
                                Map<String, String> attributes) {
            this.type = type;
            this.value = value;
            this.hashAlgorithm = hashAlgorithm;
            this.hashSalt = hashSalt;
            this.attributes = (null == attributes) ? null : Collections.unmodifiableMap(new HashMap<>(attributes));
        }

        public String getType() {
            return type;
        }

        public String getValue() { return value; }

        public String getHashAlgorithm() {
            return hashAlgorithm;
        }

        public String getHashSalt() {
            return hashSalt;
        }

        public Map<String, String> getAttributes() { return attributes; }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("\n").append("type: ").append(type);
            sb.append("\n").append("value: ").append(value);
            sb.append("\n").append("hashAlgorithm: ").append(hashAlgorithm);
            sb.append("\n").append("hashSalt: ").append(hashSalt);
            sb.append("\n").append("attributes: ").append(attributes);
            return sb.toString();
        }
    }

    /**
     * <code>CallerSource</code> is an in-memory representation of the
     * caller data stored in a source.
     */
    public static class CallerSource {
        private final String name;
        private final List<String> groups;
        private final List<String> roles;
        private final List<CredentialSource> credentials;
        private final Map<String, String> attributes;

        public CallerSource(
                String name,
                List<String> groups,
                List<String> roles,
                List<CredentialSource> credentials,
                Map<String, String> attributes ) {

            if (null == name)
                throw new NullPointerException("Caller name");
            this.name = name;
            this.groups = (null == groups) ? null : Collections.unmodifiableList(new ArrayList<>(groups));
            this.roles = (null == roles) ? null : Collections.unmodifiableList(new ArrayList<>(roles));
            this.credentials = (null == credentials) ? null : Collections.unmodifiableList(new ArrayList<>(credentials));
            this.attributes = (null == attributes) ? null : Collections.unmodifiableMap(new HashMap<>(attributes));
        }

        public String getName() {
            return name;
        }

        public List<String> getGroups() {
            return groups;
        }

        public List<String> getRoles() {
            return roles;
        }

        public List<CredentialSource> getCredentials() {
            return credentials;
        }

        public Map<String, String> getAttributes() { return attributes; }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("name: ").append(name).append("\n");
            sb.append("groups: ").append(groups).append("\n");
            sb.append("roles: ").append(roles).append("\n");
            sb.append("credentials: ").append(credentials).append("\n");
            sb.append("attributes: ").append(attributes).append("\n");
            return sb.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CallerSource that = (CallerSource) o;

            if (!name.equals(that.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }
    }

    /**
     * Determines an iterator with which to read all of the caller identity data.
     *
     * @return The iterator
     * @throws IOException An error occurred while reading the source.
     */
    public abstract Iterator<CallerSource> getCallerIterator()  throws IOException;
}