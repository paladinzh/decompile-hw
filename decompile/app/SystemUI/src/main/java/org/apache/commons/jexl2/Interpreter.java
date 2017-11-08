package org.apache.commons.jexl2;

import fyusion.vislib.BuildConfig;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.jexl2.JexlEngine.Frame;
import org.apache.commons.jexl2.JexlException.Method;
import org.apache.commons.jexl2.JexlException.Property;
import org.apache.commons.jexl2.JexlException.Variable;
import org.apache.commons.jexl2.introspection.JexlMethod;
import org.apache.commons.jexl2.introspection.JexlPropertyGet;
import org.apache.commons.jexl2.introspection.JexlPropertySet;
import org.apache.commons.jexl2.introspection.Uberspect;
import org.apache.commons.jexl2.parser.ASTAdditiveNode;
import org.apache.commons.jexl2.parser.ASTAdditiveOperator;
import org.apache.commons.jexl2.parser.ASTAmbiguous;
import org.apache.commons.jexl2.parser.ASTAndNode;
import org.apache.commons.jexl2.parser.ASTArrayAccess;
import org.apache.commons.jexl2.parser.ASTArrayLiteral;
import org.apache.commons.jexl2.parser.ASTAssignment;
import org.apache.commons.jexl2.parser.ASTBitwiseAndNode;
import org.apache.commons.jexl2.parser.ASTBitwiseComplNode;
import org.apache.commons.jexl2.parser.ASTBitwiseOrNode;
import org.apache.commons.jexl2.parser.ASTBitwiseXorNode;
import org.apache.commons.jexl2.parser.ASTBlock;
import org.apache.commons.jexl2.parser.ASTConstructorNode;
import org.apache.commons.jexl2.parser.ASTDivNode;
import org.apache.commons.jexl2.parser.ASTEQNode;
import org.apache.commons.jexl2.parser.ASTERNode;
import org.apache.commons.jexl2.parser.ASTEmptyFunction;
import org.apache.commons.jexl2.parser.ASTFalseNode;
import org.apache.commons.jexl2.parser.ASTForeachStatement;
import org.apache.commons.jexl2.parser.ASTFunctionNode;
import org.apache.commons.jexl2.parser.ASTGENode;
import org.apache.commons.jexl2.parser.ASTGTNode;
import org.apache.commons.jexl2.parser.ASTIdentifier;
import org.apache.commons.jexl2.parser.ASTIfStatement;
import org.apache.commons.jexl2.parser.ASTJexlScript;
import org.apache.commons.jexl2.parser.ASTLENode;
import org.apache.commons.jexl2.parser.ASTLTNode;
import org.apache.commons.jexl2.parser.ASTMapEntry;
import org.apache.commons.jexl2.parser.ASTMapLiteral;
import org.apache.commons.jexl2.parser.ASTMethodNode;
import org.apache.commons.jexl2.parser.ASTModNode;
import org.apache.commons.jexl2.parser.ASTMulNode;
import org.apache.commons.jexl2.parser.ASTNENode;
import org.apache.commons.jexl2.parser.ASTNRNode;
import org.apache.commons.jexl2.parser.ASTNotNode;
import org.apache.commons.jexl2.parser.ASTNullLiteral;
import org.apache.commons.jexl2.parser.ASTNumberLiteral;
import org.apache.commons.jexl2.parser.ASTOrNode;
import org.apache.commons.jexl2.parser.ASTReference;
import org.apache.commons.jexl2.parser.ASTReferenceExpression;
import org.apache.commons.jexl2.parser.ASTReturnStatement;
import org.apache.commons.jexl2.parser.ASTSizeFunction;
import org.apache.commons.jexl2.parser.ASTSizeMethod;
import org.apache.commons.jexl2.parser.ASTStringLiteral;
import org.apache.commons.jexl2.parser.ASTTernaryNode;
import org.apache.commons.jexl2.parser.ASTTrueNode;
import org.apache.commons.jexl2.parser.ASTUnaryMinusNode;
import org.apache.commons.jexl2.parser.ASTVar;
import org.apache.commons.jexl2.parser.ASTWhileStatement;
import org.apache.commons.jexl2.parser.JexlNode;
import org.apache.commons.jexl2.parser.JexlNode.Literal;
import org.apache.commons.jexl2.parser.Node;
import org.apache.commons.jexl2.parser.ParserVisitor;
import org.apache.commons.jexl2.parser.SimpleNode;
import org.apache.commons.logging.Log;

public class Interpreter implements ParserVisitor {
    protected static final Object[] EMPTY_PARAMS = new Object[0];
    protected final JexlArithmetic arithmetic;
    protected final boolean cache;
    private volatile boolean cancelled = false;
    protected final JexlContext context;
    protected final Map<String, Object> functions;
    protected Map<String, Object> functors;
    protected final Log logger;
    private String[] parameters = null;
    protected Object[] registers = null;
    protected boolean silent;
    protected boolean strict;
    protected final Uberspect uberspect;

    public Interpreter(JexlEngine jexl, JexlContext aContext, boolean strictFlag, boolean silentFlag) {
        boolean z = false;
        this.logger = jexl.logger;
        this.uberspect = jexl.uberspect;
        this.arithmetic = jexl.arithmetic;
        this.functions = jexl.functions;
        this.strict = strictFlag;
        this.silent = silentFlag;
        if (jexl.cache != null) {
            z = true;
        }
        this.cache = z;
        if (aContext == null) {
            aContext = JexlEngine.EMPTY_CONTEXT;
        }
        this.context = aContext;
        this.functors = null;
    }

    public Object interpret(JexlNode node) {
        Object obj = null;
        try {
            obj = node.jjtAccept(this, obj);
            return obj;
        } catch (Return xreturn) {
            Object value = xreturn.getValue();
            return value;
        } catch (JexlException xjexl) {
            if (this.silent) {
                obj = this.logger;
                obj.warn(xjexl.getMessage(), xjexl.getCause());
                return null;
            }
            throw xjexl;
        } finally {
            this.functors = null;
            this.parameters = null;
            this.registers = null;
        }
    }

    protected void setFrame(Frame frame) {
        if (frame == null) {
            this.parameters = null;
            this.registers = null;
            return;
        }
        this.parameters = frame.getParameters();
        this.registers = frame.getRegisters();
    }

    protected JexlNode findNullOperand(RuntimeException xrt, JexlNode node, Object left, Object right) {
        if ((xrt instanceof ArithmeticException) && "jexl.null" == xrt.getMessage()) {
            if (left == null) {
                return node.jjtGetChild(0);
            }
            if (right == null) {
                return node.jjtGetChild(1);
            }
        }
        return node;
    }

    protected Object unknownVariable(JexlException xjexl) {
        if (this.strict) {
            throw xjexl;
        }
        if (!this.silent) {
            this.logger.warn(xjexl.getMessage());
        }
        return null;
    }

    protected Object invocationFailed(JexlException xjexl) {
        if (this.strict || (xjexl instanceof Return)) {
            throw xjexl;
        }
        if (!this.silent) {
            this.logger.warn(xjexl.getMessage(), xjexl.getCause());
        }
        return null;
    }

    protected boolean isCancelled() {
        if ((this.cancelled | Thread.interrupted()) != 0) {
            this.cancelled = true;
        }
        return this.cancelled;
    }

    protected Object resolveNamespace(String prefix, JexlNode node) {
        Object namespace = null;
        if (this.functors != null) {
            namespace = this.functors.get(prefix);
            if (namespace != null) {
                return namespace;
            }
        }
        if (this.context instanceof NamespaceResolver) {
            namespace = ((NamespaceResolver) this.context).resolveNamespace(prefix);
        }
        if (namespace == null) {
            namespace = this.functions.get(prefix);
            if (prefix != null && namespace == null) {
                throw new JexlException(node, "no such function namespace " + prefix);
            }
        }
        if (namespace instanceof Class) {
            Object[] args = new Object[]{this.context};
            JexlMethod ctor = this.uberspect.getConstructorMethod(namespace, args, node);
            if (ctor != null) {
                try {
                    namespace = ctor.invoke(namespace, args);
                    if (this.functors == null) {
                        this.functors = new HashMap();
                    }
                    this.functors.put(prefix, namespace);
                } catch (Throwable xinst) {
                    throw new JexlException(node, "unable to instantiate namespace " + prefix, xinst);
                }
            }
        }
        return namespace;
    }

    public Object visit(ASTAdditiveNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        int c = 2;
        int size = node.jjtGetNumChildren();
        while (c < size) {
            Object right = node.jjtGetChild(c).jjtAccept(this, data);
            JexlNode op = node.jjtGetChild(c - 1);
            if (op instanceof ASTAdditiveOperator) {
                try {
                    String which = op.image;
                    if ("+".equals(which)) {
                        left = this.arithmetic.add(left, right);
                    } else if ("-".equals(which)) {
                        left = this.arithmetic.subtract(left, right);
                    } else {
                        throw new UnsupportedOperationException("unknown operator " + which);
                    }
                    c += 2;
                } catch (Throwable xrt) {
                    throw new JexlException(findNullOperand(xrt, node, left, right), "+/- error", xrt);
                }
            }
            throw new IllegalArgumentException("unknown operator " + op);
        }
        return left;
    }

    public Object visit(ASTAdditiveOperator node, Object data) {
        throw new UnsupportedOperationException("Shoud not be called.");
    }

    public Object visit(ASTAndNode node, Object data) {
        try {
            if (!this.arithmetic.toBoolean(node.jjtGetChild(0).jjtAccept(this, data))) {
                return Boolean.FALSE;
            }
            try {
                if (this.arithmetic.toBoolean(node.jjtGetChild(1).jjtAccept(this, data))) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } catch (Throwable xrt) {
                throw new JexlException(node.jjtGetChild(1), "boolean coercion error", xrt);
            }
        } catch (Throwable xrt2) {
            throw new JexlException(node.jjtGetChild(0), "boolean coercion error", xrt2);
        }
    }

    public Object visit(ASTArrayAccess node, Object data) {
        Object object = node.jjtGetChild(0).jjtAccept(this, data);
        int numChildren = node.jjtGetNumChildren();
        for (int i = 1; i < numChildren; i++) {
            JexlNode nindex = node.jjtGetChild(i);
            if (nindex instanceof Literal) {
                object = nindex.jjtAccept(this, object);
            } else {
                object = getAttribute(object, nindex.jjtAccept(this, null), nindex);
            }
        }
        return object;
    }

    public Object visit(ASTArrayLiteral node, Object data) {
        Object literal = node.getLiteral();
        if (literal != null) {
            return literal;
        }
        int childCount = node.jjtGetNumChildren();
        Object[] array = new Object[childCount];
        for (int i = 0; i < childCount; i++) {
            array[i] = node.jjtGetChild(i).jjtAccept(this, data);
        }
        literal = this.arithmetic.narrowArrayType(array);
        node.setLiteral(literal);
        return literal;
    }

    public Object visit(ASTAssignment node, Object data) {
        JexlNode propertyNode;
        int register = -1;
        JexlNode left = node.jjtGetChild(0);
        if (left instanceof ASTIdentifier) {
            register = ((ASTIdentifier) left).getRegister();
            if (register < 0) {
                throw new JexlException(left, "unknown variable " + left.image);
            }
        } else if (!(left instanceof ASTReference)) {
            throw new JexlException(left, "illegal assignment form 0");
        }
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        JexlNode objectNode = null;
        Object object = register < 0 ? null : this.registers[register];
        Object property = null;
        int isVariable = 1;
        int v = 0;
        StringBuilder variableName = null;
        int last = left.jjtGetNumChildren() - 1;
        boolean isRegister = last < 0 && register >= 0;
        int c = register < 0 ? 0 : 1;
        while (c < last) {
            objectNode = left.jjtGetChild(c);
            object = objectNode.jjtAccept(this, object);
            if (object == null) {
                int i;
                StringBuilder stringBuilder;
                if (!(objectNode instanceof ASTIdentifier)) {
                    if (objectNode instanceof ASTNumberLiteral) {
                        if (!((ASTNumberLiteral) objectNode).isInteger()) {
                        }
                    }
                    i = 0;
                    isVariable &= i;
                    if (isVariable != 0) {
                        throw new JexlException(objectNode, "illegal assignment form");
                    }
                    if (v == 0) {
                        stringBuilder = new StringBuilder(left.jjtGetChild(0).image);
                        v = 1;
                    }
                    while (v <= c) {
                        variableName.append('.');
                        variableName.append(left.jjtGetChild(v).image);
                        v++;
                    }
                    object = this.context.get(variableName.toString());
                    if (object == null) {
                        isVariable = 0;
                    }
                }
                i = 1;
                isVariable &= i;
                if (isVariable != 0) {
                    if (v == 0) {
                        stringBuilder = new StringBuilder(left.jjtGetChild(0).image);
                        v = 1;
                    }
                    while (v <= c) {
                        variableName.append('.');
                        variableName.append(left.jjtGetChild(v).image);
                        v++;
                    }
                    object = this.context.get(variableName.toString());
                    if (object == null) {
                        isVariable = 0;
                    }
                } else {
                    throw new JexlException(objectNode, "illegal assignment form");
                }
            }
            c++;
        }
        if (isRegister) {
            propertyNode = null;
        } else {
            propertyNode = left.jjtGetChild(last);
        }
        boolean antVar = false;
        if (propertyNode instanceof ASTIdentifier) {
            ASTIdentifier identifier = (ASTIdentifier) propertyNode;
            register = identifier.getRegister();
            if (register < 0) {
                String property2 = identifier.image;
                antVar = true;
            } else {
                isRegister = true;
            }
        } else if ((propertyNode instanceof ASTNumberLiteral) && ((ASTNumberLiteral) propertyNode).isInteger()) {
            property = ((ASTNumberLiteral) propertyNode).getLiteral();
            antVar = true;
        } else if (propertyNode instanceof ASTArrayAccess) {
            objectNode = propertyNode;
            ASTArrayAccess narray = (ASTArrayAccess) objectNode;
            Object nobject = narray.jjtGetChild(0).jjtAccept(this, object);
            if (nobject != null) {
                object = nobject;
                last = narray.jjtGetNumChildren() - 1;
                for (int i2 = 1; i2 < last; i2++) {
                    objectNode = narray.jjtGetChild(i2);
                    if (objectNode instanceof Literal) {
                        object = objectNode.jjtAccept(this, object);
                    } else {
                        object = getAttribute(object, objectNode.jjtAccept(this, null), objectNode);
                    }
                }
                property = narray.jjtGetChild(last).jjtAccept(this, null);
            } else {
                throw new JexlException(objectNode, "array element is null");
            }
        } else if (!isRegister) {
            throw new JexlException(objectNode, "illegal assignment form");
        }
        if (isRegister) {
            this.registers[register] = right;
            return right;
        } else if (antVar && isVariable != 0 && object == null) {
            if (variableName != null) {
                if (last > 0) {
                    variableName.append('.');
                }
                variableName.append(property);
                property = variableName.toString();
            }
            try {
                this.context.set(String.valueOf(property), right);
                return right;
            } catch (Throwable xsupport) {
                throw new JexlException((JexlNode) node, "context is readonly", xsupport);
            }
        } else if (property == null) {
            throw new JexlException(propertyNode, "property is null");
        } else if (object != null) {
            setAttribute(object, property, right, propertyNode);
            return right;
        } else {
            throw new JexlException(objectNode, "bean is null");
        }
    }

    public Object visit(ASTBitwiseAndNode node, Object data) {
        try {
            return this.arithmetic.bitwiseAnd(node.jjtGetChild(0).jjtAccept(this, data), node.jjtGetChild(1).jjtAccept(this, data));
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, "& error", xrt);
        }
    }

    public Object visit(ASTBitwiseComplNode node, Object data) {
        try {
            return this.arithmetic.bitwiseComplement(node.jjtGetChild(0).jjtAccept(this, data));
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, "~ error", xrt);
        }
    }

    public Object visit(ASTBitwiseOrNode node, Object data) {
        try {
            return this.arithmetic.bitwiseOr(node.jjtGetChild(0).jjtAccept(this, data), node.jjtGetChild(1).jjtAccept(this, data));
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, "| error", xrt);
        }
    }

    public Object visit(ASTBitwiseXorNode node, Object data) {
        try {
            return this.arithmetic.bitwiseXor(node.jjtGetChild(0).jjtAccept(this, data), node.jjtGetChild(1).jjtAccept(this, data));
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, "^ error", xrt);
        }
    }

    public Object visit(ASTBlock node, Object data) {
        int numChildren = node.jjtGetNumChildren();
        Object result = null;
        for (int i = 0; i < numChildren; i++) {
            result = node.jjtGetChild(i).jjtAccept(this, data);
        }
        return result;
    }

    public Object visit(ASTDivNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return this.arithmetic.divide(left, right);
        } catch (Throwable xrt) {
            if (!this.strict) {
                return new Double(0.0d);
            }
            throw new JexlException(findNullOperand(xrt, node, left, right), "divide error", xrt);
        }
    }

    public Object visit(ASTEmptyFunction node, Object data) {
        Object o = node.jjtGetChild(0).jjtAccept(this, data);
        if (o == null) {
            return Boolean.TRUE;
        }
        if ((o instanceof String) && BuildConfig.FLAVOR.equals(o)) {
            return Boolean.TRUE;
        }
        if (o.getClass().isArray() && ((Object[]) o).length == 0) {
            return Boolean.TRUE;
        }
        if (o instanceof Collection) {
            return !((Collection) o).isEmpty() ? Boolean.FALSE : Boolean.TRUE;
        } else if (!(o instanceof Map)) {
            return Boolean.FALSE;
        } else {
            return !((Map) o).isEmpty() ? Boolean.FALSE : Boolean.TRUE;
        }
    }

    public Object visit(ASTEQNode node, Object data) {
        try {
            return !this.arithmetic.equals(node.jjtGetChild(0).jjtAccept(this, data), node.jjtGetChild(1).jjtAccept(this, data)) ? Boolean.FALSE : Boolean.TRUE;
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, "== error", xrt);
        }
    }

    public Object visit(ASTFalseNode node, Object data) {
        return Boolean.FALSE;
    }

    public Object visit(ASTForeachStatement node, Object data) {
        Object result = null;
        ASTIdentifier loopVariable = (ASTIdentifier) ((ASTReference) node.jjtGetChild(0)).jjtGetChild(0);
        int register = loopVariable.getRegister();
        Object iterableValue = node.jjtGetChild(1).jjtAccept(this, data);
        if (iterableValue != null && node.jjtGetNumChildren() >= 3) {
            JexlNode statement = node.jjtGetChild(2);
            Iterator<?> itemsIterator = this.uberspect.getIterator(iterableValue, node);
            if (itemsIterator != null) {
                while (itemsIterator.hasNext()) {
                    if (isCancelled()) {
                        throw new Cancel(node);
                    }
                    Object value = itemsIterator.next();
                    if (register >= 0) {
                        this.registers[register] = value;
                    } else {
                        this.context.set(loopVariable.image, value);
                    }
                    result = statement.jjtAccept(this, data);
                }
            }
        }
        return result;
    }

    public Object visit(ASTGENode node, Object data) {
        try {
            return !this.arithmetic.greaterThanOrEqual(node.jjtGetChild(0).jjtAccept(this, data), node.jjtGetChild(1).jjtAccept(this, data)) ? Boolean.FALSE : Boolean.TRUE;
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, ">= error", xrt);
        }
    }

    public Object visit(ASTGTNode node, Object data) {
        try {
            return !this.arithmetic.greaterThan(node.jjtGetChild(0).jjtAccept(this, data), node.jjtGetChild(1).jjtAccept(this, data)) ? Boolean.FALSE : Boolean.TRUE;
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, "> error", xrt);
        }
    }

    public Object visit(ASTERNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            if ((right instanceof Pattern) || (right instanceof String)) {
                return !this.arithmetic.matches(left, right) ? Boolean.FALSE : Boolean.TRUE;
            } else if (right instanceof Set) {
                return !((Set) right).contains(left) ? Boolean.FALSE : Boolean.TRUE;
            } else if (right instanceof Map) {
                return !((Map) right).containsKey(left) ? Boolean.FALSE : Boolean.TRUE;
            } else if (right instanceof Collection) {
                return !((Collection) right).contains(left) ? Boolean.FALSE : Boolean.TRUE;
            } else {
                Object[] argv = new Object[]{left};
                JexlMethod vm = this.uberspect.getMethod(right, "contains", argv, node);
                if (vm == null) {
                    if (this.arithmetic.narrowArguments(argv)) {
                        vm = this.uberspect.getMethod(right, "contains", argv, node);
                        if (vm != null) {
                            return !this.arithmetic.toBoolean(vm.invoke(right, argv)) ? Boolean.FALSE : Boolean.TRUE;
                        }
                    }
                    Iterator<?> it = this.uberspect.getIterator(right, node);
                    if (it == null) {
                        Object obj;
                        if (this.arithmetic.equals(left, right)) {
                            obj = Boolean.TRUE;
                        } else {
                            obj = Boolean.FALSE;
                        }
                        return obj;
                    }
                    while (it.hasNext()) {
                        Object next = it.next();
                        if (next != left) {
                            if (next != null) {
                                if (next.equals(left)) {
                                }
                            }
                        }
                        return Boolean.TRUE;
                    }
                    return Boolean.FALSE;
                }
                return !this.arithmetic.toBoolean(vm.invoke(right, argv)) ? Boolean.FALSE : Boolean.TRUE;
            }
        } catch (InvocationTargetException e) {
            throw new JexlException((JexlNode) node, "=~ invocation error", e.getCause());
        } catch (Throwable e2) {
            throw new JexlException((JexlNode) node, "=~ error", e2);
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, "=~ error", xrt);
        }
    }

    public Object visit(ASTIdentifier node, Object data) {
        if (isCancelled()) {
            throw new Cancel(node);
        }
        String name = node.image;
        if (data != null) {
            return getAttribute(data, name, node);
        }
        int register = node.getRegister();
        if (register >= 0) {
            return this.registers[register];
        }
        Object value = this.context.get(name);
        if (value != null || (node.jjtGetParent() instanceof ASTReference) || this.context.has(name) || isTernaryProtected(node)) {
            return value;
        }
        return unknownVariable(new Variable(node, name));
    }

    public Object visit(ASTVar node, Object data) {
        return visit((ASTIdentifier) node, data);
    }

    public Object visit(ASTIfStatement node, Object data) {
        try {
            if (this.arithmetic.toBoolean(node.jjtGetChild(0).jjtAccept(this, data))) {
                return node.jjtGetChild(1).jjtAccept(this, data);
            }
            if (node.jjtGetNumChildren() != 3) {
                return null;
            }
            return node.jjtGetChild(2).jjtAccept(this, data);
        } catch (JexlException error) {
            throw error;
        } catch (Throwable xrt) {
            throw new JexlException(node.jjtGetChild(0), "if error", xrt);
        }
    }

    public Object visit(ASTNumberLiteral node, Object data) {
        if (data != null && node.isInteger()) {
            return getAttribute(data, node.getLiteral(), node);
        }
        return node.getLiteral();
    }

    public Object visit(ASTJexlScript node, Object data) {
        int numChildren = node.jjtGetNumChildren();
        Object result = null;
        for (int i = 0; i < numChildren; i++) {
            result = node.jjtGetChild(i).jjtAccept(this, data);
        }
        return result;
    }

    public Object visit(ASTLENode node, Object data) {
        try {
            return !this.arithmetic.lessThanOrEqual(node.jjtGetChild(0).jjtAccept(this, data), node.jjtGetChild(1).jjtAccept(this, data)) ? Boolean.FALSE : Boolean.TRUE;
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, "<= error", xrt);
        }
    }

    public Object visit(ASTLTNode node, Object data) {
        try {
            return !this.arithmetic.lessThan(node.jjtGetChild(0).jjtAccept(this, data), node.jjtGetChild(1).jjtAccept(this, data)) ? Boolean.FALSE : Boolean.TRUE;
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, "< error", xrt);
        }
    }

    public Object visit(ASTMapEntry node, Object data) {
        Object key = node.jjtGetChild(0).jjtAccept(this, data);
        Object value = node.jjtGetChild(1).jjtAccept(this, data);
        return new Object[]{key, value};
    }

    public Object visit(ASTMapLiteral node, Object data) {
        int childCount = node.jjtGetNumChildren();
        Map<Object, Object> map = new HashMap();
        for (int i = 0; i < childCount; i++) {
            Object[] entry = (Object[]) node.jjtGetChild(i).jjtAccept(this, data);
            map.put(entry[0], entry[1]);
        }
        return map;
    }

    private Object call(JexlNode node, Object bean, ASTIdentifier methodNode, int argb) {
        JexlException jexlException;
        if (isCancelled()) {
            throw new Cancel(node);
        }
        String methodName = methodNode.image;
        int argc = node.jjtGetNumChildren() - argb;
        Object[] argv = new Object[argc];
        for (int i = 0; i < argc; i++) {
            argv[i] = node.jjtGetChild(i + argb).jjtAccept(this, null);
        }
        JexlException xjexl = null;
        try {
            Object eval;
            if (this.cache) {
                Object cached = node.jjtGetValue();
                if (cached instanceof JexlMethod) {
                    JexlMethod me = (JexlMethod) cached;
                    eval = me.tryInvoke(methodName, bean, argv);
                    if (!me.tryFailed(eval)) {
                        return eval;
                    }
                }
            }
            boolean cacheable = this.cache;
            JexlMethod vm = this.uberspect.getMethod(bean, methodName, argv, node);
            if (vm == null) {
                if (this.arithmetic.narrowArguments(argv)) {
                    vm = this.uberspect.getMethod(bean, methodName, argv, node);
                }
                if (vm == null) {
                    Object functor = null;
                    if (bean != this.context) {
                        JexlPropertyGet gfunctor = this.uberspect.getPropertyGet(bean, methodName, node);
                        if (gfunctor != null) {
                            functor = gfunctor.tryInvoke(bean, methodName);
                        }
                    } else {
                        int register = methodNode.getRegister();
                        if (register < 0) {
                            functor = this.context.get(methodName);
                        } else {
                            functor = this.registers[register];
                        }
                    }
                    if (functor instanceof Script) {
                        Script script = (Script) functor;
                        JexlContext jexlContext = this.context;
                        if (argv.length <= 0) {
                            argv = null;
                        }
                        return script.execute(jexlContext, argv);
                    } else if (functor instanceof JexlMethod) {
                        vm = (JexlMethod) functor;
                        cacheable = false;
                    } else {
                        xjexl = new Method(node, methodName);
                    }
                }
            }
            if (xjexl == null) {
                eval = vm.invoke(bean, argv);
                if (cacheable && vm.isCacheable()) {
                    node.jjtSetValue(vm);
                }
                return eval;
            }
        } catch (InvocationTargetException e) {
            jexlException = new JexlException(node, "method invocation error", e.getCause());
        } catch (Throwable e2) {
            jexlException = new JexlException(node, "method error", e2);
        }
        return invocationFailed(xjexl);
    }

    public Object visit(ASTMethodNode node, Object data) {
        if (data == null) {
            if (node.jjtGetParent().jjtGetChild(0) != node) {
                throw new JexlException(node, "attempting to call method on null");
            }
            data = resolveNamespace(null, node);
            if (data == null) {
                data = this.context;
            }
        }
        return call(node, data, (ASTIdentifier) node.jjtGetChild(0), 1);
    }

    public Object visit(ASTFunctionNode node, Object data) {
        return call(node, resolveNamespace(node.jjtGetChild(0).image, node), (ASTIdentifier) node.jjtGetChild(1), 2);
    }

    public Object visit(ASTConstructorNode node, Object data) {
        if (isCancelled()) {
            throw new Cancel(node);
        }
        Object cobject = node.jjtGetChild(0).jjtAccept(this, data);
        int argc = node.jjtGetNumChildren() - 1;
        Object[] argv = new Object[argc];
        for (int i = 0; i < argc; i++) {
            argv[i] = node.jjtGetChild(i + 1).jjtAccept(this, null);
        }
        JexlException xjexl = null;
        try {
            if (this.cache) {
                Object cached = node.jjtGetValue();
                if (cached instanceof JexlMethod) {
                    JexlMethod mctor = (JexlMethod) cached;
                    Object eval = mctor.tryInvoke(null, cobject, argv);
                    if (!mctor.tryFailed(eval)) {
                        return eval;
                    }
                }
            }
            JexlMethod ctor = this.uberspect.getConstructorMethod(cobject, argv, node);
            if (ctor == null) {
                if (this.arithmetic.narrowArguments(argv)) {
                    ctor = this.uberspect.getConstructorMethod(cobject, argv, node);
                }
                if (ctor == null) {
                    xjexl = new Method(node, cobject.toString());
                }
            }
            if (xjexl == null) {
                Object instance = ctor.invoke(cobject, argv);
                if (this.cache && ctor.isCacheable()) {
                    node.jjtSetValue(ctor);
                }
                return instance;
            }
        } catch (InvocationTargetException e) {
            xjexl = new JexlException((JexlNode) node, "constructor invocation error", e.getCause());
        } catch (Throwable e2) {
            xjexl = new JexlException((JexlNode) node, "constructor error", e2);
        }
        return invocationFailed(xjexl);
    }

    public Object visit(ASTModNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return this.arithmetic.mod(left, right);
        } catch (Throwable xrt) {
            if (!this.strict) {
                return new Double(0.0d);
            }
            throw new JexlException(findNullOperand(xrt, node, left, right), "% error", xrt);
        }
    }

    public Object visit(ASTMulNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return this.arithmetic.multiply(left, right);
        } catch (Throwable xrt) {
            throw new JexlException(findNullOperand(xrt, node, left, right), "* error", xrt);
        }
    }

    public Object visit(ASTNENode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            return !this.arithmetic.equals(left, right) ? Boolean.TRUE : Boolean.FALSE;
        } catch (Throwable xrt) {
            throw new JexlException(findNullOperand(xrt, node, left, right), "!= error", xrt);
        }
    }

    public Object visit(ASTNRNode node, Object data) {
        Object left = node.jjtGetChild(0).jjtAccept(this, data);
        Object right = node.jjtGetChild(1).jjtAccept(this, data);
        try {
            if ((right instanceof Pattern) || (right instanceof String)) {
                return !this.arithmetic.matches(left, right) ? Boolean.TRUE : Boolean.FALSE;
            } else if (right instanceof Set) {
                return !((Set) right).contains(left) ? Boolean.TRUE : Boolean.FALSE;
            } else if (right instanceof Map) {
                return !((Map) right).containsKey(left) ? Boolean.TRUE : Boolean.FALSE;
            } else if (right instanceof Collection) {
                return !((Collection) right).contains(left) ? Boolean.TRUE : Boolean.FALSE;
            } else {
                Object[] argv = new Object[]{left};
                JexlMethod vm = this.uberspect.getMethod(right, "contains", argv, node);
                if (vm == null) {
                    if (this.arithmetic.narrowArguments(argv)) {
                        vm = this.uberspect.getMethod(right, "contains", argv, node);
                        if (vm != null) {
                            return !this.arithmetic.toBoolean(vm.invoke(right, argv)) ? Boolean.TRUE : Boolean.FALSE;
                        }
                    }
                    Iterator<?> it = this.uberspect.getIterator(right, node.jjtGetChild(1));
                    if (it == null) {
                        Object obj;
                        if (this.arithmetic.equals(left, right)) {
                            obj = Boolean.FALSE;
                        } else {
                            obj = Boolean.TRUE;
                        }
                        return obj;
                    }
                    while (it.hasNext()) {
                        Object next = it.next();
                        if (next != left) {
                            if (next != null) {
                                if (next.equals(left)) {
                                }
                            }
                        }
                        return Boolean.FALSE;
                    }
                    return Boolean.TRUE;
                }
                return !this.arithmetic.toBoolean(vm.invoke(right, argv)) ? Boolean.TRUE : Boolean.FALSE;
            }
        } catch (InvocationTargetException e) {
            throw new JexlException((JexlNode) node, "!~ invocation error", e.getCause());
        } catch (Throwable e2) {
            throw new JexlException((JexlNode) node, "!~ error", e2);
        } catch (Throwable xrt) {
            throw new JexlException((JexlNode) node, "!~ error", xrt);
        }
    }

    public Object visit(ASTNotNode node, Object data) {
        return !this.arithmetic.toBoolean(node.jjtGetChild(0).jjtAccept(this, data)) ? Boolean.TRUE : Boolean.FALSE;
    }

    public Object visit(ASTNullLiteral node, Object data) {
        return null;
    }

    public Object visit(ASTOrNode node, Object data) {
        try {
            if (this.arithmetic.toBoolean(node.jjtGetChild(0).jjtAccept(this, data))) {
                return Boolean.TRUE;
            }
            try {
                if (this.arithmetic.toBoolean(node.jjtGetChild(1).jjtAccept(this, data))) {
                    return Boolean.TRUE;
                }
                return Boolean.FALSE;
            } catch (Throwable xrt) {
                throw new JexlException(node.jjtGetChild(1), "boolean coercion error", xrt);
            }
        } catch (Throwable xrt2) {
            throw new JexlException(node.jjtGetChild(0), "boolean coercion error", xrt2);
        }
    }

    public Object visit(ASTReference node, Object data) {
        int numChildren = node.jjtGetNumChildren();
        Object result = null;
        StringBuilder variableName = null;
        String propertyName = null;
        boolean isVariable = true;
        int v = 0;
        for (int c = 0; c < numChildren; c++) {
            if (isCancelled()) {
                throw new Cancel(node);
            }
            JexlNode theNode = node.jjtGetChild(c);
            if (result == null && (theNode instanceof ASTNumberLiteral) && ((ASTNumberLiteral) theNode).isInteger()) {
                isVariable &= v <= 0 ? 0 : 1;
            } else {
                isVariable &= theNode instanceof ASTIdentifier;
                result = theNode.jjtAccept(this, result);
            }
            if (result == null && isVariable) {
                if (v == 0) {
                    variableName = new StringBuilder(node.jjtGetChild(0).image);
                    v = 1;
                }
                while (v <= c) {
                    variableName.append('.');
                    variableName.append(node.jjtGetChild(v).image);
                    v++;
                }
                result = this.context.get(variableName.toString());
            } else {
                propertyName = theNode.image;
            }
        }
        if (result == null && isVariable && !isTernaryProtected(node) && !this.context.has(variableName.toString())) {
            if (numChildren != 1 || !(node.jjtGetChild(0) instanceof ASTIdentifier) || ((ASTIdentifier) node.jjtGetChild(0)).getRegister() < 0) {
                return unknownVariable(propertyName == null ? new Variable(node, variableName.toString()) : new Property(node, propertyName));
            }
        }
        return result;
    }

    public Object visit(ASTReferenceExpression node, Object data) {
        return visit((ASTArrayAccess) node, data);
    }

    public Object visit(ASTReturnStatement node, Object data) {
        throw new Return(node, null, node.jjtGetChild(0).jjtAccept(this, data));
    }

    private boolean isTernaryProtected(JexlNode node) {
        for (JexlNode walk = node.jjtGetParent(); walk != null; walk = walk.jjtGetParent()) {
            if (walk instanceof ASTTernaryNode) {
                return true;
            }
            if (!(walk instanceof ASTReference)) {
                if (!(walk instanceof ASTArrayAccess)) {
                    break;
                }
            }
        }
        return false;
    }

    public Object visit(ASTSizeFunction node, Object data) {
        Object val = node.jjtGetChild(0).jjtAccept(this, data);
        if (val != null) {
            return Integer.valueOf(sizeOf(node, val));
        }
        throw new JexlException((JexlNode) node, "size() : argument is null", null);
    }

    public Object visit(ASTSizeMethod node, Object data) {
        return Integer.valueOf(sizeOf(node, data));
    }

    public Object visit(ASTStringLiteral node, Object data) {
        if (data == null) {
            return node.image;
        }
        return getAttribute(data, node.getLiteral(), node);
    }

    public Object visit(ASTTernaryNode node, Object data) {
        Object condition = node.jjtGetChild(0).jjtAccept(this, data);
        if (node.jjtGetNumChildren() != 3) {
            if (condition != null && this.arithmetic.toBoolean(condition)) {
                return condition;
            }
            return node.jjtGetChild(1).jjtAccept(this, data);
        } else if (condition != null && this.arithmetic.toBoolean(condition)) {
            return node.jjtGetChild(1).jjtAccept(this, data);
        } else {
            return node.jjtGetChild(2).jjtAccept(this, data);
        }
    }

    public Object visit(ASTTrueNode node, Object data) {
        return Boolean.TRUE;
    }

    public Object visit(ASTUnaryMinusNode node, Object data) {
        JexlNode valNode = node.jjtGetChild(0);
        try {
            Object number = this.arithmetic.negate(valNode.jjtAccept(this, data));
            if ((valNode instanceof ASTNumberLiteral) && (number instanceof Number)) {
                number = this.arithmetic.narrowNumber((Number) number, ((ASTNumberLiteral) valNode).getLiteralClass());
            }
            return number;
        } catch (Throwable xrt) {
            throw new JexlException(valNode, "arithmetic error", xrt);
        }
    }

    public Object visit(ASTWhileStatement node, Object data) {
        Object result = null;
        Node expressionNode = node.jjtGetChild(0);
        while (this.arithmetic.toBoolean(expressionNode.jjtAccept(this, data))) {
            if (isCancelled()) {
                throw new Cancel(node);
            } else if (node.jjtGetNumChildren() > 1) {
                result = node.jjtGetChild(1).jjtAccept(this, data);
            }
        }
        return result;
    }

    private int sizeOf(JexlNode node, Object val) {
        if (val instanceof Collection) {
            return ((Collection) val).size();
        }
        if (val.getClass().isArray()) {
            return Array.getLength(val);
        }
        if (val instanceof Map) {
            return ((Map) val).size();
        }
        if (val instanceof String) {
            return ((String) val).length();
        }
        Object[] params = new Object[0];
        JexlMethod vm = this.uberspect.getMethod(val, "size", EMPTY_PARAMS, node);
        if (vm != null && vm.getReturnType() == Integer.TYPE) {
            try {
                return ((Integer) vm.invoke(val, params)).intValue();
            } catch (Throwable e) {
                throw new JexlException(node, "size() : error executing", e);
            }
        }
        throw new JexlException(node, "size() : unsupported type : " + val.getClass(), null);
    }

    protected Object getAttribute(Object object, Object attribute, JexlNode node) {
        if (object == null) {
            throw new JexlException(node, "object is null");
        } else if (isCancelled()) {
            throw new Cancel(node);
        } else {
            JexlPropertyGet vg;
            Object value;
            if (node != null && this.cache) {
                Object cached = node.jjtGetValue();
                if (cached instanceof JexlPropertyGet) {
                    vg = (JexlPropertyGet) cached;
                    value = vg.tryInvoke(object, attribute);
                    if (!vg.tryFailed(value)) {
                        return value;
                    }
                }
            }
            vg = this.uberspect.getPropertyGet(object, attribute, node);
            if (vg != null) {
                try {
                    value = vg.invoke(object);
                    if (node != null && this.cache && vg.isCacheable()) {
                        node.jjtSetValue(vg);
                    }
                    return value;
                } catch (Exception xany) {
                    if (node != null) {
                        JexlException xjexl = new Property(node, attribute.toString());
                        if (this.strict) {
                            throw xjexl;
                        } else if (!this.silent) {
                            this.logger.warn(xjexl.getMessage());
                        }
                    } else {
                        throw new RuntimeException(xany);
                    }
                }
            }
            return null;
        }
    }

    protected void setAttribute(Object object, Object attribute, Object value, JexlNode node) {
        if (isCancelled()) {
            throw new Cancel(node);
        }
        if (node != null && this.cache) {
            JexlPropertySet cached = node.jjtGetValue();
            if (cached instanceof JexlPropertySet) {
                JexlPropertySet setter = cached;
                if (!setter.tryFailed(setter.tryInvoke(object, attribute, value))) {
                    return;
                }
            }
        }
        JexlException xjexl = null;
        JexlPropertySet vs = this.uberspect.getPropertySet(object, attribute, value, node);
        if (vs == null) {
            Object[] narrow = new Object[]{value};
            if (this.arithmetic.narrowArguments(narrow)) {
                vs = this.uberspect.getPropertySet(object, attribute, narrow[0], node);
            }
        }
        if (vs != null) {
            try {
                vs.invoke(object, value);
                if (node != null && this.cache && vs.isCacheable()) {
                    node.jjtSetValue(vs);
                }
                return;
            } catch (Throwable xrt) {
                if (node != null) {
                    xjexl = new JexlException(node, "set object property error", xrt);
                } else {
                    throw xrt;
                }
            } catch (Throwable xany) {
                if (node != null) {
                    xjexl = new JexlException(node, "set object property error", xany);
                } else {
                    throw new RuntimeException(xany);
                }
            }
        }
        if (xjexl == null) {
            if (node != null) {
                xjexl = new Property(node, attribute.toString());
            } else {
                throw new UnsupportedOperationException("unable to set object property, class: " + object.getClass().getName() + ", property: " + attribute + ", argument: " + value.getClass().getSimpleName());
            }
        }
        if (this.strict) {
            throw xjexl;
        }
        if (!this.silent) {
            this.logger.warn(xjexl.getMessage());
        }
    }

    public Object visit(SimpleNode node, Object data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object visit(ASTAmbiguous node, Object data) {
        throw new UnsupportedOperationException("unexpected type of node");
    }
}
