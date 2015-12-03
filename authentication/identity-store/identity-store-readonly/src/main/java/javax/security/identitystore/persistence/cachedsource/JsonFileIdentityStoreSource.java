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

import javax.enterprise.inject.Alternative;
import javax.json.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * <code>JsonFileIdentityStoreSource</code> is a {@link CachedIdentityStoreSource}
 * implementation which determines identity data from a given JSON file.
 */
@Alternative
public class JsonFileIdentityStoreSource extends CachedIdentityStoreSource {

    private final File idStoreFile;

    /**
     * Utility for converting a JsonObject into a String.
     *
     * @param name
     * @param jsonObject
     * @return
     */
    private static String getString(String name, JsonObject jsonObject) {
        if (null == name || null == jsonObject)
            return null;
        JsonString jsonString = jsonObject.getJsonString(name);
        return null == jsonString ? null : jsonString.getString();
    }

    /**
     * <code>CallerIterator</code> is an implementation of {@link Iterator<CallerSource>}
     * and supplies identity data by reading the associated JSON file.
     */
    static final class CallerIterator implements Iterator<CallerSource> {

        private final JsonArray callersArray;
        private int currentElementIndex = -1;
        private final File idStoreFile;

        IOException newBadSchemaException(String reason) {
            return new IOException("Bad schema for JSON file identity store \"" +
                    idStoreFile +"\". " + (null == reason ? "" : reason));
        }

        CallerIterator(File idStoreFile) throws IOException {
            if (null == idStoreFile)
                throw new NullPointerException("Id Store File");
            this.idStoreFile = idStoreFile;
            JsonReader jsonReader;
            try {
                jsonReader = Json.createReader(new InputStreamReader(new FileInputStream(idStoreFile)));
            } catch (IOException ioe) {
                ioe.printStackTrace();
                // TODO: Logging
                throw ioe;
            }
            JsonStructure jsonStructure = jsonReader.read();
            if ((null == jsonStructure) || (jsonStructure.getValueType() != JsonValue.ValueType.OBJECT)) {
                throw newBadSchemaException("Unexpected top structure, expected " +
                    JsonValue.ValueType.OBJECT + " was " +
                    ((null == jsonStructure) ? "null" : jsonStructure.getValueType()) + ".");
            }
            JsonObject jsonObject = (JsonObject) jsonStructure;
            callersArray = jsonObject.getJsonArray("callers");
        }

        @Override
        public boolean hasNext() {
            return ((null != callersArray) && (currentElementIndex + 1) < callersArray.size());
        }

        @Override
        public CallerSource next() {
            if (!hasNext())
                throw new NoSuchElementException("No Caller instances");

            ++currentElementIndex;
            // TODO: Tighter error checking: nulls, schema, size
            JsonObject currentCaller = (JsonObject)callersArray.get(currentElementIndex);
            if (null == currentCaller) {
                return null;
            }

            // name
            String name = getString("name", currentCaller);
            if (null == name ) {
                // TODO: Log it
                System.out.println("Caller missing required name.");
                return null;
            }
            // groups
            JsonArray groupsJsonArray = currentCaller.getJsonArray("groups");
            List<String> groupsStringList = null;
            if (null != groupsJsonArray) {
                List<JsonString> groupsJsonStringList = groupsJsonArray.getValuesAs(JsonString.class);
                groupsStringList = new ArrayList<>(groupsJsonStringList.size());
                for (JsonString groupJsonString : groupsJsonStringList) {
                    if (null != groupJsonString && null != groupJsonString.getString()) {
                        groupsStringList.add(groupJsonString.getString());
                    }
                }
            }
            // roles
            JsonArray rolesJsonArray = currentCaller.getJsonArray("roles");
            List<String> rolesStringList = null;
            if (null != rolesJsonArray) {
                List<JsonString> rolesJsonStringList = rolesJsonArray.getValuesAs(JsonString.class);
                rolesStringList = new ArrayList<>(rolesJsonStringList.size());
                for (JsonString roleJsonString : rolesJsonStringList) {
                    if (null != roleJsonString && null != roleJsonString.getString()) {
                        rolesStringList.add(roleJsonString.getString());
                    }
                }
            }
            // credentials
            JsonArray credentialsJsonArray = currentCaller.getJsonArray("credentials");
            List<CredentialSource> credentialsList = null;
            if (null != credentialsJsonArray) {
                List<JsonObject> credentialsJsonObjectList = credentialsJsonArray.getValuesAs(JsonObject.class);
                credentialsList = new ArrayList<>(credentialsJsonObjectList.size());
                for (JsonObject credentialJsonObject : credentialsJsonObjectList) {
                    if (null != credentialJsonObject) {
                        String type = getString("type", credentialJsonObject);
                        String value = getString("value", credentialJsonObject);
                        String hashAlgorithm = getString("hash-algorithm", credentialJsonObject);
                        String hashSalt = getString("hash-salt", credentialJsonObject);
                        // attributes
                        JsonArray attributesJsonArray = credentialJsonObject.getJsonArray("attributes");
                        HashMap<String, String> attributesMap = null;
                        if (null != attributesJsonArray) {
                            List<JsonObject> attributesJsonObjectList = attributesJsonArray.getValuesAs(JsonObject.class);
                            attributesMap = new HashMap<>(attributesJsonObjectList.size());
                            for (JsonObject attribute : attributesJsonObjectList) {
                                if (null != attribute) {
                                    String attrName = getString("name", attribute);
                                    String attrValue = getString("value", attribute);
                                    if ((null != attrName) && (null != attrValue))
                                        attributesMap.put(attrName, attrValue);
                                }
                            }
                        }
                        credentialsList.add(new CredentialSource(type, value, hashAlgorithm, hashSalt, attributesMap));
                    }
                }
            }

            // attributes
            JsonArray attributesJsonArray = currentCaller.getJsonArray("attributes");
            HashMap<String, String> attributesMap = null;
            if (null != attributesJsonArray) {
                List<JsonObject> attributesJsonObjectList = attributesJsonArray.getValuesAs(JsonObject.class);
                attributesMap = new HashMap<>(attributesJsonObjectList.size());
                for (JsonObject attribute : attributesJsonObjectList) {
                    if (null != attribute) {
                        String attrName = getString("name", attribute);
                        String attrValue = getString("value", attribute);
                        if ((null != attrName) && (null != attrValue))
                            attributesMap.put(attrName, attrValue);
                    }
                }
            }

            return new CallerSource(name, groupsStringList, rolesStringList, credentialsList, attributesMap);
        }

    }

    /**
     * Constructor
     *
     * @param idStoreFile The associated JSON file.
     */
    public JsonFileIdentityStoreSource(File idStoreFile) {
        if (null == idStoreFile) {
            throw new NullPointerException("Id Store File");
        }
        this.idStoreFile = idStoreFile;
    }

    /**
     * Determines an iterator with which to read all of the caller identity data.
     *
     * @return The iterator
     * @throws IOException An error occurred while reading the source.
     */
    @Override
    public Iterator<CallerSource> getCallerIterator() throws IOException {
        File localIdStoreFile = idStoreFile;
        if (null == localIdStoreFile) {
            // TODO: Log it
            System.out.println("Unable to get caller iterator, no file provided.");
            return null;
        } else {
            return new CallerIterator(localIdStoreFile);
        }
    }
}