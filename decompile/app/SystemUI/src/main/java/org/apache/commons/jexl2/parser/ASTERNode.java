package org.apache.commons.jexl2.parser;

public class ASTERNode extends JexlNode {
    public ASTERNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
