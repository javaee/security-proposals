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

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Tests {@link javax.security.idm.model.Group}
 */
public class GroupTest {

    @Test
    public void constructor_noArg() {
        Group group = new Group();
        assertNull("No arg constructor", group.getName());
    }

    @Test
    public void constructor_name() {
        Group group = new Group("myGroup");
        assertEquals("myGroup", group.getName());
    }

    @Test
    public void name() {
        Group group = new Group();
        assertNull("No arg constructor", group.getName());
        group.setName("myGroup");
        assertEquals("myGroup", group.getName());
    }

    @Test
    public void parentPath () {
        Group root = new Group("root");
        Group greatGrampa = new Group("greatGrampa");
        greatGrampa.setParentGroup(root);
        assertSame(root,greatGrampa.getParentGroup());

        Group grampa = new Group("grampa", greatGrampa);
        Group dad = new Group("dad", grampa);
        Group me = new Group("me", dad);

        assertEquals("me", "/root/greatGrampa/grampa/dad/me", me.getPath());
        assertEquals("dad", "/root/greatGrampa/grampa/dad", dad.getPath());
        assertEquals("grampa", "/root/greatGrampa/grampa", grampa.getPath());
        assertEquals("greatGrampa", "/root/greatGrampa", greatGrampa.getPath());
        assertEquals("root", "/root", root.getPath());
    }

}
