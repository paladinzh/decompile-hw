package org.apache.commons.jexl2.parser;

public class ASTFunctionNode extends JexlNode {
    public ASTFunctionNode(int id) {
        super(id);
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
