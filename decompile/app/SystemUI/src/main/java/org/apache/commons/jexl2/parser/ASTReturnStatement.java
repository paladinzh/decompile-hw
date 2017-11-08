package org.apache.commons.jexl2.parser;

public class ASTReturnStatement extends JexlNode {
    public ASTReturnStatement(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
