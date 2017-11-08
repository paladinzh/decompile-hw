package tmsdk.bg.module.wifidetect;

import android.net.LocalServerSocket;
import android.net.LocalSocket;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import tmsdk.common.utils.ScriptHelper;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
class a {
    static final a zE = new a();
    LocalServerSocket zC;
    boolean zD = false;

    private a() {
    }

    public static a el() {
        return zE;
    }

    public void cn(String str) {
        d.g("WifiDetectManager", "[Beg]checkArp-binaryPath:[" + str + "]");
        if (new File(str).exists()) {
            List arrayList = new ArrayList();
            arrayList.add("chmod 0755 " + str + "\n");
            arrayList.add(str + " " + 10);
            d.g("WifiDetectManager", "ScriptHelper.runScript-cmds:[" + arrayList + "]");
            d.g("WifiDetectManager", "[End]checkArp-runScript-ret:[" + ScriptHelper.runScript(-1, arrayList) + "]");
            return;
        }
        d.g("WifiDetectManager", "binaryFile not exist");
    }

    public int em() {
        d.g("WifiDetectManager", "startServerAutoStop");
        int i = 261;
        this.zC = new LocalServerSocket("tms_socket_server_path");
        this.zD = false;
        while (!this.zD) {
            d.g("WifiDetectManager", "[Beg]Server.accept");
            LocalSocket accept = this.zC.accept();
            d.g("WifiDetectManager", "[End]Server.accept:[" + accept + "]");
            if (accept != null) {
                if (!this.zD) {
                    InputStream inputStream = accept.getInputStream();
                    StringBuilder stringBuilder = new StringBuilder();
                    byte[] bArr = new byte[1024];
                    while (true) {
                        int read = inputStream.read(bArr);
                        if (read == -1) {
                            break;
                        }
                        stringBuilder.append(new String(bArr, 0, read));
                    }
                    String stringBuilder2 = stringBuilder.toString();
                    d.g("WifiDetectManager", "received from binary:[" + stringBuilder2 + "]");
                    if ("found danger".equals(stringBuilder2)) {
                        i = 262;
                    }
                    this.zD = true;
                }
            }
            if (accept != null) {
                try {
                    accept.close();
                } catch (Exception e) {
                    try {
                        d.g("WifiDetectManager", "close local socket exception: " + e.getMessage());
                    } catch (IOException e2) {
                        d.c("WifiDetectManager", "startServer:[" + e2 + "]");
                        return 263;
                    }
                }
            }
        }
        d.g("WifiDetectManager", "server has been stop, close the server");
        this.zC.close();
        this.zC = null;
        return i;
    }
}
