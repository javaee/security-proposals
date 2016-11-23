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

import org.picketlink.idm.model.AbstractAttributedType;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.Relationship;
import org.picketlink.idm.model.annotation.InheritsPrivileges;
import org.picketlink.idm.model.annotation.RelationshipStereotype;
import org.picketlink.idm.model.annotation.StereotypeProperty;
import org.picketlink.idm.query.RelationshipQueryParameter;

import static org.picketlink.idm.model.annotation.RelationshipStereotype.Stereotype.GROUP_MEMBERSHIP;
import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.RELATIONSHIP_GROUP_MEMBERSHIP_GROUP;
import static org.picketlink.idm.model.annotation.StereotypeProperty.Property.RELATIONSHIP_GROUP_MEMBERSHIP_MEMBER;

/**
 * GroupMembership is a relationship that represents an identity's membership in a Group.
 */
@RelationshipStereotype(GROUP_MEMBERSHIP)
public class GroupMembership extends AbstractAttributedType implements Relationship {

    public static final RelationshipQueryParameter MEMBER = RELATIONSHIP_QUERY_ATTRIBUTE.byName("member");
    public static final RelationshipQueryParameter GROUP = RELATIONSHIP_QUERY_ATTRIBUTE.byName("group");

    private static final long serialVersionUID = 7235520939887813023L;

    private Account member;
    private Group group;

    /**
     * No argument constructor, which creates an empty membership.
     */
    public GroupMembership() {

    }

    /**
     * Constructor
     *
     * @param member Account to add to the group
     * @param group Group to populate
     */
    public GroupMembership(Account member, Group group) {
        this.member = member;
        this.group = group;
    }

    /**
     * Determines the associated member Account.
     *
     * @return The associated member Account
     */
    @InheritsPrivileges("group")
    @StereotypeProperty(RELATIONSHIP_GROUP_MEMBERSHIP_MEMBER)
    public Account getMember() {
        return member;
    }

    /**
     * Specifies the associated member Account.
     *
     * @param member The associated member Account
     */
    public void setMember(Account member) {
        this.member = member;
    }

    /**
     * Determines the group to be populated.
     *
     * @return The group to be populated.
     */
    @StereotypeProperty(RELATIONSHIP_GROUP_MEMBERSHIP_GROUP)
    public Group getGroup() {
        return group;
    }

    /**
     * Specifies the group to be populated.
     *
     * @param group The group to be populated.
     */
    public void setGroup(Group group) {
        this.group = group;
    }
}