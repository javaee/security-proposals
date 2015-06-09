package javax.security.idm.model;

import org.glassfish.simplestub.SimpleStub;
import org.glassfish.simplestub.Stub;
import org.junit.Test;
import org.picketlink.idm.model.Account;


import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Tests {@link javax.security.idm.model.GroupMembership}
 */
public class GroupMembershipTest {

    final private Account account = Stub.create(AccountStub.class);


    @Test
    public void constructor_noArg() {
        GroupMembership groupMembership = new GroupMembership();
        assertNull("No arg constructor", groupMembership.getGroup());
        assertNull("No arg constructor", groupMembership.getMember());
    }

    @Test
    public void constructor_memberGroup() {
        Group group = new Group("me");

        GroupMembership groupMembership = new GroupMembership(account, group);
        assertSame(account, groupMembership.getMember());
        assertSame(group, groupMembership.getGroup());
    }

    @Test
    public void member() {
        GroupMembership groupMembership = new GroupMembership();

        groupMembership.setMember(account);
        assertSame(account, groupMembership.getMember());
    }

    @Test
    public void group() {
        GroupMembership groupMembership = new GroupMembership();

        Group group = new Group("me");
        groupMembership.setGroup(group);
        assertSame(group, groupMembership.getGroup());
    }

    @SimpleStub
    public static abstract class AccountStub implements Account {

    }
}

