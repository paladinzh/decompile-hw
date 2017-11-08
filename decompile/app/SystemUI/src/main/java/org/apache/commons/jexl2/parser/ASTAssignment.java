package org.apache.commons.jexl2.parser;

public class ASTAssignment extends JexlNode {
    public ASTAssignment(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
