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
