package org.apache.commons.jexl2.parser;

public class ASTUnaryMinusNode extends JexlNode {
    public ASTUnaryMinusNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
