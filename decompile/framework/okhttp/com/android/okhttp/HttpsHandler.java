package com.android.okhttp;

import com.android.okhttp.ConnectionSpec.Builder;
import java.net.Proxy;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

public final class HttpsHandler extends HttpHandler {
    private static final List<Protocol> HTTP_1_1_ONLY = Arrays.asList(new Protocol[]{Protocol.HTTP_1_1});
    private static final List<ConnectionSpec> SECURE_CONNECTION_SPECS = Arrays.asList(new ConnectionSpec[]{TLS_1_2_AND_BELOW, TLS_1_1_AND_BELOW, TLS_1_0_AND_BELOW, SSL_3_0});
    private static final ConnectionSpec SSL_3_0 = new Builder(TLS_1_2_AND_BELOW).tlsVersions(TlsVersion.SSL_3_0).build();
    private static final ConnectionSpec TLS_1_0_AND_BELOW = new Builder(TLS_1_2_AND_BELOW).tlsVersions(TlsVersion.TLS_1_0, TlsVersion.SSL_3_0).build();
    private static final ConnectionSpec TLS_1_1_AND_BELOW = new Builder(TLS_1_2_AND_BELOW).tlsVersions(TlsVersion.TLS_1_1, TlsVersion.TLS_1_0, TlsVersion.SSL_3_0).supportsTlsExtensions(true).build();
    private static final ConnectionSpec TLS_1_2_AND_BELOW = new Builder(true).tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0, TlsVersion.SSL_3_0).supportsTlsExtensions(true).build();
    private final ConfigAwareConnectionPool configAwareConnectionPool = ConfigAwareConnectionPool.getInstance();

    protected int getDefaultPort() {
        return 443;
    }

    protected OkUrlFactory newOkUrlFactory(Proxy proxy) {
        OkUrlFactory okUrlFactory = createHttpsOkUrlFactory(proxy);
        okUrlFactory.client().setConnectionPool(this.configAwareConnectionPool.get());
        return okUrlFactory;
    }

    public static OkUrlFactory createHttpsOkUrlFactory(Proxy proxy) {
        OkUrlFactory okUrlFactory = HttpHandler.createHttpOkUrlFactory(proxy);
        okUrlFactory.setUrlFilter(null);
        OkHttpClient okHttpClient = okUrlFactory.client();
        okHttpClient.setProtocols(HTTP_1_1_ONLY);
        okHttpClient.setConnectionSpecs(SECURE_CONNECTION_SPECS);
        okUrlFactory.client().setHostnameVerifier(HttpsURLConnection.getDefaultHostnameVerifier());
        okHttpClient.setSslSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory());
        return okUrlFactory;
    }
}
