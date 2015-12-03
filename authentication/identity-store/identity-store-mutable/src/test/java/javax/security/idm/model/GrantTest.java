package javax.security.idm.model;

import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Tests {@link Grant}
 */
public class GrantTest {

    final private RoleAssignable assignee = Stub.create(RoleAssignableStub.class);


    @Test
    public void constructor_noArg() {
        Grant grant = new Grant();
        assertNull("No arg constructor", grant.getAssignee());
        assertNull("No arg constructor", grant.getRole());
    }

    @Test
    public void constructor_assigneeRole() {
        Role role = new Role("me");

        Grant grant = new Grant(assignee, role);
        assertSame(assignee, grant.getAssignee());
        assertSame(role, grant.getRole());
    }

    @Test
    public void assignee() {
        Grant grant = new Grant();

        grant.setAssignee(assignee);
        assertSame(assignee, grant.getAssignee());
    }

    @Test
    public void role() {
        Grant grant = new Grant();

        Role role = new Role("me");
        grant.setRole(role);
        assertSame(role, grant.getRole());
    }

    @SimpleStub
    public static abstract class RoleAssignableStub implements RoleAssignable {

    }
}

