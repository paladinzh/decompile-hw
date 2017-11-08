package org.apache.commons.jexl2.parser;

public class ASTBitwiseOrNode extends JexlNode {
    public ASTBitwiseOrNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
