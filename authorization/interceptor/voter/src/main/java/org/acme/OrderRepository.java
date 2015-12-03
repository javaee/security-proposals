package org.acme;

import org.acme.model.Order;

import javax.ejb.Stateless;
import javax.security.auth.Voter;

/**
 *
 */
@Stateless
public class OrderRepository {


    @Voter(CheckCustomerInPortfolio.class)
    public void placeOrder(Order order) {

    }
}
