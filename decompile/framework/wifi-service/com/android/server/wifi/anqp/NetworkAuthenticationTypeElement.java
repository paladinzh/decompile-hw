package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NetworkAuthenticationTypeElement extends ANQPElement {
    private final List<NetworkAuthentication> m_authenticationTypes = new ArrayList();

    public static class NetworkAuthentication {
        private final NwkAuthTypeEnum m_type;
        private final String m_url;

        private NetworkAuthentication(NwkAuthTypeEnum type, String url) {
            this.m_type = type;
            this.m_url = url;
        }

        public NwkAuthTypeEnum getType() {
            return this.m_type;
        }

        public String getURL() {
            return this.m_url;
        }

        public String toString() {
            return "NetworkAuthentication{m_type=" + this.m_type + ", m_url='" + this.m_url + '\'' + '}';
        }
    }

    public enum NwkAuthTypeEnum {
        TermsAndConditions,
        OnLineEnrollment,
        HTTPRedirection,
        DNSRedirection,
        Reserved
    }

    public NetworkAuthenticationTypeElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        while (payload.hasRemaining()) {
            NwkAuthTypeEnum type;
            int typeNumber = payload.get() & 255;
            if (typeNumber >= NwkAuthTypeEnum.values().length) {
                type = NwkAuthTypeEnum.Reserved;
            } else {
                type = NwkAuthTypeEnum.values()[typeNumber];
            }
            this.m_authenticationTypes.add(new NetworkAuthentication(type, Constants.getPrefixedString(payload, 2, StandardCharsets.UTF_8)));
        }
    }

    public List<NetworkAuthentication> getAuthenticationTypes() {
        return Collections.unmodifiableList(this.m_authenticationTypes);
    }

    public String toString() {
        return "NetworkAuthenticationType{m_authenticationTypes=" + this.m_authenticationTypes + '}';
    }
}
