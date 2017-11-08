package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HSConnectionCapabilityElement extends ANQPElement {
    private final List<ProtocolTuple> mStatusList = new ArrayList();

    public enum ProtoStatus {
        Closed,
        Open,
        Unknown
    }

    public static class ProtocolTuple {
        private final int mPort;
        private final int mProtocol;
        private final ProtoStatus mStatus;

        private ProtocolTuple(ByteBuffer payload) throws ProtocolException {
            if (payload.remaining() < 4) {
                throw new ProtocolException("Runt protocol tuple: " + payload.remaining());
            }
            ProtoStatus protoStatus;
            this.mProtocol = payload.get() & 255;
            this.mPort = payload.getShort() & Constants.SHORT_MASK;
            int statusNumber = payload.get() & 255;
            if (statusNumber < ProtoStatus.values().length) {
                protoStatus = ProtoStatus.values()[statusNumber];
            } else {
                protoStatus = null;
            }
            this.mStatus = protoStatus;
        }

        public int getProtocol() {
            return this.mProtocol;
        }

        public int getPort() {
            return this.mPort;
        }

        public ProtoStatus getStatus() {
            return this.mStatus;
        }

        public String toString() {
            return "ProtocolTuple{mProtocol=" + this.mProtocol + ", mPort=" + this.mPort + ", mStatus=" + this.mStatus + '}';
        }
    }

    public HSConnectionCapabilityElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        while (payload.hasRemaining()) {
            this.mStatusList.add(new ProtocolTuple(payload));
        }
    }

    public List<ProtocolTuple> getStatusList() {
        return Collections.unmodifiableList(this.mStatusList);
    }

    public String toString() {
        return "HSConnectionCapability{mStatusList=" + this.mStatusList + '}';
    }
}
