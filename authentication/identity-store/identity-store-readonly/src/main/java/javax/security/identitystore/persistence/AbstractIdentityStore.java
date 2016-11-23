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

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.CDI;
import javax.security.identitystore.CredentialValidationResult;
import javax.security.identitystore.IdentityStore;
import javax.security.identitystore.annotation.ValidatorAnnotationLiteral;
import javax.security.identitystore.credential.Credential;
import javax.security.identitystore.credential.CredentialValidator;
import javax.security.identitystore.credential.UsernamePasswordCredential;

/**
 * <code>AbstractIdentityStore</code> provides common behavior for implementations
 * of {@link javax.security.identitystore.IdentityStore}.
 */
public abstract class AbstractIdentityStore implements IdentityStore {

    /**
     * Default validation behavior for username/password credentials.
     *
     * @param usernamePasswordCredential Credential to validate
     * @return The result
     */
    abstract protected CredentialValidationResult validateUsernamePassword(UsernamePasswordCredential usernamePasswordCredential);

    /**
     * Default implementation for validating any credential.
     * <p>
     * Validation occurs as follows:<br>
     * <ol>
     *     <li>If a qualified CDI bean of type
     *     <code>{@link javax.security.identitystore.credential.CredentialValidator CredentialValidator}</code> is found,
     *     delegate validation to the <code>CredentialValidator</code>.</li>
     *     <li>If the credential is of type
     *     <code>{@link javax.security.identitystore.credential.UsernamePasswordCredential UsernamePasswordCredential}</code>,
     *     delegate validation to the identity store implementation of {@link #validateUsernamePassword}.</li>
     *     <li>otherwise, fail validation.</li>
     * </ol>
     *
     * @param credential Credential to validate
     * @return The result
     */
    @Override
    public CredentialValidationResult validate(Credential credential) {
        if (null == credential) {
            throw new NullPointerException("Credential");
        }

        Class identityStoreClass = this.getClass();
        Class credentialClass = credential.getClass();
        Instance<CredentialValidator> instance = CDI.current().select(
                CredentialValidator.class, new ValidatorAnnotationLiteral(credentialClass, identityStoreClass));
        if (instance.isAmbiguous()) {
            throw new IllegalStateException("Ambiguous " + CredentialValidator.class.getName() + " for " +
                credentialClass.getName() + " and " + identityStoreClass.getName() + ".");
        }

        CredentialValidationResult result = null;
        try {
            if (!instance.isUnsatisfied()) {
                // Use overriding CredentialValidator, if found
                CredentialValidator selectedValidator = instance.get();
                result = selectedValidator.validate(credential, this);
            } else if (credential instanceof UsernamePasswordCredential) {
                // Default processing for UsernamePasswordCredential
                result = validateUsernamePassword((UsernamePasswordCredential)credential);
            } else {
                throw new IllegalStateException("Unsupported Credential type: " + credential.getClass().getName());
            }
        } catch (Exception e) {
            // TODO: Log it
            System.out.println("Unable to validate: " + e.getMessage());
            e.printStackTrace();
        }

        if (null == result) {
            return CredentialValidationResult.NOT_VALIDATED_RESULT;
        } else {
            return result;
        }
    }
}
