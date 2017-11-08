package com.android.server.wifi.anqp;

import com.android.server.wifi.anqp.Constants.ANQPElementType;
import java.net.ProtocolException;
import java.nio.ByteBuffer;

public class IPAddressTypeAvailabilityElement extends ANQPElement {
    private final IPv4Availability mV4Availability;
    private final IPv6Availability mV6Availability;

    public enum IPv4Availability {
        NotAvailable,
        Public,
        PortRestricted,
        SingleNAT,
        DoubleNAT,
        PortRestrictedAndSingleNAT,
        PortRestrictedAndDoubleNAT,
        Unknown
    }

    public enum IPv6Availability {
        NotAvailable,
        Available,
        Unknown,
        Reserved
    }

    public IPAddressTypeAvailabilityElement(ANQPElementType infoID, ByteBuffer payload) throws ProtocolException {
        super(infoID);
        if (payload.remaining() != 1) {
            throw new ProtocolException("Bad IP Address Type Availability length: " + payload.remaining());
        }
        IPv4Availability iPv4Availability;
        int ipField = payload.get();
        this.mV6Availability = IPv6Availability.values()[ipField & 3];
        ipField = (ipField >> 2) & 63;
        if (ipField < IPv4Availability.values().length) {
            iPv4Availability = IPv4Availability.values()[ipField];
        } else {
            iPv4Availability = IPv4Availability.Unknown;
        }
        this.mV4Availability = iPv4Availability;
    }

    public IPv4Availability getV4Availability() {
        return this.mV4Availability;
    }

    public IPv6Availability getV6Availability() {
        return this.mV6Availability;
    }

    public String toString() {
        return "IPAddressTypeAvailability{mV4Availability=" + this.mV4Availability + ", mV6Availability=" + this.mV6Availability + '}';
    }
}
