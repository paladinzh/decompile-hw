package org.apache.commons.jexl2.parser;

import java.util.ArrayList;
import java.util.List;

public class JJTParserState {
    private List<Integer> marks = new ArrayList();
    private int mk = 0;
    private boolean node_created;
    private List<Node> nodes = new ArrayList();
    private int sp = 0;

    public void reset() {
        this.nodes.clear();
        this.marks.clear();
        this.sp = 0;
        this.mk = 0;
    }

    public void pushNode(Node n) {
        this.nodes.add(n);
        this.sp++;
    }

    public Node popNode() {
        int i = this.sp - 1;
        this.sp = i;
        if (i < this.mk) {
            this.mk = ((Integer) this.marks.remove(this.marks.size() - 1)).intValue();
        }
        return (Node) this.nodes.remove(this.nodes.size() - 1);
    }

    public int nodeArity() {
        return this.sp - this.mk;
    }

    public void clearNodeScope(Node n) {
        while (this.sp > this.mk) {
            popNode();
        }
        this.mk = ((Integer) this.marks.remove(this.marks.size() - 1)).intValue();
    }

    public void openNodeScope(Node n) {
        this.marks.add(Integer.valueOf(this.mk));
        this.mk = this.sp;
        n.jjtOpen();
    }

    public void closeNodeScope(Node n, int num) {
        this.mk = ((Integer) this.marks.remove(this.marks.size() - 1)).intValue();
        int num2 = num;
        while (true) {
            num = num2 - 1;
            if (num2 <= 0) {
                n.jjtClose();
                pushNode(n);
                this.node_created = true;
                return;
            }
            Node c = popNode();
            c.jjtSetParent(n);
            n.jjtAddChild(c, num);
            num2 = num;
        }
    }

    public void closeNodeScope(Node n, boolean condition) {
        if (condition) {
            int a = nodeArity();
            this.mk = ((Integer) this.marks.remove(this.marks.size() - 1)).intValue();
            int a2 = a;
            while (true) {
                a = a2 - 1;
                if (a2 <= 0) {
                    n.jjtClose();
                    pushNode(n);
                    this.node_created = true;
                    return;
                }
                Node c = popNode();
                c.jjtSetParent(n);
                n.jjtAddChild(c, a);
                a2 = a;
            }
        } else {
            this.mk = ((Integer) this.marks.remove(this.marks.size() - 1)).intValue();
            this.node_created = false;
        }
    }
}
