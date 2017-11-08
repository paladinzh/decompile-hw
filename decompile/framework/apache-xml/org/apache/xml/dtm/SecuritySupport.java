package org.apache.xml.dtm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

class SecuritySupport {
    private static final Object securitySupport;

    SecuritySupport() {
    }

    static {
        SecuritySupport ss;
        try {
            Class c = Class.forName("java.security.AccessController");
            ss = new SecuritySupport12();
            if (ss == null) {
                ss = new SecuritySupport();
            }
        } catch (Exception e) {
            ss = new SecuritySupport();
        } catch (Throwable th) {
            securitySupport = new SecuritySupport();
        }
        securitySupport = ss;
    }

    static SecuritySupport getInstance() {
        return (SecuritySupport) securitySupport;
    }

    ClassLoader getContextClassLoader() {
        return null;
    }

    ClassLoader getSystemClassLoader() {
        return null;
    }

    ClassLoader getParentClassLoader(ClassLoader cl) {
        return null;
    }

    String getSystemProperty(String propName) {
        return System.getProperty(propName);
    }

    FileInputStream getFileInputStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    InputStream getResourceAsStream(ClassLoader cl, String name) {
        if (cl == null) {
            return ClassLoader.getSystemResourceAsStream(name);
        }
        return cl.getResourceAsStream(name);
    }

    boolean getFileExists(File f) {
        return f.exists();
    }

    long getLastModified(File f) {
        return f.lastModified();
    }
}
