package org.apache.commons.jexl2;

import fyusion.vislib.BuildConfig;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import org.apache.commons.jexl2.parser.JexlNode;
import org.apache.commons.jexl2.parser.ParseException;
import org.apache.commons.jexl2.parser.TokenMgrError;

public class JexlException extends RuntimeException {
    protected final transient JexlInfo info;
    protected final transient JexlNode mark;

    protected static class Cancel extends JexlException {
        protected Cancel(JexlNode node) {
            super(node, "execution cancelled", null);
        }
    }

    public static class Method extends JexlException {
        public Method(JexlNode node, String name) {
            super(node, name);
        }

        public String getMethod() {
            return super.detailedMessage();
        }

        protected String detailedMessage() {
            return "unknown, ambiguous or inaccessible method " + getMethod();
        }
    }

    public static class Parsing extends JexlException {
        public Parsing(JexlInfo node, CharSequence expr, ParseException cause) {
            super(merge(node, cause), expr.toString(), (Throwable) cause);
        }

        private static DebugInfo merge(JexlInfo node, ParseException cause) {
            DebugInfo dbgn = null;
            if (node != null) {
                dbgn = node.debugInfo();
            }
            if (cause == null) {
                return dbgn;
            }
            if (dbgn != null) {
                return new DebugInfo(dbgn.getName(), cause.getLine(), cause.getColumn());
            }
            return new DebugInfo(BuildConfig.FLAVOR, cause.getLine(), cause.getColumn());
        }

        public String getExpression() {
            return super.detailedMessage();
        }

        protected String detailedMessage() {
            return parserError("parsing", getExpression());
        }
    }

    public static class Property extends JexlException {
        public Property(JexlNode node, String var) {
            super(node, var);
        }

        public String getProperty() {
            return super.detailedMessage();
        }

        protected String detailedMessage() {
            return "inaccessible or unknown property " + getProperty();
        }
    }

    protected static class Return extends JexlException {
        private final Object result;

        protected Return(JexlNode node, String msg, Object value) {
            super(node, msg);
            this.result = value;
        }

        public Object getValue() {
            return this.result;
        }
    }

    public static class Tokenization extends JexlException {
        public Tokenization(JexlInfo node, CharSequence expr, TokenMgrError cause) {
            super(merge(node, cause), expr.toString(), (Throwable) cause);
        }

        private static DebugInfo merge(JexlInfo node, TokenMgrError cause) {
            DebugInfo dbgn = null;
            if (node != null) {
                dbgn = node.debugInfo();
            }
            if (cause == null) {
                return dbgn;
            }
            if (dbgn != null) {
                return new DebugInfo(dbgn.getName(), cause.getLine(), cause.getColumn());
            }
            return new DebugInfo(BuildConfig.FLAVOR, cause.getLine(), cause.getColumn());
        }

        public String getExpression() {
            return super.detailedMessage();
        }

        protected String detailedMessage() {
            return parserError("tokenization", getExpression());
        }
    }

    public static class Variable extends JexlException {
        public Variable(JexlNode node, String var) {
            super(node, var);
        }

        public String getVariable() {
            return super.detailedMessage();
        }

        protected String detailedMessage() {
            return "undefined variable " + getVariable();
        }
    }

    public JexlException(JexlNode node, String msg) {
        JexlInfo jexlInfo = null;
        super(msg);
        this.mark = node;
        if (node != null) {
            jexlInfo = node.debugInfo();
        }
        this.info = jexlInfo;
    }

    public JexlException(JexlNode node, String msg, Throwable cause) {
        JexlInfo jexlInfo = null;
        super(msg, unwrap(cause));
        this.mark = node;
        if (node != null) {
            jexlInfo = node.debugInfo();
        }
        this.info = jexlInfo;
    }

    public JexlException(JexlInfo dbg, String msg, Throwable cause) {
        super(msg, unwrap(cause));
        this.mark = null;
        this.info = dbg;
    }

    private static Throwable unwrap(Throwable xthrow) {
        if (xthrow instanceof InvocationTargetException) {
            return ((InvocationTargetException) xthrow).getTargetException();
        }
        if (xthrow instanceof UndeclaredThrowableException) {
            return ((UndeclaredThrowableException) xthrow).getUndeclaredThrowable();
        }
        return xthrow;
    }

    protected String detailedMessage() {
        return super.getMessage();
    }

    protected String parserError(String prefix, String expr) {
        int begin = this.info.debugInfo().getColumn();
        int end = begin + 5;
        begin -= 5;
        if (begin < 0) {
            end += 5;
            begin = 0;
        }
        int length = expr.length();
        if (length < 10) {
            return prefix + " error in '" + expr + "'";
        }
        StringBuilder append = new StringBuilder().append(prefix).append(" error near '... ");
        if (end > length) {
            end = length;
        }
        return append.append(expr.substring(begin, end)).append(" ...'").toString();
    }

    public String getMessage() {
        Debugger dbg = new Debugger();
        StringBuilder msg = new StringBuilder();
        if (this.info != null) {
            msg.append(this.info.debugString());
        }
        if (dbg.debug(this.mark)) {
            msg.append("![");
            msg.append(dbg.start());
            msg.append(",");
            msg.append(dbg.end());
            msg.append("]: '");
            msg.append(dbg.data());
            msg.append("'");
        }
        msg.append(' ');
        msg.append(detailedMessage());
        Throwable cause = getCause();
        if (cause != null && "jexl.null" == cause.getMessage()) {
            msg.append(" caused by null operand");
        }
        return msg.toString();
    }
}
