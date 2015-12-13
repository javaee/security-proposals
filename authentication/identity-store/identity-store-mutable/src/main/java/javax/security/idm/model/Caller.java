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
package javax.security.idm.model;

import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.IdentityStereotype;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.model.annotation.Unique;
import org.picketlink.idm.query.QueryParameter;

import static org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype.USER;
import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.IDENTITY_USER_NAME;

/**
 * A <code>Caller</code> is an {@link org.picketlink.idm.model.Account Account} which
 * may be authenticated and authorized to access protected resources. A
 * <code>Caller</code> may be a human user or a non-human agent.
 */
@IdentityStereotype(USER)
public class Caller extends AbstractIdentityType implements Account, RoleAssignable {

    private static final long serialVersionUID = 5631063266049466126L;

    /**
     * A query parameter used to set the <code>loginName</code> value.
     */
    public static final QueryParameter LOGIN_NAME = QUERY_ATTRIBUTE.byName("loginName");

    private String loginName;

    /**
     * No argument constructor, assumes no login name.
     */
    public Caller() {

    }

    /**
     * Constructor
     * @param loginName The login name
     */
    public Caller(String loginName) {

        this.loginName = loginName;
    }

    /**
     * Determines the login name.
     * @return The login name
     */
    @AttributeProperty
    @StereotypeProperty(IDENTITY_USER_NAME)
    @Unique
    public String getLoginName() {

        return loginName;
    }

    /**
     * Specifies the login name.
     * @param loginName The login name
     */
    public void setLoginName(String loginName) {

        this.loginName = loginName;
    }
}