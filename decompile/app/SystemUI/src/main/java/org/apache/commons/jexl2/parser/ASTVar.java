package org.apache.commons.jexl2.parser;

public class ASTVar extends ASTIdentifier {
    public ASTVar(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
