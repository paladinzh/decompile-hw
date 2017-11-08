package java.util.logging;

import dalvik.system.VMStack;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import sun.util.logging.PlatformLogger;

public class Level implements Serializable {
    public static final Level ALL = new Level("ALL", Integer.MIN_VALUE, defaultBundle);
    public static final Level CONFIG = new Level("CONFIG", PlatformLogger.CONFIG, defaultBundle);
    public static final Level FINE = new Level("FINE", 500, defaultBundle);
    public static final Level FINER = new Level("FINER", 400, defaultBundle);
    public static final Level FINEST = new Level("FINEST", 300, defaultBundle);
    public static final Level INFO = new Level("INFO", PlatformLogger.INFO, defaultBundle);
    public static final Level OFF = new Level("OFF", Integer.MAX_VALUE, defaultBundle);
    public static final Level SEVERE = new Level("SEVERE", 1000, defaultBundle);
    public static final Level WARNING = new Level("WARNING", PlatformLogger.WARNING, defaultBundle);
    private static String defaultBundle = "sun.util.logging.resources.logging";
    private static final long serialVersionUID = -8176160795706313070L;
    private String localizedLevelName;
    private final String name;
    private transient ResourceBundle rb;
    private final String resourceBundleName;
    private final int value;

    static final class KnownLevel {
        private static Map<Integer, List<KnownLevel>> intToLevels = new HashMap();
        private static Map<String, List<KnownLevel>> nameToLevels = new HashMap();
        final Level levelObject;
        final Level mirroredLevel;

        KnownLevel(Level l) {
            this.levelObject = l;
            if (l.getClass() == Level.class) {
                this.mirroredLevel = l;
            } else {
                this.mirroredLevel = new Level(l.name, l.value, l.resourceBundleName);
            }
        }

        static synchronized void add(Level l) {
            synchronized (KnownLevel.class) {
                KnownLevel o = new KnownLevel(l);
                List<KnownLevel> list = (List) nameToLevels.get(l.name);
                if (list == null) {
                    list = new ArrayList();
                    nameToLevels.put(l.name, list);
                }
                list.add(o);
                list = (List) intToLevels.get(Integer.valueOf(l.value));
                if (list == null) {
                    list = new ArrayList();
                    intToLevels.put(Integer.valueOf(l.value), list);
                }
                list.add(o);
            }
        }

        static synchronized KnownLevel findByName(String name) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = (List) nameToLevels.get(name);
                if (list != null) {
                    KnownLevel knownLevel = (KnownLevel) list.get(0);
                    return knownLevel;
                }
                return null;
            }
        }

        static synchronized KnownLevel findByValue(int value) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = (List) intToLevels.get(Integer.valueOf(value));
                if (list != null) {
                    KnownLevel knownLevel = (KnownLevel) list.get(0);
                    return knownLevel;
                }
                return null;
            }
        }

        static synchronized KnownLevel findByLocalizedLevelName(String name) {
            synchronized (KnownLevel.class) {
                for (List<KnownLevel> levels : nameToLevels.values()) {
                    for (KnownLevel l : levels) {
                        if (name.equals(l.levelObject.getLocalizedLevelName())) {
                            return l;
                        }
                    }
                }
                return null;
            }
        }

        static synchronized KnownLevel findByLocalizedName(String name) {
            synchronized (KnownLevel.class) {
                for (List<KnownLevel> levels : nameToLevels.values()) {
                    for (KnownLevel l : levels) {
                        if (name.equals(l.levelObject.getLocalizedName())) {
                            return l;
                        }
                    }
                }
                return null;
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        static synchronized KnownLevel matches(Level l) {
            synchronized (KnownLevel.class) {
                List<KnownLevel> list = (List) nameToLevels.get(l.name);
                if (list != null) {
                    for (KnownLevel level : list) {
                        Level other = level.mirroredLevel;
                        if (l.value != other.value || (l.resourceBundleName != other.resourceBundleName && (l.resourceBundleName == null || !l.resourceBundleName.equals(other.resourceBundleName)))) {
                        }
                    }
                }
            }
        }
    }

    protected Level(String name, int value) {
        this(name, value, null);
    }

    protected Level(String name, int value, String resourceBundleName) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
        this.value = value;
        this.resourceBundleName = resourceBundleName;
        if (resourceBundleName != null) {
            try {
                ClassLoader cl = VMStack.getCallingClassLoader();
                if (cl != null) {
                    this.rb = ResourceBundle.getBundle(resourceBundleName, Locale.getDefault(), cl);
                } else {
                    this.rb = ResourceBundle.getBundle(resourceBundleName);
                }
            } catch (MissingResourceException e) {
                this.rb = null;
            }
        }
        if (resourceBundleName != null) {
            name = null;
        }
        this.localizedLevelName = name;
        KnownLevel.add(this);
    }

    public String getResourceBundleName() {
        return this.resourceBundleName;
    }

    public String getName() {
        return this.name;
    }

    public String getLocalizedName() {
        return getLocalizedLevelName();
    }

    final String getLevelName() {
        return this.name;
    }

    final synchronized String getLocalizedLevelName() {
        if (this.localizedLevelName != null) {
            return this.localizedLevelName;
        }
        try {
            this.localizedLevelName = this.rb.getString(this.name);
        } catch (Exception e) {
            this.localizedLevelName = this.name;
        }
        return this.localizedLevelName;
    }

    static Level findLevel(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        KnownLevel level = KnownLevel.findByName(name);
        if (level != null) {
            return level.mirroredLevel;
        }
        try {
            int x = Integer.parseInt(name);
            level = KnownLevel.findByValue(x);
            if (level == null) {
                Level levelObject = new Level(name, x);
                level = KnownLevel.findByValue(x);
            }
            return level.mirroredLevel;
        } catch (NumberFormatException e) {
            level = KnownLevel.findByLocalizedLevelName(name);
            if (level != null) {
                return level.mirroredLevel;
            }
            return null;
        }
    }

    public final String toString() {
        return this.name;
    }

    public final int intValue() {
        return this.value;
    }

    private Object readResolve() {
        KnownLevel o = KnownLevel.matches(this);
        if (o != null) {
            return o.levelObject;
        }
        return new Level(this.name, this.value, this.resourceBundleName);
    }

    public static synchronized Level parse(String name) throws IllegalArgumentException {
        synchronized (Level.class) {
            name.length();
            KnownLevel level = KnownLevel.findByName(name);
            if (level != null) {
                Level level2 = level.levelObject;
                return level2;
            }
            try {
                int x = Integer.parseInt(name);
                level = KnownLevel.findByValue(x);
                if (level == null) {
                    Level levelObject = new Level(name, x);
                    level = KnownLevel.findByValue(x);
                }
                level2 = level.levelObject;
                return level2;
            } catch (NumberFormatException e) {
                level = KnownLevel.findByLocalizedName(name);
                if (level != null) {
                    return level.levelObject;
                }
                throw new IllegalArgumentException("Bad level \"" + name + "\"");
            }
        }
    }

    public boolean equals(Object ox) {
        boolean z = false;
        try {
            if (((Level) ox).value == this.value) {
                z = true;
            }
            return z;
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        return this.value;
    }
}
