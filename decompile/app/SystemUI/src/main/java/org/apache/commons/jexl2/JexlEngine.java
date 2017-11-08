package org.apache.commons.jexl2;

import fyusion.vislib.BuildConfig;
import java.io.Reader;
import java.io.StringReader;
import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.jexl2.JexlException.Parsing;
import org.apache.commons.jexl2.JexlException.Tokenization;
import org.apache.commons.jexl2.introspection.Uberspect;
import org.apache.commons.jexl2.introspection.UberspectImpl;
import org.apache.commons.jexl2.parser.ASTJexlScript;
import org.apache.commons.jexl2.parser.ParseException;
import org.apache.commons.jexl2.parser.Parser;
import org.apache.commons.jexl2.parser.TokenMgrError;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JexlEngine {
    public static final JexlContext EMPTY_CONTEXT = new JexlContext() {
        public Object get(String name) {
            return null;
        }

        public boolean has(String name) {
            return false;
        }

        public void set(String name, Object value) {
            throw new UnsupportedOperationException("Not supported in void context.");
        }
    };
    protected final JexlArithmetic arithmetic;
    protected SoftCache<String, ASTJexlScript> cache;
    protected volatile boolean debug;
    protected Map<String, Object> functions;
    protected final Log logger;
    protected final Parser parser;
    protected volatile boolean silent;
    protected final Uberspect uberspect;

    public static final class Frame {
        private String[] parameters = null;
        private Object[] registers = null;

        Frame(Object[] r, String[] p) {
            this.registers = r;
            this.parameters = p;
        }

        public Object[] getRegisters() {
            return this.registers;
        }

        public String[] getParameters() {
            return this.parameters;
        }
    }

    public static final class Scope {
        private Map<String, Integer> namedRegisters = null;
        private final int parms;

        public Scope(String... parameters) {
            if (parameters == null) {
                this.parms = 0;
                return;
            }
            this.parms = parameters.length;
            this.namedRegisters = new LinkedHashMap();
            for (int p = 0; p < this.parms; p++) {
                this.namedRegisters.put(parameters[p], Integer.valueOf(p));
            }
        }

        public int hashCode() {
            return this.namedRegisters != null ? this.parms ^ this.namedRegisters.hashCode() : 0;
        }

        public boolean equals(Object o) {
            return (o instanceof Scope) && equals((Scope) o);
        }

        public boolean equals(Scope frame) {
            boolean z = false;
            if (this == frame) {
                return true;
            }
            if (frame == null || this.parms != frame.parms) {
                return false;
            }
            if (this.namedRegisters != null) {
                return this.namedRegisters.equals(frame.namedRegisters);
            }
            if (frame.namedRegisters == null) {
                z = true;
            }
            return z;
        }

        public Integer getRegister(String name) {
            return this.namedRegisters == null ? null : (Integer) this.namedRegisters.get(name);
        }

        public Integer declareVariable(String name) {
            if (this.namedRegisters == null) {
                this.namedRegisters = new LinkedHashMap();
            }
            Integer register = (Integer) this.namedRegisters.get(name);
            if (register != null) {
                return register;
            }
            register = Integer.valueOf(this.namedRegisters.size());
            this.namedRegisters.put(name, register);
            return register;
        }

        public Frame createFrame(Object... values) {
            if (this.namedRegisters == null) {
                return null;
            }
            Object[] arguments = new Object[this.namedRegisters.size()];
            if (values != null) {
                System.arraycopy(values, 0, arguments, 0, Math.min(this.parms, values.length));
            }
            return new Frame(arguments, (String[]) this.namedRegisters.keySet().toArray(new String[0]));
        }
    }

    protected class SoftCache<K, V> {
        private SoftReference<Map<K, V>> ref;
        private final int size;
        final /* synthetic */ JexlEngine this$0;

        V get(K key) {
            Map<K, V> map = this.ref == null ? null : (Map) this.ref.get();
            if (map == null) {
                return null;
            }
            return map.get(key);
        }

        void put(K key, V script) {
            Map<K, V> map = null;
            if (this.ref != null) {
                map = (Map) this.ref.get();
            }
            if (map == null) {
                map = this.this$0.createCache(this.size);
                this.ref = new SoftReference(map);
            }
            map.put(key, script);
        }
    }

    private static final class UberspectHolder {
        private static final Uberspect UBERSPECT = new UberspectImpl(LogFactory.getLog(JexlEngine.class));

        private UberspectHolder() {
        }
    }

    public JexlEngine() {
        this(null, null, null, null);
    }

    public JexlEngine(Uberspect anUberspect, JexlArithmetic anArithmetic, Map<String, Object> theFunctions, Log log) {
        this.parser = new Parser(new StringReader(";"));
        this.silent = false;
        this.debug = true;
        this.functions = Collections.emptyMap();
        this.cache = null;
        if (anUberspect == null) {
            anUberspect = getUberspect(log);
        }
        this.uberspect = anUberspect;
        if (log == null) {
            log = LogFactory.getLog(JexlEngine.class);
        }
        this.logger = log;
        if (anArithmetic == null) {
            anArithmetic = new JexlArithmetic(true);
        }
        this.arithmetic = anArithmetic;
        if (theFunctions != null) {
            this.functions = theFunctions;
        }
    }

    public static Uberspect getUberspect(Log logger) {
        if (logger == null || logger.equals(LogFactory.getLog(JexlEngine.class))) {
            return UberspectHolder.UBERSPECT;
        }
        return new UberspectImpl(logger);
    }

    public boolean isSilent() {
        return this.silent;
    }

    public boolean isLenient() {
        return this.arithmetic.isLenient();
    }

    public final boolean isStrict() {
        return !isLenient();
    }

    protected Expression createExpression(ASTJexlScript tree, String text) {
        return new ExpressionImpl(this, text, tree);
    }

    public Expression createExpression(String expression) {
        return createExpression(expression, null);
    }

    public Expression createExpression(String expression, JexlInfo info) {
        ASTJexlScript tree = parse(expression, info, null);
        if (tree.jjtGetNumChildren() > 1) {
            this.logger.warn("The JEXL Expression created will be a reference to the first expression from the supplied script: \"" + expression + "\" ");
        }
        return createExpression(tree, expression);
    }

    protected Interpreter createInterpreter(JexlContext context) {
        return createInterpreter(context, isStrict(), isSilent());
    }

    protected Interpreter createInterpreter(JexlContext context, boolean strictFlag, boolean silentFlag) {
        if (context == null) {
            context = EMPTY_CONTEXT;
        }
        return new Interpreter(this, context, strictFlag, silentFlag);
    }

    protected <K, V> Map<K, V> createCache(int cacheSize) {
        final int i = cacheSize;
        return new LinkedHashMap<K, V>(cacheSize, 0.75f, true) {
            private static final long serialVersionUID = 1;

            protected boolean removeEldestEntry(Entry<K, V> entry) {
                return size() > i;
            }
        };
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected ASTJexlScript parse(CharSequence expression, JexlInfo info, Scope frame) {
        String expr = cleanExpression(expression);
        JexlInfo jexlInfo = null;
        synchronized (this.parser) {
            ASTJexlScript script;
            if (this.cache != null) {
                script = (ASTJexlScript) this.cache.get(expr);
                if (script != null) {
                    Scope f = script.getScope();
                    if (f != null || frame != null) {
                        if (f != null) {
                        }
                    }
                }
            }
            try {
                Reader reader = new StringReader(expr);
                if (info != null) {
                    jexlInfo = info.debugInfo();
                } else {
                    jexlInfo = debugInfo();
                }
                this.parser.setFrame(frame);
                script = this.parser.parse(reader, jexlInfo);
                frame = this.parser.getFrame();
                if (frame != null) {
                    script.setScope(frame);
                }
                if (this.cache != null) {
                    this.cache.put(expr, script);
                }
                this.parser.setFrame(null);
                return script;
            } catch (TokenMgrError xtme) {
                throw new Tokenization(jexlInfo, expression, xtme);
            } catch (ParseException xparse) {
                throw new Parsing(jexlInfo, expression, xparse);
            } catch (Throwable th) {
                this.parser.setFrame(null);
            }
        }
    }

    protected JexlInfo createInfo(String fn, int l, int c) {
        return new DebugInfo(fn, l, c);
    }

    protected JexlInfo debugInfo() {
        if (!this.debug) {
            return null;
        }
        Throwable xinfo = new Throwable();
        xinfo.fillInStackTrace();
        StackTraceElement[] stack = xinfo.getStackTrace();
        StackTraceElement se = null;
        Class<?> clazz = getClass();
        int s = 1;
        while (s < stack.length) {
            se = stack[s];
            String className = se.getClassName();
            if (!className.equals(clazz.getName())) {
                if (!className.equals(JexlEngine.class.getName())) {
                    if (!className.equals(UnifiedJEXL.class.getName())) {
                        break;
                    }
                    clazz = UnifiedJEXL.class;
                } else {
                    clazz = JexlEngine.class;
                }
            }
            s++;
            se = null;
        }
        if (se != null) {
            return createInfo(se.getClassName() + "." + se.getMethodName(), se.getLineNumber(), 0).debugInfo();
        }
        return null;
    }

    public static String cleanExpression(CharSequence str) {
        if (str == null) {
            return null;
        }
        int start = 0;
        int end = str.length();
        if (end <= 0) {
            return BuildConfig.FLAVOR;
        }
        while (start < end) {
            if (str.charAt(start) != ' ') {
                break;
            }
            start++;
        }
        while (end > 0 && str.charAt(end - 1) == ' ') {
            end--;
        }
        return str.subSequence(start, end).toString();
    }
}
