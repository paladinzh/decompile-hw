package com.android.mms.dom;

import com.android.mms.dom.events.EventTargetImpl;
import java.util.NoSuchElementException;
import java.util.Vector;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventException;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

public abstract class NodeImpl implements Node, EventTarget {
    private final Vector<Node> mChildNodes = new Vector();
    private final EventTarget mEventTarget = new EventTargetImpl(this);
    DocumentImpl mOwnerDocument;
    private Node mParentNode;

    public abstract String getNodeName();

    public abstract short getNodeType();

    protected NodeImpl(DocumentImpl owner) {
        this.mOwnerDocument = owner;
    }

    public Node appendChild(Node newChild) throws DOMException {
        ((NodeImpl) newChild).setParentNode(this);
        this.mChildNodes.remove(newChild);
        this.mChildNodes.add(newChild);
        return newChild;
    }

    public Node cloneNode(boolean deep) {
        return null;
    }

    public NamedNodeMap getAttributes() {
        return null;
    }

    public NodeList getChildNodes() {
        return new NodeListImpl(this, null, false);
    }

    public Node getFirstChild() {
        Node firstChild = null;
        try {
            return (Node) this.mChildNodes.firstElement();
        } catch (NoSuchElementException e) {
            return firstChild;
        }
    }

    public Node getLastChild() {
        Node lastChild = null;
        try {
            return (Node) this.mChildNodes.lastElement();
        } catch (NoSuchElementException e) {
            return lastChild;
        }
    }

    public String getLocalName() {
        return null;
    }

    public String getNamespaceURI() {
        return null;
    }

    public Node getNextSibling() {
        if (this.mParentNode == null || this == this.mParentNode.getLastChild()) {
            return null;
        }
        Vector<Node> siblings = ((NodeImpl) this.mParentNode).mChildNodes;
        return (Node) siblings.elementAt(siblings.indexOf(this) + 1);
    }

    public String getNodeValue() throws DOMException {
        return null;
    }

    public Document getOwnerDocument() {
        return this.mOwnerDocument;
    }

    public Node getParentNode() {
        return this.mParentNode;
    }

    public String getPrefix() {
        return null;
    }

    public Node getPreviousSibling() {
        if (this.mParentNode == null || this == this.mParentNode.getFirstChild()) {
            return null;
        }
        Vector<Node> siblings = ((NodeImpl) this.mParentNode).mChildNodes;
        return (Node) siblings.elementAt(siblings.indexOf(this) - 1);
    }

    public boolean hasAttributes() {
        return false;
    }

    public boolean hasChildNodes() {
        return !this.mChildNodes.isEmpty();
    }

    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return null;
    }

    public boolean isSupported(String feature, String version) {
        return false;
    }

    public void normalize() {
    }

    public Node removeChild(Node oldChild) throws DOMException {
        if (this.mChildNodes.contains(oldChild)) {
            this.mChildNodes.remove(oldChild);
            ((NodeImpl) oldChild).setParentNode(null);
            return null;
        }
        throw new DOMException((short) 8, "Child does not exist");
    }

    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        if (this.mChildNodes.contains(oldChild)) {
            try {
                this.mChildNodes.remove(newChild);
            } catch (DOMException e) {
            }
            this.mChildNodes.setElementAt(newChild, this.mChildNodes.indexOf(oldChild));
            ((NodeImpl) newChild).setParentNode(this);
            ((NodeImpl) oldChild).setParentNode(null);
            return oldChild;
        }
        throw new DOMException((short) 8, "Old child does not exist");
    }

    public void setNodeValue(String nodeValue) throws DOMException {
    }

    public void setPrefix(String prefix) throws DOMException {
    }

    private void setParentNode(Node parentNode) {
        this.mParentNode = parentNode;
    }

    public void addEventListener(String type, EventListener listener, boolean useCapture) {
        this.mEventTarget.addEventListener(type, listener, useCapture);
    }

    public void removeEventListener(String type, EventListener listener, boolean useCapture) {
        this.mEventTarget.removeEventListener(type, listener, useCapture);
    }

    public boolean dispatchEvent(Event evt) throws EventException {
        return this.mEventTarget.dispatchEvent(evt);
    }

    public String getBaseURI() {
        return null;
    }

    public short compareDocumentPosition(Node other) throws DOMException {
        throw new DOMException((short) 9, null);
    }

    public String getTextContent() throws DOMException {
        throw new DOMException((short) 9, null);
    }

    public void setTextContent(String textContent) throws DOMException {
        throw new DOMException((short) 9, null);
    }

    public boolean isSameNode(Node other) {
        throw new DOMException((short) 9, null);
    }

    public String lookupPrefix(String namespaceURI) {
        return null;
    }

    public boolean isDefaultNamespace(String namespaceURI) {
        throw new DOMException((short) 9, null);
    }

    public String lookupNamespaceURI(String prefix) {
        return null;
    }

    public boolean isEqualNode(Node arg) {
        throw new DOMException((short) 9, null);
    }

    public Object getFeature(String feature, String version) {
        return null;
    }

    public Object setUserData(String key, Object data, UserDataHandler handler) {
        throw new DOMException((short) 9, null);
    }

    public Object getUserData(String key) {
        return null;
    }
}
