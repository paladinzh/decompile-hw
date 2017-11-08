package org.apache.commons.jexl2.parser;

public class ASTModNode extends JexlNode {
    public ASTModNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
