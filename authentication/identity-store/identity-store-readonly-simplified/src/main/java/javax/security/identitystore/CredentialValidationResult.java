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
package javax.security.identitystore;

import static java.util.Collections.unmodifiableList;
import static javax.security.identitystore.CredentialValidationResult.Status.INVALID;
import static javax.security.identitystore.CredentialValidationResult.Status.NOT_VALIDATED;
import static javax.security.identitystore.CredentialValidationResult.Status.VALID;

import java.util.ArrayList;
import java.util.List;

/**
 * <code>CredentialValidationResult</code> is the result from an attempt to
 * validate an instance of
 * {@link javax.security.identitystore.credential.Credential}.
 *
 * @see javax.security.identitystore.IdentityStore#validate
 */
public class CredentialValidationResult {

	public static final CredentialValidationResult INVALID_RESULT = new CredentialValidationResult(INVALID, null, null, null);
    public static final CredentialValidationResult NOT_VALIDATED_RESULT = new CredentialValidationResult(NOT_VALIDATED, null, null, null);

	private final String callerName;
	private final Status status;
	private final List<String> roles;
	private final List<String> groups;

	public enum Status {
		/**
		 * Indicates that the credential could not be validated, for example, if
		 * no suitable
		 * {@link javax.security.identitystore.credential.CredentialValidator}
		 * could be found.
		 */
		NOT_VALIDATED,

		/**
		 * Indicates that the credential is not valid after a validation
		 * attempt.
		 */
		INVALID,

		/**
		 * Indicates that the credential is valid after a validation attempt.
		 */
		VALID
	};

	public CredentialValidationResult(Status status, String callerName, List<String> groups) {
		this(status, callerName, groups, null);
	}
	
	/**
	 * Constructor
	 *
	 * @param status
	 *            Validation status
	 * @param callerName
	 *            Validated caller
	 * @param groups
	 *            Groups associated with the caller from the identity store
	 * @param roles
	 *            Roles associated with the caller from the identity store
	 */
	public CredentialValidationResult(Status status, String callerName, List<String> groups, List<String> roles) {

		if (null == status)
			throw new NullPointerException("status");

		this.status = status;
		this.callerName = callerName;

		if (VALID == status) {
			if (null != groups)
				groups = unmodifiableList(new ArrayList<>(groups));
			this.groups = groups;

			if (null != roles)
				roles = unmodifiableList(new ArrayList<>(roles));
			this.roles = roles;
		} else {
			this.groups = null;
			this.roles = null;
		}
	}

	/**
	 * Determines the validation status.
	 *
	 * @return The validation status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Determines the Caller used to validate the credential.
	 *
	 * @return The caller name, <code>null</code> if {@link #getStatus} does not
	 *         return {@link Status#VALID VALID}.
	 */
	public String getCallerName() {
		return callerName;
	}

	/**
	 * Determines the list of groups that the specified Caller is in, based on
	 * the associated persistence store..
	 *
	 * @return The list of groups that the specified Caller is in, empty if
	 *         none. <code>null</code> if {@link #getStatus} does not return
	 *         {@link Status#VALID VALID} or if the identity store does not
	 *         support groups.
	 */
	public List<String> getCallerGroups() {
		return groups;
	}

	/**
	 * Determines the list of roles that the specified caller is in, based on
	 * the associated persistence store. The returned role list would include
	 * roles directly assigned to the Caller, and roles assigned to groups which
	 * contain the Caller.
	 *
	 * @return The list of roles that the specified caller is in, empty if none.
	 *         <code>null</code> if {@link #getStatus} does not return
	 *         {@link Status#VALID VALID} or if the identity store does not
	 *         support roles.
	 */
	public List<String> getCallerRoles() {
		return roles;
	}

}
