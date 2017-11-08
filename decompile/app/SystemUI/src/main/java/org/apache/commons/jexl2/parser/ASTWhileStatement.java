package org.apache.commons.jexl2.parser;

public class ASTWhileStatement extends JexlNode {
    public ASTWhileStatement(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
