package org.acme;

import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.http.HttpMessageContext;
import javax.security.auth.message.http.HttpServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Base64;

import static javax.security.auth.message.AuthStatus.FAILURE;

public class Provider extends HttpServerAuthModule {

    @Override
    public AuthStatus validateHttpRequest(HttpServletRequest request,
                                          HttpServletResponse response,
                                          HttpMessageContext httpMessageContext) throws AuthException {


        final String header = request.getHeader("Authorization");

        final String[] credentials = parseCredentials(header);

        final String username = credentials[0];
        final String password = credentials[1];

        if (!"snoopy".equals(username) || !"woodst0ck".equals(password)) {
            return FAILURE;
        }

        // Communicate the details of the authenticated user to the container. In many
        // cases the handler will just store the details and the container will actually handle
        // the login after we return from this method.

        return httpMessageContext.notifyContainerAboutLogin(
                // The name of the authenticated user
                "snoopy",

                // the groups/roles of the authenticated user
                Arrays.asList("RedBaron", "JoeCool", "MansBestFriend")
        );
    }

    private static String[] parseCredentials(String header) {
        final byte[] decoded = Base64.getDecoder().decode(header.replace("Basic ", ""));
        return new String(decoded).split(":");
    }
}