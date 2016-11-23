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
package javax.security.auth;

import javax.security.auth.util.CollectionUtils;
import javax.security.auth.util.StringUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * From Apache Shiro / Octopus
 */
public class NamedPermission {
    private static final long serialVersionUID = 1l;

    protected static final String WILDCARD_TOKEN = "*";
    protected static final String PART_DIVIDER_TOKEN = ":";
    protected static final String SUBPART_DIVIDER_TOKEN = ",";
    protected static final boolean DEFAULT_CASE_SENSITIVE = false;

    private String name;
    private String domain;
    private Set<String> actions;
    private Set<String> targets;

    private List<Set<String>> parts;

    /**
     * Creates a domain permission with *all* actions for *all* targets;
     */
    public NamedPermission(String name) {
        this.name = name;
        this.domain = getDomain(getClass());
        setParts(getDomain(getClass()));
    }

    public NamedPermission(String name, String actions) {
        this.name = name;
        domain = getDomain(getClass());
        this.actions = StringUtils.splitToSet(actions, SUBPART_DIVIDER_TOKEN);
        encodeParts(domain, actions, null);
    }

    public NamedPermission(String name, String actions, String targets) {
        this.name = name;
        this.domain = getDomain(getClass());
        this.actions = StringUtils.splitToSet(actions, SUBPART_DIVIDER_TOKEN);
        this.targets = StringUtils.splitToSet(targets, SUBPART_DIVIDER_TOKEN);
        encodeParts(this.domain, actions, targets);
    }

    protected NamedPermission(String name, Set<String> actions, Set<String> targets) {
        this.name = name;
        this.domain = getDomain(getClass());
        setParts(domain, actions, targets);
    }

    public NamedPermission(String name, String domain, String actions, String targets) {
        this.name = name;
        this.domain = domain;
        this.actions = StringUtils.splitToSet(actions, SUBPART_DIVIDER_TOKEN);
        this.targets = StringUtils.splitToSet(targets, SUBPART_DIVIDER_TOKEN);
        encodeParts(this.domain, actions, targets);
    }


    private void encodeParts(String domain, String actions, String targets) {
        if (!StringUtils.hasText(domain)) {
            throw new IllegalArgumentException("domain argument cannot be null or empty.");
        }
        StringBuilder sb = new StringBuilder(domain);

        if (!StringUtils.hasText(actions)) {
            if (StringUtils.hasText(targets)) {
                sb.append(PART_DIVIDER_TOKEN).append(WILDCARD_TOKEN);
            }
        } else {
            sb.append(PART_DIVIDER_TOKEN).append(actions);
        }
        if (StringUtils.hasText(targets)) {
            sb.append(PART_DIVIDER_TOKEN).append(targets);
        }
        setParts(sb.toString());
    }

    protected void setParts(String domain, Set<String> actions, Set<String> targets) {
        String actionsString = StringUtils.toDelimitedString(actions, SUBPART_DIVIDER_TOKEN);
        String targetsString = StringUtils.toDelimitedString(targets, SUBPART_DIVIDER_TOKEN);
        encodeParts(domain, actionsString, targetsString);
        this.domain = domain;
        this.actions = actions;
        this.targets = targets;
    }

    public String getName() {
        return name;
    }

    protected String getDomain(Class<? extends NamedPermission> clazz) {
        String domain = clazz.getSimpleName().toLowerCase();
        //strip any trailing 'permission' text from the name (as all subclasses should have been named):
        int index = domain.lastIndexOf("permission");
        if (index != -1) {
            domain = domain.substring(0, index);
        }
        return domain;
    }

    public String getDomain() {
        return domain;
    }

    public Set<String> getActions() {
        return actions;
    }

    public Set<String> getTargets() {
        return targets;
    }


    protected void setParts(String wildcardString) {
        setParts(wildcardString, DEFAULT_CASE_SENSITIVE);
    }

    protected void setParts(String wildcardString, boolean caseSensitive) {
        if (wildcardString == null || wildcardString.trim().length() == 0) {
            throw new IllegalArgumentException("Wildcard string cannot be null or empty. Make sure permission strings are properly formatted.");
        }

        wildcardString = wildcardString.trim();

        List<String> parts = CollectionUtils.asList(wildcardString.split(PART_DIVIDER_TOKEN));

        this.parts = new ArrayList<Set<String>>();
        for (String part : parts) {
            Set<String> subparts = CollectionUtils.asSet(part.split(SUBPART_DIVIDER_TOKEN));
            if (!caseSensitive) {
                subparts = lowercase(subparts);
            }
            if (subparts.isEmpty()) {
                throw new IllegalArgumentException("Wildcard string cannot contain parts with only dividers. Make sure permission strings are properly formatted.");
            }
            this.parts.add(subparts);
        }

        if (this.parts.isEmpty()) {
            throw new IllegalArgumentException("Wildcard string cannot contain only dividers. Make sure permission strings are properly formatted.");
        }
    }

    private Set<String> lowercase(Set<String> subparts) {
        Set<String> lowerCasedSubparts = new LinkedHashSet<String>(subparts.size());
        for (String subpart : subparts) {
            lowerCasedSubparts.add(subpart.toLowerCase());
        }
        return lowerCasedSubparts;
    }

    protected List<Set<String>> getParts() {
        return this.parts;
    }

    public boolean implies(NamedPermission p) {

        List<Set<String>> otherParts = p.getParts();

        int i = 0;
        for (Set<String> otherPart : otherParts) {
            // If this permission has less parts than the other permission, everything after the number of parts contained
            // in this permission is automatically implied, so return true
            if (getParts().size() - 1 < i) {
                return true;
            } else {
                Set<String> part = getParts().get(i);
                if (!part.contains(WILDCARD_TOKEN) && !part.containsAll(otherPart)) {
                    return false;
                }
                i++;
            }
        }

        // If this permission has more parts than the other parts, only imply it if all of the other parts are wildcards
        for (; i < getParts().size(); i++) {
            Set<String> part = getParts().get(i);
            if (!part.contains(WILDCARD_TOKEN)) {
                return false;
            }
        }

        return true;
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        for (Set<String> part : parts) {
            if (buffer.length() > 0) {
                buffer.append(":");
            }
            buffer.append(part);
        }
        return buffer.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof NamedPermission) {
            NamedPermission wp = (NamedPermission) o;
            return parts.equals(wp.parts);
        }
        return false;
    }

    public int hashCode() {
        return parts.hashCode();
    }

}
