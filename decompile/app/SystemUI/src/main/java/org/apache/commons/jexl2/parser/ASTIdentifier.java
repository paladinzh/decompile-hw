package org.apache.commons.jexl2.parser;

public class ASTIdentifier extends JexlNode {
    private int register = -1;

    public ASTIdentifier(int id) {
        super(id);
    }

    void setRegister(String r) {
        if (r.charAt(0) == '#') {
            this.register = Integer.parseInt(r.substring(1));
        }
    }

    void setRegister(int r) {
        this.register = r;
    }

    public int getRegister() {
        return this.register;
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
