package org.apache.http.client.protocol;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AUTH;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.protocol.HttpContext;

@Deprecated
public class RequestProxyAuthentication implements HttpRequestInterceptor {
    private final Log log = LogFactory.getLog(getClass());

    public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        } else if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        } else if (!request.containsHeader(AUTH.PROXY_AUTH_RESP)) {
            AuthState authState = (AuthState) context.getAttribute(ClientContext.PROXY_AUTH_STATE);
            if (authState != null) {
                AuthScheme authScheme = authState.getAuthScheme();
                if (authScheme != null) {
                    Credentials creds = authState.getCredentials();
                    if (creds == null) {
                        this.log.debug("User credentials not available");
                        return;
                    }
                    if (!(authState.getAuthScope() == null && authScheme.isConnectionBased())) {
                        try {
                            request.addHeader(authScheme.authenticate(creds, request));
                        } catch (AuthenticationException ex) {
                            if (this.log.isErrorEnabled()) {
                                this.log.error("Proxy authentication error: " + ex.getMessage());
                            }
                        }
                    }
                }
            }
        }
    }
}
