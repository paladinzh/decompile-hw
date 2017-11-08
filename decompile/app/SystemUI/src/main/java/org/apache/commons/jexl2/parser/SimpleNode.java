package org.apache.commons.jexl2.parser;

public class SimpleNode implements Node {
    protected JexlNode[] children;
    protected final int id;
    protected JexlNode parent;
    protected volatile Object value;

    public SimpleNode(int i) {
        this.id = i;
    }

    public void jjtOpen() {
    }

    public void jjtClose() {
    }

    public void jjtSetParent(Node n) {
        this.parent = (JexlNode) n;
    }

    public JexlNode jjtGetParent() {
        return this.parent;
    }

    public void jjtAddChild(Node n, int i) {
        if (this.children == null) {
            this.children = new JexlNode[(i + 1)];
        } else if (i >= this.children.length) {
            JexlNode[] c = new JexlNode[(i + 1)];
            System.arraycopy(this.children, 0, c, 0, this.children.length);
            this.children = c;
        }
        this.children[i] = (JexlNode) n;
    }

    public JexlNode jjtGetChild(int i) {
        return this.children[i];
    }

    public int jjtGetNumChildren() {
        return this.children != null ? this.children.length : 0;
    }

    public void jjtSetValue(Object value) {
        this.value = value;
    }

    public Object jjtGetValue() {
        return this.value;
    }

    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    public String toString() {
        return ParserTreeConstants.jjtNodeName[this.id];
    }
}
