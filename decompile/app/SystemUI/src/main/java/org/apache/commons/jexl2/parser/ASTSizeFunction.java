package org.apache.commons.jexl2.parser;

public class ASTSizeFunction extends JexlNode {
    public ASTSizeFunction(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
