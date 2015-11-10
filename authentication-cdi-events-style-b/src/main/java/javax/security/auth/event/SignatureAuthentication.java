/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.security.auth.event;

/**
 * Event object to support "Signing HTTP Messages" draft specification
 * identified as the "Signature" http authorization scheme
 * <p/>
 * http://tools.ietf.org/html/draft-cavage-http-signatures
 * <p/>
 * Example HTTP Authorization header:
 * <p/>
 * Authorization: Signature keyId="Test",algorithm="rsa-sha256",
 * headers="(request-target) host date", signature="KcLSABBj/m3v2Dhxi
 * CKJmzYJvnx74tDO1SaURD8Dr8XpugN5wpy8iBVJtpkHUIp4qBYpzx2QvD16t8X
 * 0BUMiKc53Age+baQFWwb2iYYJzvuUL+krrl/Q7H6fPBADBsHqEZ7IE8rR0Ys3l
 * b7J5A6VB9J/4yVTRiBcxTypW/mpr5w="
 */
public class SignatureAuthentication extends Authentication {

    private final String keyId;
    private final String algorithm;
    private final String[] headers;
    private final String signature;

    public SignatureAuthentication(final String keyId, final String algorithm, final String[] headers, final String signature) {
        this.keyId = keyId;
        this.algorithm = algorithm;
        this.headers = headers;
        this.signature = signature;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String[] getHeaders() {
        return headers;
    }

    public String getSignature() {
        return signature;
    }
}
