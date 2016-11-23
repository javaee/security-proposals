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
package javax.security.idm.model;

import org.picketlink.idm.model.AbstractIdentityType;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.IdentityStereotype;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.model.annotation.Unique;
import org.picketlink.idm.query.QueryParameter;

import static org.picketlink.idm.model.annotation.IdentityStereotype.Stereotype.ROLE;
import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.IDENTITY_ROLE_NAME;

/**
 * Represents a role, which may be assigned to account objects in various ways
 * to grant specific application privileges.
 */
@IdentityStereotype(ROLE)
public class Role extends AbstractIdentityType {

    private static final long serialVersionUID = -316782188902416548L;

    /**
     * A query parameter used to set the name value.
     */
    public static final QueryParameter NAME = QUERY_ATTRIBUTE.byName("name");

    private String name;

    /**
     * No argument constructor, defaults to <code>null</code> name.
     */
    public Role() {
    }

    /**
     * Constructor
     *
     * @param name The role name
     */
    public Role(String name) {
        this.name = name;
    }

    /**
     * Determines the role name.
     *
     * @return The role name
     */
    @AttributeProperty
    @StereotypeProperty(IDENTITY_ROLE_NAME)
    @Unique
    public String getName() {
        return name;
    }

    /**
     * Specifies the role name.
     * @param name The role name
     */
    public void setName(String name) {
        this.name = name;
    }
}