/*
 * ====
 *     DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 *     Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
 *
 *     The contents of this file are subject to the terms of either the GNU
 *     General Public License Version 2 only ("GPL") or the Common Development
 *     and Distribution License("CDDL") (collectively, the "License").  You
 *     may not use this file except in compliance with the License.  You can
 *     obtain a copy of the License at
 *     http://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 *     or packager/legal/LICENSE.txt.  See the License for the specific
 *     language governing permissions and limitations under the License.
 *
 *     When distributing the software, include this License Header Notice in each
 *     file and include the License file at packager/legal/LICENSE.txt.
 *
 *     GPL Classpath Exception:
 *     Oracle designates this particular file as subject to the "Classpath"
 *     exception as provided by Oracle in the GPL Version 2 section of the License
 *     file that accompanied this code.
 *
 *     Modifications:
 *     If applicable, add the following below the License Header, with the fields
 *     enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyright [year] [name of copyright owner]"
 *
 *     Contributor(s):
 *     If you wish your version of this file to be governed by only the CDDL or
 *     only the GPL Version 2, indicate your decision by adding "[Contributor]
 *     elects to include this software in this distribution under the [CDDL or GPL
 *     Version 2] license."  If you don't indicate a single choice of license, a
 *     recipient has the option to distribute your version of this file under
 *     either the CDDL, the GPL Version 2 or to extend the choice of license to
 *     its licensees as provided above.  However, if you add GPL Version 2 code
 *     and therefore, elected the GPL Version 2 license, then the option applies
 *     only if the new code is made subject to such option by the copyright
 *     holder.
 * ====
 *
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
import org.picketlink.idm.model.annotation.IdentityStereotype;
import org.picketlink.idm.model.annotation.AttributeProperty;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.model.annotation.InheritsPrivileges;
import org.picketlink.idm.model.annotation.Unique;
import org.picketlink.idm.query.QueryParameter;

import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.IDENTITY_GROUP_NAME;

/**
 * Represents a Group, which may be used to form collections of other identity objects.
 */
@IdentityStereotype(IdentityStereotype.Stereotype.GROUP)
public class Group extends AbstractIdentityType implements RoleAssignable {

    private static final long serialVersionUID = -8684847802444134273L;

    /**
     * A query parameter used to set the name value.
     */
    public static final QueryParameter NAME = QUERY_ATTRIBUTE.byName("name");

    /**
     * A query parameter used to set the path.
     */
    public static final QueryParameter PATH = QUERY_ATTRIBUTE.byName("path");

    /**
     * A query parameter used to set the parent value.
     */
    public static final QueryParameter PARENT = QUERY_ATTRIBUTE.byName("parentGroup");

    public static final String PATH_SEPARATOR = "/";

    private String name;
    private Group parentGroup;
    private String path;

    /**
     * No argument constructor, setting all properties to null.
     */
    public Group() {

    }

    /**
     * Constructor
     *
     * @param name The group name
     */
    public Group(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Error creating Group - name cannot be null or empty");
        }

        this.name = name;
        this.path = buildPath(this);
    }

    /**
     * Constructor
     *
     * @param name The group name
     * @param parentGroup The parent group, from which privileges may be inherited.
     */
    public Group(String name, Group parentGroup) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Error creating Group - name cannot be null or empty");
        }

        this.name = name;
        this.parentGroup = parentGroup;

        this.path = buildPath(this);
    }

    /**
     * Determines the name of the Group.
     * @return The name of the Group.
     */
    @AttributeProperty
    @StereotypeProperty(IDENTITY_GROUP_NAME)
    public String getName() {
        return name;
    }

    /**
     * Specifies the name of the Group.
     * @param name The name of the Group
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Determines the group ancestry from the root group to this group,
     * expressed as a path.
     * <p>
     * For example:<br>
     * <code>/Sales/North American/Federal/Defense/this Group</code>
     *
     * @return The group path
     */
    @AttributeProperty
    @Unique
    public String getPath() {
        this.path = buildPath(this);
        return this.path;
    }

    /**
     * Specifies the group ancestry from the root group to this group,
     * expressed as a path.
     * <p>
     * For example:<br>
     * <code>/Sales/North American/Federal/Defense/this Group</code>
     *
     * @param path The group path
     */
    public void setPath(String path) {
        // Needed for the persistence/query mechanism.
        this.path = path;
    }

    /**
     * Determines the parent Group of this Group, from which Roles and other
     * authorization are inherited.
     * @return The parent Group
     */
    @InheritsPrivileges
    @AttributeProperty
    public Group getParentGroup() {
        return this.parentGroup;
    }

    /**
     * Specifies the parent Group of this Group, from which Roles and other
     * authorization are inherited.
     * @param group The parent Group
     */
    @AttributeProperty
    public void setParentGroup(Group group) {
        this.parentGroup = group;
    }

    /**
     * Builds the group ancestry path based on the parent groups.
     *
     * @param group Starting from this group, go to ancestors
     * @return The ancestry path
     */
    private static String buildPath(Group group) {
        String name = PATH_SEPARATOR + group.getName();

        if (group.getParentGroup() != null) {
            name = buildPath(group.getParentGroup()) + name;
        }

        return name;
    }
}
