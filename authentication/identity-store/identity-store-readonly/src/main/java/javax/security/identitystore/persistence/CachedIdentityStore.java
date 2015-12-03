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

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.credential.UsernamePasswordCredential;
import javax.security.identitystore.persistence.cachedsource.CachedIdentityStoreSource;
import javax.security.identitystore.query.CallerRoleMap;
import javax.security.identitystore.query.CallerStore;
import javax.security.identitystore.query.GroupStore;
import javax.security.identitystore.query.RoleStore;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;


/**
 * <code>CachedIdentityStore</code> is an <code>{@link javax.security.identitystore.IdentityStore}</code>
 * implementation which caches the entire source of identity data locally from a given source.
 * This would typically be used for development and test deployments.
 * <p>
 * Identity data is obtained from an associated
 * <code>{@link javax.security.identitystore.persistence.cachedsource.CachedIdentityStoreSource CachedIdentityStoreSource}</code>,
 * which may be implemented using a file, programmatic API, or annotations.
 */
@Alternative
public class CachedIdentityStore
    extends AbstractIdentityStore
    implements
        CallerStore,
        GroupStore,
        RoleStore,
        CallerRoleMap {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private volatile CachedIdentityStoreSource cachedIdentityStoreSource;

    // Cached mappings
    // NOTE: Private inner classes do not work in CDI
    protected static final class Cache {
        private final Map<String, List<CachedIdentityStoreSource.CredentialSource>> callerToCredentialSource = new HashMap();

        private final Map<String, String> callers = new HashMap();
        private final Map<String, String> groups = new HashMap();
        private final Map<String, String> roles = new HashMap();
        private final Map<String, List<String>> callerToGroup = new HashMap();
        private final Map<String, List<String>> groupToCallers = new HashMap();
        private final Map<String, List<String>> callerToRoles = new HashMap();
        private final Map<String, List<String>> roleToCallers = new HashMap();
        private final Map<String, Map<String, String>> callerToCallerAttributes = new HashMap();
    }
    private volatile Cache cache = null;

    /**
     * Utility for converting char[] to byte[].
     *
     * @param charset
     * @param input  char[]
     * @return byte[]
     */
    private static byte[] getBytes(Charset charset, char[] input) {
        CharBuffer charBuffer = CharBuffer.wrap(input);
        ByteBuffer byteBuffer = charset.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(),
            byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    /**
     * Utility for converting byte[] to char[].
     *
     * @param charset
     * @param input  byte[]
     * @return char[]
     */
    private static char[] getChars(Charset charset, byte[] input) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(input);
        CharBuffer charBuffer = charset.decode(byteBuffer);
        char[] chars = Arrays.copyOfRange(charBuffer.array(),
            charBuffer.position(), charBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return chars;
    }

    /**
     * Adds a caller from the source into the local cache.
     *
     * @param callerSource
     */
    private void addCaller(Cache cache, CachedIdentityStoreSource.CallerSource callerSource) {
        Objects.requireNonNull(callerSource, "Caller Source");

        String loginName = callerSource.getName();
        cache.callers.put(loginName, loginName);
        cache.callerToGroup.put(loginName, callerSource.getGroups());
        cache.callerToRoles.put(loginName, callerSource.getRoles());
        cache.callerToCallerAttributes.put(loginName, callerSource.getAttributes());

        List<CachedIdentityStoreSource.CredentialSource> credentials = callerSource.getCredentials();
        cache.callerToCredentialSource.put(loginName, credentials);
    }

    /**
     * Builds derived maps in the local cache to facilitate queries.
     *
     * @param cache
     */
    private void buildImpliedMaps(Cache cache) {
        cache.groups.clear();
        cache.groupToCallers.clear();
        Set<Map.Entry<String, List<String>>> callerToGroupEntries = cache.callerToGroup.entrySet();
        for (Map.Entry<String, List<String>> entry : callerToGroupEntries) {
            String callerLoginName = entry.getKey();
            List<String> callerGroups = entry.getValue();
            if ((null != callerLoginName) && (null != callerGroups)) {
                for (String group : callerGroups) {
                    cache.groups.put(group, group);

                    List<String> callers = cache.groupToCallers.get(group);
                    if (null == callers) {
                        callers = new ArrayList<>();
                        cache.groupToCallers.put(group, callers);
                    }
                    if (!callers.contains(callerLoginName)) {
                        callers.add(callerLoginName);
                    }
                }
            }
        }

        cache.roles.clear();
        cache.roleToCallers.clear();
        Set<Map.Entry<String, List<String>>> callerToRoleEntries = cache.callerToRoles.entrySet();
        for (Map.Entry<String, List<String>> entry : callerToRoleEntries) {
            String callerLoginName = entry.getKey();
            List<String> callerRoles = entry.getValue();
            if ((null != callerLoginName) && (null != callerRoles)) {
                for (String role : callerRoles) {
                    cache.roles.put(role, role);

                    List<String> callers = cache.roleToCallers.get(role);
                    if (null == callers) {
                        callers = new ArrayList<>();
                        cache.roleToCallers.put(role, callers);
                    }
                    if (!callers.contains(callerLoginName)) {
                        callers.add(callerLoginName);
                    }
                }
            }
        }
    }

    /**
     * Checks the initialized state of the local cache.
     */
    private void checkInitialized() {
        if (null == cache) {
            throw new IllegalStateException("Uninitialized CachedIdentityStore");
        }
    }

    /**
     * Checks for non-null and non-empty strings.
     *
     * @param input
     * @return <code>true</code> if non-null and non-empty
     */
    private static boolean nonNullNonEmpty(String input) {
        return null != input && !input.isEmpty();
    }

    /**
     * Default validation behavior for username/password credentials.
     *
     * @param usernamePasswordCredential Credential to validate
     * @return The result
     */
    @Override
    protected CredentialValidationResult validateUsernamePassword(UsernamePasswordCredential usernamePasswordCredential) {
        return validate(
            usernamePasswordCredential.getCaller(),
            UsernamePasswordCredential.class.getName(),
            usernamePasswordCredential.getPassword().getValue());
    }

    /**
     * Validates the given char[] credential value.
     *
     * @param caller  The caller
     * @param credentialType The credential type, usually the credential class name.
     * @param credentialValue The credential value
     * @return The result
     */
    public CredentialValidationResult validate(String caller, String credentialType, char[] credentialValue) {
        checkInitialized();

        if (null == caller) {
            return CredentialValidationResult.INVALID_RESULT;
        }

        final List<CachedIdentityStoreSource.CredentialSource> credentialSources = cache.callerToCredentialSource.get(caller);

        boolean valid = false;
        if (null != credentialSources) {
            for (CachedIdentityStoreSource.CredentialSource credentialSource : credentialSources) {
                if ((null != credentialSource) && credentialType.equals(credentialSource.getType())) {
                    char[] preparedCredentialValue = Arrays.copyOf(credentialValue, credentialValue.length);
                    if (nonNullNonEmpty(credentialSource.getHashAlgorithm())) {
                        if (nonNullNonEmpty(credentialSource.getHashSalt())) {
                            preparedCredentialValue = appendSalt(credentialSource.getHashSalt(), preparedCredentialValue);
                        }
                        try {
                            preparedCredentialValue = calculateHash(credentialSource.getHashAlgorithm(), preparedCredentialValue);
                        } catch (NoSuchAlgorithmException e) {
                            // TODO: Error handling
                            e.printStackTrace();
                        }
                    }
                    if ((null != preparedCredentialValue) && Arrays.equals(preparedCredentialValue, credentialSource.getValue().toCharArray()) ) {
                        valid = true;
                        break;
                    }
                }
            }
        }

        if (valid)
            return new CredentialValidationResult(CredentialValidationResult.Status.VALID, caller, this);
        else
            return CredentialValidationResult.INVALID_RESULT;
    }

    /**
     * Validates the given byte[] credential value.
     *
     * @param caller  The caller
     * @param credentialType The credential type, usually the credential class name.
     * @param credentialValue The credential value
     * @return The result
     */
    public CredentialValidationResult validate(String caller, String credentialType, byte[] credentialValue) {
        if (null == credentialValue) {
            throw new NullPointerException("Credential value");
        }

        char[] credentialChars = getChars(StandardCharsets.ISO_8859_1, Base64.getEncoder().encode(credentialValue));
        return validate(caller, credentialType, credentialChars);
    }

    /**
     * Initializes the identity store.
     */
    @PostConstruct
    void initialize() {
        try {
            load();
        } catch (IOException ioe) {
            // TODO: Error handling: logging, prevent continued operation
            ioe.printStackTrace();
        }
    }

    /**
     * Appends the given salt to the end of the input.
     *
     * @param salt  A random salt
     * @param input Input to be salted.
     * @return The salted input
     */
    protected char[] appendSalt(String salt, char[] input) {
        // TODO: check inputs

        char[] saltArr = salt.toCharArray();
        char[] output = new char[input.length + saltArr.length];
        System.arraycopy(input, 0, output, 0, input.length);
        System.arraycopy(saltArr, 0, output, input.length, saltArr.length);
        return output;
    }

    /**
     * Hash the input using the hash algorithm.
     *
     * @param hashAlgorithm An algorithm supported by the current JVM using
     *                      <code>{@link java.security.MessageDigest#getInstance}</code>.
     * @param input The input to hash
     * @return The hashed input
     * @throws java.security.NoSuchAlgorithmException The specified hash algorithm is not supported.
     * @throws java.lang.NullPointerException A parameter is null.
     */
    protected char[] calculateHash(String hashAlgorithm, char[] input) throws NoSuchAlgorithmException {
        Objects.requireNonNull(hashAlgorithm, "calculateHash hashAlgorithm");
        Objects.requireNonNull(input, "calculateHash input");

        MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
        md.update(getBytes(DEFAULT_CHARSET, input));
        byte[] hash =  md.digest();
        return getHexString(hash).toCharArray();
    }

    /**
     * Utility to convert the given byte array into a corresponding hex string.
     *
     * @param mdbytes Byte array
     * @return Hex string
     */
    protected static String getHexString(byte[] mdbytes) {
        StringBuilder hexString = new StringBuilder();
        for (int i=0; i < mdbytes.length; i++) {
            String hex=Integer.toHexString(0xff & mdbytes[i]);
            if(hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }

    /**
     * Constructor
     *
     * @param cachedIdentityStoreSource The source of identity data
     */
    @Inject
    public CachedIdentityStore(CachedIdentityStoreSource cachedIdentityStoreSource) {
        if (null == cachedIdentityStoreSource)
            throw new NullPointerException("CachedIdentityStoreSource");
        this.cachedIdentityStoreSource = cachedIdentityStoreSource;
    }

    /**
     * Associates the source to the identity store and
     * replaces the current cache contexts with the data in the source.
     *
     * @param source The source of identity data
     * @throws IOException An error occurred while reading the source.
     */
    public void load(CachedIdentityStoreSource source) throws IOException {
        if (null == source)
            throw new NullPointerException("CachedIdentityStoreSource");

        cachedIdentityStoreSource = source;
        load();
    }

    /**
     * Replaces the current cache contexts with the data in the associated source.
     *
     * @throws IOException An error occurred while reading the source.
     */
    public void load() throws IOException {
        if (null == cachedIdentityStoreSource) {
            // TODO: Log it
            System.out.println("Unable to load null source.");
            return;
        }
        // Load cache from source
        Iterator<CachedIdentityStoreSource.CallerSource> callerIterator = cachedIdentityStoreSource.getCallerIterator();
        if (null != callerIterator) {
            Cache cache = new Cache();
            while (callerIterator.hasNext()) {
                CachedIdentityStoreSource.CallerSource callerSource = callerIterator.next();
                if (null != callerSource)
                    addCaller(cache, callerSource);
            }
            buildImpliedMaps(cache);
            // Replace cache
            this.cache = cache;
        }
    }

    /**
     * Determines the associated <code>CachedIdentityStoreSource</code>.
     *
     * @return The associated <code>CachedIdentityStoreSource</code>.
     */
    public CachedIdentityStoreSource getSource() {
        return cachedIdentityStoreSource;
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
        checkInitialized();
        return cache.callerToRoles.get(callerName);
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
        checkInitialized();
        return cache.callerToGroup.get(callerName);
    }

    ////////////////////////////////////////////////////////////////////////////
    // CallerStore
    //

    /**
     * Determines a list of callers found in the identity store.
     *
     * @param regEx A regular expression to select callers by name,
     *  <code>null</code> or empty string for all.
     * @return The list of found callers, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getCallers(String regEx) {
        checkInitialized();
        if ((null == regEx) || ("".equals(regEx)))
            return new ArrayList<>(cache.callers.keySet());

        // Build username type key

        // Scan usernames Property

        return null;
    }

    /**
     * Determines the associated attribute map for the caller in the identity store.
     *
     * @param name Caller name
     * @return The associated attributes, empty map if none,
     * <code>null</code> if not supported.
     */
    @Override
    public Map<String, String> getCallerAttributes(String name) {
        return cache.callerToCallerAttributes.get(name);
    }

    ////////////////////////////////////////////////////////////////////////////
    // RoleStore
    //

    /**
     * Determines a list of roles found in the identity store.
     *
     * @param regEx A regular expression to select roles by name,
     *  <code>null</code> or empty string for all.
     * @return The list of found roles, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getRoles(String regEx) {
        checkInitialized();
        if ((null == regEx) || ("".equals(regEx)))
            return new ArrayList<>(cache.roles.keySet());

        // scan
        return null;
    }

    /**
     * Determines a list of roles found in the identity store, which have been
     * assigned to either a caller or a group.
     * <p>
     * NOTE THAT depending on the identity store implementation, this method may
     * only consider roles which are explicitly mapped to callers and groups. Roles assigned
     * via evaluated expressions (e.g., XACML expressions) may not be considered.
     *
     * @param regEx A regular expression to select roles by name,
     *  <code>null</code> or empty string for all.
     * @return The list of found roles, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getAssignedRoles(String regEx) {
        checkInitialized();
        if ((null == regEx) || ("".equals(regEx)))
            return new ArrayList<>(cache.roles.keySet());

        // scan
        return null;
    }

    /**
     * Determines a list of roles found in the identity store, which have not been
     * assigned to either a caller or a group.
     * <p>
     * NOTE THAT depending on the identity store implementation, this method may
     * only consider roles which are explicitly mapped to callers and groups. Roles assigned
     * via evaluated expressions (e.g., XACML expressions) may not be considered.
     *
     * @param regEx A regular expression to select roles by name,
     *  <code>null</code> or empty string for all.
     * @return The list of found roles, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getUnassignedRoles(String regEx) {
        checkInitialized();
        if ((null == regEx) || ("".equals(regEx)))
            return new ArrayList<>(cache.roles.keySet());

        // scan
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // CallerRoleMap
    //

    /**
     * Determines a list of callers found in the identity store, which have been
     * assigned the given role.
     * <p>
     * NOTE THAT depending on the identity store implementation, this method may
     * only consider callers which are explicitly assigned a role. Callers assigned
     * via evaluated expressions (e.g., XACML expressions) may not be considered.
     *
     * @param role The role name
     * @param includeGroupRoles <code>true</code> to include roles assigned via
     *                          group memberships.
     * @return The list of callers which have been assigned the given role, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getCallersWithRole(String role, boolean includeGroupRoles ) {

        if (includeGroupRoles)
            return null;

        checkInitialized();
        return cache.roleToCallers.get(role);
    }

    /**
     * Determines a list of roles found in the identity store, which have been
     * assigned to the given caller.
     * <p>
     * NOTE THAT depending on the identity store implementation, this method may
     * only consider callers which are explicitly assigned a role. Callers assigned
     * via evaluated expressions (e.g., XACML expressions) may not be considered.
     *
     * @param callerName The Caller name
     * @param includeGroupRoles <code>true</code> to include roles assigned via
     *                          group memberships.
     * @return The list of roles that the specified caller has, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getCallerRoles(String callerName, boolean includeGroupRoles) {
        if (includeGroupRoles)
            return null;

        return getCallerRoles(callerName);
    }

    ////////////////////////////////////////////////////////////////////////////
    // GroupStore
    //

    /**
     * Determines a list of groups found in the identity store.
     *
     * @param regEx A regular expression to select groups by name,
     *  <code>null</code> or empty string for all.
     * @return The list of found groups, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getGroups(String regEx) {
        checkInitialized();
        if ((null == regEx) || ("".equals(regEx)))
            return new ArrayList<>(cache.groups.keySet());

        // scan
        return null;
    }

    /**
     * Determines a list of callers found in the identity store, which have been
     * assigned to the given group.
     * <p>
     * NOTE THAT depending on the identity store implementation, this method may
     * only consider callers which are explicitly assigned to groups. Callers assigned
     * via evaluated expressions (e.g., XACML expressions) may not be considered.
     *
     * @param group The group name
     * @return The list of callers which are assigned to the group, empty list if none,
     * <code>null</code> if not supported.
     */
    @Override
    public List<String> getCallersInGroup(String group) {
        checkInitialized();
        return cache.groupToCallers.get(group);
    }
}
