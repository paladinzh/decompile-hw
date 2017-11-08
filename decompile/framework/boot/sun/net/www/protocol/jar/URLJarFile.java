package sun.net.www.protocol.jar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import sun.net.www.ParseUtil;

public class URLJarFile extends JarFile {
    private static int BUF_SIZE = 2048;
    private static URLJarFileCallBack callback = null;
    private URLJarFileCloseController closeController = null;
    private Attributes superAttr;
    private Map<String, Attributes> superEntries;
    private Manifest superMan;

    public interface URLJarFileCloseController {
        void close(JarFile jarFile);
    }

    private class URLJarFileEntry extends JarEntry {
        private JarEntry je;

        URLJarFileEntry(JarEntry je) {
            super(je);
            this.je = je;
        }

        public Attributes getAttributes() throws IOException {
            if (URLJarFile.this.isSuperMan()) {
                Map<String, Attributes> e = URLJarFile.this.superEntries;
                if (e != null) {
                    Attributes a = (Attributes) e.get(getName());
                    if (a != null) {
                        return (Attributes) a.clone();
                    }
                }
            }
            return null;
        }

        public Certificate[] getCertificates() {
            Certificate[] certs = this.je.getCertificates();
            if (certs == null) {
                return null;
            }
            return (Certificate[]) certs.clone();
        }

        public CodeSigner[] getCodeSigners() {
            CodeSigner[] csg = this.je.getCodeSigners();
            if (csg == null) {
                return null;
            }
            return (CodeSigner[]) csg.clone();
        }
    }

    static JarFile getJarFile(URL url) throws IOException {
        return getJarFile(url, null);
    }

    static JarFile getJarFile(URL url, URLJarFileCloseController closeController) throws IOException {
        if (isFileURL(url)) {
            return new URLJarFile(url, closeController);
        }
        return retrieve(url, closeController);
    }

    private URLJarFile(URL url, URLJarFileCloseController closeController) throws IOException {
        super(ParseUtil.decode(url.getFile()));
        this.closeController = closeController;
    }

    private static boolean isFileURL(URL url) {
        if (url.getProtocol().equalsIgnoreCase("file")) {
            String host = url.getHost();
            if (host == null || host.equals("") || host.equals("~") || host.equalsIgnoreCase("localhost")) {
                return true;
            }
        }
        return false;
    }

    protected void finalize() throws IOException {
        close();
    }

    public ZipEntry getEntry(String name) {
        ZipEntry ze = super.getEntry(name);
        if (ze == null) {
            return null;
        }
        if (ze instanceof JarEntry) {
            return new URLJarFileEntry((JarEntry) ze);
        }
        throw new InternalError(super.getClass() + " returned unexpected entry type " + ze.getClass());
    }

    public Manifest getManifest() throws IOException {
        if (!isSuperMan()) {
            return null;
        }
        Manifest man = new Manifest();
        man.getMainAttributes().putAll((Map) this.superAttr.clone());
        if (this.superEntries != null) {
            Map<String, Attributes> entries = man.getEntries();
            for (String key : this.superEntries.keySet()) {
                entries.put(key, (Attributes) ((Attributes) this.superEntries.get(key)).clone());
            }
        }
        return man;
    }

    public void close() throws IOException {
        if (this.closeController != null) {
            this.closeController.close(this);
        }
        super.close();
    }

    private synchronized boolean isSuperMan() throws IOException {
        if (this.superMan == null) {
            this.superMan = super.getManifest();
        }
        if (this.superMan == null) {
            return false;
        }
        this.superAttr = this.superMan.getMainAttributes();
        this.superEntries = this.superMan.getEntries();
        return true;
    }

    private static JarFile retrieve(URL url) throws IOException {
        return retrieve(url, null);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static JarFile retrieve(URL url, URLJarFileCloseController closeController) throws IOException {
        Throwable th = null;
        if (callback != null) {
            return callback.retrieve(url);
        }
        InputStream inputStream = null;
        File tmpFile;
        try {
            inputStream = url.openConnection().getInputStream();
            tmpFile = File.createTempFile("jar_cache", null);
            copyToFile(inputStream, tmpFile);
            tmpFile.deleteOnExit();
            JarFile jarFile = new URLJarFile(tmpFile.toURL(), closeController);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th2) {
                    th = th2;
                }
            }
            if (th == null) {
                return jarFile;
            }
            throw th;
        } catch (Throwable th3) {
            Throwable th4 = th3;
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th5) {
                    if (th == null) {
                        th = th5;
                    } else if (th != th5) {
                        th.addSuppressed(th5);
                    }
                }
            }
            if (th == null) {
                throw th4;
            }
            throw th;
        }
    }

    static void copyToFile(InputStream in, File dst) throws IOException {
        OutputStream out = new FileOutputStream(dst);
        try {
            byte[] buf = new byte[4096];
            while (true) {
                int len = in.read(buf);
                if (len <= 0) {
                    break;
                }
                out.write(buf, 0, len);
            }
        } finally {
            out.close();
        }
    }

    public static void setCallBack(URLJarFileCallBack cb) {
        callback = cb;
    }
}
