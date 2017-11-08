package org.apache.commons.jexl2.parser;

public final class ASTReferenceExpression extends ASTArrayAccess {
    public ASTReferenceExpression(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
