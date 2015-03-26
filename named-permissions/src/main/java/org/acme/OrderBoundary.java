package org.acme;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.security.auth.PermissionsRequired;

/**
 *
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class OrderBoundary {

    @PermissionsRequired("OrderCreate")
    public void createOrder(Order order) {
        // Do with the order what you need to do. ...
    }
}
