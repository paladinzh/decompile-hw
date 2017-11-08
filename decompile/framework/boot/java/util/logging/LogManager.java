package java.util.logging;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import sun.util.logging.PlatformLogger;

public class LogManager {
    public static final String LOGGING_MXBEAN_NAME = "java.util.logging:type=Logging";
    private static final int MAX_ITERATIONS = 400;
    private static final Level defaultLevel = Level.INFO;
    private static LoggingMXBean loggingMXBean = null;
    private static LogManager manager;
    private PropertyChangeSupport changes = new PropertyChangeSupport(LogManager.class);
    private final Permission controlPermission = new LoggingPermission("control", null);
    private boolean deathImminent;
    private boolean initializedGlobalHandlers = true;
    private final ReferenceQueue<Logger> loggerRefQueue = new ReferenceQueue();
    private Properties props = new Properties();
    private volatile boolean readPrimordialConfiguration;
    private Logger rootLogger;
    private final LoggerContext systemContext = new SystemLoggerContext();
    private final LoggerContext userContext = new LoggerContext();

    private class Cleaner extends Thread {
        private Cleaner() {
            setContextClassLoader(null);
        }

        public void run() {
            LogManager mgr = LogManager.manager;
            synchronized (LogManager.this) {
                LogManager.this.deathImminent = true;
                LogManager.this.initializedGlobalHandlers = true;
            }
            LogManager.this.reset();
        }
    }

    private static class LogNode {
        HashMap<String, LogNode> children;
        final LoggerContext context;
        LoggerWeakRef loggerRef;
        LogNode parent;

        LogNode(LogNode parent, LoggerContext context) {
            this.parent = parent;
            this.context = context;
        }

        void walkAndSetParent(Logger parent) {
            if (this.children != null) {
                for (LogNode node : this.children.values()) {
                    LoggerWeakRef ref = node.loggerRef;
                    Logger logger = ref == null ? null : (Logger) ref.get();
                    if (logger == null) {
                        node.walkAndSetParent(parent);
                    } else {
                        LogManager.doSetParent(logger, parent);
                    }
                }
            }
        }
    }

    static class LoggerContext {
        static final /* synthetic */ boolean -assertionsDisabled = (!LoggerContext.class.desiredAssertionStatus());
        private final Hashtable<String, LoggerWeakRef> namedLoggers;
        private final boolean requiresDefaultLoggers;
        private final LogNode root;

        private LoggerContext() {
            this(false);
        }

        private LoggerContext(boolean requiresDefaultLoggers) {
            this.namedLoggers = new Hashtable();
            this.root = new LogNode(null, this);
            this.requiresDefaultLoggers = requiresDefaultLoggers;
        }

        Logger demandLogger(String name, String resourceBundleName) {
            return LogManager.manager.demandLogger(name, resourceBundleName, null);
        }

        private void ensureInitialized() {
            if (this.requiresDefaultLoggers) {
                ensureDefaultLogger(LogManager.manager.rootLogger);
                ensureDefaultLogger(Logger.global);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        synchronized Logger findLogger(String name) {
            ensureInitialized();
            LoggerWeakRef ref = (LoggerWeakRef) this.namedLoggers.get(name);
            if (ref == null) {
                return null;
            }
            Logger logger = (Logger) ref.get();
            if (logger == null) {
                removeLogger(name);
            }
        }

        private void ensureAllDefaultLoggers(Logger logger) {
            if (this.requiresDefaultLoggers) {
                String name = logger.getName();
                if (!name.isEmpty()) {
                    ensureDefaultLogger(LogManager.manager.rootLogger);
                }
                if (!Logger.GLOBAL_LOGGER_NAME.equals(name)) {
                    ensureDefaultLogger(Logger.global);
                }
            }
        }

        private void ensureDefaultLogger(Logger logger) {
            boolean z = false;
            if (this.requiresDefaultLoggers && logger != null && (logger == Logger.global || logger == LogManager.manager.rootLogger)) {
                if (!this.namedLoggers.containsKey(logger.getName())) {
                    addLocalLogger(logger, false);
                }
                return;
            }
            if (!-assertionsDisabled) {
                if (logger == null) {
                    z = true;
                }
                if (!z) {
                    throw new AssertionError();
                }
            }
        }

        boolean addLocalLogger(Logger logger) {
            return addLocalLogger(logger, this.requiresDefaultLoggers);
        }

        boolean addLocalLogger(Logger logger, LogManager manager) {
            return addLocalLogger(logger, this.requiresDefaultLoggers, manager);
        }

        boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded) {
            return addLocalLogger(logger, addDefaultLoggersIfNeeded, LogManager.manager);
        }

        synchronized boolean addLocalLogger(Logger logger, boolean addDefaultLoggersIfNeeded, LogManager manager) {
            if (addDefaultLoggersIfNeeded) {
                ensureAllDefaultLoggers(logger);
            }
            String name = logger.getName();
            if (name == null) {
                throw new NullPointerException();
            }
            LoggerWeakRef ref = (LoggerWeakRef) this.namedLoggers.get(name);
            if (ref != null) {
                if (ref.get() != null) {
                    return false;
                }
                removeLogger(name);
            }
            manager.getClass();
            ref = new LoggerWeakRef(logger);
            this.namedLoggers.put(name, ref);
            Level level = manager.getLevelProperty(name + ".level", null);
            if (level != null) {
                LogManager.doSetLevel(logger, level);
            }
            processParentHandlers(logger, name);
            LogNode node = getNode(name);
            node.loggerRef = ref;
            Logger parent = null;
            for (LogNode nodep = node.parent; nodep != null; nodep = nodep.parent) {
                LoggerWeakRef nodeRef = nodep.loggerRef;
                if (nodeRef != null) {
                    parent = (Logger) nodeRef.get();
                    if (parent != null) {
                        break;
                    }
                }
            }
            if (parent != null) {
                LogManager.doSetParent(logger, parent);
            }
            node.walkAndSetParent(logger);
            ref.setNode(node);
            return true;
        }

        void removeLogger(String name) {
            this.namedLoggers.remove(name);
        }

        synchronized Enumeration<String> getLoggerNames() {
            ensureInitialized();
            return this.namedLoggers.keys();
        }

        private void processParentHandlers(final Logger logger, final String name) {
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    if (!(logger == LogManager.manager.rootLogger || LogManager.manager.getBooleanProperty(name + ".useParentHandlers", true))) {
                        logger.setUseParentHandlers(false);
                    }
                    return null;
                }
            });
            int ix = 1;
            while (true) {
                int ix2 = name.indexOf(".", ix);
                if (ix2 >= 0) {
                    String pname = name.substring(0, ix2);
                    if (LogManager.manager.getProperty(pname + ".level") != null || LogManager.manager.getProperty(pname + ".handlers") != null) {
                        demandLogger(pname, null);
                    }
                    ix = ix2 + 1;
                } else {
                    return;
                }
            }
        }

        LogNode getNode(String name) {
            if (name == null || name.equals("")) {
                return this.root;
            }
            LogNode node = this.root;
            while (name.length() > 0) {
                String head;
                int ix = name.indexOf(".");
                if (ix > 0) {
                    head = name.substring(0, ix);
                    name = name.substring(ix + 1);
                } else {
                    head = name;
                    name = "";
                }
                if (node.children == null) {
                    node.children = new HashMap();
                }
                LogNode child = (LogNode) node.children.get(head);
                if (child == null) {
                    child = new LogNode(node, this);
                    node.children.put(head, child);
                }
                node = child;
            }
            return node;
        }
    }

    final class LoggerWeakRef extends WeakReference<Logger> {
        private String name;
        private LogNode node;
        private WeakReference<Logger> parentRef;

        LoggerWeakRef(Logger logger) {
            super(logger, LogManager.this.loggerRefQueue);
            this.name = logger.getName();
        }

        void dispose() {
            if (this.node != null) {
                this.node.context.removeLogger(this.name);
                this.name = null;
                this.node.loggerRef = null;
                this.node = null;
            }
            if (this.parentRef != null) {
                Logger parent = (Logger) this.parentRef.get();
                if (parent != null) {
                    parent.removeChildLogger(this);
                }
                this.parentRef = null;
            }
        }

        void setNode(LogNode node) {
            this.node = node;
        }

        void setParentRef(WeakReference<Logger> parentRef) {
            this.parentRef = parentRef;
        }
    }

    private class RootLogger extends Logger {
        private RootLogger() {
            super("", null);
            setLevel(LogManager.defaultLevel);
        }

        public void log(LogRecord record) {
            LogManager.this.initializeGlobalHandlers();
            super.log(record);
        }

        public void addHandler(Handler h) {
            LogManager.this.initializeGlobalHandlers();
            super.addHandler(h);
        }

        public void removeHandler(Handler h) {
            LogManager.this.initializeGlobalHandlers();
            super.removeHandler(h);
        }

        public Handler[] getHandlers() {
            LogManager.this.initializeGlobalHandlers();
            return super.getHandlers();
        }
    }

    static class SystemLoggerContext extends LoggerContext {
        SystemLoggerContext() {
            super();
        }

        Logger demandLogger(String name, String resourceBundleName) {
            Logger result = findLogger(name);
            if (result == null) {
                Logger newLogger = new Logger(name, resourceBundleName);
                do {
                    if (addLocalLogger(newLogger)) {
                        result = newLogger;
                        continue;
                    } else {
                        result = findLogger(name);
                        continue;
                    }
                } while (result == null);
            }
            return result;
        }
    }

    static {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                try {
                    String cname = System.getProperty("java.util.logging.manager");
                    if (cname != null) {
                        LogManager.manager = (LogManager) LogManager.getClassInstance(cname).newInstance();
                    }
                } catch (Exception ex) {
                    System.err.println("Could not load Logmanager \"" + null + "\"");
                    ex.printStackTrace();
                }
                if (LogManager.manager == null) {
                    LogManager.manager = new LogManager();
                }
                LogManager -get2 = LogManager.manager;
                LogManager -get22 = LogManager.manager;
                -get22.getClass();
                -get2.rootLogger = new RootLogger();
                LogManager.manager.addLogger(LogManager.manager.rootLogger);
                LogManager.manager.systemContext.addLocalLogger(LogManager.manager.rootLogger, false);
                LogManager.manager.userContext.addLocalLogger(LogManager.manager.rootLogger, false);
                Logger.global.setLogManager(LogManager.manager);
                LogManager.manager.addLogger(Logger.global);
                LogManager.manager.systemContext.addLocalLogger(Logger.global, false);
                LogManager.manager.userContext.addLocalLogger(Logger.global, false);
                return null;
            }
        });
    }

    protected LogManager() {
        try {
            Runtime.getRuntime().addShutdownHook(new Cleaner());
        } catch (IllegalStateException e) {
        }
    }

    public static LogManager getLogManager() {
        if (manager != null) {
            manager.readPrimordialConfiguration();
        }
        return manager;
    }

    private void readPrimordialConfiguration() {
        if (!this.readPrimordialConfiguration) {
            synchronized (this) {
                if (!this.readPrimordialConfiguration) {
                    if (System.out == null) {
                        return;
                    } else {
                        this.readPrimordialConfiguration = true;
                        try {
                            AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                                public Void run() throws Exception {
                                    LogManager.this.readConfiguration();
                                    PlatformLogger.redirectPlatformLoggers();
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                        }
                    }
                }
            }
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener l) throws SecurityException {
        if (l == null) {
            throw new NullPointerException();
        }
        checkPermission();
        this.changes.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) throws SecurityException {
        checkPermission();
        this.changes.removePropertyChangeListener(l);
    }

    private LoggerContext getUserContext() {
        return this.userContext;
    }

    private List<LoggerContext> contexts() {
        List<LoggerContext> cxs = new ArrayList();
        cxs.add(this.systemContext);
        cxs.add(getUserContext());
        return cxs;
    }

    Logger demandLogger(String name, String resourceBundleName, Class<?> caller) {
        Logger result = getLogger(name);
        if (result == null) {
            Logger newLogger = new Logger(name, resourceBundleName, caller);
            while (!addLogger(newLogger)) {
                result = getLogger(name);
                if (result != null) {
                }
            }
            return newLogger;
        }
        return result;
    }

    Logger demandSystemLogger(String name, String resourceBundleName) {
        final Logger sysLogger = this.systemContext.demandLogger(name, resourceBundleName);
        Logger logger;
        do {
            if (addLogger(sysLogger)) {
                logger = sysLogger;
                continue;
            } else {
                logger = getLogger(name);
                continue;
            }
        } while (logger == null);
        if (logger != sysLogger && sysLogger.getHandlers().length == 0) {
            final Logger l = logger;
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    for (Handler hdl : l.getHandlers()) {
                        sysLogger.addHandler(hdl);
                    }
                    return null;
                }
            });
        }
        return sysLogger;
    }

    private static Class getClassInstance(String cname) {
        if (cname == null) {
            return null;
        }
        try {
            return ClassLoader.getSystemClassLoader().loadClass(cname);
        } catch (ClassNotFoundException e) {
            try {
                return Thread.currentThread().getContextClassLoader().loadClass(cname);
            } catch (ClassNotFoundException e2) {
                return null;
            }
        }
    }

    private void loadLoggerHandlers(final Logger logger, String name, final String handlersPropertyName) {
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                String[] names = LogManager.this.parseClassNames(handlersPropertyName);
                for (String word : names) {
                    try {
                        Handler hdl = (Handler) LogManager.getClassInstance(word).newInstance();
                        String levs = LogManager.this.getProperty(word + ".level");
                        if (levs != null) {
                            Level l = Level.findLevel(levs);
                            if (l != null) {
                                hdl.setLevel(l);
                            } else {
                                System.err.println("Can't set level for " + word);
                            }
                        }
                        logger.addHandler(hdl);
                    } catch (Object ex) {
                        System.err.println("Can't load log handler \"" + word + "\"");
                        System.err.println("" + ex);
                        ex.printStackTrace();
                    }
                }
                return null;
            }
        });
    }

    final synchronized void drainLoggerRefQueueBounded() {
        int i = 0;
        while (i < 400) {
            if (this.loggerRefQueue != null) {
                LoggerWeakRef ref = (LoggerWeakRef) this.loggerRefQueue.poll();
                if (ref == null) {
                    break;
                }
                ref.dispose();
                i++;
            } else {
                break;
            }
        }
    }

    public boolean addLogger(Logger logger) {
        String name = logger.getName();
        if (name == null) {
            throw new NullPointerException();
        }
        drainLoggerRefQueueBounded();
        if (!getUserContext().addLocalLogger(logger, this)) {
            return false;
        }
        loadLoggerHandlers(logger, name, name + ".handlers");
        return true;
    }

    private static void doSetLevel(final Logger logger, final Level level) {
        if (System.getSecurityManager() == null) {
            logger.setLevel(level);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    logger.setLevel(level);
                    return null;
                }
            });
        }
    }

    private static void doSetParent(final Logger logger, final Logger parent) {
        if (System.getSecurityManager() == null) {
            logger.setParent(parent);
        } else {
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Object run() {
                    logger.setParent(parent);
                    return null;
                }
            });
        }
    }

    public Logger getLogger(String name) {
        return getUserContext().findLogger(name);
    }

    public Enumeration<String> getLoggerNames() {
        return getUserContext().getLoggerNames();
    }

    public void readConfiguration() throws IOException, SecurityException {
        InputStream in;
        checkPermission();
        String cname = System.getProperty("java.util.logging.config.class");
        if (cname != null) {
            try {
                getClassInstance(cname).newInstance();
                return;
            } catch (Object ex) {
                System.err.println("Logging configuration class \"" + cname + "\" failed");
                System.err.println("" + ex);
            }
        }
        String fname = System.getProperty("java.util.logging.config.file");
        if (fname == null) {
            fname = System.getProperty("java.home");
            if (fname == null) {
                throw new Error("Can't find java.home ??");
            }
            fname = new File(new File(fname, "lib"), "logging.properties").getCanonicalPath();
        }
        try {
            in = new FileInputStream(fname);
        } catch (Exception e) {
            in = LogManager.class.getResourceAsStream("logging.properties");
            if (in == null) {
                throw e;
            }
        }
        try {
            readConfiguration(new BufferedInputStream(in));
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public void reset() throws SecurityException {
        checkPermission();
        synchronized (this) {
            this.props = new Properties();
            this.initializedGlobalHandlers = true;
        }
        for (LoggerContext cx : contexts()) {
            Enumeration<String> enum_ = cx.getLoggerNames();
            while (enum_.hasMoreElements()) {
                Logger logger = cx.findLogger((String) enum_.nextElement());
                if (logger != null) {
                    resetLogger(logger);
                }
            }
        }
    }

    private void resetLogger(Logger logger) {
        Handler[] targets = logger.getHandlers();
        for (Handler h : targets) {
            logger.removeHandler(h);
            try {
                h.close();
            } catch (Exception e) {
            }
        }
        String name = logger.getName();
        if (name == null || !name.equals("")) {
            logger.setLevel(null);
        } else {
            logger.setLevel(defaultLevel);
        }
    }

    private String[] parseClassNames(String propertyName) {
        String hands = getProperty(propertyName);
        if (hands == null) {
            return new String[0];
        }
        hands = hands.trim();
        int ix = 0;
        Vector<String> result = new Vector();
        while (ix < hands.length()) {
            int end = ix;
            while (end < hands.length() && !Character.isWhitespace(hands.charAt(end)) && hands.charAt(end) != ',') {
                end++;
            }
            String word = hands.substring(ix, end);
            ix = end + 1;
            word = word.trim();
            if (word.length() != 0) {
                result.add(word);
            }
        }
        return (String[]) result.toArray(new String[result.size()]);
    }

    public void readConfiguration(InputStream ins) throws IOException, SecurityException {
        checkPermission();
        reset();
        this.props.load(ins);
        String[] names = parseClassNames("config");
        for (String word : names) {
            try {
                getClassInstance(word).newInstance();
            } catch (Object ex) {
                System.err.println("Can't load config class \"" + word + "\"");
                System.err.println("" + ex);
            }
        }
        setLevelsOnExistingLoggers();
        this.changes.firePropertyChange(null, null, null);
        synchronized (this) {
            this.initializedGlobalHandlers = false;
        }
    }

    public String getProperty(String name) {
        return this.props.getProperty(name);
    }

    String getStringProperty(String name, String defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        return val.trim();
    }

    int getIntProperty(String name, int defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    boolean getBooleanProperty(String name, boolean defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        val = val.toLowerCase();
        if (val.equals("true") || val.equals("1")) {
            return true;
        }
        if (val.equals("false") || val.equals("0")) {
            return false;
        }
        return defaultValue;
    }

    Level getLevelProperty(String name, Level defaultValue) {
        String val = getProperty(name);
        if (val == null) {
            return defaultValue;
        }
        Level l = Level.findLevel(val.trim());
        if (l == null) {
            l = defaultValue;
        }
        return l;
    }

    Filter getFilterProperty(String name, Filter defaultValue) {
        String val = getProperty(name);
        if (val != null) {
            try {
                return (Filter) getClassInstance(val).newInstance();
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    Formatter getFormatterProperty(String name, Formatter defaultValue) {
        String val = getProperty(name);
        if (val != null) {
            try {
                return (Formatter) getClassInstance(val).newInstance();
            } catch (Exception e) {
            }
        }
        return defaultValue;
    }

    private synchronized void initializeGlobalHandlers() {
        if (!this.initializedGlobalHandlers) {
            this.initializedGlobalHandlers = true;
            if (!this.deathImminent) {
                loadLoggerHandlers(this.rootLogger, null, "handlers");
            }
        }
    }

    void checkPermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(this.controlPermission);
        }
    }

    public void checkAccess() throws SecurityException {
        checkPermission();
    }

    private synchronized void setLevelsOnExistingLoggers() {
        Enumeration<?> enum_ = this.props.propertyNames();
        while (enum_.hasMoreElements()) {
            String key = (String) enum_.nextElement();
            if (key.endsWith(".level")) {
                String name = key.substring(0, key.length() - 6);
                Level level = getLevelProperty(key, null);
                if (level == null) {
                    System.err.println("Bad level value for property: " + key);
                } else {
                    for (LoggerContext cx : contexts()) {
                        Logger l = cx.findLogger(name);
                        if (l != null) {
                            l.setLevel(level);
                        }
                    }
                }
            }
        }
    }

    public static synchronized LoggingMXBean getLoggingMXBean() {
        LoggingMXBean loggingMXBean;
        synchronized (LogManager.class) {
            if (loggingMXBean == null) {
                loggingMXBean = new Logging();
            }
            loggingMXBean = loggingMXBean;
        }
        return loggingMXBean;
    }
}
