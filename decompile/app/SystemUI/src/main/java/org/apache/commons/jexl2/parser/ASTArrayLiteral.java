package org.apache.commons.jexl2.parser;

import org.apache.commons.jexl2.parser.JexlNode.Literal;

public final class ASTArrayLiteral extends JexlNode implements Literal<Object> {
    Object array = null;
    boolean constant = false;

    ASTArrayLiteral(int id) {
        super(id);
    }

    public void jjtClose() {
        if (this.children == null || this.children.length == 0) {
            this.array = new Object[0];
            this.constant = true;
            return;
        }
        this.constant = isConstant();
    }

    public Object getLiteral() {
        return this.array;
    }

    public void setLiteral(Object literal) {
        if (!this.constant) {
            return;
        }
        if (literal == null || literal.getClass().isArray()) {
            this.array = literal;
            return;
        }
        throw new IllegalArgumentException(literal.getClass() + " is not an array");
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
