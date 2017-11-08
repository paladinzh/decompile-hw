package org.apache.commons.jexl2.parser;

public class ASTArrayAccess extends JexlNode {
    public ASTArrayAccess(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
