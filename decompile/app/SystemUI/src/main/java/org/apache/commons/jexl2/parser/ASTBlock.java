package org.apache.commons.jexl2.parser;

public class ASTBlock extends JexlNode {
    public ASTBlock(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
