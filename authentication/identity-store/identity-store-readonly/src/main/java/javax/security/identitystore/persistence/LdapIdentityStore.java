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

import javax.enterprise.inject.Alternative;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * <code>LdapIdentityStore</code> is an {@link javax.security.identitystore.IdentityStore}
 * implementation which uses an external LDAP server as a persistence mechanism.
 */
@Alternative
public class LdapIdentityStore
    extends AbstractIdentityStore {

    final private Hashtable<String, Object> contextEnv;
    final private LdapEntryMapping entryMapping;

    /**
     * Constructor.
     *
     * @param contextEnvironment Environment used to create the initial {@link LdapContext}.
     * @param entryMapping LDAP entry distriguished name and attribute mapping
     */
    public LdapIdentityStore(
        Hashtable<String, Object> contextEnvironment,
        LdapEntryMapping entryMapping)  {
        this.contextEnv = new Hashtable(contextEnvironment);
        this.entryMapping = entryMapping;
    }

    /**
     * Formats the caller distinguished name (DN) based on the given caller name and the configured DN pattern.
     *
     * @param caller Caller name
     * @return The caller DN
     */
    String formatDn(String caller) {
        return MessageFormat.format(entryMapping.getCallerMapping().getDnPattern(), caller);
    }

    /**
     * Default validation behavior for username/password credentials.
     *
     * @param usernamePasswordCredential Credential to validate
     * @return The result
     */
    @Override
    protected CredentialValidationResult validateUsernamePassword(UsernamePasswordCredential usernamePasswordCredential) {
        final String caller = usernamePasswordCredential.getCaller();

        LdapContext authCtx = null;
        try {
            Hashtable<String, Object> env = new Hashtable<String, Object>(contextEnv);

            String principalDn = formatDn(caller);

            env.put(Context.SECURITY_AUTHENTICATION, "simple");
            env.put(Context.SECURITY_PRINCIPAL, principalDn);
            env.put(Context.SECURITY_CREDENTIALS, new String(usernamePasswordCredential.getPassword().getValue()));

            // Never use connection pool to prevent password caching
            env.put("com.sun.jndi.ldap.connect.pool", "false");

            authCtx = new InitialLdapContext(env, null);

            return new CredentialValidationResult(CredentialValidationResult.Status.VALID, caller, this);
        } catch (Exception e) {
            // TODO: Add logging
            System.out.println("Credential validation failed for caller " + caller);
        } finally {
            if (authCtx != null) {
                try {
                    authCtx.close();
                } catch (NamingException e) {
                }
            }
        }
        return CredentialValidationResult.INVALID_RESULT;
    }

    /**
     * Determines the list of groups that the specified Caller is in,
     * based on the associated persistence store.
     *
     * @param callerName The Caller name
     * @return The list of groups that the specified Caller is in, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getCallerGroups(String callerName) {
        LdapContext ctx = null;

        try {
            Hashtable<String, Object> env = new Hashtable<String, Object>(contextEnv);

            // Never use connection pool to prevent password caching
            env.put("com.sun.jndi.ldap.connect.pool", "false");

            ctx = new InitialLdapContext(env, null);

            // Get group attribute from caller entry
            String callerDn = formatDn(callerName);
            Attributes attributes = ctx.getAttributes(callerDn, new String[]{entryMapping.getCallerMapping().getGroupAttribute()});
            Attribute attribute = attributes.get(entryMapping.getCallerMapping().getGroupAttribute());
            NamingEnumeration<?> values = attribute.getAll();
            ArrayList<String> result = new ArrayList<>();
            while (values.hasMore()) {
                String dn = (String)values.next();
                LdapName ldapDN = new LdapName(dn);
                List<Rdn> rdns = ldapDN.getRdns();
                Rdn mostSpecificRdn = rdns.get(rdns.size() - 1);
                result.add((String)mostSpecificRdn.getValue());
            }
            return result;
        } catch (Exception e) {
            // TODO: Add logging
            e.printStackTrace();
            return null;
        } finally {
            if (ctx != null) {
                try {
                    ctx.close();
                } catch (NamingException e) {
                }
            }
        }
    }

    /**
     * Determines the list of roles that the specified Caller has,
     * based on the associated persistence store. The returned role list
     * would include roles directly assigned to the Caller, and roles assigned
     * to groups which contain the Caller.
     *
     * @param callerName The Caller name
     * @return The list of roles that the specified Caller has, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getCallerRoles(String callerName) {
        // No standard way to determine roles in LDAP.
        return null;
    }
}
