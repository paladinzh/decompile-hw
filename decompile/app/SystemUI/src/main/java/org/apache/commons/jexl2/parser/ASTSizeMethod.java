package org.apache.commons.jexl2.parser;

public class ASTSizeMethod extends JexlNode {
    public ASTSizeMethod(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
