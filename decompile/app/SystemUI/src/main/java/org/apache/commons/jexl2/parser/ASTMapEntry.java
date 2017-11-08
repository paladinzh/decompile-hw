package org.apache.commons.jexl2.parser;

public class ASTMapEntry extends JexlNode {
    public ASTMapEntry(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
