package com.googlecode.mp4parser.boxes.piff;

import com.coremedia.iso.Hex;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ProtectionSpecificHeader {
    protected static Map<UUID, Class<? extends ProtectionSpecificHeader>> uuidRegistry = new HashMap();
    ByteBuffer data;

    static {
        uuidRegistry.put(UUID.fromString("9A04F079-9840-4286-AB92-E65BE0885F95"), PlayReadyHeader.class);
    }

    public boolean equals(Object obj) {
        if ((obj instanceof ProtectionSpecificHeader) && getClass().equals(obj.getClass())) {
            return this.data.equals(((ProtectionSpecificHeader) obj).data);
        }
        return false;
    }

    public static ProtectionSpecificHeader createFor(UUID systemId, ByteBuffer bufferWrapper) {
        Class<? extends ProtectionSpecificHeader> aClass = (Class) uuidRegistry.get(systemId);
        ProtectionSpecificHeader protectionSpecificHeader = new ProtectionSpecificHeader();
        if (aClass != null) {
            try {
                protectionSpecificHeader = (ProtectionSpecificHeader) aClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e2) {
                throw new RuntimeException(e2);
            }
        }
        protectionSpecificHeader.parse(bufferWrapper);
        return protectionSpecificHeader;
    }

    public void parse(ByteBuffer buffer) {
        this.data = buffer;
    }

    public ByteBuffer getData() {
        return this.data;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ProtectionSpecificHeader");
        sb.append("{data=");
        ByteBuffer data = getData().duplicate();
        data.rewind();
        byte[] bytes = new byte[data.limit()];
        data.get(bytes);
        sb.append(Hex.encodeHex(bytes));
        sb.append('}');
        return sb.toString();
    }
}
