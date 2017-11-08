package org.apache.commons.jexl2.parser;

public class ASTNotNode extends JexlNode {
    public ASTNotNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
