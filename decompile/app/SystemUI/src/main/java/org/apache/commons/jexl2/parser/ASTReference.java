package org.apache.commons.jexl2.parser;

public class ASTReference extends JexlNode {
    public ASTReference(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
