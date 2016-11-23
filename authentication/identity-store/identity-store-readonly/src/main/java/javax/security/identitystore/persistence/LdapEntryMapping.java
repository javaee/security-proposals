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

/**
 * <code>LdapEntryMapping</code> contains required LDAP entry DNs and attribute
 * names, enabling LDAP bind and searches.
 */
public class LdapEntryMapping {

    private final CallerMapping callerMapping = new CallerMapping();

    /**
     * <code>CallerMapping</code> contains required LDAP entry DNs and attribute
     * names for caller entries.
     */
    public static class CallerMapping {
        private volatile String dnPattern;
        private volatile String groupAttribute;

        /**
         * Determines the distinguished name (DN) pattern for a caller entry name.
         * <p>
         * The value would be formatted like: <br>
         *     <code>"uid={0},ou=people,ou=myrealm,dc=base_domain"</code><br>
         * <p>
         * where the <code>{0}</code> would be substituted with the caller name.
         *
         * @return The distinguished name (DN) pattern for a caller entry name.
         */
        public String getDnPattern() {
            return dnPattern;
        }

        /**
         * Specifies the distinguished name (DN) pattern for a caller entry name.
         * See {@link #getDnPattern} for pattern format.
         *
         *
         * @param dnPattern  The caller DN pattern.
         * @return This <code>CallerMapping</code> instance, for additional configuration.
         * @see #getDnPattern
         */
        public CallerMapping setDnPattern(String dnPattern) {
            this.dnPattern = dnPattern;
            return this;
        }

        /**
         * Determines the name of the caller entry attribute containing the
         * names of groups of which this caller is a member.
         *
         * @return The caller entry group attribute name.
         */
        public String getGroupAttribute() {
            return groupAttribute;
        }

        /**
         * Specifies the name of the caller entry attribute containing the
         * names of groups of which this caller is a member.
         *
         * @param groupAttribute The caller entry group attribute name.
         * @return This <code>CallerMapping</code> instance, for additional configuration.
         * @see #getGroupAttribute
         */
        public CallerMapping setGroupAttribute(String groupAttribute) {
            this.groupAttribute = groupAttribute;
            return this;
        }
    }

    /**
     * Determines the contained instance of <code>CallerMapping</code>.
     *
     * @return The contained instance of <code>CallerMapping</code>.
     */
    public CallerMapping getCallerMapping() { return callerMapping; }
}
