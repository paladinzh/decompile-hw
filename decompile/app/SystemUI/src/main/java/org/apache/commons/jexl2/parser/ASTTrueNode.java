package org.apache.commons.jexl2.parser;

public class ASTTrueNode extends JexlNode {
    public ASTTrueNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
