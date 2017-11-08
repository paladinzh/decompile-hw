package com.avast.android.sdk.engine;

import com.avast.android.sdk.engine.obfuscated.al;
import com.avast.android.sdk.engine.obfuscated.ao;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.client.utils.URIUtils;

/* compiled from: Unknown */
public class ServerInformation {
    public String serverAddress;
    public String serverPath;
    public Integer serverPort;
    public String serverProtocol;

    /* compiled from: Unknown */
    private enum a {
        PAYLOAD_SERVER_ADDRESS((short) 0),
        PAYLOAD_SERVER_PROTOCOL((short) 1),
        PAYLOAD_SERVER_PORT((short) 2),
        PAYLOAD_SERVER_PATH((short) 3);
        
        private static final Map<Short, a> e = null;
        private final short f;

        static {
            e = new HashMap();
            Iterator it = EnumSet.allOf(a.class).iterator();
            while (it.hasNext()) {
                a aVar = (a) it.next();
                e.put(Short.valueOf(aVar.a()), aVar);
            }
        }

        private a(short s) {
            this.f = (short) s;
        }

        public static a a(short s) {
            return (a) e.get(Short.valueOf(s));
        }

        public final short a() {
            return this.f;
        }
    }

    private ServerInformation() {
    }

    public ServerInformation(String str, String str2, Integer num, String str3) {
        this.serverProtocol = str;
        this.serverAddress = str2;
        this.serverPort = num;
        this.serverPath = str3;
        if (num.intValue() < 1 || num.intValue() > 49151) {
            this.serverPort = Integer.valueOf(-1);
        }
    }

    public static String getVersion() {
        return "si-2";
    }

    public static Integer getVersionCode() {
        return Integer.valueOf(Integer.parseInt("si-2".substring("si-2".indexOf("-") + 1)));
    }

    public static ServerInformation parse(byte[] bArr) {
        if (bArr != null) {
            ServerInformation serverInformation = new ServerInformation();
            ServerInformation serverInformation2;
            try {
                if (((Integer) al.a(bArr, null, Integer.TYPE, 0)).intValue() + 4 == bArr.length) {
                    int i = 4;
                    while (i < bArr.length) {
                        int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue();
                        i += 4;
                        if (bArr[(i + intValue) - 1] == (byte) -1) {
                            a a = a.a(((Short) al.a(bArr, null, Short.TYPE, i)).shortValue());
                            if (a != null) {
                                switch (g.a[a.ordinal()]) {
                                    case 1:
                                        serverInformation.serverAddress = new String(bArr, i + 2, (intValue - 2) - 1);
                                        break;
                                    case 2:
                                        serverInformation.serverProtocol = new String(bArr, i + 2, (intValue - 2) - 1);
                                        break;
                                    case 3:
                                        Integer num = (Integer) al.a(bArr, null, Integer.TYPE, i + 2);
                                        if (num == null) {
                                            break;
                                        }
                                        serverInformation.serverPort = num;
                                        break;
                                    case 4:
                                        serverInformation.serverPath = new String(bArr, i + 2, (intValue - 2) - 1);
                                        break;
                                    default:
                                        break;
                                }
                            }
                            i += intValue;
                        } else {
                            throw new IllegalArgumentException("Invalid payload length");
                        }
                    }
                    serverInformation2 = serverInformation;
                    return serverInformation2;
                }
                throw new IllegalArgumentException("Invalid structure length");
            } catch (Throwable e) {
                ao.d("Exception parsing server information", e);
                serverInformation2 = null;
            }
        } else {
            ao.a("ServerInformation bytes are null");
            return null;
        }
    }

    public static List<ServerInformation> parseResultList(byte[] bArr) {
        List<ServerInformation> linkedList = new LinkedList();
        if (bArr == null) {
            return linkedList;
        }
        ao.a(al.a(bArr));
        int i = 0;
        while (i < bArr.length) {
            int intValue = ((Integer) al.a(bArr, null, Integer.TYPE, i)).intValue() + 4;
            Object obj = new byte[intValue];
            System.arraycopy(bArr, i, obj, 0, intValue);
            intValue += i;
            linkedList.add(parse(obj));
            i = intValue;
        }
        return linkedList;
    }

    public URI getUri() {
        try {
            return URIUtils.createURI(this.serverProtocol, this.serverAddress, this.serverPort.intValue(), this.serverPath, null, null);
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
