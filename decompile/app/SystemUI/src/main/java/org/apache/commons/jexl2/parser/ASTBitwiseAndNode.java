package org.apache.commons.jexl2.parser;

public class ASTBitwiseAndNode extends JexlNode {
    public ASTBitwiseAndNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
