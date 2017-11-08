package org.apache.commons.jexl2.parser;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.apache.commons.jexl2.parser.JexlNode.Literal;

public class ASTNumberLiteral extends JexlNode implements Literal<Number> {
    Class<?> clazz = null;
    Number literal = null;

    public ASTNumberLiteral(int id) {
        super(id);
    }

    public Number getLiteral() {
        return this.literal;
    }

    protected boolean isConstant(boolean literal) {
        return true;
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public Class<?> getLiteralClass() {
        return this.clazz;
    }

    public boolean isInteger() {
        return Integer.class.equals(this.clazz);
    }

    public void setNatural(String s) {
        int base;
        Class<?> rclass;
        Number result;
        if (s.charAt(0) != '0') {
            base = 10;
        } else {
            if (s.length() > 1) {
                if (s.charAt(1) == 'x' || s.charAt(1) == 'X') {
                    base = 16;
                    s = s.substring(2);
                }
            }
            base = 8;
        }
        int last = s.length() - 1;
        switch (s.charAt(last)) {
            case 'H':
            case 'h':
                rclass = BigInteger.class;
                result = new BigInteger(s.substring(0, last), base);
                break;
            case 'L':
            case 'l':
                rclass = Long.class;
                result = Long.valueOf(s.substring(0, last), base);
                break;
            default:
                rclass = Integer.class;
                try {
                    result = Integer.valueOf(s, base);
                    break;
                } catch (NumberFormatException e) {
                    try {
                        result = Long.valueOf(s, base);
                        break;
                    } catch (NumberFormatException e2) {
                        result = new BigInteger(s, base);
                        break;
                    }
                }
        }
        this.literal = result;
        this.clazz = rclass;
    }

    public void setReal(String s) {
        Number result;
        Class<?> rclass;
        int last = s.length() - 1;
        switch (s.charAt(last)) {
            case 'B':
            case 'b':
                result = new BigDecimal(s.substring(0, last));
                rclass = BigDecimal.class;
                break;
            case 'D':
            case 'd':
                rclass = Double.class;
                result = Double.valueOf(s);
                break;
            default:
                rclass = Float.class;
                try {
                    result = Float.valueOf(s);
                    break;
                } catch (NumberFormatException e) {
                    try {
                        result = Double.valueOf(s);
                        break;
                    } catch (NumberFormatException e2) {
                        result = new BigDecimal(s);
                        break;
                    }
                }
        }
        this.literal = result;
        this.clazz = rclass;
    }
}
