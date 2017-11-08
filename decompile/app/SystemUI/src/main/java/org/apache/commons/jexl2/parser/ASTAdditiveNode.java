package org.apache.commons.jexl2.parser;

public class ASTAdditiveNode extends JexlNode {
    public ASTAdditiveNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
