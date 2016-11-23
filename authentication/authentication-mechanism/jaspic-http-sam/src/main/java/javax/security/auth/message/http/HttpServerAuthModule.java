/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package javax.security.auth.message.http;

import static javax.security.auth.message.AuthStatus.SEND_SUCCESS;

import java.lang.Class;import java.lang.IllegalStateException;import java.lang.Override;import java.lang.String;import java.lang.SuppressWarnings;import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * https://java.net/jira/browse/JASPIC_SPEC-17
 *
 * A server authentication module (SAM) implementation base class, tailored for the Servlet Container Profile.
 *
 * @author Arjan Tijms
 *
 */
public abstract class HttpServerAuthModule implements ServerAuthModule {

	private CallbackHandler handler;
	private Map<String, String> options;
	private final Class<?>[] supportedMessageTypes = new Class[] { HttpServletRequest.class, HttpServletResponse.class };
	
	@Override
	@SuppressWarnings("unchecked")
	public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, @SuppressWarnings("rawtypes") Map options) throws AuthException {
		this.handler = handler;
		this.options = options;
//		initializeModule(new HttpMsgContext(handler, options, null, null));
	}

	/**
	 * A Servlet Container Profile compliant implementation should return HttpServletRequest and HttpServletResponse, so
	 * the delegation class {@link ServerAuthContext} can choose the right SAM to delegate to.
	 */
	@Override
	public Class<?>[] getSupportedMessageTypes() {
		return supportedMessageTypes;
	}

	@Override
	public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException {
		HttpMessageContext msgContext = null;
//		HttpMsgContext msgContext = new HttpMsgContext(handler, options, messageInfo, clientSubject);
		return validateHttpRequest(msgContext.getRequest(), msgContext.getResponse(), msgContext);
	}

	@Override
	public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException {
		return SEND_SUCCESS;
	}

	/**
	 * Called in response to a {@link HttpServletRequest#logout()} call.
	 *
	 */
	@Override
	public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException {
//	    HttpMsgContext msgContext = new HttpMsgContext(handler, options, messageInfo, subject);
	    HttpMessageContext msgContext = null;
		cleanHttpSubject(msgContext.getRequest(), msgContext.getResponse(), msgContext);
	}
	
	public void initializeModule(HttpMessageContext httpMessageContext) {
		
	}

	public AuthStatus validateHttpRequest(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) throws AuthException {
		throw new IllegalStateException("Not implemented");
	}

	public void cleanHttpSubject(HttpServletRequest request, HttpServletResponse response, HttpMessageContext httpMessageContext) {
	    httpMessageContext.cleanClientSubject();
	}

}