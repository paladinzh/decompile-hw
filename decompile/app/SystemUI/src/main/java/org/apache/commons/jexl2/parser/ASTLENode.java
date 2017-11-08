package org.apache.commons.jexl2.parser;

public class ASTLENode extends JexlNode {
    public ASTLENode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
