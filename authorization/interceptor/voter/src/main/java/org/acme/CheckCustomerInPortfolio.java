package org.acme;

import org.acme.model.Order;

import javax.inject.Inject;
import javax.interceptor.InvocationContext;
import javax.security.auth.AccessDecisionVoter;
import java.security.Principal;

/**
 * This is a CDI bean!,  see javax.security.auth.VoterInterceptor#executeVoter(javax.security.auth.Voter, javax.interceptor.InvocationContext)
 */
public class CheckCustomerInPortfolio implements AccessDecisionVoter {

    @Inject
    private Principal callerPrincipal;

    @Override
    public void checkPermission(InvocationContext context) {
        Order order = (Order) context.getParameters()[0];

        // Verify if customer (order.getCustomer) is in the portfolio of the principal.
    }
}
