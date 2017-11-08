package android.icu.impl;

import android.icu.impl.locale.BaseLocale;
import android.icu.util.ULocale;
import android.icu.util.UResourceBundle;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class ResourceBundleWrapper extends UResourceBundle {
    private static final boolean DEBUG = ICUDebug.enabled("resourceBundleWrapper");
    private String baseName = null;
    private ResourceBundle bundle = null;
    private List<String> keys = null;
    private String localeID = null;

    private ResourceBundleWrapper(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    protected void setLoadingStatus(int newStatus) {
    }

    protected Object handleGetObject(String aKey) {
        ResourceBundleWrapper current = this;
        Object obj = null;
        while (current != null) {
            try {
                obj = current.bundle.getObject(aKey);
                break;
            } catch (MissingResourceException e) {
                current = (ResourceBundleWrapper) current.getParent();
            }
        }
        if (obj != null) {
            return obj;
        }
        throw new MissingResourceException("Can't find resource for bundle " + this.baseName + ", key " + aKey, getClass().getName(), aKey);
    }

    public Enumeration<String> getKeys() {
        return Collections.enumeration(this.keys);
    }

    private void initKeysVector() {
        this.keys = new ArrayList();
        for (ResourceBundleWrapper current = this; current != null; current = (ResourceBundleWrapper) current.getParent()) {
            Enumeration<String> e = current.bundle.getKeys();
            while (e.hasMoreElements()) {
                String elem = (String) e.nextElement();
                if (!this.keys.contains(elem)) {
                    this.keys.add(elem);
                }
            }
        }
    }

    protected String getLocaleID() {
        return this.localeID;
    }

    protected String getBaseName() {
        return this.bundle.getClass().getName().replace('.', '/');
    }

    public ULocale getULocale() {
        return new ULocale(this.localeID);
    }

    public UResourceBundle getParent() {
        return (UResourceBundle) this.parent;
    }

    public static UResourceBundle getBundleInstance(String baseName, String localeID, ClassLoader root, boolean disableFallback) {
        UResourceBundle b = instantiateBundle(baseName, localeID, root, disableFallback);
        if (b != null) {
            return b;
        }
        String separator = BaseLocale.SEP;
        if (baseName.indexOf(47) >= 0) {
            separator = "/";
        }
        throw new MissingResourceException("Could not find the bundle " + baseName + separator + localeID, "", "");
    }

    protected static synchronized UResourceBundle instantiateBundle(String baseName, String localeID, ClassLoader root, boolean disableFallback) {
        ResourceBundleWrapper b;
        final String str;
        InputStream stream;
        InputStream bufferedInputStream;
        String defaultName;
        Exception e;
        Throwable th;
        synchronized (ResourceBundleWrapper.class) {
            if (root == null) {
                root = ClassLoaderUtil.getClassLoader();
            }
            final ClassLoader cl = root;
            String name = baseName;
            ULocale defaultLocale = ULocale.getDefault();
            if (localeID.length() != 0) {
                name = baseName + BaseLocale.SEP + localeID;
            }
            b = (ResourceBundleWrapper) UResourceBundle.loadFromCache(name, defaultLocale);
            if (b == null) {
                ResourceBundleWrapper b2;
                ResourceBundle parent = null;
                int i = localeID.lastIndexOf(95);
                boolean loadFromProperties = false;
                if (i != -1) {
                    String locName = localeID.substring(0, i);
                    parent = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName + BaseLocale.SEP + locName, defaultLocale);
                    if (parent == null) {
                        parent = (ResourceBundleWrapper) instantiateBundle(baseName, locName, cl, disableFallback);
                    }
                } else if (localeID.length() > 0) {
                    ResourceBundleWrapper parent2 = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName, defaultLocale);
                    if (parent2 == null) {
                        parent2 = (ResourceBundleWrapper) instantiateBundle(baseName, "", cl, disableFallback);
                    }
                }
                try {
                    b2 = new ResourceBundleWrapper((ResourceBundle) cl.loadClass(name).asSubclass(ResourceBundle.class).newInstance());
                    if (parent != null) {
                        try {
                            b2.setParent(parent);
                        } catch (ClassNotFoundException e2) {
                            b = b2;
                            loadFromProperties = true;
                            b2 = b;
                            if (loadFromProperties) {
                                try {
                                    str = name.replace('.', '/') + ".properties";
                                    stream = (InputStream) AccessController.doPrivileged(new PrivilegedAction<InputStream>() {
                                        public InputStream run() {
                                            if (cl != null) {
                                                return cl.getResourceAsStream(str);
                                            }
                                            return ClassLoader.getSystemResourceAsStream(str);
                                        }
                                    });
                                    if (stream != null) {
                                        bufferedInputStream = new BufferedInputStream(stream);
                                        try {
                                            b = new ResourceBundleWrapper(new PropertyResourceBundle(bufferedInputStream));
                                            if (parent != null) {
                                                try {
                                                    b.setParent(parent);
                                                } catch (Exception e3) {
                                                    try {
                                                        bufferedInputStream.close();
                                                    } catch (Exception e4) {
                                                    }
                                                    stream = bufferedInputStream;
                                                    if (b == null) {
                                                        try {
                                                            defaultName = defaultLocale.toString();
                                                            b = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName + BaseLocale.SEP + defaultName, defaultLocale);
                                                            if (b == null) {
                                                                b = (ResourceBundleWrapper) instantiateBundle(baseName, defaultName, cl, disableFallback);
                                                            }
                                                        } catch (Exception e5) {
                                                            e = e5;
                                                        }
                                                    }
                                                    if (b == null) {
                                                        b = parent;
                                                    }
                                                    b = (ResourceBundleWrapper) UResourceBundle.addToCache(name, defaultLocale, b);
                                                    if (b != null) {
                                                        b.initKeysVector();
                                                    } else if (DEBUG) {
                                                        System.out.println("Returning null for " + baseName + BaseLocale.SEP + localeID);
                                                    }
                                                    return b;
                                                } catch (Throwable th2) {
                                                    th = th2;
                                                    try {
                                                        bufferedInputStream.close();
                                                    } catch (Exception e6) {
                                                    }
                                                    throw th;
                                                }
                                            }
                                            b.baseName = baseName;
                                            b.localeID = localeID;
                                            try {
                                                bufferedInputStream.close();
                                            } catch (Exception e7) {
                                            }
                                        } catch (Exception e8) {
                                            b = b2;
                                            bufferedInputStream.close();
                                            stream = bufferedInputStream;
                                            if (b == null) {
                                                defaultName = defaultLocale.toString();
                                                b = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName + BaseLocale.SEP + defaultName, defaultLocale);
                                                if (b == null) {
                                                    b = (ResourceBundleWrapper) instantiateBundle(baseName, defaultName, cl, disableFallback);
                                                }
                                            }
                                            if (b == null) {
                                                b = parent;
                                            }
                                            b = (ResourceBundleWrapper) UResourceBundle.addToCache(name, defaultLocale, b);
                                            if (b != null) {
                                                b.initKeysVector();
                                            } else if (DEBUG) {
                                                System.out.println("Returning null for " + baseName + BaseLocale.SEP + localeID);
                                            }
                                            return b;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            b = b2;
                                            bufferedInputStream.close();
                                            throw th;
                                        }
                                        stream = bufferedInputStream;
                                    } else {
                                        b = b2;
                                    }
                                    if (b == null) {
                                        defaultName = defaultLocale.toString();
                                        b = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName + BaseLocale.SEP + defaultName, defaultLocale);
                                        if (b == null) {
                                            b = (ResourceBundleWrapper) instantiateBundle(baseName, defaultName, cl, disableFallback);
                                        }
                                    }
                                    if (b == null) {
                                        b = parent;
                                    }
                                } catch (Exception e9) {
                                    e = e9;
                                    b = b2;
                                    if (DEBUG) {
                                        System.out.println("failure");
                                    }
                                    if (DEBUG) {
                                        System.out.println(e);
                                    }
                                    b = (ResourceBundleWrapper) UResourceBundle.addToCache(name, defaultLocale, b);
                                    if (b != null) {
                                        b.initKeysVector();
                                    } else if (DEBUG) {
                                        System.out.println("Returning null for " + baseName + BaseLocale.SEP + localeID);
                                    }
                                    return b;
                                }
                            }
                            b = b2;
                            b = (ResourceBundleWrapper) UResourceBundle.addToCache(name, defaultLocale, b);
                            if (b != null) {
                                b.initKeysVector();
                            } else if (DEBUG) {
                                System.out.println("Returning null for " + baseName + BaseLocale.SEP + localeID);
                            }
                            return b;
                        } catch (NoClassDefFoundError e10) {
                            b = b2;
                            loadFromProperties = true;
                            b2 = b;
                            if (loadFromProperties) {
                                b = b2;
                            } else {
                                str = name.replace('.', '/') + ".properties";
                                stream = (InputStream) AccessController.doPrivileged(/* anonymous class already generated */);
                                if (stream != null) {
                                    b = b2;
                                } else {
                                    bufferedInputStream = new BufferedInputStream(stream);
                                    b = new ResourceBundleWrapper(new PropertyResourceBundle(bufferedInputStream));
                                    if (parent != null) {
                                        b.setParent(parent);
                                    }
                                    b.baseName = baseName;
                                    b.localeID = localeID;
                                    bufferedInputStream.close();
                                    stream = bufferedInputStream;
                                }
                                if (b == null) {
                                    defaultName = defaultLocale.toString();
                                    b = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName + BaseLocale.SEP + defaultName, defaultLocale);
                                    if (b == null) {
                                        b = (ResourceBundleWrapper) instantiateBundle(baseName, defaultName, cl, disableFallback);
                                    }
                                }
                                if (b == null) {
                                    b = parent;
                                }
                            }
                            b = (ResourceBundleWrapper) UResourceBundle.addToCache(name, defaultLocale, b);
                            if (b != null) {
                                b.initKeysVector();
                            } else if (DEBUG) {
                                System.out.println("Returning null for " + baseName + BaseLocale.SEP + localeID);
                            }
                            return b;
                        } catch (Exception e11) {
                            e = e11;
                            b = b2;
                            if (DEBUG) {
                                System.out.println("failure");
                            }
                            if (DEBUG) {
                                System.out.println(e);
                                b2 = b;
                            } else {
                                b2 = b;
                            }
                            if (loadFromProperties) {
                                str = name.replace('.', '/') + ".properties";
                                stream = (InputStream) AccessController.doPrivileged(/* anonymous class already generated */);
                                if (stream != null) {
                                    bufferedInputStream = new BufferedInputStream(stream);
                                    b = new ResourceBundleWrapper(new PropertyResourceBundle(bufferedInputStream));
                                    if (parent != null) {
                                        b.setParent(parent);
                                    }
                                    b.baseName = baseName;
                                    b.localeID = localeID;
                                    bufferedInputStream.close();
                                    stream = bufferedInputStream;
                                } else {
                                    b = b2;
                                }
                                if (b == null) {
                                    defaultName = defaultLocale.toString();
                                    b = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName + BaseLocale.SEP + defaultName, defaultLocale);
                                    if (b == null) {
                                        b = (ResourceBundleWrapper) instantiateBundle(baseName, defaultName, cl, disableFallback);
                                    }
                                }
                                if (b == null) {
                                    b = parent;
                                }
                            } else {
                                b = b2;
                            }
                            b = (ResourceBundleWrapper) UResourceBundle.addToCache(name, defaultLocale, b);
                            if (b != null) {
                                b.initKeysVector();
                            } else if (DEBUG) {
                                System.out.println("Returning null for " + baseName + BaseLocale.SEP + localeID);
                            }
                            return b;
                        }
                    }
                    b2.baseName = baseName;
                    b2.localeID = localeID;
                } catch (ClassNotFoundException e12) {
                    loadFromProperties = true;
                    b2 = b;
                    if (loadFromProperties) {
                        b = b2;
                    } else {
                        str = name.replace('.', '/') + ".properties";
                        stream = (InputStream) AccessController.doPrivileged(/* anonymous class already generated */);
                        if (stream != null) {
                            b = b2;
                        } else {
                            bufferedInputStream = new BufferedInputStream(stream);
                            b = new ResourceBundleWrapper(new PropertyResourceBundle(bufferedInputStream));
                            if (parent != null) {
                                b.setParent(parent);
                            }
                            b.baseName = baseName;
                            b.localeID = localeID;
                            bufferedInputStream.close();
                            stream = bufferedInputStream;
                        }
                        if (b == null) {
                            defaultName = defaultLocale.toString();
                            b = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName + BaseLocale.SEP + defaultName, defaultLocale);
                            if (b == null) {
                                b = (ResourceBundleWrapper) instantiateBundle(baseName, defaultName, cl, disableFallback);
                            }
                        }
                        if (b == null) {
                            b = parent;
                        }
                    }
                    b = (ResourceBundleWrapper) UResourceBundle.addToCache(name, defaultLocale, b);
                    if (b != null) {
                        b.initKeysVector();
                    } else if (DEBUG) {
                        System.out.println("Returning null for " + baseName + BaseLocale.SEP + localeID);
                    }
                    return b;
                } catch (NoClassDefFoundError e13) {
                    loadFromProperties = true;
                    b2 = b;
                    if (loadFromProperties) {
                        str = name.replace('.', '/') + ".properties";
                        stream = (InputStream) AccessController.doPrivileged(/* anonymous class already generated */);
                        if (stream != null) {
                            bufferedInputStream = new BufferedInputStream(stream);
                            b = new ResourceBundleWrapper(new PropertyResourceBundle(bufferedInputStream));
                            if (parent != null) {
                                b.setParent(parent);
                            }
                            b.baseName = baseName;
                            b.localeID = localeID;
                            bufferedInputStream.close();
                            stream = bufferedInputStream;
                        } else {
                            b = b2;
                        }
                        if (b == null) {
                            defaultName = defaultLocale.toString();
                            b = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName + BaseLocale.SEP + defaultName, defaultLocale);
                            if (b == null) {
                                b = (ResourceBundleWrapper) instantiateBundle(baseName, defaultName, cl, disableFallback);
                            }
                        }
                        if (b == null) {
                            b = parent;
                        }
                    } else {
                        b = b2;
                    }
                    b = (ResourceBundleWrapper) UResourceBundle.addToCache(name, defaultLocale, b);
                    if (b != null) {
                        b.initKeysVector();
                    } else if (DEBUG) {
                        System.out.println("Returning null for " + baseName + BaseLocale.SEP + localeID);
                    }
                    return b;
                } catch (Exception e14) {
                    e = e14;
                    if (DEBUG) {
                        System.out.println("failure");
                    }
                    if (DEBUG) {
                        System.out.println(e);
                        b2 = b;
                    } else {
                        b2 = b;
                    }
                    if (loadFromProperties) {
                        b = b2;
                    } else {
                        str = name.replace('.', '/') + ".properties";
                        stream = (InputStream) AccessController.doPrivileged(/* anonymous class already generated */);
                        if (stream != null) {
                            b = b2;
                        } else {
                            bufferedInputStream = new BufferedInputStream(stream);
                            b = new ResourceBundleWrapper(new PropertyResourceBundle(bufferedInputStream));
                            if (parent != null) {
                                b.setParent(parent);
                            }
                            b.baseName = baseName;
                            b.localeID = localeID;
                            bufferedInputStream.close();
                            stream = bufferedInputStream;
                        }
                        if (b == null) {
                            defaultName = defaultLocale.toString();
                            b = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName + BaseLocale.SEP + defaultName, defaultLocale);
                            if (b == null) {
                                b = (ResourceBundleWrapper) instantiateBundle(baseName, defaultName, cl, disableFallback);
                            }
                        }
                        if (b == null) {
                            b = parent;
                        }
                    }
                    b = (ResourceBundleWrapper) UResourceBundle.addToCache(name, defaultLocale, b);
                    if (b != null) {
                        b.initKeysVector();
                    } else if (DEBUG) {
                        System.out.println("Returning null for " + baseName + BaseLocale.SEP + localeID);
                    }
                    return b;
                }
                if (loadFromProperties) {
                    str = name.replace('.', '/') + ".properties";
                    stream = (InputStream) AccessController.doPrivileged(/* anonymous class already generated */);
                    if (stream != null) {
                        bufferedInputStream = new BufferedInputStream(stream);
                        b = new ResourceBundleWrapper(new PropertyResourceBundle(bufferedInputStream));
                        if (parent != null) {
                            b.setParent(parent);
                        }
                        b.baseName = baseName;
                        b.localeID = localeID;
                        bufferedInputStream.close();
                        stream = bufferedInputStream;
                    } else {
                        b = b2;
                    }
                    if (b == null) {
                        defaultName = defaultLocale.toString();
                        if (localeID.length() > 0 && localeID.indexOf(95) < 0 && defaultName.indexOf(localeID) == -1) {
                            b = (ResourceBundleWrapper) UResourceBundle.loadFromCache(baseName + BaseLocale.SEP + defaultName, defaultLocale);
                            if (b == null) {
                                b = (ResourceBundleWrapper) instantiateBundle(baseName, defaultName, cl, disableFallback);
                            }
                        }
                    }
                    if (b == null) {
                        b = parent;
                    }
                } else {
                    b = b2;
                }
                b = (ResourceBundleWrapper) UResourceBundle.addToCache(name, defaultLocale, b);
            }
            if (b != null) {
                b.initKeysVector();
            } else if (DEBUG) {
                System.out.println("Returning null for " + baseName + BaseLocale.SEP + localeID);
            }
        }
        return b;
    }
}
