package org.apache.commons.jexl2.parser;

public class ASTEQNode extends JexlNode {
    public ASTEQNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
