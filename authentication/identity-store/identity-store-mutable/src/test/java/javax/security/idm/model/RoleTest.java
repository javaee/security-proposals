package javax.security.idm.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests {@link javax.security.idm.model.Role}
 */
public class RoleTest {

    @Test
    public void constructor_noArg() {
        Role role = new Role();
        assertNull("No arg constructor", role.getName());
    }

    @Test
    public void constructor_name() {
        Role role = new Role("me");
        assertEquals("me", role.getName());
    }

    @Test
    public void name() {
        Role role = new Role();
        role.setName("me");
        assertEquals("me", role.getName());
    }

}
