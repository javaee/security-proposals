package javax.security.idm.model;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests {@link javax.security.idm.model.Caller}
 */
public class CallerTest {

    @Test
    public void constructor_noArg() {
        Caller caller = new Caller();
        assertNull("No arg constructor", caller.getLoginName());
    }

    @Test
    public void constructor_loginName() {
        Caller caller = new Caller("me");
        assertEquals("me", caller.getLoginName());
    }

    @Test
    public void loginName() {
        Caller caller = new Caller();
        caller.setLoginName("me");
        assertEquals("me", caller.getLoginName());
    }

}
