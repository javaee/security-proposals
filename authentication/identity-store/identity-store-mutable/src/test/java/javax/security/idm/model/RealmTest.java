package javax.security.idm.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests {@link Realm}
 */
public class RealmTest {

    @Test
    public void constructor_noArg() {
        Realm realm = new Realm();
        assertNull("No arg constructor", realm.getName());
    }

    @Test
    public void constructor_name() {
        Realm realm = new Realm("me");
        assertEquals("me", realm.getName());
    }
}
