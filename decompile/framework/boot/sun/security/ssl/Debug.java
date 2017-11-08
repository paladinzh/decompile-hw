package sun.security.ssl;

import java.io.PrintStream;
import java.security.AccessController;
import java.util.Locale;
import sun.security.action.GetPropertyAction;

public class Debug {
    private static String args;
    private String prefix;

    static {
        args = (String) AccessController.doPrivileged(new GetPropertyAction("javax.net.debug", ""));
        args = args.toLowerCase(Locale.ENGLISH);
        if (args.equals("help")) {
            Help();
        }
    }

    public static void Help() {
        System.err.println();
        System.err.println("all            turn on all debugging");
        System.err.println("ssl            turn on ssl debugging");
        System.err.println();
        System.err.println("The following can be used with ssl:");
        System.err.println("\trecord       enable per-record tracing");
        System.err.println("\thandshake    print each handshake message");
        System.err.println("\tkeygen       print key generation data");
        System.err.println("\tsession      print session activity");
        System.err.println("\tdefaultctx   print default SSL initialization");
        System.err.println("\tsslctx       print SSLContext tracing");
        System.err.println("\tsessioncache print session cache tracing");
        System.err.println("\tkeymanager   print key manager tracing");
        System.err.println("\ttrustmanager print trust manager tracing");
        System.err.println("\tpluggability print pluggability tracing");
        System.err.println();
        System.err.println("\thandshake debugging can be widened with:");
        System.err.println("\tdata         hex dump of each handshake message");
        System.err.println("\tverbose      verbose handshake message printing");
        System.err.println();
        System.err.println("\trecord debugging can be widened with:");
        System.err.println("\tplaintext    hex dump of record plaintext");
        System.err.println("\tpacket       print raw SSL/TLS packets");
        System.err.println();
        System.exit(0);
    }

    public static Debug getInstance(String option) {
        return getInstance(option, option);
    }

    public static Debug getInstance(String option, String prefix) {
        if (!isOn(option)) {
            return null;
        }
        Debug d = new Debug();
        d.prefix = prefix;
        return d;
    }

    public static boolean isOn(String option) {
        boolean z = true;
        if (args == null) {
            return false;
        }
        option = option.toLowerCase(Locale.ENGLISH);
        if (args.indexOf("all") != -1) {
            return true;
        }
        int n = args.indexOf("ssl");
        if (n != -1 && args.indexOf("sslctx", n) == -1) {
            boolean z2;
            if (option.equals("data") || option.equals("packet")) {
                z2 = true;
            } else {
                z2 = option.equals("plaintext");
            }
            if (!z2) {
                return true;
            }
        }
        if (args.indexOf(option) == -1) {
            z = false;
        }
        return z;
    }

    public void println(String message) {
        System.err.println(this.prefix + ": " + message);
    }

    public void println() {
        System.err.println(this.prefix + ":");
    }

    public static void println(String prefix, String message) {
        System.err.println(prefix + ": " + message);
    }

    public static void println(PrintStream s, String name, byte[] data) {
        s.print(name + ":  { ");
        if (data == null) {
            s.print("null");
        } else {
            for (int i = 0; i < data.length; i++) {
                if (i != 0) {
                    s.print(", ");
                }
                s.print(data[i] & 255);
            }
        }
        s.println(" }");
    }

    static boolean getBooleanProperty(String propName, boolean defaultValue) {
        String b = (String) AccessController.doPrivileged(new GetPropertyAction(propName));
        if (b == null) {
            return defaultValue;
        }
        if (b.equalsIgnoreCase("false")) {
            return false;
        }
        if (b.equalsIgnoreCase("true")) {
            return true;
        }
        throw new RuntimeException("Value of " + propName + " must either be 'true' or 'false'");
    }

    static String toString(byte[] b) {
        return sun.security.util.Debug.toString(b);
    }
}
