package com.android.mms.dom;

import java.util.Vector;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class NamedNodeMapImpl implements NamedNodeMap {
    private Vector<Node> mNodes = new Vector();

    public int getLength() {
        return this.mNodes.size();
    }

    public Node getNamedItem(String name) {
        for (int i = 0; i < this.mNodes.size(); i++) {
            if (name.equals(((Node) this.mNodes.elementAt(i)).getNodeName())) {
                return (Node) this.mNodes.elementAt(i);
            }
        }
        return null;
    }

    public Node getNamedItemNS(String namespaceURI, String localName) {
        return null;
    }

    public Node item(int index) {
        if (index < this.mNodes.size()) {
            return (Node) this.mNodes.elementAt(index);
        }
        return null;
    }

    public Node removeNamedItem(String name) throws DOMException {
        Node node = getNamedItem(name);
        if (node == null) {
            throw new DOMException((short) 8, "Not found");
        }
        this.mNodes.remove(node);
        return node;
    }

    public Node removeNamedItemNS(String namespaceURI, String localName) throws DOMException {
        return null;
    }

    public Node setNamedItem(Node arg) throws DOMException {
        Node existing = getNamedItem(arg.getNodeName());
        if (existing != null) {
            this.mNodes.remove(existing);
        }
        this.mNodes.add(arg);
        return existing;
    }

    public Node setNamedItemNS(Node arg) throws DOMException {
        return null;
    }
}
