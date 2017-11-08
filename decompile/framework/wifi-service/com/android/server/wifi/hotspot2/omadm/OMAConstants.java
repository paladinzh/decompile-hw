package com.android.server.wifi.hotspot2.omadm;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class OMAConstants {
    public static final String DevDetailURN = "urn:oma:mo:oma-dm-devdetail:1.0";
    public static final String DevDetailXURN = "urn:wfa:mo-ext:hotspot2dot0-devdetail-ext:1.0";
    public static final String DevInfoURN = "urn:oma:mo:oma-dm-devinfo:1.0";
    private static final byte[] INDENT = new byte[1024];
    public static final String MOVersion = "1.0";
    public static final String OMAVersion = "1.2";
    public static final String PPS_URN = "urn:wfa:mo:hotspot2dot0-perprovidersubscription:1.0";
    public static final String SppMOAttribute = "spp:moURN";
    public static final String[] SupportedMO_URNs = new String[]{PPS_URN, DevInfoURN, DevDetailURN, DevDetailXURN};
    public static final String SyncML = "syncml:dmddf1.2";
    public static final String SyncMLVersionTag = "VerDTD";
    public static final String TAG_Error = "spp:sppError";
    public static final String TAG_MOContainer = "spp:moContainer";
    public static final String TAG_PostDevData = "spp:sppPostDevData";
    public static final String TAG_SessionID = "spp:sessionID";
    public static final String TAG_Status = "spp:sppStatus";
    public static final String TAG_SupportedMOs = "spp:supportedMOList";
    public static final String TAG_SupportedVersions = "spp:supportedSPPVersions";
    public static final String TAG_UpdateResponse = "spp:sppUpdateResponse";
    public static final String TAG_Version = "spp:sppVersion";

    private OMAConstants() {
    }

    public static void serializeString(String s, OutputStream out) throws IOException {
        out.write(String.format("%x:", new Object[]{Integer.valueOf(s.getBytes(StandardCharsets.UTF_8).length)}).getBytes(StandardCharsets.UTF_8));
        out.write(octets);
    }

    public static void indent(int level, OutputStream out) throws IOException {
        out.write(INDENT, 0, level);
    }

    public static String deserializeString(InputStream in) throws IOException {
        StringBuilder prefix = new StringBuilder();
        while (true) {
            byte b = (byte) in.read();
            if (b == (byte) 46) {
                return null;
            }
            if (b == (byte) 58) {
                break;
            } else if (b > (byte) 32) {
                prefix.append((char) b);
            }
        }
        byte[] octets = new byte[Integer.parseInt(prefix.toString(), 16)];
        int offset = 0;
        while (offset < octets.length) {
            int amount = in.read(octets, offset, octets.length - offset);
            if (amount <= 0) {
                throw new EOFException();
            }
            offset += amount;
        }
        return new String(octets, StandardCharsets.UTF_8);
    }

    public static String readURN(InputStream in) throws IOException {
        StringBuilder urn = new StringBuilder();
        while (true) {
            byte b = (byte) in.read();
            if (b == (byte) 41) {
                return urn.toString();
            }
            urn.append((char) b);
        }
    }
}
