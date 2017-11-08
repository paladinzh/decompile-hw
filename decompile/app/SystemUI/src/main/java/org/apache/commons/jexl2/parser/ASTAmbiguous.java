package org.apache.commons.jexl2.parser;

public class ASTAmbiguous extends JexlNode {
    public ASTAmbiguous(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
