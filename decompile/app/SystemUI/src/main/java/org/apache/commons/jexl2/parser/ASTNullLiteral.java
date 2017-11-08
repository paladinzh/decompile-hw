package org.apache.commons.jexl2.parser;

public class ASTNullLiteral extends JexlNode {
    public ASTNullLiteral(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
