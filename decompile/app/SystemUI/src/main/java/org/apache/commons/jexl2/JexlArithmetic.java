package org.apache.commons.jexl2;

import fyusion.vislib.BuildConfig;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.regex.Pattern;

public class JexlArithmetic {
    protected static final BigDecimal BIGD_DOUBLE_MAX_VALUE = BigDecimal.valueOf(Double.MAX_VALUE);
    protected static final BigDecimal BIGD_DOUBLE_MIN_VALUE = BigDecimal.valueOf(Double.MIN_VALUE);
    protected static final BigInteger BIGI_LONG_MAX_VALUE = BigInteger.valueOf(Long.MAX_VALUE);
    protected static final BigInteger BIGI_LONG_MIN_VALUE = BigInteger.valueOf(Long.MIN_VALUE);
    protected final MathContext mathContext;
    protected final int mathScale;
    private volatile boolean strict;

    public JexlArithmetic(boolean lenient) {
        this(lenient, MathContext.DECIMAL128, -1);
    }

    public JexlArithmetic(boolean lenient, MathContext bigdContext, int bigdScale) {
        boolean z = false;
        if (!lenient) {
            z = true;
        }
        this.strict = z;
        this.mathContext = bigdContext;
        this.mathScale = bigdScale;
    }

    public boolean isLenient() {
        return !this.strict;
    }

    public MathContext getMathContext() {
        return this.mathContext;
    }

    public int getMathScale() {
        return this.mathScale;
    }

    public BigDecimal roundBigDecimal(BigDecimal number) {
        int mscale = getMathScale();
        if (mscale < 0) {
            return number;
        }
        return number.setScale(mscale, getMathContext().getRoundingMode());
    }

    protected Object controlNullNullOperands() {
        if (isLenient()) {
            return Integer.valueOf(0);
        }
        throw new ArithmeticException("jexl.null");
    }

    protected void controlNullOperand() {
        if (!isLenient()) {
            throw new ArithmeticException("jexl.null");
        }
    }

    protected boolean isFloatingPointNumber(Object val) {
        boolean z = false;
        if ((val instanceof Float) || (val instanceof Double)) {
            return true;
        }
        if (!(val instanceof String)) {
            return false;
        }
        String string = (String) val;
        if (string.indexOf(46) != -1 || string.indexOf(101) != -1 || string.indexOf(69) != -1) {
            z = true;
        }
        return z;
    }

    protected boolean isFloatingPoint(Object o) {
        return (o instanceof Float) || (o instanceof Double);
    }

    protected boolean isNumberable(Object o) {
        return (o instanceof Integer) || (o instanceof Long) || (o instanceof Byte) || (o instanceof Short) || (o instanceof Character);
    }

    protected Number narrowBigInteger(Object lhs, Object rhs, BigInteger bigi) {
        Object obj = 1;
        if ((lhs instanceof BigInteger) || (rhs instanceof BigInteger) || bigi.compareTo(BIGI_LONG_MAX_VALUE) > 0 || bigi.compareTo(BIGI_LONG_MIN_VALUE) < 0) {
            return bigi;
        }
        long l = bigi.longValue();
        if (!((lhs instanceof Long) || (rhs instanceof Long))) {
            Object obj2;
            if (l > 2147483647L) {
                obj2 = 1;
            } else {
                obj2 = null;
            }
            if (obj2 == null) {
                if (l >= -2147483648L) {
                    obj = null;
                }
                if (obj == null) {
                    return Integer.valueOf((int) l);
                }
            }
        }
        return Long.valueOf(l);
    }

    protected Number narrowBigDecimal(Object lhs, Object rhs, BigDecimal bigd) {
        Object obj = 1;
        if (isNumberable(lhs) || isNumberable(rhs)) {
            try {
                Object obj2;
                long l = bigd.longValueExact();
                if (l > 2147483647L) {
                    obj2 = 1;
                } else {
                    obj2 = null;
                }
                if (obj2 == null) {
                    if (l >= -2147483648L) {
                        obj = null;
                    }
                    if (obj == null) {
                        return Integer.valueOf((int) l);
                    }
                }
                return Long.valueOf(l);
            } catch (ArithmeticException e) {
            }
        }
        return bigd;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected Object narrowArrayType(Object[] untyped) {
        int size = untyped.length;
        Class commonClass = null;
        if (size > 0) {
            boolean isNumber = true;
            for (int u = 0; u < size && !Object.class.equals(commonClass); u++) {
                if (untyped[u] == null) {
                    isNumber = false;
                } else {
                    Class<?> eclass = untyped[u].getClass();
                    if (commonClass != null) {
                        if (!commonClass.equals(eclass)) {
                            if (!isNumber || !Number.class.isAssignableFrom(eclass)) {
                                while (true) {
                                    eclass = eclass.getSuperclass();
                                    if (eclass != null) {
                                        if (commonClass.isAssignableFrom(eclass)) {
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                }
                            }
                            commonClass = Number.class;
                        }
                    } else {
                        Class<?> commonClass2 = eclass;
                        isNumber &= Number.class.isAssignableFrom(commonClass2);
                    }
                }
            }
            if (!(commonClass == null || Object.class.equals(commonClass))) {
                if (isNumber) {
                    try {
                        commonClass = (Class) commonClass.getField("TYPE").get(null);
                    } catch (Exception e) {
                    }
                }
                Object typed = Array.newInstance(commonClass, size);
                for (int i = 0; i < size; i++) {
                    Array.set(typed, i, untyped[i]);
                }
                return typed;
            }
        }
        return untyped;
    }

    protected boolean narrowArguments(Object[] args) {
        boolean narrowed = false;
        for (int a = 0; a < args.length; a++) {
            Object arg = args[a];
            if (arg instanceof Number) {
                Object narg = narrow((Number) arg);
                if (narg != arg) {
                    narrowed = true;
                }
                args[a] = narg;
            }
        }
        return narrowed;
    }

    public Object add(Object left, Object right) {
        if (left == null && right == null) {
            return controlNullNullOperands();
        }
        try {
            if (!isFloatingPointNumber(left)) {
                if (!isFloatingPointNumber(right)) {
                    if ((left instanceof BigDecimal) || (right instanceof BigDecimal)) {
                        return narrowBigDecimal(left, right, toBigDecimal(left).add(toBigDecimal(right), getMathContext()));
                    }
                    return narrowBigInteger(left, right, toBigInteger(left).add(toBigInteger(right)));
                }
            }
            return new Double(toDouble(left) + toDouble(right));
        } catch (NumberFormatException e) {
            return toString(left).concat(toString(right));
        }
    }

    public Object divide(Object left, Object right) {
        if (left == null && right == null) {
            return controlNullNullOperands();
        }
        if (isFloatingPointNumber(left) || isFloatingPointNumber(right)) {
            double l = toDouble(left);
            double r = toDouble(right);
            if (r != 0.0d) {
                return new Double(l / r);
            }
            throw new ArithmeticException("/");
        } else if ((left instanceof BigDecimal) || (right instanceof BigDecimal)) {
            BigDecimal l2 = toBigDecimal(left);
            BigDecimal r2 = toBigDecimal(right);
            if (BigDecimal.ZERO.equals(r2)) {
                throw new ArithmeticException("/");
            }
            return narrowBigDecimal(left, right, l2.divide(r2, getMathContext()));
        } else {
            BigInteger l3 = toBigInteger(left);
            BigInteger r3 = toBigInteger(right);
            if (BigInteger.ZERO.equals(r3)) {
                throw new ArithmeticException("/");
            }
            return narrowBigInteger(left, right, l3.divide(r3));
        }
    }

    public Object mod(Object left, Object right) {
        if (left == null && right == null) {
            return controlNullNullOperands();
        }
        if (isFloatingPointNumber(left) || isFloatingPointNumber(right)) {
            double l = toDouble(left);
            double r = toDouble(right);
            if (r != 0.0d) {
                return new Double(l % r);
            }
            throw new ArithmeticException("%");
        } else if ((left instanceof BigDecimal) || (right instanceof BigDecimal)) {
            BigDecimal l2 = toBigDecimal(left);
            BigDecimal r2 = toBigDecimal(right);
            if (BigDecimal.ZERO.equals(r2)) {
                throw new ArithmeticException("%");
            }
            return narrowBigDecimal(left, right, l2.remainder(r2, getMathContext()));
        } else {
            BigInteger l3 = toBigInteger(left);
            BigInteger r3 = toBigInteger(right);
            BigInteger result = l3.mod(r3);
            if (!BigInteger.ZERO.equals(r3)) {
                return narrowBigInteger(left, right, result);
            }
            throw new ArithmeticException("%");
        }
    }

    public Object multiply(Object left, Object right) {
        if (left == null && right == null) {
            return controlNullNullOperands();
        }
        if (isFloatingPointNumber(left) || isFloatingPointNumber(right)) {
            return new Double(toDouble(left) * toDouble(right));
        }
        if ((left instanceof BigDecimal) || (right instanceof BigDecimal)) {
            return narrowBigDecimal(left, right, toBigDecimal(left).multiply(toBigDecimal(right), getMathContext()));
        }
        return narrowBigInteger(left, right, toBigInteger(left).multiply(toBigInteger(right)));
    }

    public Object subtract(Object left, Object right) {
        if (left == null && right == null) {
            return controlNullNullOperands();
        }
        if (isFloatingPointNumber(left) || isFloatingPointNumber(right)) {
            return new Double(toDouble(left) - toDouble(right));
        }
        if ((left instanceof BigDecimal) || (right instanceof BigDecimal)) {
            return narrowBigDecimal(left, right, toBigDecimal(left).subtract(toBigDecimal(right), getMathContext()));
        }
        return narrowBigInteger(left, right, toBigInteger(left).subtract(toBigInteger(right)));
    }

    public Object negate(Object val) {
        if (val instanceof Integer) {
            return Integer.valueOf(-((Integer) val).intValue());
        }
        if (val instanceof Double) {
            return new Double(-((Double) val).doubleValue());
        }
        if (val instanceof Long) {
            return Long.valueOf(-((Long) val).longValue());
        }
        if (val instanceof BigDecimal) {
            return ((BigDecimal) val).negate();
        }
        if (val instanceof BigInteger) {
            return ((BigInteger) val).negate();
        }
        if (val instanceof Float) {
            return new Float(-((Float) val).floatValue());
        }
        if (val instanceof Short) {
            return Short.valueOf((short) (-((Short) val).shortValue()));
        }
        if (val instanceof Byte) {
            return Byte.valueOf((byte) (-((Byte) val).byteValue()));
        }
        if (val instanceof Boolean) {
            return !((Boolean) val).booleanValue() ? Boolean.TRUE : Boolean.FALSE;
        }
        throw new ArithmeticException("Object negation:(" + val + ")");
    }

    public boolean matches(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        String arg = left.toString();
        if (right instanceof Pattern) {
            return ((Pattern) right).matcher(arg).matches();
        }
        return arg.matches(right.toString());
    }

    public Object bitwiseAnd(Object left, Object right) {
        return Long.valueOf(toLong(left) & toLong(right));
    }

    public Object bitwiseOr(Object left, Object right) {
        return Long.valueOf(toLong(left) | toLong(right));
    }

    public Object bitwiseXor(Object left, Object right) {
        return Long.valueOf(toLong(left) ^ toLong(right));
    }

    public Object bitwiseComplement(Object val) {
        return Long.valueOf(-1 ^ toLong(val));
    }

    protected int compare(Object left, Object right, String operator) {
        if (!(left == null || right == null)) {
            if ((left instanceof BigDecimal) || (right instanceof BigDecimal)) {
                return toBigDecimal(left).compareTo(toBigDecimal(right));
            }
            if ((left instanceof BigInteger) || (right instanceof BigInteger)) {
                return toBigInteger(left).compareTo(toBigInteger(right));
            }
            if (isFloatingPoint(left) || isFloatingPoint(right)) {
                double lhs = toDouble(left);
                double rhs = toDouble(right);
                if (Double.isNaN(lhs)) {
                    if (Double.isNaN(rhs)) {
                        return 0;
                    }
                    return -1;
                } else if (Double.isNaN(rhs)) {
                    return 1;
                } else {
                    if (lhs < rhs) {
                        return -1;
                    }
                    if (lhs > rhs) {
                        return 1;
                    }
                    return 0;
                }
            } else if (isNumberable(left) || isNumberable(right)) {
                long lhs2 = toLong(left);
                long rhs2 = toLong(right);
                if ((lhs2 >= rhs2 ? 1 : null) == null) {
                    return -1;
                }
                return ((lhs2 > rhs2 ? 1 : (lhs2 == rhs2 ? 0 : -1)) <= 0 ? 1 : null) == null ? 1 : 0;
            } else if ((left instanceof String) || (right instanceof String)) {
                return toString(left).compareTo(toString(right));
            } else {
                if ("==".equals(operator)) {
                    return !left.equals(right) ? -1 : 0;
                } else if (left instanceof Comparable) {
                    return ((Comparable) left).compareTo(right);
                } else {
                    if (right instanceof Comparable) {
                        return ((Comparable) right).compareTo(left);
                    }
                }
            }
        }
        throw new ArithmeticException("Object comparison:(" + left + " " + operator + " " + right + ")");
    }

    public boolean equals(Object left, Object right) {
        boolean z = false;
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if ((left instanceof Boolean) || (right instanceof Boolean)) {
            if (toBoolean(left) == toBoolean(right)) {
                z = true;
            }
            return z;
        }
        if (compare(left, right, "==") == 0) {
            z = true;
        }
        return z;
    }

    public boolean lessThan(Object left, Object right) {
        boolean z = false;
        if (left == right || left == null || right == null) {
            return false;
        }
        if (compare(left, right, "<") < 0) {
            z = true;
        }
        return z;
    }

    public boolean greaterThan(Object left, Object right) {
        boolean z = false;
        if (left == right || left == null || right == null) {
            return false;
        }
        if (compare(left, right, ">") > 0) {
            z = true;
        }
        return z;
    }

    public boolean lessThanOrEqual(Object left, Object right) {
        boolean z = false;
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (compare(left, right, "<=") <= 0) {
            z = true;
        }
        return z;
    }

    public boolean greaterThanOrEqual(Object left, Object right) {
        boolean z = false;
        if (left == right) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        if (compare(left, right, ">=") >= 0) {
            z = true;
        }
        return z;
    }

    public boolean toBoolean(Object val) {
        boolean z = true;
        if (val == null) {
            controlNullOperand();
            return false;
        } else if (val instanceof Boolean) {
            return ((Boolean) val).booleanValue();
        } else {
            if (val instanceof Number) {
                double number = toDouble(val);
                if (Double.isNaN(number) || number == 0.0d) {
                    z = false;
                }
                return z;
            } else if (!(val instanceof String)) {
                return false;
            } else {
                String strval = val.toString();
                if (strval.length() > 0) {
                    if ("false".equals(strval)) {
                    }
                    return z;
                }
                z = false;
                return z;
            }
        }
    }

    public long toLong(Object val) {
        if (val == null) {
            controlNullOperand();
            return 0;
        } else if (val instanceof Double) {
            if (Double.isNaN(((Double) val).doubleValue())) {
                return ((Double) val).longValue();
            }
            return 0;
        } else if (val instanceof Number) {
            return ((Number) val).longValue();
        } else {
            if (val instanceof String) {
                if (BuildConfig.FLAVOR.equals(val)) {
                    return 0;
                }
                return Long.parseLong((String) val);
            } else if (val instanceof Boolean) {
                return !((Boolean) val).booleanValue() ? 0 : 1;
            } else if (val instanceof Character) {
                return (long) ((Character) val).charValue();
            } else {
                throw new ArithmeticException("Long coercion: " + val.getClass().getName() + ":(" + val + ")");
            }
        }
    }

    public BigInteger toBigInteger(Object val) {
        if (val == null) {
            controlNullOperand();
            return BigInteger.ZERO;
        } else if (val instanceof BigInteger) {
            return (BigInteger) val;
        } else {
            if (val instanceof Double) {
                if (Double.isNaN(((Double) val).doubleValue())) {
                    return BigInteger.ZERO;
                }
                return new BigInteger(val.toString());
            } else if (val instanceof Number) {
                return new BigInteger(val.toString());
            } else {
                if (val instanceof String) {
                    String string = (String) val;
                    if (BuildConfig.FLAVOR.equals(string.trim())) {
                        return BigInteger.ZERO;
                    }
                    return new BigInteger(string);
                } else if (val instanceof Character) {
                    return BigInteger.valueOf((long) ((Character) val).charValue());
                } else {
                    throw new ArithmeticException("BigInteger coercion: " + val.getClass().getName() + ":(" + val + ")");
                }
            }
        }
    }

    public BigDecimal toBigDecimal(Object val) {
        if (val instanceof BigDecimal) {
            return roundBigDecimal((BigDecimal) val);
        }
        if (val == null) {
            controlNullOperand();
            return BigDecimal.ZERO;
        } else if (val instanceof String) {
            String string = ((String) val).trim();
            if (BuildConfig.FLAVOR.equals(string)) {
                return BigDecimal.ZERO;
            }
            return roundBigDecimal(new BigDecimal(string, getMathContext()));
        } else if (val instanceof Double) {
            if (Double.isNaN(((Double) val).doubleValue())) {
                return BigDecimal.ZERO;
            }
            return roundBigDecimal(new BigDecimal(val.toString(), getMathContext()));
        } else if (val instanceof Number) {
            return roundBigDecimal(new BigDecimal(val.toString(), getMathContext()));
        } else {
            if (val instanceof Character) {
                return new BigDecimal(((Character) val).charValue());
            }
            throw new ArithmeticException("BigDecimal coercion: " + val.getClass().getName() + ":(" + val + ")");
        }
    }

    public double toDouble(Object val) {
        double d = 0.0d;
        if (val == null) {
            controlNullOperand();
            return 0.0d;
        } else if (val instanceof Double) {
            return ((Double) val).doubleValue();
        } else {
            if (val instanceof Number) {
                return Double.parseDouble(String.valueOf(val));
            }
            if (val instanceof Boolean) {
                if (((Boolean) val).booleanValue()) {
                    d = 1.0d;
                }
                return d;
            } else if (val instanceof String) {
                String string = ((String) val).trim();
                return !BuildConfig.FLAVOR.equals(string) ? Double.parseDouble(string) : Double.NaN;
            } else if (val instanceof Character) {
                return (double) ((Character) val).charValue();
            } else {
                throw new ArithmeticException("Double coercion: " + val.getClass().getName() + ":(" + val + ")");
            }
        }
    }

    public String toString(Object val) {
        if (val == null) {
            controlNullOperand();
            return BuildConfig.FLAVOR;
        } else if (!(val instanceof Double)) {
            return val.toString();
        } else {
            Double dval = (Double) val;
            return !Double.isNaN(dval.doubleValue()) ? dval.toString() : BuildConfig.FLAVOR;
        }
    }

    public Number narrow(Number original) {
        return narrowNumber(original, null);
    }

    protected boolean narrowAccept(Class<?> narrow, Class<?> source) {
        return narrow == null || narrow.equals(source);
    }

    protected Number narrowNumber(Number original, Class<?> narrow) {
        if (original == null) {
            return original;
        }
        Object obj;
        Number result = original;
        if (original instanceof BigDecimal) {
            BigDecimal bigd = (BigDecimal) original;
            if (bigd.compareTo(BIGD_DOUBLE_MAX_VALUE) > 0) {
                return original;
            }
            try {
                long l = bigd.longValueExact();
                if (narrowAccept(narrow, Integer.class)) {
                    if ((l > 2147483647L ? 1 : null) == null) {
                        if (l < -2147483648L) {
                            obj = 1;
                        } else {
                            obj = null;
                        }
                        if (obj == null) {
                            return Integer.valueOf((int) l);
                        }
                    }
                }
                if (narrowAccept(narrow, Long.class)) {
                    return Long.valueOf(l);
                }
            } catch (ArithmeticException e) {
            }
        }
        if ((original instanceof Double) || (original instanceof Float) || (original instanceof BigDecimal)) {
            double value = original.doubleValue();
            if (narrowAccept(narrow, Float.class) && value <= 3.4028234663852886E38d && value >= 1.401298464324817E-45d) {
                result = Float.valueOf(result.floatValue());
            }
        } else {
            if (original instanceof BigInteger) {
                BigInteger bigi = (BigInteger) original;
                if (bigi.compareTo(BIGI_LONG_MAX_VALUE) > 0 || bigi.compareTo(BIGI_LONG_MIN_VALUE) < 0) {
                    return original;
                }
            }
            long value2 = original.longValue();
            if (narrowAccept(narrow, Byte.class)) {
                if ((value2 > 127 ? 1 : null) == null) {
                    if ((value2 < -128 ? 1 : null) == null) {
                        result = Byte.valueOf((byte) ((int) value2));
                    }
                }
            }
            if (narrowAccept(narrow, Short.class)) {
                if ((value2 > 32767 ? 1 : null) == null) {
                    if ((value2 < -32768 ? 1 : null) == null) {
                        result = Short.valueOf((short) ((int) value2));
                    }
                }
            }
            if (narrowAccept(narrow, Integer.class)) {
                if ((value2 > 2147483647L ? 1 : null) == null) {
                    if (value2 < -2147483648L) {
                        obj = 1;
                    } else {
                        obj = null;
                    }
                    if (obj == null) {
                        result = Integer.valueOf((int) value2);
                    }
                }
            }
        }
        return result;
    }
}
