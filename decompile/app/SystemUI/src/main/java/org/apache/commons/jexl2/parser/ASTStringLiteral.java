package org.apache.commons.jexl2.parser;

import org.apache.commons.jexl2.parser.JexlNode.Literal;

public final class ASTStringLiteral extends JexlNode implements Literal<String> {
    public ASTStringLiteral(int id) {
        super(id);
    }

    public String getLiteral() {
        return this.image;
    }

    protected boolean isConstant(boolean literal) {
        return true;
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
